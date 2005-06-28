/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
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
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeSettings;
import org.eclipse.core.runtime.preferences.IScopeContext;

class UpdateManagedProject21 {
	
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
		((ManagedBuildInfo)info).setVersion(ManagedBuildManager.getBuildInfoVersion().toString());
		info.setValid(true);
		// Also we check for this special case.  If the project is a C++ project, and it contains .c files, we add
		// the .c extension to the project-specific list of C++ file extensions so that these projects build as they
		// did in CDT 2.*.  Otherwise the .c files will not be compiled by default since CDT 3.0 switched to using 
		// Eclipse content types.
		if (CoreModel.hasCCNature(project)) {
			try {
				IResource[] files = project.members(IProject.EXCLUDE_DERIVED);
				for (int i=0; i<files.length; i++) {
					String ext = files[i].getFileExtension();
					if (ext != null && ext.equals("c")) {						//$NON-NLS-1$
						IContentTypeManager manager = Platform.getContentTypeManager();
						IContentType contentType = manager.getContentType("org.eclipse.cdt.core.cxxSource");	//$NON-NLS-1$
						IScopeContext projectScope = new ProjectScope(project);
						IContentTypeSettings settings = contentType.getSettings(projectScope);
						// First, copy the extensions from the "global" content type
						String[] specs = contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
						for (int j = 0; j < specs.length; j++) {
							settings.addFileSpec(specs[j], IContentType.FILE_EXTENSION_SPEC);
						}
						specs = contentType.getFileSpecs(IContentType.FILE_NAME_SPEC);
						for (int j = 0; j < specs.length; j++) {
							settings.addFileSpec(specs[j], IContentType.FILE_NAME_SPEC);
						}				
						// Add the .c extension
						settings.addFileSpec("c", IContentType.FILE_EXTENSION_SPEC);	//$NON-NLS-1$
						break;
					}
				}
			} catch (CoreException e) {
				// Ignore errors.  User will need to manually add .c extension if necessary
			}
		}

		// Save the updated file
		// If the tree is locked spawn a job to this.
		IWorkspace workspace = project.getWorkspace();
		boolean treeLock = workspace.isTreeLocked();
		ISchedulingRule rule = workspace.getRuleFactory().createRule(project);
		if (treeLock) {
			WorkspaceJob job = new WorkspaceJob(ConverterMessages.getResourceString("UpdateManagedProject.notice")) { //$NON-NLS-1$
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					ManagedBuildManager.saveBuildInfo(project, true);
					return Status.OK_STATUS;
				}
			};
			job.setRule(rule);
			job.schedule();
		} else {
			ManagedBuildManager.saveBuildInfo(project, true);
		}
		monitor.done();
	}
}
