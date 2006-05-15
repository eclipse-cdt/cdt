/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.projectconverter;


import java.io.File;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeSettings;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

class UpdateManagedProject21 {

	private static final String CONTENT_TYPE_PREF_NODE = "content-types"; //$NON-NLS-1$
	private static final String FULLPATH_CONTENT_TYPE_PREF_NODE = Platform.PI_RUNTIME + IPath.SEPARATOR + CONTENT_TYPE_PREF_NODE;
	private static final String PREF_LOCAL_CONTENT_TYPE_SETTINGS = "enabled"; //$NON-NLS-1$
	
	/**
	 * @param monitor the monitor to allow users to cancel the long-running operation
	 * @param project the <code>IProject</code> that needs to be upgraded
	 * @throws CoreException
	 */
	static void doProjectUpdate(IProgressMonitor monitor, final IProject project) throws CoreException {
		String[] projectName = new String[]{project.getName()};
		IFile file = project.getFile(ManagedBuildManager.SETTINGS_FILE_NAME);
		File settingsFile = file.getLocation().toFile();
		if (!settingsFile.exists()) {
			monitor.done();
			return;
		}
		
		// Backup the file
		monitor.beginTask(ConverterMessages.getFormattedString("UpdateManagedProject20.0", projectName), 1); //$NON-NLS-1$
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		UpdateManagedProjectManager.backupFile(file, "_21backup", monitor, project); //$NON-NLS-1$

		// No physical conversion is need since the 3.0 model is a superset of the 2.1 model 
		// We need to upgrade the version
		((ManagedBuildInfo)info).setVersion("3.0.0"); //$NON-NLS-1$
		info.setValid(true);		

		// Save the updated file.
		// But first, check for this special case.  If the project is a C++ project, and it contains .c files, we add
		// the .c extension to the project-specific list of C++ file extensions so that these projects build as they
		// did in CDT 2.*.  Otherwise the .c files will not be compiled by default since CDT 3.0 switched to using 
		// Eclipse content types.
		// If the tree is locked spawn a job to this.
		IWorkspace workspace = project.getWorkspace();
//		boolean treeLock = workspace.isTreeLocked();
		ISchedulingRule rule1 = workspace.getRuleFactory().createRule(project);
		ISchedulingRule rule2 = workspace.getRuleFactory().refreshRule(project);
		ISchedulingRule rule = MultiRule.combine(rule1, rule2);
		//since the java synchronized mechanism is now used for the build info loadding,
		//initiate the job in all cases
//		if (treeLock) {
			WorkspaceJob job = new WorkspaceJob(ConverterMessages.getResourceString("UpdateManagedProject.notice")) { //$NON-NLS-1$
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					checkForCPPWithC(monitor, project);
					ManagedBuildManager.saveBuildInfo(project, true);
					return Status.OK_STATUS;
				}
			};
			job.setRule(rule);
			job.schedule();
//		} else {
//			checkForCPPWithC(monitor, project);
//			ManagedBuildManager.saveBuildInfo(project, true);
//		}
		monitor.done();
	}

	/**
	 * @param monitor the monitor to allow users to cancel the long-running operation
	 * @param project the <code>IProject</code> that needs to be upgraded
	 */
	static void checkForCPPWithC(IProgressMonitor monitor, final IProject project) {
		// Also we check for this special case.  If the project is a C++ project, and it contains .c files, we add
		// the .c extension to the project-specific list of C++ file extensions so that these projects build as they
		// did in CDT 2.*.  Otherwise the .c files will not be compiled by default since CDT 3.0 switched to using 
		// Eclipse content types.
		if (CoreModel.hasCCNature(project)) {
			try {
				try {
					// Refresh the project here since we may be called before an import operation has fully 
					// completed setting up the project's resources
					IWorkspace workspace = project.getWorkspace();
					IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor) throws CoreException {
							project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
						}
					};
					workspace.run(runnable, project, IWorkspace.AVOID_UPDATE, monitor);
				} catch (Exception e) {}	// Ignore the error - the user may have to add .c extensions to
								// the local definition of C++ file extensions
				
				final boolean found[] = new boolean[1];
				project.accept(new IResourceProxyVisitor(){

						/* (non-Javadoc)
						 * @see org.eclipse.core.resources.IResourceProxyVisitor#visit(org.eclipse.core.resources.IResourceProxy)
						 */
						public boolean visit(IResourceProxy proxy) throws CoreException {
							if(found[0] || proxy.isDerived())
								return false;
							if(proxy.getType() == IResource.FILE){
								String ext = proxy.requestFullPath().getFileExtension();
								if (ext != null && "c".equals(ext)) { //$NON-NLS-1$
									found[0] = true;
								}
								return false;
							}
							return true;
						}
					},
					IResource.NONE);
				
				if(found[0]){
					IScopeContext projectScope = new ProjectScope(project);

					// First, we need to enable user settings on the project __explicitely__
					// Unfortunately there is no clear API in Eclipse-3.1 to do this.
					// We should revisit this code when Eclipse-3.1.x and above is out
					// with more complete API.
					Preferences contentTypePrefs = projectScope.getNode(FULLPATH_CONTENT_TYPE_PREF_NODE);
					// enable project-specific settings for this project
					contentTypePrefs.putBoolean(PREF_LOCAL_CONTENT_TYPE_SETTINGS, true);
					try {
						contentTypePrefs.flush();
					} catch (BackingStoreException e) {
						// ignore ??
					}

					// Now the project setting is on/enable.
					// Add the new association in the project user setting.
					// the conflict resolution of the ContentTypeManager framework
					// will give preference to the project settings.
					IContentTypeManager manager = Platform.getContentTypeManager();
					IContentType contentType = manager.getContentType("org.eclipse.cdt.core.cxxSource");	//$NON-NLS-1$
					IContentTypeSettings settings = contentType.getSettings(projectScope);
					// Add the .c extension on the C++ content type.
					settings.addFileSpec("c", IContentType.FILE_EXTENSION_SPEC);	//$NON-NLS-1$
				}
			} catch (CoreException e) {
				// Ignore errors.  User will need to manually add .c extension if necessary
			}
		}
	}
}