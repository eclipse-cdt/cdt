/**********************************************************************
 * Copyright (c) 2002,2003 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation 
 * IBM Rational Software
 * *********************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ToolListContentProvider implements ITreeContentProvider{
	public static final int FILE = 0x1;
	public static final int PROJECT = 0x4;
	private static Object[] EMPTY_ARRAY = new Object[0];
	private IConfiguration configRoot;
	private IResourceConfiguration resConfigRoot;
	private int elementType;

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	public ToolListContentProvider(int elementType) {
		this.elementType = elementType;
	}
	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		// If parent is configuration, return a list of its option categories
		if (parentElement instanceof IConfiguration) {
			IConfiguration config = (IConfiguration)parentElement;
			// the categories are all accessed through the tools
			return config.getFilteredTools();
		} else if( parentElement instanceof IResourceConfiguration) {
			// If parent is a resource configuration, return a list of its tools
			IResourceConfiguration resConfig = (IResourceConfiguration)parentElement;
			return resConfig.getTools();
		} else if (parentElement instanceof ITool) {
			// If this is a tool, return the categories it contains
			ITool tool = (ITool)parentElement;
			return tool.getTopOptionCategory().getChildCategories();
		} else if (parentElement instanceof IOptionCategory) {
			// Categories can have child categories
			IOptionCategory cat = (IOptionCategory)parentElement;
			return cat.getChildCategories();
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
				if(elementType == FILE)
					return resConfigRoot;
				else
					return configRoot;
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
		if(elementType == FILE) {
			resConfigRoot = (IResourceConfiguration)newInput;
			configRoot = null;
		}
		else if(elementType == PROJECT) {
			configRoot = (IConfiguration) newInput;
			resConfigRoot = null;
		}
	}
}

