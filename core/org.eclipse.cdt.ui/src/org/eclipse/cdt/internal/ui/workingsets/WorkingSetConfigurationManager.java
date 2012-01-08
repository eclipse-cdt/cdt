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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.XMLMemento;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;

/**
 * The purveyor of working set configurations. It provides a current view of the {@linkplain IWorkingSetProxy
 * working set configurations} defined in the workspace, as well as a working-copy
 * {@linkplain WorkspaceSnapshot snapshot} of the same for making modifications.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 * 
 */
public class WorkingSetConfigurationManager {

	static final String TYPE_WORKING_SET_CONFIGS = "org.eclipse.cdt.ui.workingSetConfigurations"; //$NON-NLS-1$
	static final String KEY_WORKING_SET = "workingSet"; //$NON-NLS-1$
	static final String ATTR_NAME = "name"; //$NON-NLS-1$
	static final String KEY_CONFIG = "config"; //$NON-NLS-1$
	static final String KEY_PROJECT = "project"; //$NON-NLS-1$
	static final String ATTR_CONFIG = "config"; //$NON-NLS-1$
	static final String ATTR_FACTORY = "factory"; //$NON-NLS-1$

	static IWorkingSetManager WS_MGR = CUIPlugin.getDefault().getWorkbench().getWorkingSetManager();

	private static final WorkingSetConfigurationManager INSTANCE = new WorkingSetConfigurationManager();

	private Map<String, IWorkingSetProxy> workingSets;

	private final Object storeLock = new Object();
	private IMemento store;

	private final ISchedulingRule saveRule = new ISchedulingRule() {

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	};

	/**
	 * Not instantiable by clients.
	 */
	private WorkingSetConfigurationManager() {
		store = loadMemento();
		new WorkingSetChangeTracker();
	}

	/**
	 * Obtains the default working set configuration manager.
	 * 
	 * @return the working set configuration manager
	 */
	public static WorkingSetConfigurationManager getDefault() {
		return INSTANCE;
	}

	private Map<String, IWorkingSetProxy> getWorkingSetMap() {
		Map<String, IWorkingSetProxy> result;

		synchronized (storeLock) {
			if (workingSets == null) {
				load();
			}
			result = workingSets;
		}

		return result;
	}

	/**
	 * Obtains the current immutable view of the specified working set's configurations. These configurations
	 * may be {@linkplain IWorkingSetConfiguration#activate() activated} to apply their settings to the
	 * workspace, but they cannot be modified.
	 * 
	 * @param name
	 *            the name of the working set to retrieve
	 * @return the named working set, or <code>null</code> if there is none available by that name
	 */
	public IWorkingSetProxy getWorkingSet(String name) {
		return getWorkingSetMap().get(name);
	}

	/**
	 * Obtains the current immutable view of all available working set configurations. These configurations
	 * may be {@linkplain IWorkingSetConfiguration#activate() activated} to apply their settings to the
	 * workspace, but they cannot be modified.
	 * 
	 * @return the working set configurations
	 */
	public Collection<IWorkingSetProxy> getWorkingSets() {
		return getWorkingSetMap().values();
	}

	/**
	 * Creates a new mutable snapshot of the current working set configurations. This snapshot accepts
	 * modifications and can be {@linkplain WorkspaceSnapshot#save() saved} to persist the changes.
	 * 
	 * @return a working-copy of the working set configurations
	 */
	public WorkspaceSnapshot createWorkspaceSnapshot() {
		return new WorkspaceSnapshot().initialize(getWorkingSetMap());
	}

	/**
	 * <p>
	 * Loads the working set configurations from storage.
	 * </p>
	 * <p>
	 * <b>Note</b> that this method must only be called within the <tt>storeLock</tt> monitor.
	 * </p>
	 */
	private void load() {
		workingSets = new java.util.HashMap<String, IWorkingSetProxy>();

		for (IMemento next : store.getChildren(KEY_WORKING_SET)) {
			WorkingSetProxy ws = new WorkingSetProxy();

			ws.loadState(next);

			if (ws.isValid()) {
				workingSets.put(ws.getName(), ws);
			}
		}
	}

	/**
	 * <p>
	 * Forgets the current view of the working set configurations.
	 * </p>
	 * <p>
	 * <b>Note</b> that this method must only be called within the <tt>storeLock</tt> monitor.
	 * </p>
	 */
	private void clear() {
		workingSets = null;
	}

	/**
	 * Saves the working set configurations to storage.
	 * 
	 * @param snapshot
	 *            the snapshot to save
	 */
	void save(WorkspaceSnapshot snapshot) {
		final XMLMemento memento = XMLMemento.createWriteRoot(TYPE_WORKING_SET_CONFIGS);

		for (IWorkingSetConfigurationElement next : snapshot.getWorkingSets()) {
			next.saveState(memento.createChild(KEY_WORKING_SET));
		}

		save(memento);
	}

	/**
	 * Records the specified memento as our new store and asynchronously saves it in a job.
	 * 
	 * @param memento
	 *            the new store
	 */
	private void save(final XMLMemento memento) {
		synchronized (storeLock) {
			store = memento;
			clear();
		}

		new Job(WorkingSetMessages.WSConfigManager_save_job) {
			{
				setRule(saveRule);
				setSystem(true);
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				File file = getStorage();
				FileWriter writer = null;
				try {
					writer = new FileWriter(file);
					memento.save(writer);
					writer.close();
				} catch (IOException e) {
					if (writer != null) {
						try {
							writer.close();
						} catch (IOException e2) {
							// no recovery
							CUIPlugin.log(WorkingSetMessages.WSConfigManager_closeFailed, e);
						}
					}

					file.delete(); // it is corrupt; we won't be able to load it, later

					CUIPlugin.log(WorkingSetMessages.WSConfigManager_saveFailed, e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/**
	 * Gets the file in which we persist the working set configurations.
	 * 
	 * @return the file store
	 */
	private File getStorage() {
		IPath path = CUIPlugin.getDefault().getStateLocation().append("workingSetConfigs.xml"); //$NON-NLS-1$
		return path.toFile();
	}

	/**
	 * Loads the working set configurations from storage. For compatibility, if the XML file is not available,
	 * we load from the old preference setting format.
	 * 
	 * @return the working set configuration store
	 */
	private IMemento loadMemento() {
		IMemento result = null;

		File file = getStorage();

		if (file.exists()) {
			FileReader reader = null;
			try {
				reader = new FileReader(file);
				result = XMLMemento.createReadRoot(reader);
				reader.close();
			} catch (Exception e) {
				result = null;

				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e2) {
						// no recovery
						CUIPlugin.log(WorkingSetMessages.WSConfigManager_closeFailed, e);
					}
				}

				CUIPlugin.log(WorkingSetMessages.WSConfigManager_loadFailed, e);
			}
		}

		if (result == null) {
			// fake one from the old preference storage format. This also
			// handles the case of no working set configurations ever being made
			@SuppressWarnings("deprecation")
			List<String> configSetStrings = CDTPrefUtil.readConfigSets();
			result = XMLMemento.createWriteRoot(TYPE_WORKING_SET_CONFIGS);

			// collect the unordered entries by working set
			Map<String, IMemento> configMap = new HashMap<String, IMemento>();
			for (String next : configSetStrings) {
				String[] bits = next.split(" "); //$NON-NLS-1$

				if (bits.length >= 2) {
					String configName = bits[0];
					String wsName = bits[1];

					IMemento workingSet = configMap.get(wsName);
					if (workingSet == null) {
						workingSet = result.createChild(KEY_WORKING_SET);
						configMap.put(wsName, workingSet);
					}

					workingSet.putString(ATTR_NAME, wsName);

					IMemento config = workingSet.createChild(KEY_CONFIG);
					config.putString(ATTR_NAME, configName);

					int limit = bits.length - (bits.length % 2);
					for (int i = 2; i < limit; i += 2) {
						IMemento project = config.createChild(KEY_PROJECT);
						project.putString(ATTR_NAME, bits[i]);
						project.putString(ATTR_CONFIG, bits[i + 1]);
					}
				}
			}
		}

		return result;
	}

	//
	// Nested classes
	//

	/**
	 * A working set manager listener that tracks name changes and removals of working sets to keep our
	 * configurations in synch as much as possible. It updates the memento store directly in response to
	 * changes in the working sets.
	 * 
	 * @author Christian W. Damus (cdamus)
	 * 
	 * @since 6.0
	 */
	private class WorkingSetChangeTracker extends java.util.IdentityHashMap<IWorkingSet, String> implements
			IPropertyChangeListener {

		WorkingSetChangeTracker() {
			for (IWorkingSet next : WS_MGR.getWorkingSets()) {
				put(next, next.getName());
			}

			WS_MGR.addPropertyChangeListener(this);
		}

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			String property = event.getProperty();

			if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE.equals(property)) {
				handleNameChange((IWorkingSet) event.getNewValue());
			} else if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property)) {
				handleRemove((IWorkingSet) event.getOldValue());
			} else if (IWorkingSetManager.CHANGE_WORKING_SET_ADD.equals(property)) {
				handleAdd((IWorkingSet) event.getNewValue());
			}
		}

		private void handleNameChange(IWorkingSet workingSet) {
			synchronized (storeLock) {
				String oldName = get(workingSet);
				IMemento wsMemento = null;
				if (oldName != null) {
					for (IMemento next : store.getChildren(KEY_WORKING_SET)) {
						if (oldName.equals(next.getString(ATTR_NAME))) {
							wsMemento = next;
							break;
						}
					}
				}

				if (wsMemento != null) {
					// update the memento with the new name
					wsMemento.putString(ATTR_NAME, workingSet.getName());

					// clone it
					XMLMemento newStore = XMLMemento.createWriteRoot(TYPE_WORKING_SET_CONFIGS);
					newStore.putMemento(store);

					// save it asynchronously
					save(newStore);
				}

				// and update our mapping
				put(workingSet, workingSet.getName());
			}
		}

		private void handleRemove(IWorkingSet workingSet) {
			synchronized (storeLock) {
				String name = get(workingSet);
				if (name != null) {
					// remove the memento from the store
					XMLMemento newStore = XMLMemento.createWriteRoot(TYPE_WORKING_SET_CONFIGS);
					for (IMemento next : store.getChildren(KEY_WORKING_SET)) {
						if (!name.equals(next.getString(ATTR_NAME))) {
							newStore.createChild(KEY_WORKING_SET).putMemento(next);
						}
					}

					// save asynchronously
					save(newStore);
				}

				// and update our mapping
				remove(workingSet);
			}
		}

		private void handleAdd(IWorkingSet workingSet) {
			synchronized (storeLock) {
				put(workingSet, workingSet.getName());
			}
		}
	}
}
