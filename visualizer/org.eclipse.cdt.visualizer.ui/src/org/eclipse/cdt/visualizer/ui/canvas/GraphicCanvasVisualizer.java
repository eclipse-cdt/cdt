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

package org.eclipse.cdt.visualizer.ui.canvas;

import org.eclipse.cdt.visualizer.ui.Visualizer;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


// ---------------------------------------------------------------------------
// GraphicCanvasVisualizer
// ---------------------------------------------------------------------------

/**
 * Viewer canvas -- base class for canvas that displays a collection
 * of persistent, repositionable graphic objects.
 * 
 * Note: painting is done in order objects were added,
 * so objects added last are drawn "on top" of others.
 * Use raise/lower methods to change the object z-ordering, if needed.
 */
public class GraphicCanvasVisualizer extends Visualizer
{
	// --- members ---


	// --- constructors/destructors ---
	
	/** Constructor. */
	public GraphicCanvasVisualizer()
	{
		// TODO: internationalize these strings.
		super("canvas", "Canvas Visualizer", "Displays graphic representation of selection.");
	}
	
	/** Constructor specifying name and such information. */
	public GraphicCanvasVisualizer(String name, String displayName, String description) {
		super(name, displayName, description);
	}

	/** Dispose method. */
	public void dispose() {
		super.dispose();
	}

	
	// --- control management ---
	
	/** Creates and returns visualizer control on specified parent. */
	public Control createControl(Composite parent)
	{
		if (m_control == null) {
			GraphicCanvas canvas = createCanvas(parent);
			canvas.setMenu(parent.getMenu());
			setControl(canvas);
			initializeCanvas(canvas);
		}
		return getControl();
	}
	
	/** Invoked when visualizer control should be disposed. */
	public void disposeControl()
	{
		if (m_control != null) {
			disposeCanvas();
			m_control.dispose();
			setControl(null);
		}
	}
	
	
	// --- canvas management ---
	
	/** Creates and returns visualizer canvas control. */
	public GraphicCanvas createCanvas(Composite parent)
	{
		return new GraphicCanvas(parent);
	}

	/** Invoked when canvas control should be disposed. */
	public void disposeCanvas()
	{
		
	}
	
	/** Invoked after visualizer control creation,
	 *  to allow derived classes to do any initialization of canvas.
	 */
	protected void initializeCanvas(GraphicCanvas canvas)
	{
	}
	
	/** Gets downcast reference to canvas control. */
	public GraphicCanvas getCanvas()
	{
		return (GraphicCanvas) getControl();
	}
	

	// --- menu/toolbar management ---

	/** Invoked when visualizer is selected, to populate the toolbar. */
	public void populateToolBar(IToolBarManager toolBarManager)
	{}

	/** Invoked when visualizer is selected, to populate the toolbar's menu. */
	public void populateMenu(IMenuManager menuManager)
	{}

	
	// --- context menu handling ---
	
	/** Invoked when visualizer view's context menu is invoked, to populate it. */
	public void populateContextMenu(IMenuManager menuManager)
	{}
	
}
