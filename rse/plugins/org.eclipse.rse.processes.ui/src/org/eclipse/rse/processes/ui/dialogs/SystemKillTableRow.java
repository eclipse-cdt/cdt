/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.processes.ui.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.dialogs.SystemDeleteTableRow;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;


public class SystemKillTableRow extends SystemDeleteTableRow
{
	
	private Object element;
	private String exename;
	private String pid;	
	private ImageDescriptor imageDescriptor;
	private ISystemViewElementAdapter adapter;
	private ISystemRemoteElementAdapter remoteAdapter;
	private int rowNbr = 0;
	
    public SystemKillTableRow(Object element, int rowNbr)
    {
    	super(element, rowNbr);
    	if (element instanceof SystemSimpleContentElement)
    	  element = ((SystemSimpleContentElement)element).getData();
    	this.element = element;    	
        this.adapter = getAdapter(element);    	
        this.remoteAdapter = getRemoteAdapter(element);
        this.rowNbr = rowNbr;
    	if (adapter != null)
    	  this.exename = adapter.getName(element);
    	else
    	{
    		if (element instanceof IRemoteProcess)
    		  this.exename = ((IRemoteProcess)element).getName();
    	}
    	if (element instanceof IRemoteProcess)
    		this.pid = "" + ((IRemoteProcess)element).getPid();
    	if (adapter != null)
    	  this.imageDescriptor = adapter.getImageDescriptor(element);
    	else this.imageDescriptor = SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PROCESS_ID);
    }
    
    /**
     * Return the name of the item to be deleted
     * @return display name of the item.
     */
    public String getName()
    {
    	return exename;
    }
    /**
     * Return the resource type of the item to be deleted
     * @return resource type of the item
     */
    public String getType()
    {
    	return pid;
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
    	return exename;
    }    

}