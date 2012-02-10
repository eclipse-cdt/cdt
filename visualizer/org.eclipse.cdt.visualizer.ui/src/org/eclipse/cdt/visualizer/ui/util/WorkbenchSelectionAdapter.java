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

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;


// ---------------------------------------------------------------------------
// WorkbenchSelectionAdapter
// ---------------------------------------------------------------------------

/**
 * Selection change event manager
 */
public class WorkbenchSelectionAdapter
	implements ISelectionListener, ISelectionProvider
{
	// --- members ---
	
	/** Workbench part (view) this adapter is associated with. */
	protected IViewPart m_view = null;
	
	/** Current selection */
	protected ISelection m_selection = null;
	
	/** Listeners for selection changed events. */
	protected ListenerList m_selectionListeners = null;

	/** Whether selection events are reported */
	protected boolean m_trackSelection = true;

	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public WorkbenchSelectionAdapter(IViewPart view) {
		m_view = view;
		m_selection = null;
		m_selectionListeners = new ListenerList(view, "WorkbenchSelectionAdapter for view " + view.getClass().getSimpleName())
		{
			/** Dispatches event to listeners */
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
		
		// listen for external selection changed events
		m_view.getSite().getPage().addSelectionListener(this);
		
		// set selection provider for this view
		m_view.getSite().setSelectionProvider(this);
		
		// initialize selection
		setSelection(m_view.getSite().getPage().getSelection());

	}
	
	/** Dispose method. */
	public void dispose() {
		if (m_view != null) {
			m_view.getSite().getPage().removeSelectionListener(this);
			m_view.getViewSite().setSelectionProvider(null);
			m_view = null;
		}
		m_selection = null;
		if (m_selectionListeners != null) {
			m_selectionListeners.clear();
			m_selectionListeners = null;
		}
	}
	

	// --- accessors ---
	
	/** Gets whether selection change events are reported. */
	public boolean getTrackSelection() {
		return m_trackSelection;
	}
	
	/** Sets whether selection change events are reported. */
	public void setTrackSelection(boolean trackSelection) {
		m_trackSelection = trackSelection;
	}
	
	
	// --- ISelectionListener implementation ---

	/** Invoked when selection changes externally. */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// ignore selection change events that came from us
		if (part == m_view) return;
		if (m_trackSelection) {
			workbenchSelectionChanged(part, selection);
		}
	}
	
	/** Invoked when selection changes.
	 *  Intended to be overloaded by derived implementations.
	 */
	public void workbenchSelectionChanged(ISelection selection) {
		setSelection(selection);
	}
	
	/** Invoked when selection changes.
	 *  Intended to be overloaded by derived implementations.
	 */
	public void workbenchSelectionChanged(ISelectionProvider provider, ISelection selection) {
		setSelection(provider, selection);
	}
	
	/** Invoked when selection changes.
	 *  Intended to be overloaded by derived implementations.
	 */
	public void workbenchSelectionChanged(Object provider, ISelection selection) {
		setSelection(provider, selection);
	}
	
	
	// --- ISelectionProvider implementation ---

	/** Gets current selection. */
	public ISelection getSelection() {
		return m_selection;
	}

	/** Sets current selection, and raises selection changed event. */
	public void setSelection(ISelection selection) {
		// for some reason, SelectionChangedEvent can't stand a null selection
		if (selection == null) selection = StructuredSelection.EMPTY;
		m_selection = selection;
		m_selectionListeners.raise(new SelectionChangedEvent(this, m_selection));
	}

	/** Sets current selection, and raises selection changed event. */
	public void setSelection(ISelectionProvider source, ISelection selection) {
		// for some reason, SelectionChangedEvent can't stand a null selection
		if (selection == null) selection = StructuredSelection.EMPTY;
		m_selection = selection;
		m_selectionListeners.raise(new SelectionChangedEvent(source, m_selection));
	}
	
	/** Sets current selection, and raises selection changed event. */
	public void setSelection(Object source, ISelection selection) {
		// for some reason, SelectionChangedEvent can't stand a null selection
		if (selection == null) selection = StructuredSelection.EMPTY;
		m_selection = selection;
		m_selectionListeners.raise(new SelectionChangedEvent(new SelectionProviderAdapter(source), m_selection));
	}

	/** Adds external listener for selection change events. */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionListeners.addListener(listener);
	}

	/** Removes external listener for selection change events. */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionListeners.removeListener(listener);
	}


	// --- utilities ---
	
	/** Converts selection to string, for debug display. */
	protected String selectionToString(ISelection selection) {
		String result = null;
		// convert selection to text string
		if (m_selection == null) {
			result = "No Selection";
		}
		else if (m_selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) m_selection;
			List<?> elements = structuredSelection.toList();
			int size = elements.size();
			if (size == 0) {
				result = "Empty Selection";
			}
			else {
				result = "";
				for (int i=0; i<size; i++) {
					if (i>0) result += "\n";
					Object o = elements.get(i);
					String type = o.getClass().getName();
					String value = o.toString();
					result += "[" + i + "]: type= + " + type + ", value='" + value + "'";
				}
			}
		}
		else {
			String type = m_selection.getClass().getName();
			String value = m_selection.toString();
			result = "type=" + type + ", value='" + value + "'";
		}
		return result;
	}
}
