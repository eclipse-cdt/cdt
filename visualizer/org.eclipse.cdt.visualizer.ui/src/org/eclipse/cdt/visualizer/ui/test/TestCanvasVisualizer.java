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

package org.eclipse.cdt.visualizer.ui.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvasVisualizer;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.jface.viewers.ISelection;


// ---------------------------------------------------------------------------
// TestCanvasVisualizer
// ---------------------------------------------------------------------------

/**
 * Default visualizer, used only for testing framework.
 * 
 * This view uses the CDT Visualizer framework.
 */
public class TestCanvasVisualizer extends GraphicCanvasVisualizer
{	
	// --- constants ---
	
	/** Eclipse ID for this visualizer */
	public static final String ECLIPSE_ID = "org.eclipse.cdt.visualizer.ui.test.TestCanvasVisualizer";

	
	// --- members ---
	
	/** Visualizer control. */
	TestCanvas m_canvas = null;
	

	// --- constructors/destructors ---
	
	
	// --- accessors ---
	
	/** Gets canvas control. */
	public TestCanvas getDefaultCanvas()
	{
		return m_canvas;
	}

	
	// --- IVisualizer implementation ---
	
	/** Returns non-localized unique name for this visualizer. */
	public String getName() {
		return "default";
	}
	
	/** Returns localized name to display for this visualizer. */
	public String getDisplayName() {
		// TODO: use a string resource here.
		return "Test Visualizer";
	}
	
	/** Returns localized tooltip text to display for this visualizer. */
	public String getDescription() {
		// TODO: use a string resource here.
		return "Test visualizer (for debugging only).";
	}
	
	/** Creates and returns visualizer canvas control. */
	public GraphicCanvas createCanvas(Composite parent)
	{
		m_canvas = new TestCanvas(parent);
		return m_canvas;
	}
	
	/** Invoked after visualizer control creation, */
	protected void initializeCanvas(GraphicCanvas canvas)
	{
		m_canvas.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		m_canvas.setForeground(canvas.getDisplay().getSystemColor(SWT.COLOR_BLACK));
	}


	// --- workbench selection management ---

    /**
     * Tests whether if the IVisualizer can display the selection
     * (or something reachable from it).
     */
	public int handlesSelection(ISelection selection)
	{
		// By default, we don't support anything.
		// Changing this to return 1 enables the test canvas.
		return 0;
	}

    /**
     * Invoked by VisualizerViewer when workbench selection changes.
     */
	public void workbenchSelectionChanged(ISelection selection)
	{
		String text = SelectionUtils.toString(selection);
		m_canvas.setText(text);
		m_canvas.redraw();
	}
}
