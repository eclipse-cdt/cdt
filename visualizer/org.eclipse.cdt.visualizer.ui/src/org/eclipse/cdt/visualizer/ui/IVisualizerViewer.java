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

import org.eclipse.cdt.visualizer.ui.events.IVisualizerViewerListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

// ----------------------------------------------------------------------------
// IVisualizerViewer
// ----------------------------------------------------------------------------

/**
 * CDT Visualizer Viewer interface.
 *
 * An IVisualizerViewer is a simple container for multiple
 * IVisualizers, where the currently selected IVisualizer
 * determines which IVisualizer control is displayed in the viewer.
 */
public interface IVisualizerViewer {
	// --- accessors ---

	/** Gets containing view. */
	public VisualizerView getView();

	/** Returns non-localized unique name for selected visualizer. */
	public String getVisualizerName();

	/** Returns localized name to display for selected visualizer. */
	public String getVisualizerDisplayName();

	/** Returns localized tooltip text to display for selected visualizer. */
	public String getVisualizerDescription();

	// --- control management ---

	/** Gets viewer control. */
	public Control getControl();

	// --- focus handling ---

	/**
	 * Invoked by VisualizerView when currently selected presentation,
	 * if any, should take the focus.
	 */
	public boolean setFocus();

	// --- menu/toolbar management ---

	/** Invoked when visualizer is selected, to populate the toolbar. */
	public void populateToolBar(IToolBarManager toolBarManager);

	/** Invoked when visualizer is selected, to populate the toolbar's menu. */
	public void populateMenu(IMenuManager menuManager);

	// --- context menu handling ---

	/** Invoked when context menu is about to be shown. */
	public void populateContextMenu(IMenuManager m);

	/** Gets context menu location. */
	public Point getContextMenuLocation();

	// --- selection handling ---

	/**
	 * Invoked by VisualizerView when workbench selection changes,
	 * and the change was made by some other view.
	 */
	public void workbenchSelectionChanged(ISelection selection);

	/** Adds external listener for selection change events. */
	public void addSelectionChangedListener(ISelectionChangedListener listener);

	/** Removes external listener for selection change events. */
	public void removeSelectionChangedListener(ISelectionChangedListener listener);

	/** Gets current externally-visible selection. */
	public ISelection getSelection();

	// --- events ---

	/** Adds listener for viewer events. */
	public void addVisualizerViewerListener(IVisualizerViewerListener listener);

	/** Removes listener for viewer events. */
	public void removeVisualizerViewerListener(IVisualizerViewerListener listener);

}
