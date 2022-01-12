/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

public class CPListImageDescriptor extends CompositeImageDescriptor {

	/** Flag to render the waring adornment */
	public final static int WARNING = 0x1;

	/** Flag to render the inherited adornment */
	public final static int ERROR = 0x2;

	/** Flag to render the inherited adornment */
	public final static int PATH_INHERIT = 0x4;

	private ImageDescriptor fBaseImage;
	private int flags;
	private Point fSize;

	public CPListImageDescriptor(ImageDescriptor baseImage, int flags, Point size) {
		fBaseImage = baseImage;
		this.flags = flags;
		fSize = size;
	}

	/**
	 * @see CompositeImageDescriptor#getSize()
	 */
	@Override
	protected Point getSize() {
		if (fSize == null) {
			ImageData data = fBaseImage.getImageData(100);
			setSize(new Point(data.width, data.height));
		}
		return fSize;
	}

	/**
	 * @see Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof CPListImageDescriptor)) {
			return false;
		}

		CPListImageDescriptor other = (CPListImageDescriptor) object;
		return fBaseImage.equals(other.fBaseImage) && flags == other.flags && fSize.equals(other.fSize);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return fBaseImage.hashCode() & flags | fSize.hashCode();
	}

	/**
	 * @see CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	@Override
	protected void drawCompositeImage(int width, int height) {
		ImageData bg = fBaseImage.getImageData(100);
		if (bg == null) {
			bg = DEFAULT_IMAGE_DATA;
		}
		drawImage(bg, 0, 0);
		drawOverlays();
	}

	/**
	 * Add any overlays to the image as specified in the flags.
	 */
	protected void drawOverlays() {
		Point size = getSize();
		ImageData data = null;
		int x = getSize().x;
		if ((flags & PATH_INHERIT) == PATH_INHERIT) {
			data = CPluginImages.DESC_OVR_PATH_INHERIT.getImageData(100);
			drawImage(data, x, 0);
		}
		x = 0;
		if ((flags & ERROR) != 0) {
			data = CPluginImages.DESC_OVR_ERROR.getImageData(100);
			drawImage(data, x, size.y - data.height);
			x += data.width;
		}
		if ((flags & WARNING) != 0) {
			data = CPluginImages.DESC_OVR_WARNING.getImageData(100);
			drawImage(data, x, size.y - data.height);
			x += data.width;
		}
	}

	protected void setSize(Point size) {
		fSize = size;
	}

}
