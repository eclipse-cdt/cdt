/*******************************************************************************
 * Copyright (c) 2014 Mentor Graphics and others.
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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class FlatRadioButton extends FlatButton {

	protected static final String STATE_OFF_LABEL = "OFF"; //$NON-NLS-1$
	protected static final String STATE_ON_LABEL = "ON"; //$NON-NLS-1$

	protected boolean selection = false;

	public FlatRadioButton(Composite parent, int flags) {
		super(parent, flags);
	}

	@Override
	public Point computeSize(int wHint, int hHint) {

		GC gc = new GC(this);
		if (fText == null || fText.isEmpty()) {
			Point e = gc.textExtent("A"); //$NON-NLS-1$
			return new Point(0, e.y + fMargin * 2);
		}

		Point e = gc.textExtent(fText);
		Point pOn = gc.textExtent(STATE_ON_LABEL);
		Point pOff = gc.textExtent(STATE_OFF_LABEL);
		int h = Math.max(pOn.y, pOff.y);
		return new Point(e.x + fMargin * 4 + Math.max(pOn.x, pOff.x), Math.max(e.y, h) + fMargin * 2);
	}

	@Override
	protected void onPaint(PaintEvent event) {
		GC gc = event.gc;
		Rectangle ca = getClientArea();

		Color mainColor = gc.getDevice().getSystemColor(SWT.COLOR_LIST_SELECTION);
		gc.setBackground(mainColor);
		float[] mainHSB = mainColor.getRGB().getHSB();
		float h = mainHSB[0];
		float s = mainHSB[1];
		float b = mainHSB[2];

		Point e = gc.textExtent(fText);
		Color borderColor = new Color(gc.getDevice(), new RGB(h, s, (float) (b * 0.7)));
		Color shadowColor = new Color(gc.getDevice(), new RGB(h, s, (float) (b * 0.5)));

		gc.setForeground(borderColor);
		gc.drawRectangle(0, 0, ca.width - 1, ca.height - 1);

		gc.setForeground(shadowColor);
		gc.drawLine(0, ca.height - 1, ca.width - 1, ca.height - 1);

		gc.fillRectangle(e.x + fMargin * 2, 1, ca.width - 1 - e.x - fMargin * 2, ca.height - 2);

		gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		gc.drawText(fText, fMargin, fMargin, true);

		gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
		Point pOn = gc.textExtent(STATE_ON_LABEL);
		Point pOff = gc.textExtent(STATE_OFF_LABEL);
		if (selection) {
			int offset = pOn.x < pOff.x ? (pOff.x - pOn.x) / 2 : 0;
			gc.drawText(STATE_ON_LABEL, e.x + fMargin * 3 + offset, fMargin);
		} else {
			int offset = pOn.x < pOff.x ? 0 : (pOn.x - pOff.x) / 2;
			gc.drawText(STATE_OFF_LABEL, e.x + fMargin * 3 + offset, fMargin);
		}

		borderColor.dispose();
		shadowColor.dispose();
	}

	@Override
	protected void onSelection(MouseEvent e) {
		if (isEnabled()) {
			selection = !selection;
			notifyListeners(SWT.Selection, null);
			redraw();
		}
	}

	public void setSelection(boolean selection) {
		this.selection = selection;
	}

	public boolean getSelection() {
		return selection;
	}

}
