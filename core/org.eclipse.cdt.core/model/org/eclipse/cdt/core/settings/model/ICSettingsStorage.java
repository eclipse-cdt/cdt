/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationDataProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface represents the settings storage that can be used as the root
 * of a settings tree of name-attribute-value holder elements ({@link ICStorageElement}s).
 * <br /><br />
 * In real terms this is a specialised node in the project description tree. It is specialised
 * in that it can only contain ICStorageElements as children and has no associated attributes or
 * value.  The Xml model implements this as an element called 'storageModule' which contains
 * other arbitrary Xml ICStorageElements.
 * <br /><br />
 * Both {@link ICProjectDescription} and {@link ICConfigurationDescription} implement this
 * interface thus providing the capabilities to store custom project-wide and configuration-specific
 * data in the storage file
 * <br /><br />
 * The format of the storage file is left up to the implementor.  It may be an XML file
 * (.cproject) a relational database (.cprojectdb) or any other format of the extenders choosing.
 * <br /><br />
 * These capabilities are used by the build system for persisting build configuration data
 * as well as by the CoreModel {@link ICDescriptor} storage trees. See
 * {@link CConfigurationDataProvider#loadConfiguration(ICConfigurationDescription, IProgressMonitor)}
 * and {@link CConfigurationDataProvider#applyConfiguration(ICConfigurationDescription, ICConfigurationDescription, CConfigurationData, IProgressMonitor)}
 *
 * @see ICStorageElement
 * @see ICProjectDescription
 * @see ICConfigurationDescription
 * @see ICDescriptor
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICSettingsStorage {
	/**
	 * returns the storage of the specified id
	 * @param id any custom string value uniquely representing the storage
	 * @return {@link ICStorageElement} if the settings storage does not contain the information of
	 * the specified id an empty storage is created and returned
	 * @throws CoreException
	 *
	 * @see {@link ICStorageElement}
	 */
	ICStorageElement getStorage(String id, boolean create) throws CoreException;

	/**
	 * Return a Map of StorageID -> ICStorageElement
	 * @return
	 */
//	Map<String, ICStorageElement> getStorages();

	/**
	 * Remove the storage module with the given ID from this ICSettingsStorage
	 * @param id
	 * @throws CoreException
	 */
	void removeStorage(String id) throws CoreException;

	/**
	 * Import an existing ICStorageElement storage module into the ICSettingsStorage
	 * Returns a handle on the newly imported ICSettingsStorage
	 * 
	 * NB Storage IDs are unique in an ICSettingsStorage.   Importing a storage
	 * will replace any other storage with equivalent id 
	 * @param id name of the storage to be imported
	 * @param el ICStorageElement to be imported
	 * @return ICStorageElement representing the imported storage
	 * @throws UnsupportedOperationException
	 * @since 5.1
	 */
	public ICStorageElement importStorage(String id, ICStorageElement el) throws UnsupportedOperationException, CoreException;

	/**
	 * Returns whether any non-persisted changes exist in this tree
	 * @return boolean indicating whether any elements in this tree have been modified
	 * @since 5.1
	 */
	public boolean isModified();

	/**
	 * Return whether this Settings Storage is currently read only
	 * @return whether this storage is readonly
	 * @since 5.1
	 */
	public boolean isReadOnly();

	/**
	 * Mark this Settings Storage as read only.  If keepModify is set
	 * then modified flag will not be reset
	 * @param readOnly
	 * @param keepModify
	 * @since 5.1
	 */
	void setReadOnly(boolean readOnly, boolean keepModify);

}
