/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 405390)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizer;
import org.eclipse.cdt.visualizer.ui.VisualizerAction;

/**
 * @since 1.1
 */
public class FilterCanvasAction extends VisualizerAction {

	/** Visualizer instance we're associated with. */
	MulticoreVisualizer m_visualizer = null;
	boolean m_haveFilter = false;
	

	/**
	 * @param filterType: the type of canvas object the filter applies-to.  If null, reset filter
	 */
	public FilterCanvasAction(boolean enable) {	
		m_haveFilter = enable;
		if (m_haveFilter) {
			setText(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetFilter.text")); //$NON-NLS-1$
		}
		else {
			setText(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.ClearFilter.text")); //$NON-NLS-1$
		}
	}

	/** Dispose method. */
	@Override
	public void dispose()
	{
		m_visualizer = null;
		super.dispose();
	}

	// --- init methods ---

	/** Initializes this action for the specified view. */
	public void init(MulticoreVisualizer visualizer)
	{
		m_visualizer = visualizer;
	}


	// --- methods ---

	/** Invoked when action is triggered. */
	@Override
	public void run() {
		if (m_visualizer != null) {
			if (m_haveFilter) {
				m_visualizer.applyCanvasFilter();
			}
			else {
				m_visualizer.clearCanvasFilter();
			}
		}
	}
}