/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

/**
 * Maintains a cache of overlay images.
 * 
 * @since May 30, 2003
 */
public class OverlayImageCache {
	private Map<OverlayImageDescriptor, Image> fCache = new HashMap<OverlayImageDescriptor, Image>();

	/**
	 * Returns and caches an image corresponding to the specified image
	 * descriptor.
	 * 
	 * @param imageDecsriptor
	 *            the image descriptor
	 * @return the image
	 */
	public Image getImageFor(OverlayImageDescriptor imageDescriptor) {
		Image image = fCache.get(imageDescriptor);
		if (image == null) {
			image = imageDescriptor.createImage();
			fCache.put(imageDescriptor, image);
		}
		return image;
	}

	/**
	 * Disposes of all images in the cache.
	 */
	public void disposeAll() {
		for (Image image : fCache.values()) {
			image.dispose();
		}
		fCache.clear();
	}
}
