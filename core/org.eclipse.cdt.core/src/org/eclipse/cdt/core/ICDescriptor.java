/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

public interface ICDescriptor {
	public ICOwnerInfo getProjectOwner();
	public String getPlatform();
	public IProject getProject();
	
	public ICExtensionReference[] get(String extensionPoint);
	public ICExtensionReference[] get(String extensionPoint, boolean update) throws CoreException;
	public ICExtensionReference create(String extensionPoint, String id) throws CoreException;

	public void remove(ICExtensionReference extension) throws CoreException;
	public void remove(String extensionPoint) throws CoreException;
	
	public Element getProjectData(String id) throws CoreException;
	public void saveProjectData() throws CoreException;
}
