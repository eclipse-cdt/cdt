/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 405390)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizer;
import org.eclipse.cdt.visualizer.ui.VisualizerAction;

/** Action that creates or clears a canvas filter */
public class FilterCanvasAction extends VisualizerAction {

	/** Visualizer instance we're associated with. */
	MulticoreVisualizer m_visualizer = null;

	/** Does this action enable or disable the filter mechanism? */
	boolean m_createFilter = false;

	/**
	 * Constructor
	 * @param create : Controls whether this action will create or clear the filter
	 */
	public FilterCanvasAction(boolean create) {
		m_createFilter = create;
		if (m_createFilter) {
			setText(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetFilter.text")); //$NON-NLS-1$
		} else {
			setText(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.ClearFilter.text")); //$NON-NLS-1$
		}
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
			if (m_createFilter) {
				m_visualizer.applyCanvasFilter();
			} else {
				m_visualizer.clearCanvasFilter();
			}
		}
	}
}