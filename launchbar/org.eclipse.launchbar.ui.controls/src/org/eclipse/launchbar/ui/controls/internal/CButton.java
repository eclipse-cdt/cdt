/*******************************************************************************
 * Copyright (c) 2014, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TypedListener;

public class CButton extends Canvas {
	private boolean inButton;
	private Image hotImage;
	private Image coldImage;

	public CButton(Composite parent, int style) {
		super(parent, style);
		addPaintListener(e -> {
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
		});
		addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				setSelected(true);
			}

			@Override
			public void mouseExit(MouseEvent e) {
				setSelected(false);
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				setSelected(true);
				handleSelection(inButton);
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				setSelected(true);
				handleDefaultSelection(inButton);
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

	protected void handleSelection(boolean selection) {
		// Send event
		notifyListeners(SWT.Selection, null);
	}

	protected void handleDefaultSelection(boolean selection) {
		// Send event
		notifyListeners(SWT.DefaultSelection, null);
	}

	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}

	public void setSelected(boolean sel) {
		inButton = sel;
		redraw();
	}

	public boolean isSelected() {
		return inButton;
	}
}
