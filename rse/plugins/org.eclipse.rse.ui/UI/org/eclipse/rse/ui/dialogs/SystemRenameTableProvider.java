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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.graphics.Image;

/**
 *
 */
public class SystemRenameTableProvider implements ITableLabelProvider, IStructuredContentProvider
{
    static final int COLUMN_ERROR   = 0;
    static final int COLUMN_OLDNAME = 1;
    static final int COLUMN_NEWNAME = 2;   
    static final int COLUMN_TYPE    = 3;   
    private Map imageTable = new Hashtable(20);	 
    private Object[] children = null;    
	private ISystemValidator inputValidator = null;
	    
	/**
	 * Constructor for SystemRenameTableProvider
	 */
	public SystemRenameTableProvider() 
	{
		super();
	}

    /**
     * Set the validator for the new name,as supplied by the adaptor for name checking.
     * Overrides the default which is to query it from the object's adapter.
     */
    public void setNameValidator(ISystemValidator nameValidator)
    {
    	inputValidator = nameValidator;
    }
	
	private SystemRenameTableRow getTableRow(Object element)
	{
		return (SystemRenameTableRow)element;
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
		if (column == COLUMN_ERROR)
		{
		  SystemRenameTableRow row = getTableRow(element);
		  if (row.getError())
		  {
		  	Image errorImage = JFaceResources.getImage(org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_ERROR); 
		  	   //SystemPlugin.getDefault().getImage(ISystemConstants.ICON_SYSTEM_ERROR_ID);
		    return errorImage;
		  }
		  else
		    return null;
		}
		else if (column == COLUMN_OLDNAME)
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
		if (column == COLUMN_OLDNAME)
		  text = getTableRow(element).getName();
		else if (column == COLUMN_NEWNAME)
		  text = getTableRow(element).getNewName();		
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
		  children = new SystemRenameTableRow[iss.size()];
		  Iterator i = iss.iterator();
		  int idx = 0;
		  while (i.hasNext())
		  {
		    children[idx] = new SystemRenameTableRow(i.next(),idx);		
		    if (inputValidator != null)
              ((SystemRenameTableRow)children[idx]).setNameValidator(inputValidator);
		    idx++;
		  }
        }
		return children;
	}
		
	/**
	 * 
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		
		
	}

	/**
	 * Return the 0-based row number of the given element.
	 */
	public int getRowNumber(SystemRenameTableRow row)
	{
		int matchRow = row.getRowNumber();
		/*
		int matchRow = -1;
		boolean match = false;
		for (int idx=0; !match && (matchRow<children.length); idx++)
		{
		   //match = children[idx].equals(row);
		   match = (children[idx] == row);
		   if (match)
		      matchRow = idx;
		}
		*/
		//System.out.println("getRowNumber for "+row+": "+matchRow);
		return matchRow;
	}
}