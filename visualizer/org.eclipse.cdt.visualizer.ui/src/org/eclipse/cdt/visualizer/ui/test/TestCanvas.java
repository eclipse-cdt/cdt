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

import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;


// ---------------------------------------------------------------------------
// TestCanvas
// ---------------------------------------------------------------------------

/**
 * Default canvas control, used by TestCanvasVisualizer.
 */
public class TestCanvas extends GraphicCanvas
{
	// --- members ---
	
	/** Text string to display. */
	String m_text = null;

	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public TestCanvas(Composite parent) {
		super(parent);
		initDefaultCanvas(parent);
	}
	
	/** Dispose method. */
	public void dispose() {
		cleanupDefaultCanvas();
		super.dispose();
	}

	
	// --- init methods ---
	
	/** Initializes control */
	protected void initDefaultCanvas(Composite parent) {
		// perform any initialization here
	}
	
	/** Cleans up control */
	protected void cleanupDefaultCanvas() {
	}

	
	// --- accessors ---
	
	/** Sets text string to display. */
	public void setText(String text)
	{
		m_text = text;
	}

	
	// --- methods ---
	
	/** Invoked when canvas repaint event is raised.
	 *  Default implementation clears canvas to background color.
	 */
	public void paintCanvas(GC gc) {
		super.paintCanvas(gc);

		int margin = 10;
		drawStringWrapNewlines(gc, m_text, margin, margin);
	}

	
	// --- utilities ---
	
	/** Gets line height of text, based on current font */
	public static int getTextHeight(GC gc) {
		return gc.getFontMetrics().getHeight();
	}
	
	/** Draw string, wrapping if there are any newline chars. */
	public static void drawStringWrapNewlines(GC gc, String text, int x, int y) {
		int lineHeight = getTextHeight(gc);
		drawStringWrapNewlines(gc, text, x, y, lineHeight);
	}
	
	/** Draw string, wrapping if there are any newline chars. */
	public static void drawStringWrapNewlines(GC gc, String text, int x, int y, int lineHeight) {
		if (text != null) {
			String[] lines = text.split("\n");
			for (int i=0; i<lines.length; i++) {
				gc.drawString(lines[i], x, y, true); // transparent
				y += lineHeight;
			}
		}
	}
}
