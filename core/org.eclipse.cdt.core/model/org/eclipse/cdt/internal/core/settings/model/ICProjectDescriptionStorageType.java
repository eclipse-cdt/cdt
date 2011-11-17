/*******************************************************************************
 * Copyright (c) 2008, 2010 Broadcom Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.osgi.framework.Version;

/**
 * Interface defining an ICProjectDescriptionStorageType
 * used as a factory for creating project description storages
 * for a give project
 */
public interface ICProjectDescriptionStorageType {
	/** The file name in which the storage type and version are stored */
	public static final String STORAGE_FILE_NAME = ".cproject"; //$NON-NLS-1$
	/** The document version attribute */
	public static final String STORAGE_VERSION_NAME = "fileVersion";	//$NON-NLS-1$
	/** The root element in a .cproject file */
	public static final String STORAGE_ROOT_ELEMENT_NAME = "cproject";	//$NON-NLS-1$
	/** The document's storage type id attribute in the root element */
	public static final String STORAGE_TYPE_ATTRIBUTE = "storage_type_id"; //$NON-NLS-1$

	/**
	 * The type as defined in the CProjectDescriptionStorage extension point, wraps the
	 * implemented ICProjectDescriptionType to provide proxy object for use by CProjectDescriptionStorageManager
	 */
	public static final class CProjectDescriptionStorageTypeProxy implements ICProjectDescriptionStorageType {
		private static final String ATTR_ID = "id"; //$NON-NLS-1$
		private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
		private static final String ATTR_NAME = "name"; //$NON-NLS-1$
		private static final String ATTR_VERSION = "version"; //$NON-NLS-1$
		private static final String ATTR_MAX_VERSION = "max_version"; //$NON-NLS-1$
		private static final String ATTR_MIN_VERSION = "min_version"; //$NON-NLS-1$

		public final String id;
		public final String name;
		public final ICProjectDescriptionStorageType storageType;
		public final Version version;
		public final Version max_version;
		public final Version min_version;

		/**
		 * CProjectDescription Proxy Type.
		 * @param el
		 * @throws CoreException
		 * @throws IllegalArgumentException
		 */
		public CProjectDescriptionStorageTypeProxy(IConfigurationElement el) throws CoreException, IllegalArgumentException {
			this (el.getNamespaceIdentifier() + "." + getString(el, ATTR_ID, null), //$NON-NLS-1$
				   getString(el, ATTR_NAME, null),
				   (ICProjectDescriptionStorageType)el.createExecutableExtension(ATTR_CLASS),
				   getVersion(el, ATTR_VERSION, null),
				   getVersion(el, ATTR_MIN_VERSION, Version.emptyVersion),
				   getVersion(el, ATTR_MAX_VERSION, Version.emptyVersion));
		}
		/**
		 * Constructor verifies that version is in the range (min_version, max_version]
		 * @param name
		 * @param storageType
		 * @param version
		 * @param min_version
		 * @param max_version
		 */
		public CProjectDescriptionStorageTypeProxy(String id, String name, ICProjectDescriptionStorageType storageType, Version version,
				Version min_version, Version max_version) {
			this.id = id;
			this.name = name;
			this.storageType = storageType;
			this.version = version;
			this.min_version = min_version;
			this.max_version = max_version == Version.emptyVersion ? version : max_version;
			if (min_version != Version.emptyVersion && version.compareTo(min_version) <= 0)
				throw new IllegalArgumentException("CProjectDescriptionStorageType Version: " + version + //$NON-NLS-1$
						" must be > that min_version: " + min_version); //$NON-NLS-1$
			if (max_version != Version.emptyVersion && version.compareTo(max_version) > 0)
				throw new IllegalArgumentException("CProjectDescriptionStorageType Version: " + version + //$NON-NLS-1$
						" must be < that max_version: " + max_version); //$NON-NLS-1$
		}
		/** Indicates if this type is compatible with the provided version */
		public boolean isCompatible(Version version) {
			if (version.compareTo(max_version) > 0)
				return false;
			if (version.compareTo(min_version) <= 0)
				return false;
			return true;
		}
		@Override
		public boolean createsCProjectXMLFile() {
			return storageType.createsCProjectXMLFile();
		}
		@Override
		public AbstractCProjectDescriptionStorage getProjectDescriptionStorage(CProjectDescriptionStorageTypeProxy type, IProject project, Version version) {
			return storageType.getProjectDescriptionStorage(type, project, version);
		}

		private static Version getVersion(IConfigurationElement element, String id, Version defaultValue) throws IllegalArgumentException{
			String value = element.getAttribute(id);
			if (value==null)
				return defaultValue;
			Version v;
			try {
				v = new Version(value);
			} catch (Exception e) {
				// If an exception occurred return the default value
				v = defaultValue;
			}
			return v;
		}

		private static String getString(IConfigurationElement element, String id, String defaultValue) throws IllegalArgumentException {
			String val = element.getAttribute(id);
			if (val != null)
				return val;
			if (defaultValue != null)
				return defaultValue;
			throw new IllegalArgumentException("Couldn't find value for extension attribute " + id); //$NON-NLS-1$
		}
	}


	/**
	 * Return a new storage instance to be for persisting / loading cproject descriptions
	 * from the passed in IProject
	 * @param type proxy which created this
	 * @param project - IProject
	 * @param version - Version number of the description as reported by the AbstractCProjectDescriptionStorage
	 * 					on last save
	 * @return AbstractCProjectDescriptionStorage
	 */
	public AbstractCProjectDescriptionStorage getProjectDescriptionStorage(CProjectDescriptionStorageTypeProxy type, IProject project, Version version);

	/**
	 * Method indicating whether this project storage type writes a .cproject file.
	 *
	 * If this method returns true then you must ensure that the .cproject file is an
	 * xml file with a {@link #STORAGE_VERSION_NAME} tag and {@link #STORAGE_TYPE_ATTRIBUTE} id
	 * in the {@link #STORAGE_ROOT_ELEMENT_NAME} e.g.:
	 * <pre>
	 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
	 * &lt;?fileVersion 4.0.0?&gt;
	 * &lt;cproject storageType="storage_type_id"&gt; ....
	 * &lt;/cproject&gt;
     * </pre>
	 *
	 * If this method returns false, then the CProjectDescriptionStorageType creates
	 * a '.cproject' containing this data
	 *
	 * @return boolean indicating whether this storage type writes a (compatible) .cproject file
	 * @see CProjectDescriptionStorageTypeProxy
	 *
	 */
	public boolean createsCProjectXMLFile();

}
