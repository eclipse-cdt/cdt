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

package org.eclipse.rse.ui.view;
import org.eclipse.rse.model.IHost;


/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is a special-case provider when we want the list to be empty.
 */
public class SystemEmptyListAPIProviderImpl 
       extends SystemAbstractAPIProvider
       implements ISystemViewInputProvider
{


	protected Object[] emptyList = new Object[0];
	
	/**
	 * Constructor 
	 */
	public SystemEmptyListAPIProviderImpl()
	{
		super();
	}
	
    // ----------------------------------
    // SYSTEMVIEWINPUTPROVIDER METHODS...
    // ----------------------------------
	/**
	 * Return the children objects to consistute the root elements in the system view tree.
	 * <p>We return an empty list.
	 */
	public Object[] getSystemViewRoots()
	{
		return emptyList;
	}
	/**
	 * Return true if {@link #getSystemViewRoots()} will return a non-empty list
	 * We return false.
	 */
	public boolean hasSystemViewRoots()
	{
		return false;		
	}
	/**
	 * This method is called by the connection adapter when the user expands
	 *  a connection. This method must return the child objects to show for that
	 *  connection.
	 * <p>We return an empty list
	 */
	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		return emptyList; // 
	}
	/**
	 * This method is called by the connection adapter when deciding to show a plus-sign
	 * or not beside a connection. Return true if this connection has children to be shown.
	 * <p>we return false
	 */
	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		return false;		
	}

    /**
     * Return true to show the action bar (ie, toolbar) above the viewer.
     * The action bar contains connection actions, predominantly.
     */
    public boolean showActionBar()
    {
    	return false;
    }
    /**
     * Return true to show the button bar above the viewer.
     * The tool bar contains "Get List" and "Refresh" buttons and is typicall
     * shown in dialogs that list only remote system objects.
     */
    public boolean showButtonBar()
    {
    	return true;
    }	
    /**
     * Return true to show right-click popup actions on objects in the tree.
     */
    public boolean showActions()
    {
    	return false;
    }
    


    // ----------------------------------
    // OUR OWN METHODS...    
    // ----------------------------------
     
}