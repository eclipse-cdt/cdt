package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIImages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
 
class ToolListLabelProvider extends LabelProvider {
	private final Image IMG_TOOL = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_TOOL);
	private final Image IMG_CAT = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CAT);
	private static final String TREE_LABEL = "BuildPropertyPage.label.ToolTree";	//$NON-NLS-1$
	private static final String ERROR_UNKNOWN_ELEMENT = "BuildPropertyPage.error.Unknown_tree_element";	//$NON-NLS-1$

	public Image getImage(Object element) {
		// If the element is a configuration, return the folder image
		if (element instanceof IOptionCategory) {
			IOptionCategory cat = (IOptionCategory)element;
			IOptionCategory [] children = cat.getChildCategories();
			if (children.length > 0){
				return IMG_TOOL;
			} else {
				return IMG_CAT;
			}
		} else {
			throw unknownElement(element);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		if (element instanceof IOptionCategory) {
			IOptionCategory cat = (IOptionCategory)element;
			return cat.getName();
		}
		else {
			throw unknownElement(element);
		}
	}

	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException(ManagedBuilderUIPlugin.getFormattedString(ERROR_UNKNOWN_ELEMENT, element.getClass().getName()));
	}
}