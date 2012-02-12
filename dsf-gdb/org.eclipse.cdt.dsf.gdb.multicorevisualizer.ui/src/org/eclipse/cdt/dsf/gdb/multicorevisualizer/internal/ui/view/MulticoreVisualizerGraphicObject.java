/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import org.eclipse.cdt.visualizer.ui.canvas.GraphicObject;
import org.eclipse.swt.graphics.GC;

/**
 * Graphic object for MulticoreVisualizer.
 */
public class MulticoreVisualizerGraphicObject extends GraphicObject
{
	// --- members ---

	// --- constructors/destructors ---
	
	/** Constructor */
	public MulticoreVisualizerGraphicObject() {
	}
	
	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
	}

	
	// --- methods ---
	
	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		gc.fillRectangle(m_bounds);
		gc.drawRectangle(m_bounds);
	}
}
