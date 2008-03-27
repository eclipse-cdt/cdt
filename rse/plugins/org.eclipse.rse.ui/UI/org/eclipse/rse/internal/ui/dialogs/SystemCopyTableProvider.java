/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Rupen Mardirossian (IBM) - [210682] Created for SystemCopyDialog
 ********************************************************************************/

package org.eclipse.rse.internal.ui.dialogs;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
/**
 * This class is the table provider class for the SystemCopyDialog
 */
public class SystemCopyTableProvider implements ITableLabelProvider, IStructuredContentProvider
{
	
    static final int COLUMN_IMAGE = 0;
    static final int COLUMN_NAME = 1;      
    protected Map imageTable = new Hashtable(20);	 
    protected Object[] children = null;    
    
	/**
	 * Constructor for SystemCopyTableProvider
	 */
	public SystemCopyTableProvider() 
	{
		super();
	}
	
	private SystemCopyTableRow getTableRow(Object element)
	{
		return (SystemCopyTableRow)element;
	}
 
    private Image getImageFromDescriptor(ImageDescriptor descriptor)
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
    	//System.out.println("...image = " + image);	    
	    return image;      	  
    }
     
	/**
	 * @see ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int column) 
	{
		if (column == COLUMN_IMAGE)
		  	return getImageFromDescriptor(getTableRow(element).getImageDescriptor());
		else
		  	return null;
	}

	/**
	 * @see ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int column) 
	{
		String text = ""; //$NON-NLS-1$
		if (column == COLUMN_NAME)
		  	text = getTableRow(element).getName();
		return text;  
	}

	/**
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) 
	{
	}

	/**
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() 
	{
		// The following we got from WorkbenchLabelProvider
        if (imageTable != null)
        {
	      	Collection imageValues = imageTable.values();
	      	if (imageValues!=null)
	      	{
	        	Iterator images = imageValues.iterator();	    	
	        	if (images!=null)
	          		while (images.hasNext())
    	        		((Image)images.next()).dispose();
    	    	imageTable = null;	    
	      	}
        }
	}

	/**
	 * @see IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) 
	{
		return true;
	}

	/**
	 * @see IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) 
	{
	}
	
	/**
	 * Return rows. Input must be an IStructuredSelection.
	 */
	public Object[] getElements(Object inputElement)
	{
        if (children == null)
        {
		  	List list = (List)inputElement;
		  	children = new SystemCopyTableRow[list.size()];
		  	for(int i=0;i<list.size();i++)
		  	{
		  		children[i] = new SystemCopyTableRow(list.get(i), i);
		  	}
        }
		return children;
	}
	
	/**
	 * Return the 0-based row number of the given element.
	 */
	public int getRowNumber(SystemCopyTableRow row)
	{
		return row.getRowNumber();
	}
		
	/**
	 * 
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

}
