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
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizer;
import org.eclipse.cdt.visualizer.ui.VisualizerAction;

/**
 * @since 1.1
 */
public class FilterCanvasAction extends VisualizerAction {

	/** Visualizer instance we're associated with. */
	MulticoreVisualizer m_visualizer = null;
	
	/** The model class that the white-list filter applies-to.  A null value indicates nothing is filtered-out */
	Class<?> m_filterClass = null;


	/**
	 * @param filterType: the type of canvas object the filter applies-to.  If null, reset filter
	 */
	public FilterCanvasAction(Class<?> filterType) {
		m_filterClass = filterType;
		
		if (m_filterClass == null) {
			setText(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.ClearFilter.text")); //$NON-NLS-1$
		}
		else if (m_filterClass.equals(VisualizerThread.class)  ) {
			setText(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetThreadFilter.text")); //$NON-NLS-1$
		}
		else if (m_filterClass.equals(VisualizerCore.class)) {
			setText(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetCoreFilter.text")); //$NON-NLS-1$
		}
		else if (m_filterClass.equals(VisualizerCPU.class)) {
			setText(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetCPUFilter.text")); //$NON-NLS-1$
		}
	}

	/** Dispose method. */
	@Override
	public void dispose()
	{
		m_visualizer = null;
		m_filterClass = null;
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
			if (m_filterClass != null) {
				m_visualizer.applyCanvasFilter(m_filterClass);
			}
			else {
				m_visualizer.clearCanvasFilter();
			}
		}
	}
}