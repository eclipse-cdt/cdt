/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.core.testplugin;

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
		return text + "*";
	}
}
