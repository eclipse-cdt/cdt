/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.ui.internal.controls;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class CButton extends Canvas {

	private boolean inButton;
	private Image hotImage;
	private Image coldImage;
	
	public CButton(Composite parent, int style) {
		super(parent, style);
		
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				if (inButton) {
					if (hotImage != null) {
						e.gc.drawImage(hotImage, 0, 0);
					} else if (coldImage != null) {
						e.gc.drawImage(coldImage, 0, 0);
					}
				} else {
					if (coldImage != null) {
						e.gc.drawImage(coldImage, 0, 0);
					} else if (hotImage != null) {
						e.gc.drawImage(hotImage, 0, 0);
					}
				}
			}
		});
		
		addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				inButton = true;
				redraw();
			}
			@Override
			public void mouseExit(MouseEvent e) {
				inButton = false;
				redraw();
			}
		});
	}

	@Override
	public void dispose() {
		super.dispose();

		if (hotImage != null)
			hotImage.dispose();

		if (coldImage != null)
			coldImage.dispose();
	}
	
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int width = 0;
		int height = 0;
		if (hotImage != null) {
			Rectangle bounds = hotImage.getBounds();
			width = bounds.width;
			height = bounds.height;
		}
		if (coldImage != null) {
			Rectangle bounds = coldImage.getBounds();
			if (bounds.width > width)
				width = bounds.width;
			if (bounds.height > height)
				height = bounds.height;
		}
		return new Point(width, height);
	}
	
	public void setHotImage(Image image) {
		this.hotImage = image;
	}

	public void setColdImage(Image image) {
		this.coldImage = image;
	}

}
