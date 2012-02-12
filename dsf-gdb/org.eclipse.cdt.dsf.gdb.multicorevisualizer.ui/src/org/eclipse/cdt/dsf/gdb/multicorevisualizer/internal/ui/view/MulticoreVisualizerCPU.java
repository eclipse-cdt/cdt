/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * Graphic object for MulticoreVisualizer.
 */
public class MulticoreVisualizerCPU extends MulticoreVisualizerGraphicObject
{
	// --- members ---
	
	/** CPU ID. */
	protected int m_id;
	
	/** Child cores. */
	protected ArrayList<MulticoreVisualizerCore> m_cores;
	

	// --- constructors/destructors ---
	
	/** Constructor */
	public MulticoreVisualizerCPU(int id)
	{
		m_id = id;
		m_cores = new ArrayList<MulticoreVisualizerCore>();
	}
	
	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
	}
	
	
	// --- accessors ---
	
	/** Gets CPU ID. */
	public int getID() {
		return m_id;
	}

	
	// --- methods ---
	
	/** Adds child core. */
	public void addCore(MulticoreVisualizerCore core)
	{
		m_cores.add(core);
	}
	
	/** Removes child core. */
	public void removeCore(MulticoreVisualizerCore core)
	{
		m_cores.remove(core);
	}
	
	/** Gets list of child cores. */
	public List<MulticoreVisualizerCore> getCores()
	{
		return m_cores;
	}

	
	// --- paint methods ---
	
	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		Color fg, bg;
		fg = Colors.getColor(0,255,0);
		bg = Colors.getColor(0,64,0);
		gc.setForeground(fg);
		gc.setBackground(bg);
		
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
			Color fg, bg;
			fg = Colors.getColor(0,255,0);
			bg = Colors.getColor(0,64,0);
			gc.setForeground(fg);
			gc.setBackground(bg);
			
			int text_indent = 6;
			int tx = m_bounds.x + m_bounds.width  - text_indent;
			int ty = m_bounds.y + m_bounds.height - text_indent;
			GUIUtils.drawTextAligned(gc, Integer.toString(m_id), tx, ty, false, false);
		}
	}

}
