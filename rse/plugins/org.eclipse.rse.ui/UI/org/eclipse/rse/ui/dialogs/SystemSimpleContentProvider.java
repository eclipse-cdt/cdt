/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.dialogs;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

/**
 * When we need to populate a TreeViewer in a dialog,
 *  we can use the {@link org.eclipse.rse.ui.dialogs.SystemSimpleContentElement} class to
 *  represent each element, and then use this provider
 *  to drive the tree.
 * @see org.eclipse.rse.ui.dialogs.SystemSimpleContentElement
 * @see org.eclipse.rse.ui.dialogs.SystemSimpleSelectDialog
 */
public class SystemSimpleContentProvider extends LabelProvider
	implements ITreeContentProvider, ILabelProvider
{
    private Map imageTable = new Hashtable(5);	     

	/**
	 * Constructor for SystemSelectFilterPoolContentProvider
	 */
	public SystemSimpleContentProvider() 
	{
		super();
	}

	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object element) 
	{
		return getElement(element).getChildren();
	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) 
	{
		return getElement(element).getParent();
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) 
	{
		Object[] children = getChildren(element);
		if (children == null)
		  return false;
		else
		  return children.length > 0;
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object element) 
	{
		return getChildren(element);
	}

	/**
	 * @see IContentProvider#dispose()
	 */
	public void dispose() 
	{
		
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) 
	{		
	}


    // -------------------------
    // ILabelProvider methods...
    // -------------------------
    /**
     * Returns the image for the given object.
     */
    public Image getImage(Object element) 
    {
	    ImageDescriptor descriptor = getElement(element).getImageDescriptor();
	    if (descriptor == null)
		  return null;
	    Image image = (Image) imageTable.get(descriptor);
	    if (image == null) 
	    {
		  image = descriptor.createImage();
		  imageTable.put(descriptor, image);
	    }
	    return image;    	
    }
    /**
     * Returns the label text for the given object.
     */
    public String getText(Object element) 
    {
    	return getElement(element).getName();
    }


    // -------------------------
    // Local/private methods...
    // -------------------------
    /**
     * Casts the given object to SystemSimpleContentElement
     */
    protected SystemSimpleContentElement getElement(Object element)
    {
    	return (SystemSimpleContentElement)element;
    }

    /**
     * Returns the image for the given object, given its image descriptor
     */    
    protected Image getImageFromDescriptor(ImageDescriptor descriptor)
    {
    	if (descriptor == null)
    	  return null;
	    //obtain the cached image corresponding to the descriptor
	    Image image = (Image) imageTable.get(descriptor);
	    if (image == null) 
	    {
		  image = descriptor.createImage();
		  imageTable.put(descriptor, image);
	    }
	    return image;      	  
    }

}