/********************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view.search;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.ui.part.ViewPart;


/**
 * This class is the content provider for the remote systems search viewer.
 */
public class SystemSearchViewContentProvider implements ITreeContentProvider {


	private ViewPart viewPart;

	/**
	 * Constructor for SystemSearchViewContentProvider.
	 */
	public SystemSearchViewContentProvider() {
		super();
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentElement) {
		
		if (parentElement == null) {
			return null;
		}
		
		if (parentElement instanceof IAdaptable) {
			ISystemViewElementAdapter adapter = getViewAdapter(parentElement);
			
			if (adapter == null) {
				return null;
			}
			else {
				return adapter.getChildren((IAdaptable)parentElement, new NullProgressMonitor());
			}
		}
		
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) {
		
		if (element == null) {
			return null;
		}
		
		if (element instanceof IAdaptable) {
			ISystemViewElementAdapter adapter = getViewAdapter(element);
			
			if (adapter == null) {
				return null;
			}
			else {
				return adapter.getParent(element);
			}
		}
		
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		
		if (element == null) {
			return false;
		}
		
		if (element instanceof IAdaptable) {
			ISystemViewElementAdapter adapter = getViewAdapter(element);
			
			if (adapter == null) {
				return false;
			}
			else {
				return adapter.hasChildren((IAdaptable)element);
			}
		}
		
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		
		if (inputElement == null) {
			return null;
		}
		
		if (inputElement instanceof IAdaptable) {
			ISystemViewElementAdapter adapter = getViewAdapter(inputElement);
			
			if (adapter == null) {
				return null;
			}
			else {
				return adapter.getChildren((IAdaptable)inputElement, new NullProgressMonitor());
			}
		}
		
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
		if (newInput == null) {
			return;
		}
		
		if (newInput instanceof IAdaptable) {
			ISystemViewElementAdapter adapter = getViewAdapter(newInput);
			
			if (adapter != null) {
				viewer.refresh();
			}
		}
	}
	
	/**
	 * Get the adapter for the given object.
	 * @param the object
	 * @return the adapter
	 */
	public ISystemViewElementAdapter getViewAdapter(Object element) 
	{
    	return SystemAdapterHelpers.getViewAdapter(element);
	}
	/**
	 * Set the ViewPart of this provider
	 * @param ViewPart of this provider
	 */
	public void setViewPart(ViewPart viewPart) 
	{
			this.viewPart = viewPart;
	}
	/**
	 * Get the ViewPart of this provider
	 * @return ViewPart of this provider
	 */
	public ViewPart getViewPart() 
	{
		return viewPart;
	}
}