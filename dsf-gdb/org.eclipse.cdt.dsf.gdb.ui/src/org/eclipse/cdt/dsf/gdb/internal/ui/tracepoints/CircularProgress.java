/*******************************************************************************
 * Copyright (c) 2013, 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dmitry Kozlov (Mentor Graphics) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class CircularProgress extends Canvas {

	private static final String PERCENT_SIGN = "%"; //$NON-NLS-1$
	private static final String PERCENT_TEXT = "100%"; //$NON-NLS-1$
	private static final int PROGRESS_WIDTH = 8;
	private static final int PROGRESS_MARGIN = 2;
	private static final int PROGRESS_ARC = 15;
	final protected int[] fBufferProgressMeasure = { 0 };

	public CircularProgress(Composite parent, int flags) {
		super(parent, flags);

		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				onPaint(e);
			}
		});

		GC gc = new GC(this);
		Point e = gc.textExtent(PERCENT_TEXT);
		int size = e.x + 8 * PROGRESS_MARGIN + 2 * PROGRESS_WIDTH;
		setBounds(0, 0, size, size);
	}

	/**
	 * Set progress as number of percent (0-100)
	 */
	public void setProgress(int progress) {
		fBufferProgressMeasure[0] = progress;
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return computeSize(wHint, hHint);
	}

	@Override
	public Point computeSize(int wHint, int hHint) {
		return new Point(getBounds().width, getBounds().height);
	}

	private void onPaint(PaintEvent e) {
		Rectangle clientArea = getClientArea();
		int margin = PROGRESS_MARGIN;
		int width = PROGRESS_WIDTH;
		e.gc.setBackground(getParent().getBackground());
		e.gc.fillRectangle(0, 0, clientArea.width, clientArea.height);
		e.gc.setBackground(getParent().getDisplay().getSystemColor(SWT.COLOR_GRAY));
		e.gc.fillOval(margin, margin, clientArea.width - 2 * margin, clientArea.height - 2 * margin);
		e.gc.setBackground(getParent().getBackground());
		e.gc.fillOval(margin + width, margin + width, clientArea.width - 2 * (margin + width),
				clientArea.height - 2 * (margin + width));

		String progress;

		e.gc.setBackground(getParent().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
		if (0 <= fBufferProgressMeasure[0] && fBufferProgressMeasure[0] <= 100) {
			// Draw usual progress bar and text value in %
			progress = fBufferProgressMeasure[0] + PERCENT_SIGN;
			int n = (int) (fBufferProgressMeasure[0] * 3.6);
			e.gc.fillArc(margin, margin, clientArea.width - 2 * margin, clientArea.height - 2 * margin, 90, -n);
		} else {
			// Draw constantly moving progress without exact value and text value 100%
			progress = PERCENT_TEXT;
			int n = (int) ((fBufferProgressMeasure[0] % 100) * 3.6);
			// Fill in the full buffer first
			e.gc.fillOval(margin, margin, clientArea.width - 2 * margin, clientArea.height - 2 * margin);
			// Move progress bar within the buffer
			e.gc.setBackground(getParent().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
			e.gc.fillArc(margin, margin, clientArea.width - 2 * margin, clientArea.height - 2 * margin,
					90 - n - PROGRESS_ARC, -PROGRESS_ARC);
		}
		e.gc.setBackground(getParent().getBackground());
		e.gc.fillOval(margin + width, margin + width, clientArea.width - 2 * (margin + width),
				clientArea.width - 2 * (margin + width));

		// Progress % in the text form
		e.gc.setForeground(getParent().getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		e.gc.setFont(getParent().getDisplay().getSystemFont());
		Point p = e.gc.textExtent(progress);
		e.gc.drawText(progress, (clientArea.width - p.x) / 2 + 1, (clientArea.height - p.y) / 2);
	}
}
