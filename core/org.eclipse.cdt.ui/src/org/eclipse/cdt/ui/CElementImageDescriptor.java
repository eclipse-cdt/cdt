/*******************************************************************************
 * Copyright (c) 2000 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;


/**
 * A CImageDescriptor consists of a base image and several adornments. The adornments
 * are computed according to the flags either passed during creation or set via the method
 * <code>setAdornments</code>. 
 * </p>
 * It is guaranteed that objects that conform to this interface are also instances of type
 * <code>ImageDescriptor</code>
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development 
 * and expected to change before reaching stability.
 * </p>
 * 
 * @since 2.0 
 */
public class CElementImageDescriptor extends CompositeImageDescriptor {
	
	/** Flag to render the abstract adornment */
	public final static int TEMPLATE= 		0x001;
	
	/** Flag to render the const adornment */
	public final static int CONSTANT=		0x002;
	
	/** Flag to render the volatile adornment */
	public final static int VOLATILE=		0x004;
	
	/** Flag to render the static adornment */
	public final static int STATIC=			0x008;
	
	/** Flag to render the runnable adornment */
	public final static int RUNNABLE= 		0x010;
	
	/** Flag to render the waring adornment */
	public final static int WARNING=			0x020;
	
	/** Flag to render the error adornment */
	public final static int ERROR=			0x040;
	
	/** Flag to render the 'override' adornment */
	public final static int OVERRIDES= 		0x080;
	
	/** Flag to render the 'implements' adornment */
	public final static int IMPLEMENTS= 		0x100;		

	private ImageDescriptor fBaseImage;
	private int fFlags;
	private Point fSize;

	/**
	 * Create a new JavaElementImageDescriptor.
	 * 
	 * @param baseImage an image descriptor used as the base image
	 * @param flags flags indicating which adornments are to be rendered. See <code>setAdornments</code>
	 * 	for valid values.
	 * @param size the size of the resulting image
	 * @see #setAdornments(int)
	 */
	public CElementImageDescriptor(ImageDescriptor baseImage, int flags, Point size) {
		fBaseImage= baseImage;
		Assert.isNotNull(fBaseImage);
		fFlags= flags;
		Assert.isTrue(fFlags >= 0);
		fSize= size;
		Assert.isNotNull(fSize);
	}
	
	/**
	 * Sets the descriptors adornments. Valid values are: <code>ABSTRACT</code>, <code>FINAL</code>,
	 * <code>SYNCHRONIZED</code>, </code>STATIC<code>, </code>RUNNABLE<code>, </code>WARNING<code>, 
	 * </code>ERROR<code>, </code>OVERRIDDEN<code>, or any combination of those.
	 * 
	 * @param adornments the image descritpors adornments
	 */
	public void setAdornments(int adornments) {
		Assert.isTrue(adornments >= 0);
		fFlags= adornments;
	}

	/**
	 * Returns the current adornments.
	 * 
	 * @return the current adornments
	 */
	public int getAdronments() {
		return fFlags;
	}

	/**
	 * Sets the size of the image created by calling <code>createImage()</code>.
	 * 
	 * @param size the size of the image returned from calling <code>createImage()</code>
	 */
	public void setImageSize(Point size) {
		Assert.isNotNull(size);
		Assert.isTrue(size.x >= 0 && size.y >= 0);
		fSize= size;
	}
	
	/**
	 * Returns the size of the image created by calling <code>createImage()</code>.
	 * 
	 * @return the size of the image created by calling <code>createImage</code>
	 */
	public Point getImageSize() {
		return new Point(fSize.x, fSize.y);
	}
	
	/* (non-Javadoc)
	 * Method declared in CompositeImageDescriptor
	 */
	protected Point getSize() {
		return fSize;
	}
	
	/* (non-Javadoc)
	 * Method declared on Object.
	 */
	public boolean equals(Object object) {
		if (!CElementImageDescriptor.class.equals(object.getClass()))
			return false;
			
		CElementImageDescriptor other= (CElementImageDescriptor)object;
		return (fBaseImage.equals(other.fBaseImage) && fFlags == other.fFlags && fSize.equals(other.fSize));
	}
	
	/* (non-Javadoc)
	 * Method declared on Object.
	 */
	public int hashCode() {
		return fBaseImage.hashCode() | fFlags | fSize.hashCode();
	}
	
	/* (non-Javadoc)
	 * Method declared in CompositeImageDescriptor
	 */
	protected void drawCompositeImage(int width, int height) {
		ImageData bg;
		if ((bg= fBaseImage.getImageData()) == null)
			bg= DEFAULT_IMAGE_DATA;
			
		drawImage(bg, 0, 0);
		drawTopRight();
		drawBottomRight();
		drawBottomLeft();
	}	
	
	private void drawTopRight() {		
		int x= getSize().x;
		ImageData data= null;
		if ((fFlags & VOLATILE) != 0) {
			data= CPluginImages.DESC_OVR_VOLATILE.getImageData();
			x-= data.width;
			drawImage(data, x, 0);
		}
		if ((fFlags & CONSTANT) != 0) {
			data= CPluginImages.DESC_OVR_CONSTANT.getImageData();
			x-= data.width;
			drawImage(data, x, 0);
		}
		if ((fFlags & STATIC) != 0) {
			data= CPluginImages.DESC_OVR_STATIC.getImageData();
			x-= data.width;
			drawImage(data, x, 0);
		} 
		if ((fFlags & TEMPLATE) != 0) {
			data= CPluginImages.DESC_OVR_TEMPLATE.getImageData();
			x-= data.width;
			drawImage(data, x, 0);
		}
	}		
	
	private void drawBottomRight() {
		//Point size= getSize();
		//int x= size.x;
		//ImageData data= null;
		 /*if ((fFlags & SYNCHRONIZED) != 0) {
			data= CPluginImages.DESC_OVR_SYNCH.getImageData();
			x-= data.width;
			drawImage(data, x, size.y - data.height);
		}
		if ((fFlags & RUNNABLE) != 0) {
			data= CPluginImages.DESC_OVR_RUN.getImageData();
			x-= data.width;
			drawImage(data, x, size.y - data.height);
		}
		if ((fFlags & OVERRIDES) != 0) {
			data= CPluginImages.DESC_OVR_OVERRIDES.getImageData();
			x-= data.width;
			drawImage(data, x, size.y - data.height);
		}
		if ((fFlags & IMPLEMENTS) != 0) {
			data= CPluginImages.DESC_OVR_IMPLEMENTS.getImageData();
			x-= data.width;
			drawImage(data, x, size.y - data.height);
		}	*/		
	}		
	
	private void drawBottomLeft() {
		Point size= getSize();
		int x= 0;
		ImageData data= null;
		if ((fFlags & ERROR) != 0) {
			data= CPluginImages.DESC_OVR_ERROR.getImageData();
			drawImage(data, x, size.y - data.height);
			x+= data.width;
		}
		if ((fFlags & WARNING) != 0) {
			data= CPluginImages.DESC_OVR_WARNING.getImageData();
			drawImage(data, x, size.y - data.height);
			x+= data.width;
		}
	}		
}

