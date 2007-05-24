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

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.RSECorePlugin;

class PFMetadataAnchor implements PFPersistenceAnchor {
	
	public IStatus deleteProfileLocation(String profileName, IProgressMonitor monitor) {
		IStatus result = Status.OK_STATUS;
		File profileFolder = getProfileFolder(profileName);
		if (profileFolder.exists()) {
			profileFolder.delete();
		}
		return result;
	}

	public PFPersistenceLocation getProfileLocation(String profileLocationName) {
		File profileFolder = getProfileFolder(profileLocationName);
		PFPersistenceLocation result = new PFMetadataLocation(profileFolder);
		return result;
	}

	public String[] getProfileLocationNames() {
		List names = new Vector(10);
		File providerFolder = getProviderFolder();
		File[] profileCandidates = providerFolder.listFiles();
		for (int i = 0; i < profileCandidates.length; i++) {
			File profileCandidate = profileCandidates[i];
			if (profileCandidate.isDirectory()) {
				String candidateName = profileCandidate.getName();
				if (candidateName.startsWith(PFConstants.AB_PROFILE)) {
					names.add(candidateName);
				}
			}
		}
		String[] result = new String[names.size()];
		names.toArray(result);
		return result;
	}
	
	/**
	 * @return the folder that acts as the parent for profile folders.
	 */
	private File getProviderFolder() {
		IPath statePath = RSECorePlugin.getDefault().getStateLocation();
		File stateFolder = new File(statePath.toOSString());
		File providerFolder = getFolder(stateFolder, "profiles"); //$NON-NLS-1$
		return providerFolder;
	}
	
	/**
	 * Returns the File (directory) in which a profile is stored. 
	 * @return The folder that was created or found.
	 */
	private File getProfileFolder(String profileLocationName) {
		File  providerFolder = getProviderFolder();
		File profileFolder = getFolder(providerFolder, profileLocationName);
		return profileFolder;
	}

	/**
	 * Returns the specified folder of the parent container. If the folder does
	 * not exist it creates it.
	 * @param parent the parent folder
	 * @param name the name of the folder to find or create
	 * @return the found or created folder
	 */
	private File getFolder(File parent, String name) {
		File folder = new File(parent, name);
		if (!folder.exists()) {
			folder.mkdir();
		}
		return folder;
	}

}
