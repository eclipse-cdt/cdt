/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Allows to test decorators for Java elements
 */
public class CElementDecorator extends LabelProvider implements ILabelDecorator {

	/*
	 * @see ILabelDecorator#decorateImage(Image, Object)
	 */
	@Override
	public Image decorateImage(Image image, Object element) {
		return null;
	}

	/*
	 * @see ILabelDecorator#decorateText(String, Object)
	 */
	@Override
	public String decorateText(String text, Object element) {
		return text + "*";
	}
}
