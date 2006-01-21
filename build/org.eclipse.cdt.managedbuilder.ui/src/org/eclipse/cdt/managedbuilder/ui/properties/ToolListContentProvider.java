/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
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

import org.eclipse.cdt.managedbuilder.core.*;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import java.util.ArrayList;
import java.util.List;

public class ToolListContentProvider implements ITreeContentProvider{
	public static final int FILE = 0x1;
	public static final int PROJECT = 0x4;
	private IConfiguration configRoot;
	private IResourceConfiguration resConfigRoot;
	private int elementType;
	private ToolListElement[] elements;

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	public ToolListContentProvider(int elementType) {
		this.elementType = elementType;
	}

	private ToolListElement[] createElements(IConfiguration config) {
		IOptionCategory toolChainCategories[]; 
		ITool filteredTools[];
		List elementList = new ArrayList();
		if (config != null) {
			// Get the the option categories of the toolChain  
			IToolChain toolChain = config.getToolChain();
			toolChainCategories = toolChain.getChildCategories();
			//  Create an element for each one
			for (int i=0; i<toolChainCategories.length; i++) {
				ToolListElement e = new ToolListElement(null, toolChain, toolChainCategories[i]);
				elementList.add(e);
				createChildElements(e);
			}
			//  Get the tools to be displayed
			filteredTools = config.getFilteredTools();
			//  Create an element for each one
			for (int i=0; i<filteredTools.length; i++) {
				ToolListElement e = new ToolListElement(filteredTools[i]);
				elementList.add(e);
				createChildElements(e);
			}
		}
		return (ToolListElement[])elementList.toArray(new ToolListElement[elementList.size()]);		
	}

	private ToolListElement[] createElements(IResourceConfiguration resConfig) {
		List elementList = new ArrayList();
		if (resConfig != null) {
			ITool[] tools = resConfig.getTools();
			//  Create an element for each one
			for (int i=0; i<tools.length; i++) {
				ToolListElement e = new ToolListElement(tools[i]);
				elementList.add(e);
				createChildElements(e);
			}
		}
		return (ToolListElement[])elementList.toArray(new ToolListElement[elementList.size()]);		
	}

	private void createChildElements(ToolListElement parentElement) {

		IOptionCategory parent = parentElement.getOptionCategory();
		IHoldsOptions optHolder = parentElement.getHoldOptions();
		if (parent == null) {
			parent = parentElement.getTool().getTopOptionCategory();	//  Must be an ITool
			optHolder = parentElement.getTool();
		}
		IOptionCategory[] cats = parent.getChildCategories();
		//  Create an element for each one
		for (int i=0; i<cats.length; i++) {
			ToolListElement e = new ToolListElement(parentElement, optHolder, cats[i]);
			parentElement.addChildElement(e);
			createChildElements(e);
		}
	}
	
	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IConfiguration ||
			parentElement instanceof IResourceConfiguration	) {
			return elements;
		}
		return ((ToolListElement)parentElement).getChildElements();
	}
	
	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return elements;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return ((ToolListElement)element).getParent();
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
		//  If the input hasn't changed, there is no reason to re-create the content
		if (oldInput == newInput) return;
		
		if(elementType == FILE) {
			resConfigRoot = (IResourceConfiguration)newInput;
			configRoot = null;
			//  Create a ToolListElement to represent each item that will appear
			//  in the TreeViewer.
			elements = createElements(resConfigRoot);
		}
		else if(elementType == PROJECT) {
			configRoot = (IConfiguration) newInput;
			resConfigRoot = null;
			//  Create a ToolListElement to represent each item that will appear
			//  in the TreeViewer.
			elements = createElements(configRoot);
		}
	}
}

