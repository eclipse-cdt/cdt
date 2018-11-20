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
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 396268)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizer;
import org.eclipse.cdt.visualizer.ui.VisualizerAction;

/**
 * Action that sets the load meter refresh period
 */
public class SetLoadMeterPeriodAction extends VisualizerAction {

	/** Visualizer instance we're associated with. */
	MulticoreVisualizer m_visualizer = null;

	final int m_period;

	public SetLoadMeterPeriodAction(String label, int period) {
		super(label, AS_RADIO_BUTTON);
		m_period = period;

		setDescription(
				MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetLoadMeterPeriod.description")); //$NON-NLS-1$
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

	/** Invoked when action is triggered. */
	@Override
	public void run() {
		if (!isChecked())
			return;

		if (m_visualizer != null) {
			m_visualizer.setLoadMeterTimerPeriod(m_period);
		}
	}
}
