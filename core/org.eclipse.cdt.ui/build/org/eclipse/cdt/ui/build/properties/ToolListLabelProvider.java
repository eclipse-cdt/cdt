package org.eclipse.cdt.ui.build.properties;

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

import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.IOptionCategory;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

class ToolListLabelProvider extends LabelProvider {
	private final Image IMG_FOLDER = CUIPlugin.getDefault().getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	private final Image IMG_TOOL = CPluginImages.get(CPluginImages.IMG_BUILD_TOOL);
	private static final String TREE_LABEL = "BuildPropertyPage.label.ToolTree";	//$NON-NLS-1$

	public Image getImage(Object element) {
		// If the element is a configuration, return the folder image
		if (element instanceof IConfiguration) {
			return IMG_FOLDER;
		} else if (element instanceof IOptionCategory) {
			IOptionCategory cat = (IOptionCategory)element;
			IOptionCategory [] children = cat.getChildCategories();
			if (children.length > 0){
				return IMG_FOLDER;
			} else {
				return IMG_TOOL;
			}
		} else {
			throw unknownElement(element);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		if (element instanceof IConfiguration) {
			IConfiguration config = (IConfiguration)element;
			return CUIPlugin.getResourceString(TREE_LABEL);
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
		return new RuntimeException("Unknown type of element in tree of type " + element.getClass().getName());
	}
}