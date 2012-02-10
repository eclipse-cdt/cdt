/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.util;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;


// ---------------------------------------------------------------------------
// SelectionProviderAdapter
// ---------------------------------------------------------------------------

/**
 * Wrapper for selection "providers" that don't happen to implement
 * ISelectionProvider interface.
 */
public class SelectionProviderAdapter
	implements ISelectionProvider
{
	// --- members ---
	
	/** Real source object. */
	protected Object m_source = null;
	
	/** Selection manager. */
	protected SelectionManager m_selectionManager = null;
	
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public SelectionProviderAdapter(Object source) {
		m_source = source;
		m_selectionManager = new SelectionManager(this, "SelectionProviderAdapter for source " + m_source.toString());
	}
	
	/** Dispose method. */
	public void dispose() {
		m_source = null;
	    if (m_selectionManager != null) {
	    	m_selectionManager.dispose();
	    	m_selectionManager = null;
	    }
	}
	
	
	// --- accessors ---
	
	/** Gets wrapped selection source. */
	public Object getActualSource() {
		return m_source;
	}

	
	// --- ISelectionProvider implementation ---

	/** Adds selection change listener.
	 *  Default implementation does nothing.
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		m_selectionManager.addSelectionChangedListener(listener);
	}

	/** Removes selection change listener.
	 *  Default implementation does nothing.
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		m_selectionManager.removeSelectionChangedListener(listener);
	}

	/** Gets selection.
	 *  Default implementation does nothing.
	 */
	public ISelection getSelection() {
		return m_selectionManager.getSelection();
	}

	/** Sets selection.
	 *  Default implementation does nothing.
	 */
	public void setSelection(ISelection selection)
	{
		m_selectionManager.setSelection(selection);
	}
}
