/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.runtime.CoreException;

/**
 * this interface represents the settings storage that can be used for storing
 * data in the tree-like format of name-value holder elements
 * 
 * Both {@link ICProjectDescription} and {@link ICConfigurationDescription} implement this
 * interface thus providing the capabilities to store custom project-wide and configuration-specific
 * data in the storage file (.cproject)
 * 
 * These capabilities could be used, e.g. by the Build System 
 * (org.eclipse.cdt.core.CConfigurationDataProvider extension implementer)
 * for loadding/storing data on the {@link CConfigurationDataProvider#loadConfiguration(ICConfigurationDescription)}
 * and {@link CConfigurationDataProvider#applyConfiguration(ICConfigurationDescription, org.eclipse.cdt.core.settings.model.extension.CConfigurationData)}
 * requests 
 *
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

//	/**
//	 * 
//	 * @param id any custom string value uniquely representing the storage 
//	 * @return true if the setting storage contains storage of the specified id and false - otherwise
//	 * @throws CoreException
//	 */
//	boolean containsStorage(String id) throws CoreException;
	
	void removeStorage(String id) throws CoreException;
}
