/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

/**
 * Maintains a cache of overlay images.
 * 
 * @since May 30, 2003
 */
public class OverlayImageCache
{
	private Map cache = new HashMap();

	/**
	 * Returns and caches an image corresponding to the specified image descriptor.
	 * 
	 * @param imageDecsriptor the image descriptor 
	 * @return the image
	 */
	public Image getImageFor( OverlayImageDescriptor imageDescriptor )
	{
		Image image = (Image)cache.get( imageDescriptor );
		if ( image == null )
		{
			image = imageDescriptor.createImage();
			cache.put( imageDescriptor, image );
		}
		return image;
	}

	/**
	 * Disposes of all images in the cache.
	 */
	public void disposeAll()
	{
		for ( Iterator it = cache.values().iterator(); it.hasNext(); )
		{
			Image image = (Image)it.next();
			image.dispose();
		}
		cache.clear();
	}
}
