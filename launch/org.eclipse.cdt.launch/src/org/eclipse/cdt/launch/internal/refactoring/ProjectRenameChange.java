/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
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
	 * Initializes me.
	 * 
	 * @param launchConfig
	 *            the launch configuration that I change
	 * @param oldName
	 *            the old project name
	 * @param newName
	 *            the new project name
	 */
	public ProjectRenameChange(ILaunchConfiguration launchConfig,
			String oldName, String newName) {
		super(launchConfig);

		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public String getName() {
		if (changeName == null) {
			changeName = NLS.bind(LaunchMessages.getString("ProjectRenameChange.name"), //$NON-NLS-1$
					getLaunchConfiguration().getName());
		}

		return changeName;
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {

		ILaunchConfiguration launchConfig = getLaunchConfiguration();
		ILaunchConfigurationWorkingCopy copy = launchConfig.getWorkingCopy();

		IResource[] mapped = launchConfig.getMappedResources();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject oldProject = root.getProject(oldName);
		IProject newProject = root.getProject(newName);

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

		copy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				newName);

		try {
			copy.doSave();
		} catch (CoreException e) {
			LaunchUIPlugin.log(new MultiStatus(LaunchUIPlugin.PLUGIN_ID, 0,
					new IStatus[] { e.getStatus() }, NLS.bind(
							LaunchMessages.getString("ProjectRenameChange.saveFailed"), //$NON-NLS-1$
							launchConfig.getName()), null));
			return null; // not undoable, as we didn't effect our change
		}

		return new ProjectRenameChange(getLaunchConfiguration(), newName,
				oldName);
	}

}
