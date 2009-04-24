/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import static org.eclipse.cdt.internal.ui.workingsets.WorkingSetConfigurationManager.ATTR_CONFIG;
import static org.eclipse.cdt.internal.ui.workingsets.WorkingSetConfigurationManager.ATTR_NAME;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IMemento;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Default implementation of the {@link IWorkingSetProjectConfiguration} interface. Clients may extend this
 * class to implement additional project configuration settings.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 * 
 */
public class WorkingSetProjectConfiguration implements IWorkingSetProjectConfiguration {
	private final IWorkingSetConfiguration workingSetConfig;
	private String projectName;
	private String selectedConfiguration;

	/**
	 * Initializes me with my parent working set configuration.
	 * 
	 * @param parent
	 *            my parent working set configuration
	 */
	protected WorkingSetProjectConfiguration(IWorkingSetConfiguration parent) {
		this.workingSetConfig = parent;
	}

	public IWorkingSetConfiguration getWorkingSetConfiguration() {
		return workingSetConfig;
	}

	public String getProjectName() {
		return projectName;
	}

	/**
	 * Sets my project name. Note that this <b>does not</b> change the name of the project that I represent.
	 * Rather, it changes <i>which</i> project I represent.
	 * 
	 * @param projectName
	 *            my new project name
	 */
	protected void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public IProject resolveProject() {
		IProject result = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());

		if ((result != null) && !result.isAccessible()) {
			result = null;
		}

		return result;
	}

	public String getSelectedConfigurationID() {
		return selectedConfiguration;
	}

	protected void setSelectedConfigurationID(String id) {
		this.selectedConfiguration = id;
	}

	public ICConfigurationDescription resolveSelectedConfiguration() {
		ICConfigurationDescription result = null;

		IProject project = resolveProject();
		if (project != null) {
			ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(project);

			if (desc != null) {
				result = desc.getConfigurationById(getSelectedConfigurationID());
			}
		}

		return result;
	}

	public Collection<ICConfigurationDescription> resolveConfigurations() {
		ICConfigurationDescription[] result = null;

		IProject project = resolveProject();
		if (project != null) {
			ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(project);

			if (desc != null) {
				result = desc.getConfigurations();
			}
		}

		return (result == null) ? Collections.<ICConfigurationDescription> emptyList() : Arrays
				.asList(result);
	}

	public boolean isActive() {
		ICConfigurationDescription desc = resolveSelectedConfiguration();

		return (desc != null) && desc.isActive();
	}

	public void activate() {
		ICConfigurationDescription config = resolveSelectedConfiguration();

		if (config != null) {
			ICProjectDescription desc = config.getProjectDescription();

			if (desc.getActiveConfiguration() != config) {
				try {
					IProject project = desc.getProject();
					desc.setActiveConfiguration(config);
					CoreModel.getDefault().setProjectDescription(project, desc);
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
		}
	}

	public IStatus build(IProgressMonitor monitor) {
		IStatus result = Status.OK_STATUS;

		ICConfigurationDescription config = resolveSelectedConfiguration();

		if (config == null) {
			result = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, NLS.bind(
					WorkingSetMessages.WSProjConfig_noConfig, getProjectName()));
		} else {
			if (!isActive()) {
				activate();
				result = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, NLS.bind(
						WorkingSetMessages.WSProjConfig_activatedWarning, config.getName(), getProjectName()));
			}

			monitor = SubMonitor.convert(monitor);

			try {
				resolveProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
			} catch (CoreException e) {
				if (result.isOK()) {
					result = e.getStatus();
				} else {
					result = new MultiStatus(CUIPlugin.PLUGIN_ID, 0, new IStatus[] { result, e.getStatus() },
							NLS.bind(WorkingSetMessages.WSProjConfig_buildProblem, getProjectName()), null);
				}
			}
		}

		return result;
	}

	public void saveState(IMemento memento) {
		memento.putString(ATTR_NAME, getProjectName());

		if (getSelectedConfigurationID() != null) {
			memento.putString(ATTR_CONFIG, getSelectedConfigurationID());
		}
	}

	public void loadState(IMemento memento) {
		projectName = memento.getString(ATTR_NAME);
		selectedConfiguration = memento.getString(ATTR_CONFIG);
	}

	public ISnapshot createSnapshot(IWorkingSetConfiguration.ISnapshot workingSetConfig,
			WorkspaceSnapshot workspace) {

		return new Snapshot(workingSetConfig, this, workspace);
	}

	//
	// Nested classes
	//

	/**
	 * Default implementation of the mutable project configuration snapshot.
	 * 
	 * @author Christian W. Damus (cdamus)
	 * 
	 * @since 6.0
	 */
	public static class Snapshot extends WorkingSetProjectConfiguration implements
			IWorkingSetProjectConfiguration.ISnapshot {

		private final IProject project;
		private final WorkspaceSnapshot workspace;

		/**
		 * Initializes me with the project that I represent and the workspace snapshot. I discover the
		 * currently defined configurations for my project and initially select the one that is currently
		 * active, or the first available if none is active (which would be odd).
		 * 
		 * @param parent
		 *            my parent working set configuration
		 * @param projectConfig
		 *            the project configuration to copy
		 * @param workspace
		 *            the current workspace snapshot
		 */
		protected Snapshot(IWorkingSetConfiguration parent, IWorkingSetProjectConfiguration projectConfig,
				WorkspaceSnapshot workspace) {

			super(parent);

			this.project = projectConfig.resolveProject();
			this.workspace = workspace;

			String selected = projectConfig.getSelectedConfigurationID();
			if (selected == null) {
				selected = workspace.getActiveConfigurationID(project);
			}
			setSelectedConfigurationID(selected);
		}

		@Override
		public IWorkingSetConfiguration.ISnapshot getWorkingSetConfiguration() {
			return (IWorkingSetConfiguration.ISnapshot) super.getWorkingSetConfiguration();
		}

		public final WorkspaceSnapshot getWorkspaceSnapshot() {
			return workspace;
		}

		@Override
		public final IProject resolveProject() {
			return project;
		}

		@Override
		public final String getProjectName() {
			return resolveProject().getName();
		}

		public void setSelectedConfigurationID(String id) {
			super.setSelectedConfigurationID(id);
		}

		@Override
		public boolean isActive() {
			return workspace.isActive(this);
		}

		@Override
		public void activate() {
			workspace.activate(resolveProject(), getSelectedConfigurationID());
		}

		@Override
		public IStatus build(IProgressMonitor monitor) {
			return workspace.build(resolveProject(), getSelectedConfigurationID(), monitor);
		}

		@Override
		public Collection<ICConfigurationDescription> resolveConfigurations() {
			return workspace.getConfigurations(resolveProject());
		}

		@Override
		public ICConfigurationDescription resolveSelectedConfiguration() {
			return workspace.getConfiguration(resolveProject(), getSelectedConfigurationID());
		}
	}
}