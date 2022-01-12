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
 *     Xavier Raynaud (Kalray) - Bug 431690
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.swt.graphics.GC;

/** Status bar graphic object */
public class MulticoreVisualizerStatusBar extends MulticoreVisualizerGraphicObject {

	// --- members ---

	/** message to display in status bar */
	protected String m_statusMessage = null;

	// --- constructors/destructors ---

	/** Constructor */
	public MulticoreVisualizerStatusBar() {
	}

	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
		m_statusMessage = null;
	}

	// --- accessors ---

	public void setMessage(String message) {
		m_statusMessage = message;
	}

	// --- paint methods ---

	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {

		if (m_statusMessage == null)
			return;

		// Display message text
		gc.setForeground(IMulticoreVisualizerConstants.COLOR_STATUS_BAR_TEXT);
		int tx = m_bounds.x;
		int ty = m_bounds.y + 15;
		GUIUtils.drawTextAligned(gc, m_statusMessage, m_bounds, tx, ty, true, false);
	}

}
