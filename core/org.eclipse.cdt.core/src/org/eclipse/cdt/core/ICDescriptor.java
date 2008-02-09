/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

/**
 * Models meta-data stored with a CDT project
 */
public interface ICDescriptor {
	public ICOwnerInfo getProjectOwner();
	public String getPlatform();
	
	/**
	 * @return the associated project
	 */
	public IProject getProject();
	
	public ICExtensionReference[] get(String extensionPoint);
	public ICExtensionReference[] get(String extensionPoint, boolean update) throws CoreException;
	public ICExtensionReference create(String extensionPoint, String id) throws CoreException;

	public void remove(ICExtensionReference extension) throws CoreException;
	public void remove(String extensionPoint) throws CoreException;
	
	/**
	 * @param id an identifier that uniquely identifies the client
	 * @return a non-null {@link Element} to which client specific meta-data may be attached
	 * @throws CoreException
	 */
	public Element getProjectData(String id) throws CoreException;
	
	/**
	 * Saves any changes made to {@link Element} objects obtained from {@link #getProjectData(String)}
	 * to a CDT defined project meta-data file.
	 * @throws CoreException
	 */
	public void saveProjectData() throws CoreException;
	
	ICConfigurationDescription getConfigurationDescription();
}
