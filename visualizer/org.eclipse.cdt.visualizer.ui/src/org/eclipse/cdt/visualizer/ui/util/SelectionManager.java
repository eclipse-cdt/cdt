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
import org.eclipse.jface.viewers.SelectionChangedEvent;


// ---------------------------------------------------------------------------
// SelectionManager
// ---------------------------------------------------------------------------

/**
 * Selection management utility class
 */
public class SelectionManager
	implements ISelectionProvider
{
	// --- members ---
	
	/** Actual source to report for selection change events. */
	protected ISelectionProvider m_source = null;
	
	/** Manager label, also used on listener list. */
	protected String m_label = null;

	/** Current selection, if any. */
	protected ISelection m_selection = SelectionUtils.EMPTY_SELECTION;
	
    /** Selection changed listeners */
    protected ListenerList m_selectionListeners = null;

    /** Whether selection events are enabled */
    protected boolean m_selectionEventsEnabled = true;

    // --- constructors/destructors ---
    
    /** Constructor. */
    public SelectionManager(ISelectionProvider source, String label)
    {
    	m_source = (source == null) ? this : source;
    	m_label = label;
		m_selectionListeners = new ListenerList(this, label + ", listener list") {
			public void raise(Object listener, Object event) {
                if (listener instanceof ISelectionChangedListener &&
                        event instanceof SelectionChangedEvent)
                {
                        ISelectionChangedListener typedListener = (ISelectionChangedListener) listener;
                        SelectionChangedEvent typedEvent = (SelectionChangedEvent) event;
                        typedListener.selectionChanged(typedEvent);
                }
			}
		};
    }
    
    /** Dispose method. */
    public void dispose()
    {
    	m_selectionEventsEnabled = false;
    	m_selection = SelectionUtils.EMPTY_SELECTION;
    	if (m_selectionListeners != null) {
    		m_selectionListeners.clear();
    		m_selectionListeners = null;
    	}
    	// m_label = null; // leave label, to aid in debugging cleanup
    	m_source = null;
    }
    
    // --- ISelectionProvider implementation ---

    /** Adds selection changed listener. */
    public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		if (listener == null) return;
		m_selectionListeners.addListener(listener);
		// fake a selection changed event so new listener can update itself properly
		listener.selectionChanged(new SelectionChangedEvent(m_source, getSelection()));
	}

    /** Removes selection changed listener. */
	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		if (listener == null) return;
		m_selectionListeners.removeListener(listener);
	}

	/** Returns current selection. */
	public ISelection getSelection()
	{
		return m_selection;
	}

	/** Sets selection, and raises change event. */
	public void setSelection(ISelection selection)
	{
		setSelection(selection, true);
	}
	
	/** Sets selection, and raises change event with specified provider as the source. */
	public void setSelection(ISelectionProvider provider, ISelection selection)
	{
		setSelection(provider, selection, true);
	}
	
	/** Sets selection, and raises change event
	 *  if raiseEvent is true. */
	public void setSelection(ISelection selection, boolean raiseEvent)
	{
		if (selection == null)
			selection = SelectionUtils.EMPTY_SELECTION;
		m_selection = selection;
		if (raiseEvent) raiseSelectionChangedEvent();
	}

	/** Sets selection, and raises change event with specified provider as the source
	 *  if raiseEvent is true. */
	public void setSelection(ISelectionProvider provider, ISelection selection, boolean raiseEvent)
	{
		if (selection == null)
			selection = SelectionUtils.EMPTY_SELECTION;
		m_selection = selection;
		if (raiseEvent) raiseSelectionChangedEvent(provider);
	}
	
	/** Returns true if we currently have a non-emptr selection. */
	public boolean hasSelection()
	{
		return (SelectionUtils.getSelectionSize(m_selection) > 0);
	}
	
	
	// --- methods ---
	
    /** Gets whether selection events are enabled. */
    public boolean getSelectionEventsEnabled() {
    	return m_selectionEventsEnabled;
    }

    /** Sets whether selection events are enabled. */
    public void setSelectionEventsEnabled(boolean enabled) {
    	m_selectionEventsEnabled = enabled;
    }

	/** Raises selection changed event. */
	public void raiseSelectionChangedEvent() {
		if (m_selectionEventsEnabled)
			m_selectionListeners.raise(new SelectionChangedEvent(m_source, getSelection()));
	}
	
	/** Raises selection changed event with specified provider as source. */
	public void raiseSelectionChangedEvent(ISelectionProvider provider) {
		if (m_selectionEventsEnabled)
			m_selectionListeners.raise(new SelectionChangedEvent(provider, getSelection()));
	}
}
