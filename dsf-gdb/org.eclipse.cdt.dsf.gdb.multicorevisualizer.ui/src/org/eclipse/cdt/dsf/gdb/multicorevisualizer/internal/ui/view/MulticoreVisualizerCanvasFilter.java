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

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.IVisualizerModelObject;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.jface.viewers.ISelection;


/**
 * White-list Filter for the graphical objects displayed in the multicore 
 * visualizer canvas. 
 * <p>  
 * The filter is created by calling applyFilter().  Any object selected at that 
 * moment become part of the "filter-to" white-list.  genDynamicFilterList() 
 * is called to dynamically expend the filter to include the current ancestor(s)
 * of the elements part of the list.  
 * <p>
 * Then displayObject() can be called with a model object as parameter, to 
 * know if the dynamic filter currently permits the display of that element.
 * <p>
 * Once applied, a filter remains in effect until disposed or replaced. 
 */
public class MulticoreVisualizerCanvasFilter {
		
	// The list of elements we white list 
	// Note: the filter might potentially have a longer life than the model.  
	// So instead of saving the model objects we save their ids.
	List<IVisualizerModelObject> m_filterList = null;
	// the dynamically expanded list, containing elements in the
	// white list and their parents - recalculated when required
	// since some elements can move around and change parent
	List<IVisualizerModelObject> m_dynamicFilterList = null;
	// reference to the canvas
	private MulticoreVisualizerCanvas m_canvas = null;
	
	// is the filter is active/set
	private boolean m_filterActive = false;	
	
	// for stats
	private int m_nShownCpu = 0;
	private int m_nShownCore = 0;
	private int m_nShownThread = 0;
	private int m_totalCpu = 0;
	private int m_totalCore = 0;
	private int m_totalThread = 0;

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
     * Set-up a canvas white-list filter.  Any applicable selected object is added to 
     * the white-list. 
     */
    public void applyFilter() {
        if (isFilterActive()) {
            clearFilter();
        }

        m_filterList = new ArrayList<IVisualizerModelObject>();
        m_dynamicFilterList = new ArrayList<IVisualizerModelObject>();

        m_filterActive = true;

        // get list of selected objects the filter applies-for
        ISelection selection = m_canvas.getSelection();
        List<Object> selectedObjects = SelectionUtils.getSelectedObjects(selection);
       
        for (Object obj : selectedObjects) {
        	if (obj instanceof IVisualizerModelObject) {
        		m_filterList.add((IVisualizerModelObject)obj);
        	}
        }
    }
    
    /**
     * Re-computes the dynamic filter list.  The dynamic filter list 
     * contains what the user added to the filter plus their parent objects.
     */
    public void genDynamicFilterList() {
    	resetCounters();
    	if(m_filterList == null)
    		return;
    	
    	m_dynamicFilterList.clear();
    	
    	for (IVisualizerModelObject elem : m_filterList) {
    		// add element to list
    		addElementToFilterList(elem);

    		// also add all its ancestors
    		IVisualizerModelObject parent = elem.getParent();
    		while (parent != null) {
    			addElementToFilterList(parent);
    			parent = parent.getParent();
    		}
    	}
    }

	/**
	 * Tells if a candidate model object should be displayed, according to the 
	 * filter in place.  The type and id of the object play a role in the decision.
	 * <p>  
	 * note: If no filter is set, any object can be displayed.
	 */
	public boolean displayObject (final IVisualizerModelObject candidate) {
		// filter not active? Let anything be displayed 
		if (!m_filterActive) {
			return true;
		}
					
		// Candidate is in white filter list?  
		if (isElementInFilterList(candidate)) {
			return true;
		}
		
		// candidate is not in white-list
		return false;
	}
	
	/**
	 * Adds an element to the dynamic filter list, if an equivalent 
	 * element is not already there.
	 */
	private void addElementToFilterList(final IVisualizerModelObject elem) {
		// add element to list
		if (!isElementInFilterList(elem)) {
			m_dynamicFilterList.add(elem);
			stepStatCounter(elem);
		}
	}
	
	/**
	 * Checks if an element already has an equivalent in the 
	 * dynamic filter list.
	 */
	private boolean isElementInFilterList(final IVisualizerModelObject candidate) {
		// Candidate in dynamic filter list?  We can't just compare
		// object directly since it's possible the model was rebuilt
		// since the filter was put in place.  If we find objects of
		// the same type with the same ID, they represent the same
		// thing.
		for (IVisualizerModelObject elem : m_dynamicFilterList) {
			if (candidate.getClass() == elem.getClass()) {
				if (candidate.getID() == elem.getID()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** Removes any canvas filter currently in place */
	public void clearFilter() {
		if (m_filterList != null) {
			m_filterList.clear();
			m_filterList = null;
		}
		
		if (m_dynamicFilterList != null) {
			m_dynamicFilterList.clear();
			m_dynamicFilterList = null;
		}
		resetCounters();
		m_filterActive = false;
		
	}

	/** tells if a canvas filter is currently in place */
	public boolean isFilterActive() {
		return m_filterActive;
	}

	
	/** Counters methods */
	
    /**	
     * Used to step the filtered counters for a given type of 
     * model object.
     */
	private void stepStatCounter(IVisualizerModelObject modelObj) {
		if (modelObj instanceof VisualizerCPU) {
			m_nShownCpu++;
		}
		else if(modelObj instanceof VisualizerCore) {
			m_nShownCore++;
		}
		else if(modelObj instanceof VisualizerThread) {
			m_nShownThread++;
		}
	}
	
	/**	Reset the filtering counters */
	private void resetCounters() {
		m_nShownCpu = 0;
		m_nShownCore = 0;
		m_nShownThread = 0;
		// refresh total counts since the model can change
		if (m_canvas != null) {
			VisualizerModel model = m_canvas.getModel();
			if (model != null) {
				m_totalCpu = model.getCPUCount();
				m_totalCore = model.getCoreCount();
				m_totalThread = model.getThreadCount();
			}
		}
	}
	
	/**	returns a String giving the current filtering stats */
	public String getStats() {
		return "Showing: CPUs: ("+m_nShownCpu+"/"+m_totalCpu+"), " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"Cores: ("+m_nShownCore+"/"+m_totalCore+"), " + //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
				"Threads: ("+m_nShownThread+"/"+m_totalThread+")";    //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}
}





