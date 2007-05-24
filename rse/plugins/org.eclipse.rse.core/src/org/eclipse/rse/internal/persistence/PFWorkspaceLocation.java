/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.persistence;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.core.RSECorePlugin;

class PFWorkspaceLocation implements PFPersistenceLocation {
	IFolder baseFolder = null;
	
	public PFWorkspaceLocation(IFolder baseResource) {
		this.baseFolder = baseResource;
	}
	
	public boolean exists() {
		return baseFolder.exists();
	}
	
	public void ensure() {
		if (!baseFolder.exists()) {
			try {
				baseFolder.create(true, true, null);
			} catch (CoreException e) {
				logException(e);
			}
		}
	}
	
	public PFPersistenceLocation getChild(String childName) {
		IPath path = new Path(childName);
		IFolder member = baseFolder.getFolder(path);
		PFPersistenceLocation result = new PFWorkspaceLocation(member);
		return result;
	}
	
	public PFPersistenceLocation[] getChildren() {
		IResource[] members;
		try {
			members = baseFolder.members();
		} catch (CoreException e) {
			logException(e);
			members = new IResource[0];
		}
		List children = new ArrayList(members.length);
		for (int i = 0; i < members.length; i++) {
			IResource member = members[i];
			if (member.getType() == IResource.FOLDER) {
				PFPersistenceLocation child = new PFWorkspaceLocation((IFolder)member);
				children.add(child);
			}
		}
		PFPersistenceLocation[] result = new PFPersistenceLocation[children.size()];
		children.toArray(result);
		return result;
	}
	
	public URI getLocator() {
		return baseFolder.getLocationURI();
	}
	
	public String getName() {
		return baseFolder.getName();
	}
	
	public boolean hasContents() {
		IPath propertiesFileName = new Path(PFConstants.PROPERTIES_FILE_NAME);
		IFile propertiesFile = baseFolder.getFile(propertiesFileName);
		boolean result = propertiesFile.exists();
		return result;
	}
	
	public void keepChildren(Set keepSet) {
		try {
			IResource[] children = baseFolder.members();
			for (int i = 0; i < children.length; i++) {
				IResource child = children[i];
				if (child.getType() == IResource.FOLDER) {
					String childFolderName = child.getName();
					if (!keepSet.contains(childFolderName)) {
						child.delete(true, null);
					}
				}
			}
		} catch (CoreException e) {
			logException(e);
		}
	}
	
	public void setContents(InputStream stream) {
		IPath propertiesFileName = new Path(PFConstants.PROPERTIES_FILE_NAME);
		IFile propertiesFile = baseFolder.getFile(propertiesFileName);
		try {
			if (propertiesFile.exists()) {
				propertiesFile.setContents(stream, IResource.FORCE | IResource.KEEP_HISTORY, null);
			} else {
				propertiesFile.create(stream, IResource.FORCE | IResource.KEEP_HISTORY, null);
			}
		} catch (CoreException e) {
			logException(e);
		}
	}
	
	public InputStream getContents() {
		InputStream result = null;
		IPath propertiesFileName = new Path(PFConstants.PROPERTIES_FILE_NAME);
		IFile propertiesFile = baseFolder.getFile(propertiesFileName);
		if (propertiesFile.exists()) {
			try {
				result = propertiesFile.getContents();
			} catch (CoreException e) {
				logException(e);
			}
		}
		return result;
	}
	
	private void logException(Exception e) {
		RSECorePlugin.getDefault().getLogger().logError("unexpected exception", e); //$NON-NLS-1$
	}

}
