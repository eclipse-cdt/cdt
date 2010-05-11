/*******************************************************************************
 * Copyright (c) 2008, 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.settings.model.xml2;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionStorageManager;
import org.eclipse.cdt.internal.core.settings.model.ICProjectDescriptionStorageType;
import org.eclipse.cdt.internal.core.settings.model.ICProjectDescriptionStorageType.CProjectDescriptionStorageTypeProxy;
import org.eclipse.cdt.internal.core.settings.model.xml.InternalXmlStorageElement;
import org.eclipse.cdt.internal.core.settings.model.xml.XmlProjectDescriptionStorage;
import org.eclipse.cdt.internal.core.settings.model.xml.XmlStorage;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/**
 * This class extends XmlProjectDescriptionStroage to provide the following
 * functionality:
 *   - Configuration Description storage modules are persisted in separate XML
 *     files stored under .csettings in the project root directory.  They
 *     are linked into the main .cproject file via "externalCElementFile" element
 *
 * It is backwards compatible with XmlProjectDescriptionStorage.  If it finds a file
 * with Version less than 5.0, it will delegate to the previous XmlProjectDescriptionStorage
 *
 * This allows users to more easily version control their CDT project descriptions
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=226457">Bug 226457</a>
 */
public class XmlProjectDescriptionStorage2 extends XmlProjectDescriptionStorage {
	/** Folder Name for storing Project specific configuration settings */
	static final String STORAGE_FOLDER_NAME = ".csettings"; //$NON-NLS-1$
	/** Element providing the name of the external C Element file */
	public static final String EXTERNAL_CELEMENT_KEY = "externalCElementFile"; //$NON-NLS-1$
	private static final String ID = "id";	//$NON-NLS-1$

	/** Override the description version, we support version '5.0' files */
	@SuppressWarnings("hiding")
	public static final Version STORAGE_DESCRIPTION_VERSION = new Version("5.0"); //$NON-NLS-1$
	/** The storage type id of this extended project description type */
	@SuppressWarnings("hiding")
	public static final String STORAGE_TYPE_ID = CCorePlugin.PLUGIN_ID + ".XmlProjectDescriptionStorage2"; //$NON-NLS-1$

	/** Map from storageModuleId to modification time stamp */
	Map<String, Long> modificationMap = Collections.synchronizedMap(new HashMap<String, Long>());

	public XmlProjectDescriptionStorage2(CProjectDescriptionStorageTypeProxy type, IProject project, Version version) {
		super(type, project, version);
	}

	/*
	 * Check for external modification in the module files in the .csettings directory
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.settings.model.xml.XmlProjectDescriptionStorage#checkExternalModification()
	 */
	@Override
	protected synchronized boolean checkExternalModification() {
		// Check if our parent thinks we need a reload
		if (super.checkExternalModification())
			return true;
		// If not currently loaded, then nothing to do
		if (getLoadedDescription() == null)
			return false;
		final boolean[] needReload = new boolean[] { false };
		try {
			project.getFolder(STORAGE_FOLDER_NAME).accept(new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (modificationMap.containsKey(proxy.getName()))
						if (modificationMap.get(proxy.getName()) != proxy.getModificationStamp()) {
							// There may be old storages in here, ensure we don't infinite reload...
							modificationMap.put(proxy.getName(), proxy.getModificationStamp());
							setCurrentDescription(null, true);
							needReload[0] = true;
						}
					return true;
				}
			}, IResource.NONE);
		} catch (CoreException e) {
			// STORAGE_FOLDER_NAME doesn't exist... or something went wrong during reload
			if (project.getFolder(STORAGE_FOLDER_NAME).exists())
				CCorePlugin.log("XmlProjectDescriptionStorage2: Problem checking for external modification: " + e.getMessage()); //$NON-NLS-1$
		}
		return needReload[0];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.settings.model.xml.XmlProjectDescriptionStorage#createStorage(org.eclipse.core.resources.IContainer, java.lang.String, boolean, boolean, boolean)
	 */
	@Override
	protected final InternalXmlStorageElement createStorage(IContainer container, String fileName, boolean reCreate, boolean createEmptyIfNotFound, boolean readOnly) throws CoreException {
		InternalXmlStorageElement el = super.createStorage(container, fileName, reCreate, createEmptyIfNotFound, readOnly);

		Queue<ICStorageElement> nodesToCheck = new LinkedList<ICStorageElement>();
		nodesToCheck.addAll(Arrays.asList(el.getChildren()));
		while (!nodesToCheck.isEmpty()) {
			ICStorageElement currEl = nodesToCheck.remove();

			// If not a storageModule element or doesn't have EXTERNAL_CELEMENT_KEY, continue
			if (!XmlStorage.MODULE_ELEMENT_NAME.equals(currEl.getName())
					|| currEl.getAttribute(EXTERNAL_CELEMENT_KEY) == null) {
				nodesToCheck.addAll(Arrays.asList(currEl.getChildren()));
				continue;
			}

			try {
				// Found -- load the Document pointed to (allows multiple layers of indirection if need be)
				InternalXmlStorageElement el2 = createStorage(project.getFolder(STORAGE_FOLDER_NAME),
																	currEl.getAttribute(EXTERNAL_CELEMENT_KEY),
																	reCreate, createEmptyIfNotFound, readOnly);
				// Update the modification stamp
				modificationMap.put(currEl.getAttribute(EXTERNAL_CELEMENT_KEY), project.getFolder(STORAGE_FOLDER_NAME).getFile(currEl.getAttribute(EXTERNAL_CELEMENT_KEY)).getModificationStamp());

				ICStorageElement currParent = currEl.getParent();
				// Get the storageModule element in the new Document
				ICStorageElement[] childStorages = el2.getChildrenByName(XmlStorage.MODULE_ELEMENT_NAME);
				Assert.isTrue(childStorages.length == 1, "More than one storageModule in external Document!"); //$NON-NLS-1$

				// Import the loaded element
				currParent.importChild(childStorages[0]);
				// Remove the placeholder
				currParent.removeChild(currEl);
			} catch (CoreException e) {
				// If we're here there was a problem loading csettings when there is a .cproject file.
				CCorePlugin.log(e);
				throw e;
			}
		}
		return el;
	}

	/*
	 * Serialize the Project Description:
	 *    - The .cproject file contains the root XML Elements.
	 *    - We serialize the storageModule children of the CConfiguration elements (in the org.eclipse.cdt.settings module)
	 *      to separate files in the .csettings  directory to prevent unmanageably large XML deltas
	 * Return the modification stamp of the main .cproject file as our super method does
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.settings.model.xml.XmlProjectDescriptionStorage#serialize(org.eclipse.core.resources.IContainer, java.lang.String, org.eclipse.cdt.core.settings.model.ICStorageElement)
	 */
	@Override
	protected long serialize(IContainer container, String file, ICStorageElement element) throws CoreException {
		// If the current loaded version is less than our version, then delegate to parent to handle persisting
		// i.e. don't auto-upgrade
		if (version.compareTo(type.version) < 0)
			return super.serialize(container, file, element);

		// Copy the original passed in element, as we're going to re-write the children
		InternalXmlStorageElement copy = copyElement(element, false);
		// Map containing external CConfiguration elements to be serialized
		Map<String, InternalXmlStorageElement> externalStorageElements = new HashMap<String, InternalXmlStorageElement>();

		// Iterate through the initial children
		for (ICStorageElement el : copy.getChildren()) {
			// Only interested in root level org.eclipse.cdt.settings storageModules
			if (!CProjectDescriptionManager.MODULE_ID.equals(el.getAttribute(XmlStorage.MODULE_ID_ATTRIBUTE)))
				continue;

			Queue<ICStorageElement> configStorages = new LinkedList<ICStorageElement>();
			configStorages.addAll(Arrays.asList(el.getChildren()));
			while (!configStorages.isEmpty()) {
				InternalXmlStorageElement iEl = (InternalXmlStorageElement)configStorages.remove();

				// If not a stroageModule element, the just add children ...
				// e.g. configuration element...
				if (iEl.getAttribute(XmlStorage.MODULE_ID_ATTRIBUTE) == null) {
					configStorages.addAll(Arrays.asList(iEl.getChildren()));
					continue;
				}

				// Persist this storage Element fileName = configurationID_moduleID
				String storageId = ((Element)iEl.fElement.getParentNode()).getAttribute(ID) + "_" //$NON-NLS-1$
								  + iEl.getAttribute(XmlStorage.MODULE_ID_ATTRIBUTE);

				// create a dummy storage for the element <cproject> held
				InternalXmlStorageElement newEl = createStorage(project.getFolder(STORAGE_FOLDER_NAME), storageId, false, true, false);
				newEl.importChild(copyElement(iEl, false));
				externalStorageElements.put(storageId, newEl);

				// Clear other attributes and children
				iEl.clear();
				// Set the external c element key
				iEl.fElement.setAttribute(EXTERNAL_CELEMENT_KEY, storageId);
			}
		}

		// Serialize the Root XML document Element (.cproject)
		long cproject_mod_time = super.serialize(project, ICProjectDescriptionStorageType.STORAGE_FILE_NAME, copy);

		// Check that the .csettings directory exists and is writable
		IFolder csettings = project.getFolder(STORAGE_FOLDER_NAME);
		try {
			CProjectDescriptionStorageManager.ensureWritable(csettings);
			if (!csettings.exists())
				csettings.create(true, true, new NullProgressMonitor());
		} catch (CoreException e) {
			if (!csettings.exists())
				throw e;
		}

		// Serialize all the external elements
		for (Map.Entry<String, InternalXmlStorageElement> e : externalStorageElements.entrySet())
			modificationMap.put(e.getKey(), super.serialize(csettings, e.getKey(), e.getValue()));

		return cproject_mod_time;
	}

	@Override
	protected Version getVersion() {
		return STORAGE_DESCRIPTION_VERSION;
	}

	@Override
	protected String getStorageTypeId() {
		return STORAGE_TYPE_ID;
	}

}
