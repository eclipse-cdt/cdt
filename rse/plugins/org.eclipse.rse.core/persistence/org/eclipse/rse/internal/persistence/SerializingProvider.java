/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.internal.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;
import org.eclipse.rse.persistence.dom.RSEDOM;

/**
 * This is class is used to restore an RSE DOM from disk and import it into RSE.
 * @author dmcknigh
 *
 */
public class SerializingProvider implements IRSEPersistenceProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#getSavedProfileNames()
	 */
	public String[] getSavedProfileNames() {
		/*
		 * We look for folders inside the RemoteSystemsConnections folder that contain
		 * a single file named folderName.rsedom. We return the array of folder names.
		 */
		List names = new Vector(10);
		try {
			IProject project = RSEPersistenceManager.getRemoteSystemsProject();
			IResource[] candidates = project.members();
			for (int i = 0; i < candidates.length; i++) {
				IResource candidate = candidates[i];
				if (candidate.getType() == IResource.FOLDER) {
					IFolder candidateFolder = (IFolder) candidate;
					IResource[] children = candidateFolder.members();
					if (children.length == 1) {
						IResource child = children[0];
						if (child.getType() == IResource.FILE) {
							String profileName = candidateFolder.getName();
							String domFileName = profileName + ".rsedom"; //$NON-NLS-1$
							String childName = child.getName();
							if (childName.equals(domFileName)) {
								names.add(profileName);
							}
						}
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] result = new String[names.size()];
		names.toArray(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#loadRSEDOM(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RSEDOM loadRSEDOM(String profileName, IProgressMonitor monitor) {
		RSEDOM dom = null;
		IFile profileFile = getProfileFile(profileName, monitor);
		if (profileFile.exists()) {
			//System.out.println("loading "+ profileFile.getLocation().toOSString() + "..."); // DWD debugging
			try {
				InputStream iStream = profileFile.getContents();

				ObjectInputStream inStream = new ObjectInputStream(iStream);
				dom = (RSEDOM) inStream.readObject();
				inStream.close();
			} catch (Exception e) {
				e.printStackTrace();
				try {
					profileFile.delete(true, false, monitor);
				} catch (Exception e2) {
					e.printStackTrace();
				}

			}
		}
		return dom;
	}

	private IFile getProfileFile(String domName, IProgressMonitor monitor) {
		IProject project = RSEPersistenceManager.getRemoteSystemsProject();

		// before loading, make sure the project is in synch
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (Exception e) {
		}

		IFolder folder = project.getFolder(domName);
		if (!folder.exists()) {
			try {
				folder.create(true, true, monitor);
			} catch (Exception e) {
			}
		}
		return folder.getFile(domName + ".rsedom"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceProvider#saveRSEDOM(org.eclipse.rse.persistence.dom.RSEDOM, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean saveRSEDOM(RSEDOM dom, IProgressMonitor monitor) {

		IFile profileFile = getProfileFile(dom.getName(), monitor);
		File osFile = profileFile.getLocation().toFile();
		//	System.out.println("saving "+ osFile.getAbsolutePath() + "..."); // DWD debugging
		try {
			OutputStream oStream = new FileOutputStream(osFile);
			ObjectOutputStream outStream = new ObjectOutputStream(oStream);
			outStream.writeObject(dom);
			outStream.close();
			profileFile.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public IStatus deleteProfile(String profileName, IProgressMonitor monitor) {
		IStatus result = Status.OK_STATUS;
		IFile profileFile = getProfileFile(profileName, monitor);
		if (profileFile.exists()) {
			try {
				profileFile.delete(IResource.FORCE | IResource.KEEP_HISTORY, monitor);
			} catch (CoreException e) {
				result = new Status(IStatus.ERROR, null, 0, Messages.SerializingProvider_UnexpectedException, e);
			}
		}
		return result;
	}
}