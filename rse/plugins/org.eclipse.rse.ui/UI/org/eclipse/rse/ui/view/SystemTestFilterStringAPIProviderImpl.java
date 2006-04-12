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
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;


/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is used when the contents are used to show the resolution of a single filter string.
 */
public class SystemTestFilterStringAPIProviderImpl 
       extends SystemAbstractAPIProvider
       implements ISystemViewInputProvider,  ISystemMessages
{


	protected String filterString = null;
	protected ISubSystem subsystem = null;
	protected Object[] emptyList = new Object[0];
	protected Object[] msgList   = new Object[1];	
	protected SystemMessageObject nullObject     = null;
	protected SystemMessageObject canceledObject = null;	
	protected SystemMessageObject errorObject    = null;	
	/**
	 * Constructor 
	 * @param subsystem The subsystem that will resolve the filter string
	 * @param filterString The filter string to test
	 */
	public SystemTestFilterStringAPIProviderImpl(ISubSystem subsystem, String filterString)
	{
		super();
		this.subsystem = subsystem;
		this.filterString = filterString;
	}
	
	private void initMsgObjects()
	{
		nullObject     = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_EMPTY),ISystemMessageObject.MSGTYPE_EMPTY, null);
		canceledObject = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_LIST_CANCELLED),ISystemMessageObject.MSGTYPE_CANCEL, null);
		errorObject    = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_FAILED),ISystemMessageObject.MSGTYPE_ERROR, null);
	}
	
	/**
	 * Change the input subsystem
	 */
	public void setSubSystem(ISubSystem subsystem)
	{
		this.subsystem = subsystem;
	}
	/**
	 * Change the input filter string
	 */
	public void setFilterString(String filterString)
	{
		this.filterString = filterString;
	}
	
    // ----------------------------------
    // SYSTEMVIEWINPUTPROVIDER METHODS...
    // ----------------------------------
	/**
	 * Return the children objects to consistute the root elements in the system view tree.
	 * We return the result of asking the subsystem to resolve the filter string.
	 */
	public Object[] getSystemViewRoots()
	{
		Object[] children = emptyList;
		if (subsystem == null)
		  return children;
		try
		{
	 	   children = subsystem.resolveFilterString(filterString, shell);
	 	   if ((children == null) || (children.length==0))
	 	   {
	 	   	 if (nullObject == null)
	 	   	   initMsgObjects();
	 	   	 msgList[0] = nullObject;
	 	     children = msgList;
	 	   }
		} catch (InterruptedException exc)
		{
		   if (canceledObject == null)
		     initMsgObjects();
		   msgList[0] = canceledObject;
		   children = msgList;
		} catch (Exception exc)
		{
		   if (errorObject == null)
		     initMsgObjects();
		   msgList[0] = errorObject;
		   children = msgList;			
		   SystemBasePlugin.logError("Error in SystemTestFilterStringAPIProviderImpl#getSystemViewRoots()",exc);
		}
		return children;
	}
	/**
	 * Return true if {@link #getSystemViewRoots()} will return a non-empty list
	 * We return true on the assumption the filter string will resolve to something.
	 */
	public boolean hasSystemViewRoots()
	{
		return true;		
	}
	/**
	 * This method is called by the connection adapter when the user expands
	 *  a connection. This method must return the child objects to show for that
	 *  connection.
	 * <p>NOT APPLICABLE TO US
	 */
	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		return null; // 
	}
	/**
	 * This method is called by the connection adapter when deciding to show a plus-sign
	 * or not beside a connection. Return true if this connection has children to be shown.
	 * <p>NOT APPLICABLE TO US
	 */
	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		return true;		
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