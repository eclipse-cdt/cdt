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

package org.eclipse.cdt.visualizer.ui;

import org.eclipse.cdt.visualizer.ui.util.SelectionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

// ---------------------------------------------------------------------------
// Visualizer
// ---------------------------------------------------------------------------

/**
 * Base class for IVisualizer implementations.
 */
abstract public class Visualizer implements IVisualizer, ISelectionChangedListener {
	// --- members ---

	/** Visualizer's non-localized name. */
	protected String m_name;

	/** Visualizer's localized, displayable name. */
	protected String m_displayName;

	/** Visualizer's localized, displayable description. */
	protected String m_description;

	/** The parent view control. */
	protected IVisualizerViewer m_viewer;

	/** The visualizer control. */
	protected Control m_control;

	/** Externally visible selection manager. */
	protected SelectionManager m_selectionManager;

	// --- constructors/destructors ---

	/** Constructor. */
	public Visualizer() {
		// TODO: internationalize these strings.
		this("visualizer", "Visualizer", "Displays graphic representation of selection.");
	}

	/** Constructor. */
	public Visualizer(String name, String displayName, String description) {
		setName(name);
		setDisplayName(displayName);
		setDescription(description);
		m_selectionManager = new SelectionManager(this, "Visualizer selection manager");
	}

	/** Dispose method. */
	public void dispose() {
		m_name = null;
		m_displayName = null;
		m_description = null;
		disposeControl();
		m_viewer = null;
		if (m_selectionManager != null) {
			m_selectionManager.dispose();
			m_selectionManager = null;
		}
	}

	// --- init methods ---

	/** Invoked when visualizer is created, to permit any initialization.
	 *  Intended to be overridden. Default implementation does nothing.
	 */
	@Override
	public void initializeVisualizer() {
	}

	/** Invoked when visualizer is disposed, to permit any cleanup.
	 *  Intended to be overridden. Default implementation calls dispose().
	 */
	@Override
	public void disposeVisualizer() {
		dispose();
	}

	// --- accessors ---

	/** Sets non-localized name. */
	@Override
	public void setName(String name) {
		m_name = name;
	}

	/** Gets non-localized name. */
	@Override
	public String getName() {
		return m_name;
	}

	/** Sets localized display name. */
	@Override
	public void setDisplayName(String displayName) {
		m_displayName = displayName;
	}

	/** Gets localized display name. */
	@Override
	public String getDisplayName() {
		return m_displayName;
	}

	/** Sets localized description string. */
	@Override
	public void setDescription(String description) {
		m_description = description;
	}

	/** Gets localized description string. */
	@Override
	public String getDescription() {
		return m_description;
	}

	// --- viewer management ---

	/** Sets viewer we're associated with. */
	@Override
	public void setViewer(IVisualizerViewer viewer) {
		m_viewer = viewer;
	}

	/** Gets viewer we're associated with. */
	@Override
	public IVisualizerViewer getViewer() {
		return m_viewer;
	}

	// --- visualizer selection management ---

	/** Invoked when visualizer has been selected. */
	@Override
	public void visualizerSelected() {
	}

	/** Invoked when another visualizer has been selected, hiding this one. */
	@Override
	public void visualizerDeselected() {
	}

	// --- control management ---

	/** Creates and returns visualizer's UI control on specified parent control. */
	@Override
	abstract public Control createControl(Composite parent);

	/** Invoked when visualizer control should be disposed. */
	abstract public void disposeControl();

	/**
	 * Sets visualizer control.
	 * Intended to be called from createControl();
	 */
	protected void setControl(Control control) {
		m_control = control;
	}

	/** Gets visualizer control.
	 *  Returns null if createControl() has not yet been called.
	 */
	@Override
	public Control getControl() {
		return m_control;
	}

	// --- menu/toolbar management ---

	/** Invoked by VisualizerViewer when toolbar needs to be populated. */
	@Override
	public void populateToolBar(IToolBarManager toolBarManager) {
	}

	/** Invoked by VisualizerViewer when toolbar menu needs to be populated. */
	@Override
	public void populateMenu(IMenuManager menuManager) {
	}

	// --- context menu handling ---

	/** Invoked by VisualizerViewer when context menu needs to be populated. */
	@Override
	public void populateContextMenu(IMenuManager menuManager) {
	}

	// --- workbench selection management ---

	/**
	 * Tests whether if the IVisualizer can display the selection
	 * (or something reachable from it).
	 */
	@Override
	public int handlesSelection(ISelection selection) {
		// Default implementation doesn't know how to display anything.
		return 0;
	}

	/**
	 * Invoked by VisualizerViewer when workbench selection changes.
	 */
	@Override
	public void workbenchSelectionChanged(ISelection selection) {
		// Default implementation does nothing.
	}

	// --- ISelectionProvider implementation ---

	// Delegate to selection manager.

	/** Adds external listener for selection change events. */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionManager.addSelectionChangedListener(listener);
	}

	/** Removes external listener for selection change events. */
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionManager.removeSelectionChangedListener(listener);
	}

	/** Raises selection changed event. */
	public void raiseSelectionChangedEvent() {
		m_selectionManager.raiseSelectionChangedEvent();
	}

	/** Gets current externally-visible selection. */
	@Override
	public ISelection getSelection() {
		return m_selectionManager.getSelection();
	}

	/** Sets externally-visible selection. */
	@Override
	public void setSelection(ISelection selection) {
		m_selectionManager.setSelection(selection);
	}

	/** Sets externally-visible selection. */
	public void setSelection(ISelection selection, boolean raiseEvent) {
		m_selectionManager.setSelection(selection, raiseEvent);
	}

	/** Returns true if we currently have a non-empty selection. */
	public boolean hasSelection() {
		return m_selectionManager.hasSelection();
	}

	// --- ISelectionChangedListener implementation ---

	/**
	 * Intended to be invoked when visualizer control's selection changes.
	 * Sets control selection as its own selection,
	 * and raises selection changed event for any listeners.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		setSelection(event.getSelection());
	}
}
