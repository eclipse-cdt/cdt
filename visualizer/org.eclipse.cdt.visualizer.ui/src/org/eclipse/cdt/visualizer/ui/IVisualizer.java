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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

// ----------------------------------------------------------------------------
// IVisualizer
// ----------------------------------------------------------------------------

/**
 * CDT Visualizer interface.
 *
 * An IVisualizer encapsulates a specific graphic presentation of the
 * currently selected object (launch, target, etc.).
 *
 * It knows how to create a Control to draw on, which is displayed by
 * the Visualizer Viewer, and also knows how to draw its presentation on
 * that control.
 *
 * A visualizer can be generic (e.g. knows how to draw any kind of
 * launch) or specific (e.g. specialized for a particular type of
 * launch or execution target). The viewer automatically chooses
 * the most specific IVisualizer that reports it is able to render
 * the current selection.
 */
public interface IVisualizer extends ISelectionProvider {
	// --- init methods ---

	/** Invoked when visualizer is created, to permit any initialization. */
	public void initializeVisualizer();

	/** Invoked when visualizer is disposed, to permit any cleanup. */
	public void disposeVisualizer();

	// --- accessors ---

	/** Sets non-localized name. */
	public void setName(String name);

	/** Gets non-localized name. */

	public String getName();

	/** Sets localized display name. */
	public void setDisplayName(String displayName);

	/** Gets localized display name. */
	public String getDisplayName();

	/** Sets localized description string. */
	public void setDescription(String description);

	/** Gets localized description string. */
	public String getDescription();

	// --- viewer management ---

	/** Called by viewer when visualizer is added to it. */
	public void setViewer(IVisualizerViewer viewer);

	/** Returns viewer control for this visualizer. */
	public IVisualizerViewer getViewer();

	// --- visualizer selection management ---

	/** Invoked when visualizer has been selected. */
	public void visualizerSelected();

	/** Invoked when another visualizer has been selected, hiding this one. */
	public void visualizerDeselected();

	// --- control management ---

	/** Creates and returns visualizer's UI control on specified parent control. */
	public Control createControl(Composite parent);

	/** Gets visualizer control.
	 *  Returns null if createControl() has not yet been called.
	 */
	public Control getControl();

	// --- menu/toolbar management ---

	/**
	 * Invoked when visualizer is selected, to populate the toolbar.
	 * The toolbar starts in a completely cleared state.
	 * The Visualizer can add/edit actions as desired.
	 * There is no need to invoke update on the toolbar, the viewer handles that.
	 */
	public void populateToolBar(IToolBarManager toolBarManager);

	/**
	 * Invoked when visualizer is selected, to populate the toolbar's menu.
	 * The toolbar starts in a completely cleared state.
	 * The Visualizer can add/edit actions as desired.
	 * There is no need to invoke update on the toolbar, the viewer handles that.
	 */
	public void populateMenu(IMenuManager menuManager);

	/**
	 * Invoked when visualizer view's context menu is invoked, to populate it.
	 * The toolbar starts in a completely cleared state.
	 * The Visualizer can add/edit actions as desired.
	 * There is no need to invoke update on the toolbar, the viewer handles that.
	 */
	public void populateContextMenu(IMenuManager menuManager);

	// --- workbench selection management ---

	/**
	 * Tests whether if the IVisualizer can display the selection
	 * (or something reachable from it).
	 *
	 * Returns a positive "weight" if true, and zero otherwise.
	 * If multiple visualizers can handle a given selection,
	 * the one reporting the highest weight value "wins".
	 * In case of ties, an arbitrary visualizer is selected.
	 *
	 * The weight should reflect the specificity of the visualizer;
	 * in other words, a "default" visualization for a given selection
	 * should have a low weight, and a special case should have
	 * a higher weight; this allows the visualizer view to "fall back"
	 * to the default visualization in the general case.
	 */
	public int handlesSelection(ISelection selection);

	/**
	 * Invoked by VisualizerViewer when workbench selection changes.
	 */
	public void workbenchSelectionChanged(ISelection selection);

	// --- selection changed event handling ---

	/** Adds external listener for selection change events. */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener);

	/** Removes external listener for selection change events. */
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener);

	/** Gets current externally-visible selection. */
	@Override
	public ISelection getSelection();

	/** Sets current externally-visible selection. */
	@Override
	public void setSelection(ISelection selection);

}
