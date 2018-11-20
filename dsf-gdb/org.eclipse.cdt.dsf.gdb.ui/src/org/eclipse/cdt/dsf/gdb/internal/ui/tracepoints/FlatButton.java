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
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *     Dmitry Kozlov (Mentor Graphics) - extend to be inheritance-friendly
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TypedListener;

public class FlatButton extends Canvas {

	protected String fText;
	protected int fMargin = 4;
	protected Cursor fHandCursor;

	public FlatButton(Composite parent, int flags) {
		super(parent, flags);

		fHandCursor = getDisplay().getSystemCursor(SWT.CURSOR_HAND);
		setCursor(fHandCursor);

		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				onPaint(e);
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				onSelection(e);
			}
		});
	}

	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}

	public void setText(String text) {
		fText = text;
	}

	public String getText() {
		return fText;
	}

	public void setMargin(int margin) {
		fMargin = margin;
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return computeSize(wHint, hHint);
	}

	@Override
	public Point computeSize(int wHint, int hHint) {

		GC gc = new GC(this);
		if (fText == null || fText.isEmpty()) {
			Point e = gc.textExtent("A"); //$NON-NLS-1$
			return new Point(0, e.y + fMargin * 2);
		}

		Point e = gc.textExtent(fText);
		return new Point(e.x + fMargin * 2, e.y + fMargin * 2);
	}

	protected void onPaint(PaintEvent event) {
		GC gc = event.gc;
		Rectangle ca = getClientArea();

		Color mainColor = gc.getDevice().getSystemColor(SWT.COLOR_LIST_SELECTION);
		gc.setBackground(mainColor);
		float[] mainHSB = mainColor.getRGB().getHSB();
		float h = mainHSB[0];
		float s = mainHSB[1];
		float b = mainHSB[2];

		Color borderColor = new Color(gc.getDevice(), new RGB(h, s, (float) (b * 0.7)));
		Color shadowColor = new Color(gc.getDevice(), new RGB(h, s, (float) (b * 0.5)));

		gc.setForeground(borderColor);
		gc.drawRectangle(0, 0, ca.width - 1, ca.height - 1);

		gc.setForeground(shadowColor);
		gc.drawLine(0, ca.height - 1, ca.width - 1, ca.height - 1);

		gc.fillRectangle(1, 1, ca.width - 2, ca.height - 2);

		gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
		gc.drawText(fText, fMargin, fMargin, true);

		borderColor.dispose();
		shadowColor.dispose();
	}

	protected void onSelection(MouseEvent e) {
		if (isEnabled()) {
			notifyListeners(SWT.Selection, null);
		}
	}
}
