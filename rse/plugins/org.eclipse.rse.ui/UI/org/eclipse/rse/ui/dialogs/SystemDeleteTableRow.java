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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemViewResources;


/**
 * Represents one row in the table in the SystemDeleteDialog dialog.
 */
public class SystemDeleteTableRow 
{
	
	private Object element;
	private String name;
	private String type;	
	private ImageDescriptor imageDescriptor;
	private ISystemViewElementAdapter adapter;
	private ISystemRemoteElementAdapter remoteAdapter;
	private int rowNbr = 0;
	
    public SystemDeleteTableRow(Object element, int rowNbr)
    {
    	if (element instanceof SystemSimpleContentElement)
    	  element = ((SystemSimpleContentElement)element).getData();
    	this.element = element;    	
        this.adapter = getAdapter(element);    	
        this.remoteAdapter = getRemoteAdapter(element);
        this.rowNbr = rowNbr;
    	//this.oldName = getAdapter(element).getText(element);
    	if (adapter != null)
    	  this.name = adapter.getName(element);
    	else
    	{
    		if (element instanceof ISystemTypedObject)
    		  this.name = ((ISystemTypedObject)element).getName();
    		else if (element instanceof IResource)
    		  this.name = ((IResource)element).getName();
    	}
    	ISystemViewElementAdapter typeAdapter = adapter;
    	Object typeElement = element;
    	if (typeElement instanceof ISystemFilterPoolReference)
    	{
    	  typeElement = ((ISystemFilterPoolReference)typeElement).getReferencedFilterPool();
    	  typeAdapter = getAdapter(typeElement);
    	}
    	if (typeAdapter != null)
    	  this.type = typeAdapter.getType(typeElement);
    	else
    	{
    		if (element instanceof ISystemTypedObject)
    		  this.type = ((ISystemTypedObject)element).getType();
    		else if (element instanceof IResource)
    		{
    			if ((element instanceof IFolder) || (element instanceof IProject))
    			  this.type = SystemViewResources.RESID_PROPERTY_FILE_TYPE_FOLDER_VALUE;
    			else
    			  this.type = SystemViewResources.RESID_PROPERTY_FILE_TYPE_FILE_VALUE;
    		}
    		else
    	      this.type = element.getClass().getName();
    	}
    	if (adapter != null)
    	  this.imageDescriptor = adapter.getImageDescriptor(element);
    	else if (element instanceof ISystemTypedObject)
    	  this.imageDescriptor = ((ISystemTypedObject)element).getImageDescriptor();
    	else if (element instanceof IFolder)
		  this.imageDescriptor = //PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER); 
		  	RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FOLDER_ID);
    	else if (element instanceof IFile)
    	  this.imageDescriptor = RSEUIPlugin.getDefault().getWorkbench().getEditorRegistry().getImageDescriptor(name);
    }
    
    /**
     * Return the name of the item to be deleted
     * @return display name of the item.
     */
    public String getName()
    {
    	return name;
    }
    /**
     * Return the resource type of the item to be deleted
     * @return resource type of the item
     */
    public String getType()
    {
    	return type;
    }
    /**
     * Return the 0-based row number of this item
     * @return 0-based row number
     */
    public int getRowNumber()
    {
    	return rowNbr;
    }

	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 */
	public ImageDescriptor getImageDescriptor()
	{
    	return imageDescriptor;
    }
    
    /**
     * Get the input object this row represents
     */
    public Object getElement()
    {
    	return element;
    }
    /**
     * Get the input object adapter for the input object this row represents
     */
    public ISystemViewElementAdapter getAdapter()
    {
    	return adapter;
    }
    /**
     * Get the input object remote adapter for the input object this row represents
     */
    public ISystemRemoteElementAdapter getRemoteAdapter()
    {
    	return remoteAdapter;
    }
    /**
     * Return true if this is a remote object
     */
    public boolean isRemote()
    {
    	return (remoteAdapter != null);
    }
        
    /**
     * Returns the implementation of ISystemViewElement for the given
     * object.  Returns null if the adapter is not defined or the
     * object is not adaptable.
     */
    protected ISystemViewElementAdapter getAdapter(Object o) 
    {
    	return SystemAdapterHelpers.getAdapter(o);
    }
    
    /**
     * Returns the implementation of ISystemRemoteElement for the given
     * object.  Returns null if this object does not adaptable to this.
     */
    protected ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
    {
    	return SystemAdapterHelpers.getRemoteAdapter(o);
    }
    
    public String toString()
    {
    	return name;
    }    


    /* THESE CAUSE GRIEF IF TWO OBJECTS WITH SAME NAME ARE SHOWN
    public boolean equals(Object o)
    {
        if (o instanceof SystemRenameTableRow)
          return ((SystemRenameTableRow)o).getOldName().equalsIgnoreCase(getOldName());
        else if (o instanceof SystemDeleteTableRow)
          return ((SystemDeleteTableRow)o).getOldName().equalsIgnoreCase(getOldName());
        else if (o instanceof String)
          return ((String)o).equalsIgnoreCase(getOldName());
        else
          return super.equals(o);
    }
    public int hashCode()
    {
        return getOldName().hashCode();
    }
    */      
}