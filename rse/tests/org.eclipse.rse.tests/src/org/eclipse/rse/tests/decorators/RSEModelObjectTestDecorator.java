/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0 
 * which accompanies this distribution, and is available at 
 * https://www.eclipse.org/legal/epl-2.0/ 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.tests.decorators;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * RSE Model Object test decorator.
 * 
 * @since 3.2
 */
public class RSEModelObjectTestDecorator extends LabelProvider implements ILabelDecorator {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
	 */
	public Image decorateImage(Image image, Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
	 */
	public String decorateText(String text, Object element) {
		return text + " (RSE Test Decoration)";
	}
}
