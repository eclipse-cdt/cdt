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
 *     Marc Dumais (Ericsson) - Add CPU/core load information to the multicore visualizer (Bug 396268)
 *     Marc Dumais (Ericsson) - Bug 404894
 *     Xavier Raynaud (Kalray) - Bug 431690
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * Graphic object for MulticoreVisualizer.
 */
public class MulticoreVisualizerCPU extends MulticoreVisualizerGraphicObject {
	// --- members ---

	/** CPU ID. */
	protected int m_id;

	/** Child cores. */
	protected ArrayList<MulticoreVisualizerCore> m_cores;

	/** Load meter associated to this CPU */
	protected MulticoreVisualizerLoadMeter m_loadMeter;

	/** Background color used to draw cpu */
	protected static final Color BG_COLOR = IMulticoreVisualizerConstants.COLOR_CPU_BG;

	/** Foreground coloe used to draw cpu */
	protected static final Color FG_COLOR = IMulticoreVisualizerConstants.COLOR_CPU_FG;

	// --- constructors/destructors ---

	/** Constructor */
	public MulticoreVisualizerCPU(int id) {
		m_id = id;
		m_cores = new ArrayList<>();

		// default load meter
		m_loadMeter = new MulticoreVisualizerLoadMeter(null, null);
	}

	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
		if (m_loadMeter != null) {
			m_loadMeter.dispose();
		}
	}

	// --- accessors ---

	/** Gets CPU ID. */
	public int getID() {
		return m_id;
	}

	// --- methods ---

	/** Adds child core. */
	public void addCore(MulticoreVisualizerCore core) {
		m_cores.add(core);
	}

	/** Removes child core. */
	public void removeCore(MulticoreVisualizerCore core) {
		m_cores.remove(core);
	}

	/** Gets list of child cores. */
	public List<MulticoreVisualizerCore> getCores() {
		return m_cores;
	}

	/** Sets the load meter associated to this CPU */
	public void setLoadMeter(MulticoreVisualizerLoadMeter meter) {
		m_loadMeter = meter;
	}

	/** Gets the load meter associated to this CPU */
	public MulticoreVisualizerLoadMeter getLoadMeter() {
		return m_loadMeter;
	}

	// --- paint methods ---

	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		gc.setBackground(BG_COLOR);

		// We want the load meter to share the same BG color
		m_loadMeter.setParentBgColor(BG_COLOR);

		// highlight in a different color if selected
		if (m_selected) {
			gc.setForeground(IMulticoreVisualizerConstants.COLOR_SELECTED);
		} else {
			gc.setForeground(FG_COLOR);
		}

		gc.fillRectangle(m_bounds);
		gc.drawRectangle(m_bounds);
	}

	/** Returns true if object has decorations to paint. */
	@Override
	public boolean hasDecorations() {
		return true;
	}

	/** Invoked to allow element to paint decorations on top of anything drawn on it */
	@Override
	public void paintDecorations(GC gc) {
		if (m_bounds.height > 20) {
			gc.setForeground(IMulticoreVisualizerConstants.COLOR_CPU_FG);
			gc.setBackground(IMulticoreVisualizerConstants.COLOR_CPU_BG);

			int text_indent_x = 2;
			int text_indent_y = 0;
			int tx = m_bounds.x + m_bounds.width - text_indent_x;
			int ty = m_bounds.y + m_bounds.height - text_indent_y;
			GUIUtils.drawTextAligned(gc, Integer.toString(m_id), m_bounds, tx, ty, false, false);
		}
	}

}
