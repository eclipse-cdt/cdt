/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkingSet;

/**
 * <p>
 * A snapshot of the working set configurations and project configurations across the workspace at the time
 * when it was created. The snapshot maintains a delta from that original state to the current state, for such
 * comparison operations as determining which projects need to be re-built because their active configurations
 * have changed. The snapshot provides mutable working-copy views of the working set configurations at the
 * time of snapshot creation.
 * </p>
 * <p>
 * To make changes to working set configurations, first
 * {@linkplain WorkingSetConfigurationManager#createWorkspaceSnapshot() obtain a snapshot} from the
 * {@link WorkingSetConfigurationManager}. Then make edits to the various snapshots of the configuration
 * elements and {@linkplain #save() save} the snapshot
 * </p>
 *
 * @author Christian W. Damus (cdamus)
 *
 * @since 6.0
 *
 */
public class WorkspaceSnapshot {
	private Map<String, IWorkingSetProxy.ISnapshot> workingSets = new java.util.HashMap<>();
	private Map<IProject, ProjectState> projectStates = new java.util.HashMap<>();

	/**
	 * Initializes me. I capture the current C/C++ active configuration state of the projects in the
	 * workspace.
	 */
	WorkspaceSnapshot() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		for (IProject next : root.getProjects()) {
			ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(next);

			if (desc != null) {
				projectStates.put(next, createProjectState(next, desc));
			}
		}
	}

	/**
	 * Creates a project state using the registered factory, if possible.
	 *
	 * @param project
	 *            a workspace project
	 * @param desc
	 *            its CDT project description
	 *
	 * @return its state capture (will never be <code>null</code>)
	 */
	private static ProjectState createProjectState(IProject project, ICProjectDescription desc) {
		ProjectState result = null;
		IWorkingSetProjectConfigurationFactory factory = IWorkingSetProjectConfigurationFactory.Registry.INSTANCE
				.getFactory(project);

		if (factory != null) {
			result = factory.createProjectState(project, desc);
		}

		if (result == null) {
			// the default-default
			result = new ProjectState(project, desc);
		}

		return result;
	}

	/**
	 * Initializes me with the specified working sets to copy for editing.
	 *
	 * @param workingSets
	 *            the working sets to copy
	 * @return myself
	 */
	WorkspaceSnapshot initialize(Map<String, IWorkingSetProxy> workingSets) {
		for (IWorkingSet next : WorkingSetConfigurationManager.WS_MGR.getWorkingSets()) {
			IWorkingSetProxy workingSet = workingSets.get(next.getName());

			if (workingSet == null) {
				workingSet = new WorkingSetProxy();
				((WorkingSetProxy) workingSet).setName(next.getName());
			}

			if (workingSet.isValid()) {
				this.workingSets.put(workingSet.getName(), workingSet.createSnapshot(this));
			}
		}

		return this;
	}

	/**
	 * Obtains a mutable snapshot of the named working set.
	 *
	 * @param name
	 *            the working set to retrieve
	 *
	 * @return the working set snapshot, or <code>null</code> if there is no working set by this name
	 *
	 * @see #getWorkingSets()
	 */
	public IWorkingSetProxy.ISnapshot getWorkingSet(String name) {
		return workingSets.get(name);
	}

	/**
	 * Obtains snapshots of all of the working sets currently defined by the workbench.
	 *
	 * @return the working set snapshots
	 *
	 * @see #getWorkingSet(String)
	 */
	public Collection<IWorkingSetProxy.ISnapshot> getWorkingSets() {
		return workingSets.values();
	}

	/**
	 * Obtains the project state recording the initial configuration of the specified <tt>project</tt> at the
	 * time that this snapshot was taken.
	 *
	 * @param project
	 *            a project
	 * @return its snapshot/delta state
	 */
	public ProjectState getState(IProject project) {
		return projectStates.get(project);
	}

	/**
	 * Queries the ID of the configuration of the specified project that was active when the workspace
	 * snapshot was taken.
	 *
	 * @param project
	 *            a project
	 * @return its active configuration ID at the time of the snapshot
	 */
	String getBaselineConfigurationID(IProject project) {
		String result = null;
		ProjectState state = getState(project);

		if (state != null) {
			result = state.getBaselineConfigurationID();
		}

		return result;
	}

	/**
	 * Queries the ID of the currently active configuration of the specified project.
	 *
	 * @param project
	 *            a project
	 *
	 * @return the current active configuration ID
	 */
	String getActiveConfigurationID(IProject project) {
		String result = null;
		ProjectState state = getState(project);

		if (state != null) {
			result = state.getActiveConfigurationID();
		}

		return result;
	}

	/**
	 * Queries whether the configuration selected by the given project in a working set configuration is
	 * currently active in the workspace.
	 *
	 * @param project
	 *            a project configuration element in a working set configuration
	 *
	 * @return whether the project's selected configuration is active
	 *
	 * @see #activate(IProject, String)
	 */
	boolean isActive(IWorkingSetProjectConfiguration project) {
		boolean result = false;
		ProjectState state = getState(project.resolveProject());

		if (state != null) {
			result = state.isActive(project.getSelectedConfigurationID());
		}

		return result;
	}

	/**
	 * Activates, in the workspace, the specified configuration of a project. This method has no effect if the
	 * given configuration is already active.
	 *
	 * @param project
	 *            the project for which to set the active configuration
	 * @param configID
	 *            the ID of the configuration to activate
	 *
	 * @see #isActive(IWorkingSetProjectConfiguration)
	 */
	void activate(IProject project, String configID) {
		ProjectState state = getState(project);

		if (state != null) {
			state.activate(configID);
		}
	}

	IStatus build(IProject project, String configID, IProgressMonitor monitor) {
		ProjectState state = getState(project);

		if (state != null) {
			return state.build(configID, monitor);
		}

		return new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID,
				NLS.bind(WorkingSetMessages.WorkspaceSnapshot_buildNoProj, project.getName()));
	}

	/**
	 * Obtains the configurations of the specified project, as known at the time that this snapshot was taken.
	 *
	 * @param project
	 *            a project
	 *
	 * @return its configurations, which may be an empty collection if the project is not a C/C++ project
	 */
	public Collection<ICConfigurationDescription> getConfigurations(IProject project) {
		Collection<ICConfigurationDescription> result;
		ProjectState state = getState(project);

		if (state == null) {
			result = Collections.emptyList();
		} else {
			result = state.getConfigurations();
		}

		return result;
	}

	/**
	 * Obtains the specified configuration of a project, as known at the time that this snapshot was taken.
	 *
	 * @param project
	 *            a project
	 * @param id
	 *            the ID of a configuration
	 *
	 * @return the configuration, or <code>null</code> if there is none such by this ID
	 */
	public ICConfigurationDescription getConfiguration(IProject project, String id) {
		ProjectState state = getState(project);
		return (state == null) ? null : state.getConfiguration(id);
	}

	/**
	 * Queries the projects that need to be built because their active configurations have been changed since
	 * this snapshot was taken.
	 *
	 * @return the projects needing to be re-built
	 */
	public Collection<IProject> getProjectsToBuild() {
		Collection<IProject> result = new java.util.ArrayList<>();

		for (ProjectState next : projectStates.values()) {
			if (next.needsBuild()) {
				result.add(next.getProject());
			}
		}

		return result;
	}

	/**
	 * Saves my working set configuration settings.
	 */
	public void save() {
		WorkingSetConfigurationManager.getDefault().save(this);
	}

	//
	// Nested classes
	//

	/**
	 * Capture of the current state of a project at the time when a {@linkplain WorkspaceSnapshot workspace
	 * snapshot} was taken, and its delta from that original state. This tracks at least the C/C++ project
	 * description (if any) and the original active configuration. Subclasses may track additional
	 * configuration details.
	 *
	 * @author Christian W. Damus (cdamus)
	 *
	 * @since 6.0
	 */
	public static class ProjectState {
		private final IProject project;
		private final ICProjectDescription projectDescription;
		private final String baselineConfigID;
		private String currentConfigID;
		private String lastBuiltConfigID;

		/**
		 * Initializes me with a project and its description.
		 *
		 * @param project
		 *            the project whose state I track
		 * @param desc
		 *            the project's description, from which I capture the initial state snapshot
		 */
		protected ProjectState(IProject project, ICProjectDescription desc) {
			this.project = project;
			this.projectDescription = desc;

			if (desc != null) {
				ICConfigurationDescription config = desc.getActiveConfiguration();
				this.baselineConfigID = (config == null) ? "" : config.getId(); //$NON-NLS-1$
			} else {
				this.baselineConfigID = ""; //$NON-NLS-1$
			}

			this.currentConfigID = this.baselineConfigID;
		}

		/**
		 * Obtains the project that I track.
		 *
		 * @return my project
		 */
		public final IProject getProject() {
			return project;
		}

		/**
		 * Obtains the project description that was current when the snapshot was taken.
		 *
		 * @return my project description
		 */
		protected final ICProjectDescription getProjectDescription() {
			return projectDescription;
		}

		/**
		 * Queries whether my project needs to be re-built because its active configuration has been changed
		 * since the snapshot was taken, and it hasn't been built already.
		 *
		 * @return whether I need to be re-built
		 */
		public boolean needsBuild() {
			return !currentConfigID.equals(baselineConfigID) && !currentConfigID.equals(lastBuiltConfigID);
		}

		/**
		 * Queries whether the specified configuration is currently active in the workspace for my project.
		 *
		 * @param configID
		 *            the ID of a project build configuration
		 * @return whether it is my project's active configuration
		 */
		public boolean isActive(String configID) {
			return currentConfigID.equals(configID);
		}

		/**
		 * Queries the ID of the currently active configuration.
		 *
		 * @return the current active configuration ID
		 */
		protected String getActiveConfigurationID() {
			return currentConfigID;
		}

		/**
		 * Queries the ID of the configuration of my project that was active when the workspace snapshot was
		 * taken.
		 *
		 * @return its active configuration ID at the time of the snapshot
		 */
		protected String getBaselineConfigurationID() {
			return baselineConfigID;
		}

		/**
		 * Sets my project's active configuration to the specified configuration. This method has no effect if
		 * this configuration is already active.
		 *
		 * @param configID
		 *            the ID of the configuration to activate
		 */
		protected void activate(String configID) {
			if (!currentConfigID.equals(configID) && (projectDescription != null)) {
				try {
					ICConfigurationDescription realConfig = projectDescription.getConfigurationById(configID);
					realConfig.setActive();
					CoreModel.getDefault().setProjectDescription(project, projectDescription);
					currentConfigID = configID;
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
		}

		/**
		 * Builds the specified configuration of my project. I update myself to record a new build baseline if
		 * the build succeeds.
		 *
		 * @param configID
		 *            the configuration to build
		 * @param monitor
		 *            a monitor to report build progress
		 *
		 * @return the status of the build
		 */
		protected IStatus build(String configID, IProgressMonitor monitor) {
			IStatus result = Status.OK_STATUS;

			ICConfigurationDescription config = getConfiguration(configID);

			if (config == null) {
				result = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID,
						NLS.bind(WorkingSetMessages.WSProjConfig_noConfig, getProject().getName()));
			} else {
				if (!isActive(configID)) {
					activate(configID);
					result = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID,
							NLS.bind(WorkingSetMessages.WSProjConfig_activatedWarning, config.getName(),
									getProject().getName()));
				}

				monitor = SubMonitor.convert(monitor);

				try {
					getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);

					// update the build baseline to this config, which is now active
					built(configID);
				} catch (CoreException e) {
					if (result.isOK()) {
						result = e.getStatus();
					} else {
						result = new MultiStatus(CUIPlugin.PLUGIN_ID, 0, new IStatus[] { result, e.getStatus() },
								NLS.bind(WorkingSetMessages.WSProjConfig_buildProblem, getProject().getName()), null);
					}
				}
			}

			return result;
		}

		/**
		 * Records that we built the specified configuration ID. I will not {@linkplain #needsBuild() need a
		 * build} if the last build configuration is my active configuration.
		 *
		 * @param configID
		 *            the configuration that was built (not <code>null</code>)
		 */
		protected void built(String configID) {
			lastBuiltConfigID = configID;
		}

		/**
		 * Obtains the configurations of my project that were defined at the time that the snapshot was taken.
		 *
		 * @return my project's configurations, which may be empty if it is not a C/C++ project
		 */
		protected Collection<ICConfigurationDescription> getConfigurations() {
			Collection<ICConfigurationDescription> result;

			if (projectDescription == null) {
				result = Collections.emptyList();
			} else {
				result = Arrays.asList(projectDescription.getConfigurations());
			}

			return result;
		}

		/**
		 * Obtains the specified configuration of my project as it was defined at the time that the snapshot
		 * was taken.
		 *
		 * @param id
		 *            a configuration ID
		 * @return the matching configuration, or <code>null</code> if it did not exist
		 */
		protected ICConfigurationDescription getConfiguration(String id) {
			return (projectDescription == null) ? null : projectDescription.getConfigurationById(id);
		}
	}
}