/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * Provides a view of any image that needs to be displayed by a test case.
 */
public class TestSuiteImageView extends ViewPart {

	private Canvas imageCanvas;
	private Image image;
	private Color backgroundColor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		imageCanvas = new Canvas(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		backgroundColor = new Color(parent.getDisplay(), new RGB(255, 255, 255));
		clearImage();
	}
	
	public void setImage(ImageDescriptor descriptor) {
		clearImage();
		image = descriptor.createImage();
		imageCanvas.setBackgroundImage(image);
	}
	
	public void clearImage() {
		if (imageCanvas != null) {
			imageCanvas.setBackground(backgroundColor);
		}
		if (image != null) {
			image.dispose();
		}
		image = null;
	}
	
	public void setFocus() {
	}
	
	public void dispose() {
		clearImage();
		backgroundColor.dispose();
	}

}
