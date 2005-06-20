/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Timesys - Initial API and implementation 
 * IBM Rational Software
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
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
	 *  Gets the top level contents to be displayed in the tool list.
	 *  If defined, first display the toolChain's option categories (unfiltered).
	 *  Then display the the tools which are relevant for the project's nature.
	 */
	private Object[] getToplevelContent(IConfiguration config) {
		Object toolChainsCategories[]; 
		Object filteredTools[];
		Object all[];
		// Get the the option categories of the toolChain  
		IToolChain toolChain = config.getToolChain();
		toolChainsCategories = toolChain.getChildCategories();
		// Get the tools to be displayed
		filteredTools = config.getFilteredTools();
		// Add up both arrays and return
		int i;
		int len = toolChainsCategories.length+filteredTools.length;
		all = new Object[len];
		for (i=0; i < toolChainsCategories.length; i++) 
			all[i] = toolChainsCategories[i];
		for (; i < len; i++) 
			all[i] = filteredTools[i-toolChainsCategories.length];
		
		return all;
	}
	
	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		// If parent is configuration, return a list of its option categories
		if (parentElement instanceof IConfiguration) {
			IConfiguration config = (IConfiguration)parentElement;
			// Get the contents to be displayed for the configuration
			return getToplevelContent(config);
		} else if( parentElement instanceof IResourceConfiguration) {
			// If parent is a resource configuration, return a list of its tools.
			// Resource configurations do not support categories that are children 
			// of toolchains. The reason for this is that options in such categories 
			// are intended to be global.
			// TODO: Remove this restriction in future? Requires getToplevelContent() variant
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

