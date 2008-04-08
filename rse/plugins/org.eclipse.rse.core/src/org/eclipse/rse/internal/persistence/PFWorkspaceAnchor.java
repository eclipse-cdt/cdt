/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [189858] delayed the creation of the remote systems project by using handle-only operations.
 * David Dykstal (IBM) - [225988] need API to mark persisted profiles as migrated
 *******************************************************************************/

package org.eclipse.rse.internal.persistence;

import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.IRSECoreStatusCodes;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;
import org.eclipse.rse.persistence.dom.RSEDOM;

class PFWorkspaceAnchor implements PFPersistenceAnchor {

	public String[] getProfileLocationNames() {
		List names = new Vector(10);
		IFolder providerFolder = getProviderFolder();
		if (providerFolder.isAccessible()) {
			try {
				IResource[] profileCandidates = providerFolder.members();
				for (int i = 0; i < profileCandidates.length; i++) {
					IResource profileCandidate = profileCandidates[i];
					if (profileCandidate.getType() == IResource.FOLDER) {
						String candidateName = profileCandidate.getName();
						if (candidateName.startsWith(PFConstants.AB_PROFILE)) {
							names.add(candidateName);
						}
					}
				}
			} catch (CoreException e) {
				logException(e);
			}
		}
		String[] result = new String[names.size()];
		names.toArray(result);
		return result;
	}
	
	public IStatus deleteProfileLocation(String profileName, IProgressMonitor monitor) {
		IStatus result = Status.OK_STATUS;
		IFolder profileFolder = getProfileFolder(profileName);
		if (profileFolder.exists()) {
			try {
				profileFolder.delete(IResource.FORCE, monitor);
			} catch (CoreException e) {
				result = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IRSECoreStatusCodes.EXCEPTION_OCCURRED, RSECoreMessages.PropertyFileProvider_UnexpectedException, e);
			}
		}
		return result;
	}
	
	public PFPersistenceLocation getProfileLocation(String profileLocationName) {
		IFolder profileFolder = getProfileFolder(profileLocationName);
		PFPersistenceLocation result = new PFWorkspaceLocation(profileFolder);
		return result;
	}

	public Job makeSaveJob(RSEDOM dom, IRSEPersistenceProvider provider) {
		return new PFWorkspaceJob(dom, provider);
	}

	/**
	 * Returns the IFolder in which a profile is stored. 
	 * This is a handle operation, the resulting folder may not exist.
	 * @return The folder that was found.
	 */
	private IFolder getProfileFolder(String profileLocationName) {
		IFolder providerFolder = getProviderFolder();
		IFolder profileFolder = getFolder(providerFolder, profileLocationName);
		return profileFolder;
	}

	/**
	 * Returns the IFolder in which this persistence provider stores its profiles.
	 * This is a handle operation. It will not create the folder if it is not 
	 * found.
	 * @return The folder that contains the profiles for this provider.
	 */
	private IFolder getProviderFolder() {
		IProject project = SystemResourceManager.getRemoteSystemsProject(false);
		if (project.isAccessible()) {
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (Exception e) {
			}
		}
		IFolder providerFolder = getFolder(project, "dom.properties"); //$NON-NLS-1$
		return providerFolder;
	}
	
	/**
	 * Returns the specified folder of the parent container.
	 * This is a handle operation. The folder may not exist.
	 * @param parent the parent container - typically a project or folder
	 * @param name the name of the folder to find
	 * @return the found folder (may not exist)
	 */
	private IFolder getFolder(IContainer parent, String name) {
		IPath path = new Path(name);
		IFolder folder = parent.getFolder(path);
		return folder;
	}

	private void logException(Exception e) {
		RSECorePlugin.getDefault().getLogger().logError("unexpected exception", e); //$NON-NLS-1$
	}

}
