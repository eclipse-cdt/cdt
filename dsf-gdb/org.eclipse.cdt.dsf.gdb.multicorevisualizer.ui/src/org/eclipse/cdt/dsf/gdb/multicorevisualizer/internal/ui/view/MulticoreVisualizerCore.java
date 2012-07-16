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

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * MulticoreVisualizer CPU core object.
 */
public class MulticoreVisualizerCore extends MulticoreVisualizerGraphicObject
{
	// --- members ---
	
	/** Parent CPU. */
	protected MulticoreVisualizerCPU m_cpu = null;
	
	/** Core ID. */
	protected int m_id;
	
	/** List of threads currently on this core. */
	protected ArrayList<MulticoreVisualizerThread> m_threads;
	
	// --- constructors/destructors ---
	
	/** Constructor */
	public MulticoreVisualizerCore(MulticoreVisualizerCPU cpu, int id) {
		m_cpu = cpu;
		if (m_cpu != null) m_cpu.addCore(this);
		m_id = id;
		m_threads = new ArrayList<MulticoreVisualizerThread>();
	}
	
	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
		if (m_threads != null) {
			m_threads.clear();
			m_threads = null;
		}
	}
	
	
	// --- accessors ---
	
	/** Gets parent CPU. */
	public MulticoreVisualizerCPU getCPU() {
		return m_cpu;
	}

	/** Gets Core ID. */
	public int getID() {
		return m_id;
	}

	
	// --- methods ---
	
	/** Adds child thread. */
	public void addThread(MulticoreVisualizerThread thread)
	{
		m_threads.add(thread);
	}
	
	/** Removes child thread. */
	public void removeThread(MulticoreVisualizerThread thread)
	{
		m_threads.remove(thread);
	}
	
	/** Removes all child threads. */
	public void removeAllThreads()
	{
		m_threads.clear();
	}
	
	/** Gets list of child threads. */
	public List<MulticoreVisualizerThread> getThreads()
	{
		return m_threads;
	}

	/**
	 * A core state is based on its thread states.
	 * If any thread is CRASHED, the core is CRASHED.
	 * If no thread is CRASHED and any thread is SUSPENDED, the core is SUSPENDED.
	 * If no thread is CRASHED and no thread is SUSPENDED, the core is RUNNING.
	 */
	protected VisualizerExecutionState getCoreState() {
		VisualizerExecutionState state = VisualizerExecutionState.RUNNING;
		
		for (MulticoreVisualizerThread thread : m_threads) {
			switch (thread.getState()) {
			case CRASHED:
				// As soon as we have a crashed thread, we mark
				// the core as crashed.
				return VisualizerExecutionState.CRASHED;
			case SUSPENDED:
				// As soon as we have a suspended thread, we
				// consider the core as suspended.  However,
				// we keep looping through the threads
				// looking for a crashed one.
				state = VisualizerExecutionState.SUSPENDED;
				break;
			}
		}
		
		return state;
	}
	
	/** Returns core color for current state. */
	protected Color getCoreStateColor(boolean foreground) {
		VisualizerExecutionState state = getCoreState();
		
		switch (state) {
		case RUNNING:
			if (foreground) return IMulticoreVisualizerConstants.COLOR_RUNNING_CORE_FG;
			return IMulticoreVisualizerConstants.COLOR_RUNNING_CORE_BG;
		case SUSPENDED:
			if (foreground) return IMulticoreVisualizerConstants.COLOR_SUSPENDED_CORE_FG;
			return IMulticoreVisualizerConstants.COLOR_SUSPENDED_CORE_BG;
		case CRASHED:
			if (foreground) return IMulticoreVisualizerConstants.COLOR_CRASHED_CORE_FG;
			return IMulticoreVisualizerConstants.COLOR_CRASHED_CORE_BG;
		}
		
		assert false;
		return Colors.BLACK;
	}
	
	// --- paint methods ---
	
	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		gc.setForeground(getCoreStateColor(true));
		gc.setBackground(getCoreStateColor(false));

		gc.fillRectangle(m_bounds);
		gc.drawRectangle(m_bounds);
		
		if (m_bounds.height > 16) {
			int text_indent = 3;
			int tx = m_bounds.x + m_bounds.width - text_indent;
			int ty = m_bounds.y + text_indent;
			GUIUtils.drawTextAligned(gc, Integer.toString(m_id), tx, ty, false, true);
		}
	}
}
