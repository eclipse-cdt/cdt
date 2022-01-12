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
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 407321)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.IVisualizerModelObject;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener;

/**
 * Manages the canvas filters for all current debug sessions.
 */
public class MulticoreVisualizerCanvasFilterManager implements SessionEndedListener {

	/**  map containing the filter lists for different debug sessions */
	private Hashtable<String, MulticoreVisualizerCanvasFilter> m_filterSessionMap = null;

	/** reference to the canvas */
	private MulticoreVisualizerCanvas m_canvas = null;

	/** filter currently in place */
	private MulticoreVisualizerCanvasFilter m_currentFilter = null;

	/** Constructor */
	public MulticoreVisualizerCanvasFilterManager(MulticoreVisualizerCanvas canvas) {
		m_filterSessionMap = new Hashtable<>();
		m_canvas = canvas;
		// add listener to be notified when a debug session ends
		DsfSession.addSessionEndedListener(this);
	}

	/** dispose method */
	public void dispose() {
		// remove listener
		DsfSession.removeSessionEndedListener(this);

		// dispose of any filter
		if (m_filterSessionMap != null) {
			for (MulticoreVisualizerCanvasFilter filter : m_filterSessionMap.values()) {
				filter.dispose();
			}
			m_filterSessionMap.clear();
			m_filterSessionMap = null;
		}
		m_canvas = null;
		m_currentFilter = null;
	}

	/**
	 * Returns the filter for a given debug session if it exists, or else
	 * creates a new filter for that session.
	 */
	public MulticoreVisualizerCanvasFilter getFilter(String sessionId) {
		MulticoreVisualizerCanvasFilter filter = null;
		// already a filter for this session ?
		filter = m_filterSessionMap.get(sessionId);

		// no filter yet for this session? Create one
		if (filter == null) {
			// create one
			filter = new MulticoreVisualizerCanvasFilter(m_canvas);
			m_filterSessionMap.put(sessionId, filter);

		}
		return filter;
	}

	/** Removes the filter in place for a given debug session, if any. */
	public void removeFilter(String sessionId) {
		if (m_filterSessionMap.containsKey(sessionId)) {
			m_filterSessionMap.remove(sessionId);
		}
	}

	/** Re-computes the current filter to reflect latest model changes */
	public void updateCurrentFilter() {
		if (m_currentFilter != null) {
			m_currentFilter.updateFilter();
		}
	}

	/** Sets-up a filter, based on the current canvas selection */
	public void applyFilter() {
		if (m_currentFilter != null) {
			m_currentFilter.applyFilter();
		}
	}

	/** Clears the current filter */
	public void clearFilter() {
		if (m_currentFilter != null) {
			m_currentFilter.clearFilter();
		}
	}

	/** Sets the current filter for a given model/session */
	public void setCurrentFilter(String sessionId) {
		if (sessionId == null) {
			m_currentFilter = null;
		} else {
			m_currentFilter = getFilter(sessionId);
		}
	}

	/** Returns the current filter */
	public MulticoreVisualizerCanvasFilter getCurrentFilter() {
		return m_currentFilter;
	}

	/** Checks if the current filter is active */
	public boolean isCurrentFilterActive() {
		if (m_currentFilter != null) {
			return m_currentFilter.isFilterActive();
		}
		return false;
	}

	/** Checks if the current filter permits the display of an object */
	public boolean displayObject(final IVisualizerModelObject obj) {
		if (m_currentFilter != null) {
			return m_currentFilter.displayObject(obj);
		}
		return false;
	}

	@Override
	public void sessionEnded(DsfSession session) {
		// session has ended - remove corresponding filter
		removeFilter(session.getId());
	}
}
