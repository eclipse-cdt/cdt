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
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
/**
 * This class is the table provider class for the delete dialog
 */
public class SystemDeleteTableProvider implements ITableLabelProvider, IStructuredContentProvider
{
	
    static final int COLUMN_IMAGE = 0;
    static final int COLUMN_NAME = 1;   
    static final int COLUMN_TYPE = 2;   
    protected Map imageTable = new Hashtable(20);	 
    protected Object[] children = null;    
    
	/**
	 * Constructor for SystemDeleteTableProvider
	 */
	public SystemDeleteTableProvider() 
	{
		super();
	}
	
	private SystemDeleteTableRow getTableRow(Object element)
	{
		return (SystemDeleteTableRow)element;
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
		String text = "";
		if (column == COLUMN_NAME)
		  	text = getTableRow(element).getName();
		else if (column == COLUMN_TYPE)
		  	text = getTableRow(element).getType();		  
		//System.out.println("INSIDE GETCOLUMNTEXT: " + column + ", " + text + ", " + getTableRow(element));
		return text;  
	}

	/**
	 * @see IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
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
		  	IStructuredSelection iss = (IStructuredSelection)inputElement;
		  	children = new SystemDeleteTableRow[iss.size()];
		  	Iterator i = iss.iterator();
		  	int idx = 0;
		  	while (i.hasNext())
		  	{
		    	children[idx] = new SystemDeleteTableRow(i.next(), idx);		
		    	idx++;
		  	}
        }
		return children;
	}
	
	/**
	 * Return the 0-based row number of the given element.
	 */
	public int getRowNumber(SystemDeleteTableRow row)
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