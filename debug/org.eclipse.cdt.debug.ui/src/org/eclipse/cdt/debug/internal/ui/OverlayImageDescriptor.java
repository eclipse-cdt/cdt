/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

import java.util.Arrays;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

public class OverlayImageDescriptor extends CompositeImageDescriptor
{
	static final int DEFAULT_WIDTH = 16;
	static final int DEFAULT_HEIGHT = 16;

	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_LEFT = 2;
	public static final int BOTTOM_RIGHT = 3;

	// the base image
	private Image fBase;

	// the overlay images
	private ImageDescriptor[] fOverlays;

	// the size
	private Point fSize;

	public OverlayImageDescriptor( Image base, ImageDescriptor[] overlays )
	{
		fBase = base;
		fOverlays = overlays;
		fSize = new Point( DEFAULT_WIDTH, DEFAULT_HEIGHT );
	}

	public OverlayImageDescriptor( Image base, ImageDescriptor[] overlays, Point size )
	{
		fBase = base;
		fOverlays = overlays;
		fSize = size;
	}

	/**
	 * Draw the fOverlays for the reciever.
	 */
	protected void drawOverlays( ImageDescriptor[] overlays )
	{
		Point size = getSize();

		for ( int i = 0; i < overlays.length; i++ )
		{
			ImageDescriptor overlay = overlays[i];
			if ( overlay == null )
				continue;
			ImageData overlayData = overlay.getImageData();
			//Use the missing descriptor if it is not there.
			if ( overlayData == null )
				overlayData = ImageDescriptor.getMissingImageDescriptor().getImageData();
			switch( i )
			{
				case TOP_LEFT:
					drawImage( overlayData, 0, 0 );
					break;
				case TOP_RIGHT:
					drawImage( overlayData, size.x - overlayData.width, 0 );
					break;
				case BOTTOM_LEFT:
					drawImage( overlayData, 0, size.y - overlayData.height );
					break;
				case BOTTOM_RIGHT:
					drawImage( overlayData, size.x - overlayData.width, size.y - overlayData.height );
					break;
			}
		}
	}

	public boolean equals( Object o )
	{
		if ( !( o instanceof OverlayImageDescriptor ) )
			return false;
		OverlayImageDescriptor other = (OverlayImageDescriptor)o;
		return fBase.equals( other.fBase ) && Arrays.equals( fOverlays, other.fOverlays );
	}

	public int hashCode()
	{
		int code = fBase.hashCode();
		for ( int i = 0; i < fOverlays.length; i++ )
		{
			if ( fOverlays[i] != null )
				code ^= fOverlays[i].hashCode();
		}
		return code;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	protected void drawCompositeImage( int width, int height )
	{
		drawImage( fBase.getImageData(), 0, 0 );
		drawOverlays( fOverlays );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	protected Point getSize()
	{
		return fSize;
	}

}
