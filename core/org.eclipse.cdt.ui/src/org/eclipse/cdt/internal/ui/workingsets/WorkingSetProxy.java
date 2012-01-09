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
import static org.eclipse.cdt.internal.ui.workingsets.WorkingSetConfigurationManager.KEY_CONFIG;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;

import com.ibm.icu.text.UCharacterIterator;
import com.ibm.icu.text.UForwardCharacterIterator;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

/**
 * Default implementation of the {@link IWorkingSetProxy} interface.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 */
public class WorkingSetProxy implements IWorkingSetProxy {
	private String name;
	private Map<String, IWorkingSetConfiguration> configurations;

	/**
	 * Initializes me.
	 */
	public WorkingSetProxy() {
		super();
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets my name. This <b>does not</b> change the name of the working set that I represent. Rather, it
	 * changes <i>which</i> working set I represent.
	 * 
	 * @param name
	 *            my new name
	 */
	void setName(String name) {
		this.name = name;
	}

	@Override
	public IWorkingSet resolve() {
		return WorkingSetConfigurationManager.WS_MGR.getWorkingSet(name);
	}

	@Override
	public Collection<IProject> resolveProjects() {
		Set<IProject> result = new java.util.HashSet<IProject>();

		IWorkingSet resolvedWS = resolve();
		if (resolvedWS != null) {
			for (IAdaptable next : resolvedWS.getElements()) {
				IProject proj = (IProject) next.getAdapter(IProject.class);

				if (proj != null) {
					result.add(proj);
				}
			}
		}

		return result;
	}

	@Override
	public boolean isValid() {
		return !resolveProjects().isEmpty();
	}

	private Map<String, IWorkingSetConfiguration> getConfigurationsMap() {
		if (configurations == null) {
			configurations = new java.util.HashMap<String, IWorkingSetConfiguration>();
		}

		return configurations;
	}

	@Override
	public IWorkingSetConfiguration getConfiguration(String name) {
		return getConfigurationsMap().get(name);
	}

	@Override
	public Collection<IWorkingSetConfiguration> getConfigurations() {
		return getConfigurationsMap().values();
	}

	@Override
	public void saveState(IMemento memento) {
		memento.putString(ATTR_NAME, getName());

		for (IWorkingSetConfiguration next : getConfigurations()) {
			if (!isTransient(next)) {
				next.saveState(memento.createChild(KEY_CONFIG));
			}
		}
	}

	/**
	 * Queries whether the specified configuration is transient, meaning that it should not be persisted in
	 * the working set configuration store. The default implementation just returns <code>false</code>;
	 * subclasses may redefine as required.
	 * 
	 * @param config
	 *            a working set configuration
	 * @return whether it should be omitted from persistence
	 */
	protected boolean isTransient(IWorkingSetConfiguration config) {
		return false;
	}

	@Override
	public void loadState(IMemento memento) {
		setName(memento.getString(ATTR_NAME));

		for (IMemento next : memento.getChildren(KEY_CONFIG)) {
			IWorkingSetConfiguration config = createWorkingSetConfiguration();
			config.loadState(next);
			getConfigurationsMap().put(config.getName(), config);
		}
	}

	/**
	 * Creates a new child working set configuration element. Subclasses may override to create custom
	 * implementations.
	 * 
	 * @return the new working set configuration
	 */
	protected IWorkingSetConfiguration createWorkingSetConfiguration() {
		return new WorkingSetConfiguration(this);
	}

	/**
	 * Provides simple access to the child configurations, to remove the specified configuration.
	 * 
	 * @param config
	 *            a configuration to remove
	 */
	protected void basicRemoveConfiguration(IWorkingSetConfiguration config) {
		getConfigurationsMap().remove(config.getName());
	}

	/**
	 * Provides simple access to the child configurations, to add the specified configuration.
	 * 
	 * @param config
	 *            a configuration to add
	 */
	protected void basicAddConfiguration(IWorkingSetConfiguration config) {
		getConfigurationsMap().put(config.getName(), config);
	}

	@Override
	public ISnapshot createSnapshot(WorkspaceSnapshot workspace) {
		Snapshot result = new Snapshot(this, workspace);

		result.updateActiveConfigurations();

		return result;
	}

	//
	// Nested classes
	//

	/**
	 * The default implementation of a mutable working set snapshot.
	 * 
	 * @noextend This class is not intended to be subclassed by clients.
	 * 
	 * @author Christian W. Damus (cdamus)
	 * 
	 * @since 6.0
	 */
	public static class Snapshot extends WorkingSetProxy implements IWorkingSetProxy.ISnapshot {
		private final WorkspaceSnapshot workspace;
		private IWorkingSetConfiguration.ISnapshot readOnlyConfig;

		/**
		 * Initializes me with the current workspace snapshot.
		 * 
		 * @param workingSet
		 *            the original working set element to copy
		 * @param workspace
		 *            the workspace snapshot
		 */
		protected Snapshot(IWorkingSetProxy workingSet, WorkspaceSnapshot workspace) {
			super();

			this.workspace = workspace;

			setName(workingSet.getName());

			for (IWorkingSetConfiguration next : workingSet.getConfigurations()) {
				basicAddConfiguration(next.createSnapshot(this, workspace));
			}
		}

		@Override
		public final WorkspaceSnapshot getWorkspaceSnapshot() {
			return workspace;
		}

		@Override
		public IWorkingSetConfiguration.ISnapshot createConfiguration(String name) {
			if ((name == null) || (name.length() == 0)) {
				throw new IllegalArgumentException("name is empty"); //$NON-NLS-1$
			}
			if (getConfiguration(name) != null) {
				throw new IllegalArgumentException("name is already in use"); //$NON-NLS-1$
			}

			IWorkingSetConfiguration.ISnapshot result = createWorkingSetConfiguration();
			result.setName(name);

			heuristicSelectProjectConfigurations(result);

			basicAddConfiguration(result);
			updateActiveConfigurations();

			return result;
		}

		/**
		 * Heuristically attempts to select reasonable default project configurations for a new working-set
		 * configuration. This implementation does a best-effort match of project configuration names against
		 * the working set configuration name.
		 * 
		 * @param newConfig
		 *            the new working set configuration
		 */
		protected void heuristicSelectProjectConfigurations(IWorkingSetConfiguration.ISnapshot newConfig) {
			String nameToSearch = getSearchKey(newConfig.getName());

			for (IWorkingSetProjectConfiguration next : newConfig.getProjectConfigurations()) {
				IWorkingSetProjectConfiguration.ISnapshot project = (IWorkingSetProjectConfiguration.ISnapshot) next;

				for (ICConfigurationDescription config : project.resolveConfigurations()) {
					if (nameToSearch.equalsIgnoreCase(getSearchKey(config.getName()))) {
						// a match! Select this config
						project.setSelectedConfigurationID(config.getId());
						break;
					}
				}
			}

		}

		private String getSearchKey(String configurationName) {
			StringBuilder result = new StringBuilder(configurationName.length());

			UCharacterIterator iter = UCharacterIterator.getInstance(configurationName);
			for (int cp = iter.nextCodePoint(); cp != UForwardCharacterIterator.DONE; cp = iter.nextCodePoint()) {
				if (Character.isLetterOrDigit(cp)) {
					result.appendCodePoint(cp);
				}
			}

			return result.toString();
		}

		/**
		 * I create working-set configuration snapshots that are mutable, as I am.
		 */
		@Override
		protected IWorkingSetConfiguration.ISnapshot createWorkingSetConfiguration() {
			return new WorkingSetConfiguration.Snapshot(this, workspace);
		}

		@Override
		public void removeConfiguration(IWorkingSetConfiguration config) {
			if (WorkingSetConfiguration.isReadOnly(config)) {
				throw new IllegalArgumentException("config is read-only"); //$NON-NLS-1$
			}

			basicRemoveConfiguration(config);
		}

		@Override
		public boolean updateActiveConfigurations() {
			boolean result = getConfigurations().isEmpty();

			boolean hasActiveConfig = false;
			for (IWorkingSetConfiguration next : getConfigurations()) {
				if (next.isActive() && !WorkingSetConfiguration.isReadOnly(next)) {
					hasActiveConfig = true;
					break;
				}
			}

			if (hasActiveConfig) {
				if (readOnlyConfig != null) {
					basicRemoveConfiguration(readOnlyConfig);
					result = true;
				}
				readOnlyConfig = null;
			} else {
				WorkingSetConfiguration.Snapshot ro = new WorkingSetConfiguration.Snapshot(this, workspace,
						true);
				ro.basicSetName(""); // don't want to validate this name //$NON-NLS-1$
				readOnlyConfig = ro;
				basicAddConfiguration(readOnlyConfig);
				result = true;
			}

			return result;
		}

		/**
		 * Read-only working set configuration snapshots are transient.
		 */
		@Override
		protected boolean isTransient(IWorkingSetConfiguration config) {
			return WorkingSetConfiguration.isReadOnly(config);
		}
	}
}