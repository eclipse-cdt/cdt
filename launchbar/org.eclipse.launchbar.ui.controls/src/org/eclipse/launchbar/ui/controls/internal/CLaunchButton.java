/*******************************************************************************
 * Copyright (c) 2022 Mat Booth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TypedListener;

/**
 * Customised toolbar item button for the build/launch/stop launch bar buttons.
 */
public class CLaunchButton extends Canvas {

	private Image image;

	// Mouse states
	private boolean hover;
	private boolean pressed;

	public CLaunchButton(Composite parent, int style) {
		super(parent, style);

		addPaintListener(e -> {
			if (pressed && hover) {
				e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			} else if (hover) {
				e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
			} else {
				e.gc.setBackground(getBackground());
			}
			int arc = 3;
			Point size = getSize();
			e.gc.fillRoundRectangle(0, 0, size.x - 1, size.y - 1, arc, arc);
			if (image != null) {
				e.gc.drawImage(image, 10, 5);
			}
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			e.gc.drawRoundRectangle(0, 0, size.x - 1, size.y - 1, arc, arc);
		});

		addMouseMoveListener(e -> {
			// We don't get enter and exit events whilst a mouse button is down i.e., during click-drag,
			// so we also need to track the hover state here because during a click-drag we still get
			// move events even when the pointer is not over the control
			if (e.getSource() instanceof Control button) {
				Point size = button.getSize();
				if (e.x >= 0 && e.x < size.x && e.y >= 0 && e.y < size.y) {
					hover = true;
				} else {
					hover = false;
				}
				redraw();
			}
		});
		addMouseTrackListener(MouseTrackListener.mouseEnterAdapter(e -> {
			hover = true;
			redraw();
		}));
		addMouseTrackListener(MouseTrackListener.mouseExitAdapter(e -> {
			hover = false;
			redraw();
		}));
		addMouseListener(MouseListener.mouseDownAdapter(e -> {
			pressed = true;
			redraw();
		}));
		addMouseListener(MouseListener.mouseUpAdapter(e -> {
			if (pressed && hover) {
				notifyListeners(SWT.Selection, null);
			}
			pressed = false;
			redraw();
		}));
	}

	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int width = 0;
		int height = 0;
		if (image != null) {
			Rectangle bounds = image.getBounds();
			width = bounds.width + 20;
			height = bounds.height + 10;
		}
		return new Point(width, height);
	}

	public void setImage(Image image) {
		this.image = image;
		redraw();
	}
}
