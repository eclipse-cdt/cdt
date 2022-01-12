/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 460476)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizer;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.PersistentSettingsManager;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.PersistentSettingsManager.PersistentParameter;
import org.eclipse.cdt.visualizer.ui.VisualizerAction;
import org.eclipse.jface.action.Action;

/** Actions that shows/hides the debug actions on the Multicore Visualizer toolbar */
public class ShowDebugToolbarAction extends VisualizerAction {
	// --- members ---

	/** Visualizer instance we're associated with. */
	private MulticoreVisualizer m_visualizer;

	/** persistent settings manager */
	private PersistentSettingsManager m_persistentSettingsManager;

	/** Persistent parameter that remembers if the debug actions should be shown or not */
	private PersistentParameter<Boolean> m_showDebugActions;

	// --- constructors/destructors ---

	/** Constructor.
	 * @param  showDebugActions : show the debug actions by default
	 * @param MVInstanceId : id that uniquely identifies a Multicore Visualizer instance
	 */
	public ShowDebugToolbarAction(boolean showDebugActions, String MVInstanceId) {
		super(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.ShowDebugToolbar.text"), //$NON-NLS-1$
				Action.AS_CHECK_BOX);
		setDescription(
				MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.ShowDebugToolbar.description")); //$NON-NLS-1$

		m_persistentSettingsManager = new PersistentSettingsManager("ShowDebugToolbarAction", MVInstanceId); //$NON-NLS-1$
		m_showDebugActions = m_persistentSettingsManager.getNewParameter(Boolean.class, "showDebugActionsInMVToolbar", //$NON-NLS-1$
				true, showDebugActions);

		// Set initial state
		this.setChecked(m_showDebugActions.value());
	}

	/** Dispose method. */
	@Override
	public void dispose() {
		m_visualizer = null;
		super.dispose();
	}

	// --- init methods ---

	/** Initializes this action for the specified view. */
	public void init(MulticoreVisualizer visualizer) {
		m_visualizer = visualizer;
	}

	// --- methods ---

	/** Invoked when action is triggered. */
	@Override
	public void run() {
		if (m_visualizer != null) {
			m_showDebugActions.set(isChecked());
			// trigger refresh of canvas
			m_visualizer.raiseVisualizerChangedEvent();
		}
	}
}
