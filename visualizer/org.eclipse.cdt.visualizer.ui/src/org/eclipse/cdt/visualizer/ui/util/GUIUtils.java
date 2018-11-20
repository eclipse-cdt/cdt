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
 *     Xavier Raynaud (Kalray) - Bug 431690
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

//---------------------------------------------------------------------------
// GUIUtils
//---------------------------------------------------------------------------

/**
 * Assorted high-level UI utilities.
 */
public class GUIUtils {

	// --- display methods ---

	/** Gets current SWT display. */
	static public Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	// --- delayed execution methods ---

	/** Posts the specified runnable for execution by the UI thread.
	 *  Nonblocking, returns immediately.
	 */
	static public void exec(Runnable runnable) {
		Display display = getDisplay();
		exec(display, runnable, false);
	}

	/** Posts the specified runnable for execution by the UI thread.
	 *  Nonblocking, returns immediately.
	 */
	static public void exec(Runnable runnable, boolean blocking) {
		Display display = getDisplay();
		exec(display, runnable, blocking);
	}

	/** Posts the specified runnable for execution by the UI thread.
	 *  If blocking is true, waits for completion, otherwise returns immediately.
	 */
	static public void execAndWait(Runnable runnable) {
		Display display = getDisplay();
		exec(display, runnable, true);
	}

	/** Posts the specified runnable for execution by the UI thread.
	 *  If blocking is true, waits for completion, otherwise returns immediately.
	 */
	static protected void exec(Display display, Runnable runnable, boolean blocking) {
		if (display != null && runnable != null) {
			if (blocking) {
				display.syncExec(runnable);
			} else {
				display.asyncExec(runnable);
			}
		}
	}

	// --- drawing methods ---

	/** Draws transparent text, with the default alignment (top/left). */
	static public void drawText(GC gc, String text, int x, int y) {
		gc.drawText(text, x, y, SWT.DRAW_TRANSPARENT);
	}

	/** Draws transparent text, with the default alignment (top/left).
	 * @since 1.1*/
	static public void drawText(GC gc, String text, Rectangle clip, int x, int y) {
		Rectangle oldClip = gc.getClipping();
		gc.setClipping(clip);
		drawText(gc, text, x, y);
		gc.setClipping(oldClip);
	}

	/** Draws transparent text, with the specified alignments. */
	static public void drawTextAligned(GC gc, String text, int x, int y, boolean left, boolean top) {
		if (left && top) {
			gc.drawText(text, x, y, SWT.DRAW_TRANSPARENT);
		} else {
			Point te = gc.textExtent(text);
			int dx = left ? 0 : te.x;
			int dy = top ? 0 : te.y;
			gc.drawText(text, x - dx, y - dy, SWT.DRAW_TRANSPARENT);
		}
	}

	/** Draws transparent text, with the specified alignments.
	 * @since 1.1*/
	static public void drawTextAligned(GC gc, String text, Rectangle clip, int x, int y, boolean left, boolean top) {
		Rectangle oldClip = gc.getClipping();
		gc.setClipping(clip);
		drawTextAligned(gc, text, x, y, left, top);
		gc.setClipping(oldClip);
	}

	/** Draws transparent text, centered on the specified point. */
	static public void drawTextCentered(GC gc, String text, int x, int y) {
		Point te = gc.textExtent(text);
		// Rounding produces less "jumpy" display when graphics are resized.
		gc.drawText(text, x - (int) Math.round(te.x / 2.0), y - (int) Math.round(te.y / 2.0), SWT.DRAW_TRANSPARENT);
	}

	/** Draws transparent text, centered on the specified point.
	 * @since 1.1*/
	static public void drawTextCentered(GC gc, String text, Rectangle clip, int x, int y) {
		Rectangle oldClip = gc.getClipping();
		gc.setClipping(clip);
		drawTextCentered(gc, text, x, y);
		gc.setClipping(oldClip);
	}

	// --- graphic methods ---

	/** Shrinks rectangle by specified margin on all edges. */
	public static void inset(Rectangle rect, int margin) {
		rect.x += margin;
		rect.y += margin;
		rect.width -= margin * 2;
		if (rect.width < 0)
			rect.width = 0;
		rect.height -= margin * 2;
		if (rect.height < 0)
			rect.height = 0;
	}
}
