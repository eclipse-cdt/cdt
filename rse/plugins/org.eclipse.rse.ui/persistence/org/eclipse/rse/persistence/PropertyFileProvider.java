/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed the initial implementation:
 * David McKnight, David Dykstal.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.persistence;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.model.ISystemProfileManager;
import org.eclipse.rse.persistence.dom.RSEDOM;

/**
 * This is class is used to restore an RSE DOM from disk and import it into RSE.
 * It stores the DOM as a tree of folders and .properties files.
 */
// TODO: dwd WIP
public class PropertyFileProvider implements IRSEPersistenceProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#saveRSEDOM(org.eclipse.rse.persistence.dom.RSEDOM, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean saveRSEDOM(RSEDOM dom, IProgressMonitor monitor) {
		String profileName = dom.getName();
		IFolder profileFolder = getFolder(profileName, monitor);
		System.out.println("saving to " + profileFolder.getFullPath().toString() + "..."); // TODO: dwd debugging
		try {
			// TODO: dwd function
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#loadRSEDOM(org.eclipse.rse.model.ISystemProfileManager, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RSEDOM loadRSEDOM(ISystemProfileManager profileManager, String profileName, IProgressMonitor monitor) {
		RSEDOM dom = null;
		IFolder profileFolder = getFolder(profileName, monitor);
		if (profileFolder.exists()) {
			System.out.println("loading from " + profileFolder.getFullPath().toString() + "..."); // TODO: dwd debugging
			try {
				// TODO: dwd function
			} catch (Exception e) {
				e.printStackTrace();
				try {
					profileFolder.delete(true, false, monitor);
				} catch (Exception e2) {
					e.printStackTrace();
				}
			}
		}
		return dom;
	}

	/**
	 * Returns the IFolder in which a profile can be stored. The folder is unique to 
	 * the type of persistence provider.
	 * @param monitor a progress monitor.
	 * @return The folder that was created or found.
	 */
	private IFolder getFolder(String profileName, IProgressMonitor monitor) {
		IProject project = SystemResourceManager.getRemoteSystemsProject();
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor); // ensure RemoteSystemsConnections project is in sync with the file system. 
		} catch (Exception e) {
		}
		IFolder providerFolder = project.getFolder("org.eclipse.rse.dom.properties");
		if (!providerFolder.exists()) {
			try {
				providerFolder.create(true, true, monitor);
			} catch (Exception e) {
			}
		}
		IFolder profileFolder = providerFolder.getFolder(profileName);
		if (!profileFolder.exists()) {
			try {
				profileFolder.create(true, true, monitor);
			} catch (Exception e) {
			}
		}
		return profileFolder;
	}
	
}