/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.util;

import java.util.ArrayList;

// ---------------------------------------------------------------------------
// ListenerList
// ---------------------------------------------------------------------------

/**
 * Utility class for managing a list of event listeners.
 * Maintains a list of listener instances, and dispatches events to them.
 *
 * To use this class, create a derived type that implements the raise(listener, event)
 * method to appropriately delegate an event to a listener.
 *
 * Note: it is the responsibility of the user of this class to check types
 * of listeners and events (for example, by having strongly-typed add/remove methods
 * that delegate to the add/remove methods on this class).
 */
abstract public class ListenerList {
	// --- members ---

	/** Object that owns this listener list */
	protected Object m_owner = null;

	/** listener list display label */
	protected String m_label = null;

	/** listener list */
	protected ArrayList<Object> m_listeners = null;

	// --- constructors/destructors ---

	/** Constructor. */
	public ListenerList(Object owner, String label) {
		m_owner = owner;
		m_label = label;
	}

	/** Dispose method. */
	public void dispose() {
		m_owner = null;
		m_label = null;
		if (m_listeners != null) {
			clear();
			m_listeners = null;
		}
	}

	// --- methods ---

	/** Clears list of listeners */
	public synchronized void clear() {
		if (m_listeners != null) {
			m_listeners.clear();
		}
	}

	/** Returns count of current listeners. */
	public synchronized int size() {
		return (m_listeners == null) ? 0 : m_listeners.size();
	}

	/** Adds a listener */
	public synchronized void addListener(Object listener) {
		if (m_listeners == null) {
			m_listeners = new ArrayList<>();
		}
		if (!m_listeners.contains(listener)) {
			m_listeners.add(listener);
		}
	}

	/** Removes a listener */
	public synchronized void removeListener(Object listener) {
		if (m_listeners != null) {
			m_listeners.remove(listener);
		}
	}

	/**
	 * Dispatches event to all attached listeners
	 * Invokes raise(listener, event) for each attached listener.
	 */
	public void raise(final Object event) {
		// we can't use an iterator here, because
		// the listener list might change while we're walking it,
		// which would make the iterator throw a ConcurrentModificationException,
		// hence we'll make a private copy of the listener list
		ArrayList<Object> listeners = null;
		synchronized (this) {
			// keep the lock on the listener list as brief as possible
			if (m_listeners != null) {
				listeners = new ArrayList<>(m_listeners);
			}
		}
		int count = (listeners == null) ? 0 : listeners.size();
		for (int i = 0; i < count; i++) {
			Object listener = listeners.get(i);
			try {
				raise(listener, event);
			} catch (Throwable t) {
				// TODO: decide how to log this
			}
		}
	}

	/**
	 * Dispatches typed event to specified listener
	 * Intended to be overloaded by derived class to cast listener and event
	 * to appropriate type and invoke appropriate listener method(s).
	 *
	 * For example:
	 *
	 * 	ListenerList m_listeners =
	 *      new ListenerList(this, "VisualizerViewer event listeners")
	 *	{
	 *		public void raise(Object listener, Object event) {
	 *			if (listener instanceof IVisualizerViewerListener &&
	 *				event instanceof VisualizerViewerEvent)
	 *			{
	 *				IVisualizerViewerListener typedListener = (IVisualizerViewerListener) listener;
	 *				VisualizerViewerEvent typedEvent        = (VisualizerViewerEvent) event;
	 *				typedListener.visualizerEvent(VisualizerViewer.this, typedEvent);
	 *			}
	 *		}
	 *	};
	 *
	 */
	abstract protected void raise(Object listener, Object event);
}
