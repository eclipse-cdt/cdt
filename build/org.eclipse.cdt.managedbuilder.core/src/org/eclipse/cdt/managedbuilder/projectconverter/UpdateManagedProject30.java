/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.projectconverter;

import java.io.File;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class UpdateManagedProject30 {

	/**
	 * @param monitor the monitor to allow users to cancel the long-running operation
	 * @param project the <code>IProject</code> that needs to be upgraded
	 */
	static void doProjectUpdate(IProgressMonitor monitor, final IProject project) throws CoreException {
		String[] projectName = new String[] { project.getName() };
		IFile file = project.getFile(ManagedBuildManager.SETTINGS_FILE_NAME);
		File settingsFile = file.getLocation().toFile();
		if (!settingsFile.exists()) {
			monitor.done();
			return;
		}

		// Backup the file
		monitor.beginTask(ConverterMessages.getFormattedString("UpdateManagedProject20.0", projectName), 1); //$NON-NLS-1$
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		UpdateManagedProjectManager.backupFile(file, "_30backup", monitor, project); //$NON-NLS-1$

		// No physical conversion is need since the 3.1 model is a superset of the 3.0 model
		// We need to upgrade the version
		((ManagedBuildInfo) info).setVersion("3.1.0"); //$NON-NLS-1$
		//		info.setValid(true);

		//no need to persist data here
		//		// Save the updated file.
		//		IWorkspace workspace = project.getWorkspace();
		////		boolean treeLock = workspace.isTreeLocked();
		//		ISchedulingRule rule1 = workspace.getRuleFactory().createRule(project);
		//		ISchedulingRule rule2 = workspace.getRuleFactory().refreshRule(project);
		//		ISchedulingRule rule = MultiRule.combine(rule1, rule2);
		//		//since the java synchronized mechanism is now used for the build info loadding,
		//		//initiate the job in all cases
		////		if (treeLock) {
		//			WorkspaceJob job = new WorkspaceJob(ConverterMessages.getResourceString("UpdateManagedProject.notice")) { //$NON-NLS-1$
		//				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		//					ManagedBuildManager.saveBuildInfo(project, true);
		//					return Status.OK_STATUS;
		//				}
		//			};
		//			job.setRule(rule);
		//			job.schedule();
		////		} else {
		////			checkForCPPWithC(monitor, project);
		////			ManagedBuildManager.saveBuildInfo(project, true);
		////		}
		//		monitor.done();
	}

}
