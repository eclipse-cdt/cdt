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

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.jface.viewers.ISelection;


/**
 * White-list Filter for the graphical objects displayed in the multicore 
 * visualizer canvas. The filter can be set for one type of objects.
 * <p>  
 * The filter is created by calling applyFilter() with the type of the model 
 * object the filter should apply-for.  Any object of that type selected at that 
 * time become part of the "filter-to" white-list.  
 * <p>
 * According to the type of object the filter is applied-for, the behavior will be: 
 * <p>
 * - VisualizerCPU: Only the CPUs that are part of the filter white-list will be 
 *   displayed, with all their cores and threads. Cores and threads not on those
 *   CPUs will not be displayed. 
 *   <p>
 * - VisualizerCore: Only the cores that are part of the filter white-list will 
 *   be displayed, with their threads and enclosing CPUs.  CPUs with no core 
 *   selected will not be displayed. 
 *   <p>
 * - VisualizerThread: Only the threads that are part of the filter white-list 
 *   will be displayed.  The core on which those thread(s) happen to be will
 *   be the only ones shown, as well as their encapsulating CPUs.  As the thread
 *   part of the white-list move around the cores/CPUs, the display will seem to 
 *   follow them, showing the cores/CPUs where they happen to be at any point in 
 *   time and hiding the rest.  
 *   <p>
 *
 * Once applied, a filter remains in effect until disposed or replaced. 
 */
public class MulticoreVisualizerCanvasFilter {
		
	// What type of model objects the current filter white-lists
	Class<?> m_filterClass = null;
	
	// The list of elements we white list 
	// Note: the filter might potentially have a longer life than the model.  
	// So instead of saving the model objects we save their ids.
	List<Integer> m_selectedIdList = null;
	
	// reference to the canvas
	MulticoreVisualizerCanvas m_canvas = null;
	
	// is the filter is active/set
	boolean m_filterActive = false;	


	// --- constructors/destructors ---

	/** Constructor. */
	public MulticoreVisualizerCanvasFilter(MulticoreVisualizerCanvas canvas) {
		m_canvas = canvas;
	}
	
	/** Dispose	method */
	public void dispose() {
		clearFilter();
		m_canvas = null;
	}

	
	// --- filter methods ---
	
	/**
	 * Set-up a canvas white-list filter.  
	 * <p>
	 * @param classToFilter : class the filter applies-to
	 */ 
	public void applyFilter(Class<?> classToFilter) {
		if(isFilterActive()) {
			clearFilter();
		}

		m_selectedIdList = new ArrayList<Integer>();

		if (classToFilter.equals(VisualizerThread.class) ||
				classToFilter.equals(VisualizerCore.class) ||
				classToFilter.equals(VisualizerCPU.class)) 
		{
			m_filterClass = classToFilter;
			m_filterActive = true;

			// get list of selected objects the filter applies-for
			ISelection selection = m_canvas.getSelection();
			List<Object> selectedObjects = SelectionUtils.getSelectedObjects(selection);
			for (Object obj : selectedObjects) {
				if (obj.getClass() ==  m_filterClass) {
					if (m_filterClass == VisualizerCore.class) {
						VisualizerCore c = (VisualizerCore) obj;
						m_selectedIdList.add(c.getID());
					}
					else if (m_filterClass == VisualizerThread.class) {
						VisualizerThread t = (VisualizerThread) obj;
						m_selectedIdList.add(t.getTID());
					}
					else if (m_filterClass == VisualizerCPU.class) {
						VisualizerCPU c = (VisualizerCPU) obj;
						m_selectedIdList.add(c.getID());
					}
				}
			}
		}
	}

	/**
	 * Tells if a candidate model object should be displayed, according to the filter
	 * in place.  The type and id of the object play a role in the decision.
	 * <p>  
	 * note: If no filter is set, any object can be displayed, and do this method 
	 * will always return true, in that case.    
	 * @param candidate: model object
	 */
	public boolean displayObject (final Object candidate) {
		// filter not active? Let anything be displayed 
		if (!m_filterActive) {
			return true;
		}

		// filter on CPU(s)? 
		if (m_filterClass.equals(VisualizerCPU.class) ) {
			if (candidate instanceof VisualizerCPU) {
				int cpuid = ((VisualizerCPU) candidate).getID();
				return m_selectedIdList.contains(cpuid);
			}
			else if (candidate instanceof VisualizerCore || 
					candidate instanceof VisualizerThread) 
			{
				return true;
			}
		}

		// filter on core(s)?
		if (m_filterClass.equals(VisualizerCore.class) ) {
			if (candidate instanceof VisualizerCore) {
				int coreid = ((VisualizerCore) candidate).getID();
				return m_selectedIdList.contains(coreid);
			}
			else if (candidate instanceof VisualizerCPU) {
				for (VisualizerCore c : ((VisualizerCPU) candidate).getCores()) {
					if (m_selectedIdList.contains(c.getID())) {
						return true;
					}
				}
				return false;
			}
			else if (candidate instanceof VisualizerThread) {
				return true;
			}
		}

		// filter on thread(s)?
		if (m_filterClass.equals(VisualizerThread.class) ) {
			if (candidate instanceof VisualizerThread) {
				int tid = ((VisualizerThread) candidate).getTID();
				return m_selectedIdList.contains(tid);
			}
			else if (candidate instanceof VisualizerCore) {
				// the core contains a selected thread? 
				for (VisualizerThread t : m_canvas.getModel().getThreads()) {
					if (m_selectedIdList.contains(t.getTID())) {
						if (t.getCore().getID() == ((VisualizerCore)candidate).getID()) {
							return true;
						}
					}
				}
			}
			else if (candidate instanceof VisualizerCPU) {
				// the cpu contains core that contains a selected thread?
				for (VisualizerThread t : m_canvas.getModel().getThreads()) {
					if (m_selectedIdList.contains(t.getTID())) {
						if (t.getCore().getCPU().getID() == ((VisualizerCPU)candidate).getID()) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/** Removes any canvas filter currently in place */
	public void clearFilter() {
		if(m_selectedIdList != null) {
			m_selectedIdList.clear();
			m_selectedIdList = null;
		}
		m_filterActive = false;
		m_filterClass = null;
	}

	/** tells if a canvas filter is currently in place */
	public boolean isFilterActive() {
		return m_filterActive;
	}
}





