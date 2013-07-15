/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.internal.remote.ui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * @since 7.0
 */
public class OverlayImageDescriptor extends CompositeImageDescriptor {

	static final int DEFAULT_WIDTH = 16;
	static final int DEFAULT_HEIGHT = 16;

	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_LEFT = 2;
	public static final int BOTTOM_RIGHT = 3;

	private final ImageDescriptor fBase;
	private final ImageDescriptor fOverlay;
	private final int fPosition;
	private final int fOffset = 3;

	public OverlayImageDescriptor(ImageDescriptor base, ImageDescriptor overlay, int pos) {
		fBase = base;
		fOverlay = overlay;
		fPosition = pos;
	}

	/*
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int,
	 * int)
	 */
	@Override
	protected void drawCompositeImage(int width, int height) {
		ImageData imageData = fBase.getImageData();
		if (imageData != null) {
			drawImage(imageData, 0, 0);
		}
		ImageData overlayData = fOverlay.getImageData();
		if (overlayData != null) {
			Point pos = null;
			switch (fPosition) {
			case TOP_LEFT:
				pos = new Point(-overlayData.width / 2, -overlayData.height / 2);
				break;
			case TOP_RIGHT:
				pos = new Point(imageData.width - overlayData.width / 2, 0);
				break;
			case BOTTOM_LEFT:
				pos = new Point(0, imageData.height - overlayData.height / 2);

				break;
			case BOTTOM_RIGHT:
				pos = new Point(imageData.width - overlayData.width / 2, imageData.height - overlayData.height / 2);
				break;
			}
			drawImage(overlayData, pos.x - fOffset, pos.y - fOffset);
		}
	}

	@Override
	protected Point getSize() {
		return new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
}
