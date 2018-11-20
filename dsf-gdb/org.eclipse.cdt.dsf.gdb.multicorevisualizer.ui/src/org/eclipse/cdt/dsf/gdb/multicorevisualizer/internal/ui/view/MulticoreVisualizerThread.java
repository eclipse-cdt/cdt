/*******************************************************************************
 * Copyright (c) 2012, 2014 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *     Xavier Raynaud (Kalray) - Bug 431690, 432151, 431935
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * MulticoreVisualizer Thread object.
 */
public class MulticoreVisualizerThread extends MulticoreVisualizerGraphicObject {
	// --- constants ---

	/** Thread "pixie" spot width/height */
	public static final int THREAD_SPOT_SIZE = 18;

	/** Minimum containing object size to allow thread to draw itself. */
	public static final int MIN_PARENT_WIDTH = THREAD_SPOT_SIZE + 4;

	// --- members ---

	/** Parent CPU. */
	protected MulticoreVisualizerCore m_core;

	/** Visualizer model thread. */
	protected VisualizerThread m_thread;

	/** Whether this thread is part of a currently selected process. */
	protected boolean m_processSelected = true;

	// --- constructors/destructors ---

	/** Constructor */
	public MulticoreVisualizerThread(MulticoreVisualizerCore core, VisualizerThread thread) {
		m_core = core;
		m_thread = thread;
	}

	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
		m_thread = null;
	}

	// --- accessors ---

	/** Gets parent Core. */
	public MulticoreVisualizerCore getCore() {
		return m_core;
	}

	/** Sets parent Core. */
	public void setCore(MulticoreVisualizerCore core) {
		m_core = core;
	}

	/** Gets thread model object. */
	public VisualizerThread getThread() {
		return m_thread;
	}

	/** Gets Process ID. */
	public int getPID() {
		return m_thread.getPID();
	}

	/** Gets Thread ID. */
	public int getTID() {
		return m_thread.getTID();
	}

	/** Gets thread state. */
	public VisualizerExecutionState getState() {
		return m_thread.getState();
	}

	/** Sets whether thread's process is selected. */
	public void setProcessSelected(boolean processSelected) {
		m_processSelected = processSelected;
	}

	/** Gets whether thread's process is selected. */
	public boolean getProcessSelected() {
		return m_processSelected;
	}

	// --- methods ---

	/** Gets thread color based on current state. */
	protected Color getThreadStateColor() {
		switch (m_thread.getState()) {
		case RUNNING:
			return IMulticoreVisualizerConstants.COLOR_RUNNING_THREAD;
		case SUSPENDED:
			return IMulticoreVisualizerConstants.COLOR_SUSPENDED_THREAD;
		case CRASHED:
			return IMulticoreVisualizerConstants.COLOR_CRASHED_THREAD;
		case EXITED:
			return IMulticoreVisualizerConstants.COLOR_EXITED_THREAD;
		}

		assert false;
		return Colors.BLACK;
	}

	// --- paint methods ---

	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		if (m_core.getWidth() >= MIN_PARENT_WIDTH) {
			gc.setBackground(getThreadStateColor());

			int x = m_bounds.x;
			int y = m_bounds.y;
			int w = THREAD_SPOT_SIZE;
			int h = THREAD_SPOT_SIZE;

			// draw an alpha-shaded "pixie" light for each thread
			int step1 = 3;
			int step2 = 6;
			int alpha1 = 128;
			int alpha2 = 196;
			int alpha3 = 255;
			if (!m_processSelected) {
				alpha1 -= 64;
				alpha2 -= 64;
				alpha3 -= 64;
			}
			gc.setAlpha(alpha1);
			gc.fillOval(x, y, w, h);
			gc.setAlpha(alpha2);
			gc.fillOval(x + step1, y + step1, w - step1 * 2, h - step1 * 2);
			gc.setAlpha(alpha3);
			gc.fillOval(x + step2, y + step2, w - step2 * 2, h - step2 * 2);
			gc.setAlpha(255);

			// special case: for the "process" thread, draw an enclosing circle
			if (m_thread.isProcessThread()) {
				// Subtract one from the width and height
				// in the case of drawOval because that method
				// adds a pixel to each value for some reason
				gc.setForeground(IMulticoreVisualizerConstants.COLOR_PROCESS_THREAD);
				gc.drawOval(x, y, w - 1, h - 1);
			}

			// draw text annotations
			gc.setBackground(IMulticoreVisualizerConstants.COLOR_THREAD_TEXT_BG);
			gc.setForeground(IMulticoreVisualizerConstants.COLOR_THREAD_TEXT_FG);

			// if it has an associated debugger, add a marker
			// (for now, every thread is debugged.)
			//			GUIUtils.drawText(gc, "D", x+w, y-8); //$NON-NLS-1$

			// draw TID, in format "<gdb tid> - ( <os tid> )
			String displayTID = m_thread.getGDBTID() + " - ( " + m_thread.getTID() + " )"; //$NON-NLS-1$ //$NON-NLS-2$
			GUIUtils.drawText(gc, displayTID, x + w + 4, y + 2);

			// draw selection marker, if any
			if (m_selected) {
				gc.setForeground(IMulticoreVisualizerConstants.COLOR_SELECTED);
				gc.drawOval(x - 2, y - 2, w + 3, h + 3);
				gc.drawOval(x - 3, y - 3, w + 5, h + 5);
			}
		}
	}

	@Override
	public String getTooltip(int x, int y) {
		return m_thread.getLocationInfo();
	}

}
