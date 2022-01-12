/*******************************************************************************
 * Copyright (c) 2009, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation9
 *******************************************************************************/

package org.eclipse.cdt.launch.internal.refactoring;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.osgi.util.NLS;

/**
 * A change to update a launch configuration with a new project name.
 *
 * @author Christian W. Damus (cdamus)
 *
 * @since 6.0
 */
class ProjectRenameChange extends AbstractLaunchConfigChange {

	private String changeName;

	private String oldName;
	private String newName;

	/**
	 * The project relative path of the .launch file if the launch config is a
	 * non-local one and is stored within the project.
	 */
	private IPath projectRelativePath;

	/**
	 * Initializes me.
	 *
	 * @param launchConfig
	 *            the launch configuration that I change
	 * @param oldName
	 *            the old project name
	 * @param newName
	 *            the new project name
	 */
	public ProjectRenameChange(ILaunchConfiguration launchConfig, String oldName, String newName) {
		super(launchConfig);

		this.oldName = oldName;
		this.newName = newName;

		// keep the project relative path if launch config is contained in the old project
		if (!launchConfig.isLocal()) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject oldProject = root.getProject(oldName);
			IPath oldConfig = launchConfig.getFile().getLocation();
			if (oldConfig != null && oldProject.getLocation().isPrefixOf(oldConfig)) {
				projectRelativePath = oldConfig.makeRelativeTo(oldProject.getLocation());
			}
		}
	}

	@Override
	public String getName() {
		if (changeName == null) {
			changeName = NLS.bind(LaunchMessages.ProjectRenameChange_name, getLaunchConfiguration().getName());
		}

		return changeName;
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject oldProject = root.getProject(oldName);
		IProject newProject = root.getProject(newName);

		ILaunchConfiguration launchConfig = getLaunchConfiguration();
		if (projectRelativePath != null) {
			// If the launch config is non-local and lives in the project, we
			// need to update its representation in the new project folder, not
			// the old one
			ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
			launchConfig = mgr.getLaunchConfiguration(newProject.getFile(projectRelativePath));
		}

		ILaunchConfigurationWorkingCopy copy = launchConfig.getWorkingCopy();
		IResource[] mapped = launchConfig.getMappedResources();

		if ((oldProject != null) && (newProject != null)) {
			if ((mapped != null) && (mapped.length > 0)) {
				for (int i = 0; i < mapped.length; i++) {
					if (oldProject.equals(mapped[i])) {
						mapped[i] = newProject;
					}
				}

				copy.setMappedResources(mapped);
			}
		}

		copy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, newName);

		// Update the program name if it corresponds to the project name
		IPath pathProgName = new Path(
				launchConfig.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "")); //$NON-NLS-1$
		String progExtension = pathProgName.getFileExtension();
		String progName = pathProgName.removeFileExtension().lastSegment();
		if (oldName.equals(progName)) {
			pathProgName = pathProgName.removeLastSegments(1).append(newName);
			if (progExtension != null)
				pathProgName = pathProgName.addFileExtension(progExtension);
			copy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, pathProgName.toOSString());
		}

		try {
			// Note: for non-local LCs, this will end up updating the .launch
			// file on disk but Eclipse's in memory representation will not
			// get updated. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=288368#c1
			// This comment can/should be removed when 288368 is fixed.
			copy.doSave();
		} catch (CoreException e) {
			LaunchUIPlugin.log(new MultiStatus(LaunchUIPlugin.PLUGIN_ID, 0, new IStatus[] { e.getStatus() },
					NLS.bind(LaunchMessages.ProjectRenameChange_saveFailed, launchConfig.getName()), null));
			return null; // not undoable, as we didn't effect our change
		}

		return new ProjectRenameChange(launchConfig, newName, oldName);
	}

}
