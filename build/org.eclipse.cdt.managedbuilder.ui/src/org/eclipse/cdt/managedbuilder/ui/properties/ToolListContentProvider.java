package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation 
 * IBM Rational Software
 * *********************************************************************/

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ToolListContentProvider implements ITreeContentProvider{
	private static Object[] EMPTY_ARRAY = new Object[0];
	private IConfiguration root;

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		// If parent is configuration, return a list of its option categories
		if (parentElement instanceof IConfiguration) {
			IConfiguration config = (IConfiguration)parentElement;
			ITool [] tools = config.getTools();
			IOptionCategory [] categories = new IOptionCategory[tools.length];
			// The categories are accessed through the tool
			for (int index = 0; index < tools.length; ++index) {
				categories[index] = tools[index].getTopOptionCategory();
			}
			return categories;
		} else if (parentElement instanceof IOptionCategory) {
			// Categories can have child categories
			IOptionCategory cat = (IOptionCategory)parentElement;
			IOptionCategory [] children = cat.getChildCategories();
			if (children == null) {
				return EMPTY_ARRAY;
			} else {
				return children;
			}
		} else {
			return EMPTY_ARRAY;
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof IOptionCategory) {
			// Find the parent category
			IOptionCategory cat = (IOptionCategory)element; 
			IOptionCategory parent = cat.getOwner();
			// Then we need to get the configuration we belong to
			if (parent == null) {
				ITool tool = cat.getTool();
				return root;
			}
			return parent;
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		root = (IConfiguration) newInput;
	}
}

