/**********************************************************************
 * Copyright (c) 2002,2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIImages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
 
public class ToolListLabelProvider extends LabelProvider {
	private final Image IMG_TOOL = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_TOOL);
	private final Image IMG_CAT = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CAT);
	private static final String TREE_LABEL = "BuildPropertyPage.label.ToolTree";	//$NON-NLS-1$
	private static final String ERROR_UNKNOWN_ELEMENT = "BuildPropertyPage.error.Unknown_tree_element";	//$NON-NLS-1$

	public Image getImage(Object element) {
		// Return a tool image for a tool or tool reference
		if (element instanceof ITool) {
			return IMG_TOOL;
		}
		else if (element instanceof IOptionCategory) {
			return IMG_CAT;
		} else {
			throw unknownElement(element);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		if (element instanceof ITool) {
			// Handles tool references as well
			ITool tool = (ITool)element;
			return tool.getName();
		}
		else if (element instanceof IOptionCategory) {
			IOptionCategory cat = (IOptionCategory)element;
			return cat.getName();
		}
		else {
			throw unknownElement(element);
		}
	}

	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException(ManagedBuilderUIMessages.getFormattedString(ERROR_UNKNOWN_ELEMENT, element.getClass().getName()));
	}
}