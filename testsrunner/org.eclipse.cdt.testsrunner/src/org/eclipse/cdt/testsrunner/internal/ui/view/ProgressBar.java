/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view;

import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * A progress bar with a red/green indication for testing success or failure.
 */
public class ProgressBar extends Canvas {

	/** Default bar width */
	private static final int DEFAULT_WIDTH = 160;

	/** Default bar height */
	private static final int DEFAULT_HEIGHT = 18;

	/** Testing session to show progress bar for. */
	private ITestingSession testingSession;

	/** Current bar width. */
	private int colorBarWidth;

	/** The bar color when everything is OK (no tests failed and no testing errors). */
	private Color okColor;

	/** The bar color when there are tests failed and or testing errors. */
	private Color failureColor;

	/** The bar color when the testing session was stopped by user. */
	private Color stoppedColor;

	public ProgressBar(Composite parent, ITestingSession testingSession) {
		super(parent, SWT.NONE);

		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				updateInfoFromSession();
			}
		});
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				paint(e);
			}
		});

		// Manage progress bar colors
		Display display = parent.getDisplay();
		failureColor = new Color(display, 159, 63, 63);
		okColor = new Color(display, 95, 191, 95);
		stoppedColor = new Color(display, 120, 120, 120);
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				failureColor.dispose();
				okColor.dispose();
				stoppedColor.dispose();
			}
		});
		setTestingSession(testingSession);
	}

	/**
	 * Sets the testing session to show information about.
	 *
	 * @param testingSession testing session (null is not acceptable)
	 */
	public void setTestingSession(ITestingSession testingSession) {
		this.testingSession = testingSession;
		updateInfoFromSession();
	}

	/**
	 * Updates the progress from the currently set testing session.
	 */
	public void updateInfoFromSession() {
		recalculateColorBarWidth();
		redraw();
	}

	/**
	 * Sets the color of the progress bar depending on the testing session.
	 *
	 * @param gc gc
	 */
	private void setStatusColor(GC gc) {
		if (testingSession.wasStopped())
			gc.setBackground(stoppedColor);
		else if (testingSession.hasErrors())
			gc.setBackground(failureColor);
		else
			gc.setBackground(okColor);
	}

	/**
	 * Calculate the width of the progress rectangle in a widget.
	 *
	 * @note If total tests count is known it is used to determine width of the
	 * progress rectangle. If it isn't the width of progress rectangle is set to
	 * the half of a widget.
	 */
	private void recalculateColorBarWidth() {
		Rectangle r = getClientArea();
		int newColorBarWidth;
		if (testingSession.getTotalCounter() > 0) {
			newColorBarWidth = testingSession.getCurrentCounter() * (r.width - 2) / testingSession.getTotalCounter();
		} else {
			newColorBarWidth = testingSession.getCurrentCounter() > 0 ? (r.width - 2) / 2
					: (testingSession.isFinished() ? r.width - 2 : 0);
		}
		colorBarWidth = Math.max(0, newColorBarWidth);
	}

	/**
	 * Draws the widget border
	 */
	private void drawBevelRect(GC gc, int x, int y, int w, int h, Color topleft, Color bottomright) {
		gc.setForeground(topleft);
		gc.drawLine(x, y, x + w - 1, y);
		gc.drawLine(x, y, x, y + h - 1);

		gc.setForeground(bottomright);
		gc.drawLine(x + w, y, x + w, y + h);
		gc.drawLine(x, y + h, x + w, y + h);
	}

	/**
	 * Handles paint event and redraws the widget if necessary.
	 *
	 * @param event paint event
	 */
	private void paint(PaintEvent event) {
		GC gc = event.gc;
		Display disp = getDisplay();

		Rectangle rect = getClientArea();
		gc.fillRectangle(rect);
		drawBevelRect(gc, rect.x, rect.y, rect.width - 1, rect.height - 1,
				disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
				disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));

		setStatusColor(gc);
		colorBarWidth = Math.min(rect.width - 2, colorBarWidth);
		gc.fillRectangle(1, 1, colorBarWidth, rect.height - 2);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		Point size = new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		if (wHint != SWT.DEFAULT) {
			size.x = wHint;
		}
		if (hHint != SWT.DEFAULT) {
			size.y = hHint;
		}
		return size;
	}
}
