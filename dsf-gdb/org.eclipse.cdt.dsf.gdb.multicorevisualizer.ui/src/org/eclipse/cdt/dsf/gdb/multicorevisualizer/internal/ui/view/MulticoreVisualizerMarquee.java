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
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * MulticoreVisualizer "marquee" (selection box) object.
 */
public class MulticoreVisualizerMarquee extends MulticoreVisualizerGraphicObject {
	// --- members ---

	// --- constructors/destructors ---

	/** Constructor */
	public MulticoreVisualizerMarquee() {
		setVisible(false);
	}

	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
	}

	// --- Object methods ---

	/** Returns string representation of element */
	@Override
	public String toString() {
		return "MarqueeGraphicObject[" + //$NON-NLS-1$
				m_bounds.x + "," + //$NON-NLS-1$
				m_bounds.y + "," + //$NON-NLS-1$
				m_bounds.width + "," + //$NON-NLS-1$
				m_bounds.height + "]"; //$NON-NLS-1$
	}

	// --- accessors ---

	// --- methods ---

	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		if (m_visible) {
			Color bg = Colors.BLACK;
			Color fg = IMulticoreVisualizerConstants.COLOR_SELECTED;
			gc.setBackground(bg);
			gc.setForeground(fg);
			gc.drawRectangle(m_bounds);
		}
	}
}
