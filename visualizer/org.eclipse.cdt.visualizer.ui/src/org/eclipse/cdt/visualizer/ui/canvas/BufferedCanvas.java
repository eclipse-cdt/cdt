/*******************************************************************************
 * Copyright (c) 2012, 2013 Tilera Corporation and others.
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
 *     Marc Dumais (Ericsson) - Bug 399281
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.canvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

// ---------------------------------------------------------------------------
// BufferedCanvas
// ---------------------------------------------------------------------------

/** Canvas control with double-buffering support. */
public class BufferedCanvas extends Canvas implements PaintListener, ControlListener {
	// --- members ---

	/** double-buffering image */
	protected Image m_doubleBuffer = null;

	/** buffer GC */
	protected GC m_doubleBufferGC = null;

	// --- constructors/destructors ---

	/** Constructor. */
	public BufferedCanvas(Composite parent) {
		super(parent, SWT.NO_BACKGROUND | // don't automatically clear background on paint event
				SWT.NO_REDRAW_RESIZE // don't automatically repaint on resize event
		);
		initBufferedCanvas();
	}

	/** Dispose method. */
	@Override
	public void dispose() {
		super.dispose();
		cleanupBufferedCanvas();
	}

	// --- init methods ---

	/** Initializes control. */
	protected void initBufferedCanvas() {
		addControlListener(this);
		addPaintListener(this);
	}

	/** Cleans up control. */
	protected void cleanupBufferedCanvas() {
		if (!this.isDisposed()) {
			removePaintListener(this);
			removeControlListener(this);
		}
		if (m_doubleBuffer != null) {
			m_doubleBuffer.dispose();
			m_doubleBuffer = null;
		}
	}

	// --- event handlers ---

	/** Invoked when control is moved/resized */
	@Override
	public void controlMoved(ControlEvent e) {
		// do nothing, we don't care
	}

	/** Invoked when control is resized */
	@Override
	public void controlResized(ControlEvent e) {
		resized(getBounds());
	}

	// --- resize methods ---

	/** Invoked when control is resized.
	 *  Default implementation does nothing,
	 *  intended to be overridden by derived types.
	 */
	public void resized(Rectangle bounds) {

	}

	// --- GC management ---

	/** Gets/creates GC for current background buffer.
	 *  NOTE: The GC is disposed whenever the canvas size changes,
	 *  so caller should not retain a reference to this GC.
	 */
	protected synchronized GC getBufferedGC() {
		if (m_doubleBufferGC == null) {

			m_doubleBufferGC = new GC(m_doubleBuffer);
		}
		return m_doubleBufferGC;
	}

	/** Disposes of current background buffer GC. */
	protected synchronized void disposeBufferedGC() {
		if (m_doubleBufferGC != null) {
			m_doubleBufferGC.dispose();
			m_doubleBufferGC = null;
		}
	}

	// --- paint methods ---

	/** Invoked when control needs to be repainted */
	@Override
	public void paintControl(PaintEvent e) {
		// Handle last paint event of a cluster.
		if (e.count <= 1) {
			Display display = e.display;
			GC gc = e.gc;
			paintDoubleBuffered(display, gc);
		}
	}

	/** Internal -- handles double-buffering support, calls paintCanvas() */
	// NOTE: need display to create image buffer, not for painting code
	protected void paintDoubleBuffered(Display display, GC gc) {
		// get/create background image buffer
		Rectangle clientArea = getClientArea();
		int width = clientArea.width;
		int height = clientArea.height;
		if (m_doubleBuffer == null || m_doubleBuffer.getBounds().width < width
				|| m_doubleBuffer.getBounds().height < height) {
			m_doubleBuffer = new Image(display, width, height);
			disposeBufferedGC();
		}

		// create graphics context for buffer
		GC bgc = getBufferedGC();

		// copy current GC properties into it as defaults
		bgc.setBackground(gc.getBackground());
		bgc.setForeground(gc.getForeground());
		bgc.setFont(gc.getFont());
		bgc.setAlpha(255);

		// invoke paintCanvas() method to paint into the buffer
		try {
			paintCanvas(bgc);
		} catch (Throwable t) {
			// Throwing an exception in painting code can hang Eclipse,
			// so catch any exceptions here.
			System.err.println("BufferedCanvas: Exception thrown in painting code: \n" + t);
		}

		// then copy image buffer to actual canvas (reduces repaint flickering)
		gc.drawImage(m_doubleBuffer, 0, 0);
	}

	/** Invoked when canvas repaint event is raised.
	 *  Default implementation clears canvas to background color.
	 */
	public void paintCanvas(GC gc) {
		clearCanvas(gc);
	}

	/** Clears canvas to background color. */
	public void clearCanvas(GC gc) {
		Rectangle bounds = getClientArea();
		gc.fillRectangle(bounds);
	}

	// --- update methods ---

	/** Redraws control */
	@Override
	public void update() {
		// guard against update events that happen
		// after app has shut down
		if (!isDisposed()) {
			redraw();
		}
	}
}
