/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.testplugin;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Allows to test decorators for Java elements
 */
public class CElementDecorator extends LabelProvider implements ILabelDecorator {

	/*
	 * @see ILabelDecorator#decorateImage(Image, Object)
	 */
	public Image decorateImage(Image image, Object element) {
		return null;
	}

	/*
	 * @see ILabelDecorator#decorateText(String, Object)
	 */
	public String decorateText(String text, Object element) {
		return text + "*"; //$NON-NLS-1$
	}
}
