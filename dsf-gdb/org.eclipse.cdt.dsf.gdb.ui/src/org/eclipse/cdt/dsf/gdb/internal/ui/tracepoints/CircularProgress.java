/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	
	private static final String PERCENT_SIGN = "%";  //$NON-NLS-1$
	
	final protected int[] fBufferProgressMeasure = {0};

	public CircularProgress(Composite parent, int flags)
	{
		super(parent, flags);

		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				onPaint(e);
			}
		});

		GC gc = new GC(this);
		Point e = gc.textExtent(PERCENT_SIGN); 
		setBounds(0, 0, e.x * 5, e.x * 5);
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
		int margin = 2;
		int width = 8;
		e.gc.setBackground(getParent().getBackground());
		e.gc.fillRectangle(0, 0, clientArea.width, clientArea.height);
		e.gc.setBackground(getParent().getDisplay().getSystemColor(SWT.COLOR_GRAY));
		e.gc.fillOval(margin, margin, clientArea.width-2*margin, clientArea.height-2*margin);
		e.gc.setBackground(getParent().getBackground());
		e.gc.fillOval(margin+width, margin+width, clientArea.width-2*(margin+width), clientArea.height-2*(margin+width));
		int n = (int) (fBufferProgressMeasure[0] * 3.6);
		e.gc.setBackground(getParent().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
		e.gc.fillArc(margin, margin, clientArea.width-2*margin, clientArea.height-2*margin, 90 , -n );
		e.gc.setBackground(getParent().getBackground());
		e.gc.fillOval(margin+width, margin+width, clientArea.width-2*(margin+width), clientArea.width-2*(margin+width));

		// Progress % in the text form
		e.gc.setForeground(getParent().getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		e.gc.setFont(getParent().getDisplay().getSystemFont());
		String progress = fBufferProgressMeasure[0] + PERCENT_SIGN;
		Point p = e.gc.textExtent(progress);
		e.gc.drawText(progress,(clientArea.width - p.x)/2+3,(clientArea.height-p.y)/2+1);
	}
}
