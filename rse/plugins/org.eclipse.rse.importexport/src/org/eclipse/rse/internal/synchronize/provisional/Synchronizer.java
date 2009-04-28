/*******************************************************************************
 * Copyright (c) 2008, 2009 Takuya Miyamoto and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Takuya Miyamoto - initial API and implementation
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.provisional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.eclipse.rse.internal.importexport.SystemImportExportResources;
import org.eclipse.rse.internal.importexport.files.UniFilePlus;
import org.eclipse.rse.internal.synchronize.ISynchronizeData;
import org.eclipse.rse.internal.synchronize.filesystem.FileSystemProvider;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;

public class Synchronizer implements ISynchronizer {
	private ISynchronizeData data;
	private ISynchronizeConnectionManager connector;

	/**
	 * TODO in the future, remoteRoot is probably needed for import or export
	 */
	private UniFilePlus remoteRoot;

	public Synchronizer(ISynchronizeData data) {
		this.data = data;
		this.connector = new SynchronizeConnectionManager();
	}

	public boolean run(ISynchronizeOperation operation) {
		IProject[] projects = null;
		List<IResource> elements = data.getElements();
		Set<IProject> projectSet = new HashSet<IProject>();
		
		for (IResource resource : elements) {
			projectSet.add(resource.getProject());
			if (!resource.exists()){
				IContainer parent = resource.getParent();
				if (!parent.exists()){
					createEmptyFolders(parent);
				}
			}
		}

		// get resources to synchronize in the type of Array.
		projects = projectSet.toArray(new IProject[projectSet.size()]);

		try {
			// if user request new synchronization, previous mapping are
			// removed.
			if (data.getSynchronizeType() == ISynchronizeOperation.SYNC_MODE_OVERRIDE_DEST || 
					data.getSynchronizeType() == ISynchronizeOperation.SYNC_MODE_OVERRIDE_SOURCE || 
					data.getSynchronizeType() == ISynchronizeOperation.SYNC_MODE_UI_REVIEW_INITIAL) {
				for (int i = 0; i < projects.length; i++) {
					
					IProject project = projects[i];
					// user should be prompted before disconnect or he/she will lose team synch info
					if (connector.isConnected(project)){
						
						RepositoryProvider provider = RepositoryProvider.getProvider(project);
						if (!(provider instanceof FileSystemProvider)){						
							String msg = NLS.bind(SystemImportExportResources.RESID_SYNCHRONIZE_DISCONNECT_WARNING, project.getName());
							SystemMessage msgObj = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID, IStatus.WARNING, msg);
						
							SystemMessageDialog dlg = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), msgObj);
							if (!dlg.openQuestionNoException(true)){												
								return false;
							}
						}

						connector.disconnect(project);
					}
				}

			}
			
			// create new connection for each project
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				connector.connect(project);
				FileSystemProvider provider = (FileSystemProvider) RepositoryProvider.getProvider(project);
				provider.reset();
				String remoteLocation = data.getRemoteLocation();
				IPath localLocation = data.getLocalLocation();				
				provider.setRemoteLocation(remoteLocation);
				provider.setLocalLocation(localLocation);
				this.remoteRoot = provider.getRemoteRootFolder();
			}
			
			

			// run actual synchronize operation.
			// TODO currently, not support last synchronization date.
			operation.synchronize(data.getElements(), remoteRoot.remoteFile, null, null, data.getSynchronizeType());
		} catch (TeamException e1) {
			e1.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return true;
	}
	
	private void createEmptyFolders(IContainer container){
		List emptyParent = new ArrayList();
		boolean go = true;
		
		IContainer empty = container;
		
		//check to see which parent folders need to be created
		while(go) {
			if(!empty.exists() && empty instanceof IFolder){
				emptyParent.add(empty);
			}
			else {
				go=false;
			}
			empty = empty.getParent();
		}
		
		IFolder emptyFolder = null;		
		
		// create empty parent folders
		for(int j=emptyParent.size()-1;j>=0;j--){
			emptyFolder = (IFolder) emptyParent.get(j);
			if(!emptyFolder.exists()){
				try {
					emptyFolder.create(true, true, new NullProgressMonitor());
				}
				catch (CoreException e){}
			}
		}
	}
}
