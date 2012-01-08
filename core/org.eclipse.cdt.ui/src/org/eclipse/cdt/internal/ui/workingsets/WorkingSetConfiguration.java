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

import static org.eclipse.cdt.internal.ui.workingsets.WorkingSetConfigurationManager.ATTR_NAME;
import static org.eclipse.cdt.internal.ui.workingsets.WorkingSetConfigurationManager.KEY_PROJECT;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Default implementation of the {@link IWorkingSetConfiguration} interface.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 * 
 */
public class WorkingSetConfiguration implements IWorkingSetConfiguration {
	private final IWorkingSetProxy workingSet;
	private String name;

	private Map<String, IWorkingSetProjectConfiguration> projects;

	/**
	 * Initializes me with my parent working set.
	 * 
	 * @param workingSet
	 *            my parent working set
	 */
	protected WorkingSetConfiguration(IWorkingSetProxy workingSet) {
		this.workingSet = workingSet;
	}

	/**
	 * Obtains my parent working set.
	 * 
	 * @return my parent
	 */
	@Override
	public IWorkingSetProxy getWorkingSet() {
		return workingSet;
	}

	/**
	 * Obtains my name.
	 * 
	 * @return my name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets my name.
	 * 
	 * @param name
	 *            my new name
	 * 
	 * @throws IllegalArgumentException
	 *             if the specified name is <code>null</code> or empty, or if it is already used by another
	 *             configuration in my warking set
	 */
	void setName(String name) {
		if ((name == null) || (name.length() == 0)) {
			throw new IllegalArgumentException("name is empty"); //$NON-NLS-1$
		}

		if (!name.equals(getName())) {
			if (getWorkingSet().getConfiguration(name) != null) {
				throw new IllegalArgumentException("name is already in use"); //$NON-NLS-1$
			}

			basicSetName(name);
		}
	}

	/**
	 * Provides simple access to the name for setting it.
	 * 
	 * @param name
	 *            my new name
	 */
	protected void basicSetName(String name) {
		this.name = name;
	}

	private Map<String, IWorkingSetProjectConfiguration> getProjects() {
		if (projects == null) {
			projects = new java.util.HashMap<String, IWorkingSetProjectConfiguration>();

			for (IProject next : workingSet.resolveProjects()) {
				IWorkingSetProjectConfiguration child = createProjectConfiguration(next);

				// the project may not be a C/C++ project
				if (child != null) {
					basicAddProjectConfiguration(child);
				}
			}
		}

		return projects;
	}

	protected void basicAddProjectConfiguration(IWorkingSetProjectConfiguration projectConfig) {
		if (projects == null) {
			projects = new java.util.HashMap<String, IWorkingSetProjectConfiguration>();
		}

		projects.put(projectConfig.getProjectName(), projectConfig);
	}

	@Override
	public IWorkingSetProjectConfiguration getProjectConfiguration(String projectName) {
		return getProjects().get(projectName);
	}

	@Override
	public java.util.Collection<IWorkingSetProjectConfiguration> getProjectConfigurations() {
		return getProjects().values();
	}

	@Override
	public boolean isActive() {
		boolean result = !getProjects().isEmpty();

		if (result) {
			for (IWorkingSetProjectConfiguration next : getProjectConfigurations()) {
				if (!next.isActive()) {
					result = false;
					break;
				}
			}
		}

		return result;
	}

	@Override
	public void activate() {
		if (!isActive()) {
			for (IWorkingSetProjectConfiguration next : getProjectConfigurations()) {
				next.activate();
			}
		}

		// this is a "recently used" working set
		IWorkingSet ws = getWorkingSet().resolve();
		if (ws != null) {
			WorkingSetConfigurationManager.WS_MGR.addRecentWorkingSet(ws);
		}
	}

	@Override
	public IStatus build(IProgressMonitor monitor) {
		MultiStatus result = new MultiStatus(CUIPlugin.PLUGIN_ID, 0,
				WorkingSetMessages.WSConfig_build_problems, null);

		List<IWorkingSetProjectConfiguration> toBuild = new java.util.ArrayList<IWorkingSetProjectConfiguration>(
				getProjectConfigurations().size());
		for (IWorkingSetProjectConfiguration next : getProjectConfigurations()) {
			IProject project = next.resolveProject();

			if ((project != null) && (project.isAccessible())) {
				toBuild.add(next);
			}
		}

		SubMonitor progress = SubMonitor.convert(monitor, NLS.bind(WorkingSetMessages.WSConfig_build_task,
				getWorkingSet().getName()), toBuild.size());

		try {
			for (IWorkingSetProjectConfiguration next : toBuild) {
				if (progress.isCanceled()) {
					result.add(Status.CANCEL_STATUS);
					break;
				}

				IStatus status = next.build(progress.newChild(1));

				if ((status != null && !status.isOK())) {
					result.add(status);
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}

		return result.isOK() ? Status.OK_STATUS : result;
	}

	@Override
	public void saveState(IMemento memento) {
		memento.putString(ATTR_NAME, getName());

		for (IWorkingSetProjectConfiguration next : getProjectConfigurations()) {
			next.saveState(memento.createChild(KEY_PROJECT));
		}
	}

	@Override
	public void loadState(IMemento memento) {
		setName(memento.getString(ATTR_NAME));

		Map<String, IMemento> projectMementos = new java.util.HashMap<String, IMemento>();
		for (IMemento next : memento.getChildren(KEY_PROJECT)) {
			projectMementos.put(next.getString(ATTR_NAME), next);
		}

		for (IWorkingSetProjectConfiguration next : getProjectConfigurations()) {
			IMemento state = projectMementos.get(next.getProjectName());
			if (state != null) {
				next.loadState(state);
			}
		}
	}

	/**
	 * Creates a new project configuration for the specified project. May be overridden by subclasses to
	 * create a different implementation.
	 * 
	 * @param project
	 *            a workspace project
	 * @return a new project configuration element for it
	 */
	protected IWorkingSetProjectConfiguration createProjectConfiguration(IProject project) {
		IWorkingSetProjectConfiguration result = null;

		IWorkingSetProjectConfigurationFactory factory = IWorkingSetProjectConfigurationFactory.Registry.INSTANCE
				.getFactory(project);
		if (factory != null) {
			result = factory.createProjectConfiguration(this, project);
		}

		return result;
	}

	@Override
	public ISnapshot createSnapshot(IWorkingSetProxy.ISnapshot workingSet, WorkspaceSnapshot workspace) {

		return new Snapshot(workingSet, this, workspace);
	}

	/**
	 * Utility method to query whether the specified configuration is a read-only snapshot.
	 * 
	 * @param config
	 *            a working set configuration
	 * @return whether it is a read-only snapshot
	 * 
	 * @see IWorkingSetConfiguration.ISnapshot#isReadOnly()
	 */
	static boolean isReadOnly(IWorkingSetConfiguration config) {
		return (config instanceof WorkingSetConfiguration.Snapshot)
				&& ((WorkingSetConfiguration.Snapshot) config).isReadOnly();
	}

	//
	// Nested classes
	//

	/**
	 * Default implementation of the mutable working set configuration snapshot.
	 * 
	 * @author Christian W. Damus (cdamus)
	 * 
	 * @noextend This class is not intended to be subclassed by clients.
	 * 
	 * @since 6.0
	 */
	public static class Snapshot extends WorkingSetConfiguration implements
			IWorkingSetConfiguration.ISnapshot {

		private final boolean readOnly;
		private final WorkspaceSnapshot workspace;

		/**
		 * Initializes me with the current workspace snapshot.
		 * 
		 * @param workingSet
		 *            my parent working set
		 * @param workspace
		 *            the current workspace snapshot
		 */
		protected Snapshot(IWorkingSetProxy workingSet, WorkspaceSnapshot workspace) {
			this(workingSet, workspace, false);
		}

		/**
		 * Initializes me as a special read-only configuration that shows what is the current active
		 * configuration of the projects in a working set when none of its named configurations is active.
		 * 
		 * @param workingSet
		 *            my parent working set
		 * @param workspace
		 *            the current workspace snapshot
		 * @param readOnly
		 *            whether I am read-only. A read-only configuration cannot be modified in the dialog
		 */
		protected Snapshot(IWorkingSetProxy workingSet, WorkspaceSnapshot workspace, boolean readOnly) {
			super(workingSet);

			this.readOnly = readOnly;
			this.workspace = workspace;
		}

		/**
		 * Initializes me with the current workspace snapshot.
		 * 
		 * @param workingSet
		 *            my parent working set
		 * @param config
		 *            the working set configuration that I copy
		 * @param workspace
		 *            the current workspace snapshot
		 */
		protected Snapshot(IWorkingSetProxy workingSet, IWorkingSetConfiguration config,
				WorkspaceSnapshot workspace) {
			this(workingSet, workspace);

			setName(config.getName());

			for (IWorkingSetProjectConfiguration next : config.getProjectConfigurations()) {
				basicAddProjectConfiguration(next.createSnapshot(this, workspace));
			}
		}

		@Override
		public final IWorkingSetProxy.ISnapshot getWorkingSet() {
			return (IWorkingSetProxy.ISnapshot) super.getWorkingSet();
		}

		@Override
		public final WorkspaceSnapshot getWorkspaceSnapshot() {
			return workspace;
		}

		/**
		 * Queries whether I am a read-only view of the current active configurations of my working set's
		 * projects.
		 * 
		 * @return whether I am read-only
		 */
		@Override
		public final boolean isReadOnly() {
			return readOnly;
		}

		@Override
		public void setName(String name) {
			super.setName(name);
		}

		/**
		 * I create project configurations that are mutable, as I am.
		 */
		@Override
		protected IWorkingSetProjectConfiguration createProjectConfiguration(IProject project) {
			IWorkingSetProjectConfiguration result = null;

			IWorkingSetProjectConfigurationFactory factory = IWorkingSetProjectConfigurationFactory.Registry.INSTANCE
					.getFactory(project);
			if (factory != null) {
				result = factory.createProjectConfiguration(this, project).createSnapshot(this, workspace);
			}

			return result;
		}
	}
}