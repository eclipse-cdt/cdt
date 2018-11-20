/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 441713)
 *     Marc Dumais (Ericsson) - Bug 442312
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizer;
import org.eclipse.cdt.visualizer.ui.VisualizerAction;

/** Pins the multicore visualizer to the current debug session */
public class PinToDebugSessionAction extends VisualizerAction {

	// --- members ---

	/** current active state of pinning */
	private boolean m_pinActive;

	/** Visualizer instance we're associated with. */
	MulticoreVisualizer m_visualizer = null;

	// --- constructors/destructors ---

	/** Constructor. */
	public PinToDebugSessionAction() {
		super(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.PinToDebugSession.text"), //$NON-NLS-1$
				MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.PinToDebugSession.description"), //$NON-NLS-1$
				CDebugImages.DESC_LCL_PIN_VIEW);

		// at first, this action is disabled (un-pinned)
		setChecked(false);
		m_pinActive = false;
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
		// Toggle pinned state
		m_pinActive = !m_pinActive;

		if (m_pinActive) {
			m_visualizer.pin();
		} else {
			m_visualizer.unpin();
		}
		// update the toolbar
		m_visualizer.raiseVisualizerChangedEvent();
	}
}
