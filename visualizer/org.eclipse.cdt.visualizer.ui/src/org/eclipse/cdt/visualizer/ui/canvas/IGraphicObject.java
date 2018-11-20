/*******************************************************************************
 * Copyright (c) 2012, 2014 Tilera Corporation and others.
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

package org.eclipse.cdt.visualizer.ui.canvas;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

// ---------------------------------------------------------------------------
// IGraphicObject
// ---------------------------------------------------------------------------

/**
 * An object that can be displayed and manipulated on a GraphicCanvas.
 */
public interface IGraphicObject {
	// --- methods ---

	/** Paints object using specified graphics context.
	 *  If decorations is false, draws ordinary object content.
	 *  If decorations is true, paints optional "decorations" layer.
	 */
	public void paint(GC gc, boolean decorations);

	/** Returns true if object has decorations to paint. */
	public boolean hasDecorations();

	/** Gets model data (if any) associated with this graphic object */
	public Object getData();

	/** Sets model data (if any) associated with this graphic object */
	public void setData(Object data);

	/** Whether graphic object contains the specified point. */
	public boolean contains(int x, int y);

	/** Returns true if element bounds are within specified rectangle. */
	public boolean isWithin(Rectangle region);
}
