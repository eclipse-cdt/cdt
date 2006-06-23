/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui;

import java.util.Arrays;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

public class OverlayImageDescriptor extends CompositeImageDescriptor {

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

	public OverlayImageDescriptor( Image base, ImageDescriptor[] overlays ) {
		this( base, overlays, new Point( DEFAULT_WIDTH, DEFAULT_HEIGHT ) );
	}

	public OverlayImageDescriptor( Image base, ImageDescriptor[] overlays, Point size ) {
		setBase( base );
		setOverlays( overlays );
		setSize( size );
	}

	/**
	 * Draw the fOverlays for the reciever.
	 */
	protected void drawOverlays(ImageDescriptor[] overlays) {
		Point size = getSize();

		for ( int i = 0; i < overlays.length; i++ ) {
			ImageDescriptor overlay = overlays[i];
			if ( overlay == null )
				continue;
			ImageData overlayData = overlay.getImageData();
			//Use the missing descriptor if it is not there.
			if ( overlayData == null )
				overlayData = ImageDescriptor.getMissingImageDescriptor().getImageData();
			switch( i ) {
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

	public boolean equals( Object o ) {
		if ( !(o instanceof OverlayImageDescriptor) )
			return false;
		OverlayImageDescriptor other = (OverlayImageDescriptor)o;
		return getBase().equals( other.getBase() ) && Arrays.equals( getOverlays(), other.getOverlays() );
	}

	public int hashCode() {
		int code = getBase().hashCode();
		for (int i = 0; i < getOverlays().length; i++) {
			if ( getOverlays()[i] != null )
				code ^= getOverlays()[i].hashCode();
		}
		return code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int,
	 *      int)
	 */
	protected void drawCompositeImage( int width, int height ) {
		drawImage( getBase().getImageData(), 0, 0 );
		drawOverlays( getOverlays() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	protected Point getSize() {
		return this.fSize;
	}

	private Image getBase() {
		return this.fBase;
	}

	private void setBase( Image base ) {
		this.fBase = base;
	}

	private ImageDescriptor[] getOverlays() {
		return this.fOverlays;
	}

	private void setOverlays( ImageDescriptor[] overlays ) {
		this.fOverlays = overlays;
	}

	private void setSize( Point size ) {
		this.fSize = size;
	}
}
