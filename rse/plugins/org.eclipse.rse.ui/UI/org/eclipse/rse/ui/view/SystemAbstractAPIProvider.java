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
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.widgets.Shell;



/**
 * This is a base class that a provider of root nodes to the remote systems tree viewer part can
 * use as a parent class.
 */
public abstract class SystemAbstractAPIProvider 
       implements ISystemViewInputProvider, ISystemMessages
{


	protected Shell shell;
	protected Viewer viewer;
	protected ISystemRegistry sr;
	
	protected Object[] emptyList = new Object[0];
	protected Object[] msgList   = new Object[1];	
	protected SystemMessageObject nullObject     = null;
	protected SystemMessageObject canceledObject = null;	
	protected SystemMessageObject errorObject    = null;	
	
	
	/**
	 * Constructor 
	 */
	public SystemAbstractAPIProvider()
	{
		super();
		sr = RSEUIPlugin.getTheSystemRegistry();
	}
	
    /**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
    public Object getAdapter(Class adapterType)
    {
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);	
    }           

    /**
     * Set the shell in case it is needed for anything.
     * The label and content provider will call this.
     */
    public void setShell(Shell shell)
    {
    	this.shell = shell;
    }
    
    /**
     * Return the shell of the current viewer
     */
    public Shell getShell()
    {
    	return shell;
    }

    /**
     * Set the viewer in case it is needed for anything.
     * The label and content provider will call this.
     */
    public void setViewer(Viewer viewer)
    {
    	this.viewer = viewer;
    }
    
    /**
     * Return the viewer we are currently associated with
     */
    public Viewer getViewer()
    {
    	return viewer;
    }

    /**
     * Return true to show the action bar (ie, toolbar) above the viewer.
     * The action bar contains connection actions, predominantly.
     * We return false
     */
    public boolean showActionBar()
    {
    	return false;
    }
    
    /**
     * Return true to show the button bar above the viewer.
     * The tool bar contains "Get List" and "Refresh" buttons and is typically
     * shown in dialogs that list only remote system objects.
     * We return false.
     */
    public boolean showButtonBar()
    {
    	return false;
    }	
    
    /**
     * Return true to show right-click popup actions on objects in the tree.
     * We return false.
     */
    public boolean showActions()
    {
    	return false;
    }

	private void initMsgObjects()
	{
		nullObject     = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_EMPTY),ISystemMessageObject.MSGTYPE_EMPTY, null);
		canceledObject = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_LIST_CANCELLED),ISystemMessageObject.MSGTYPE_CANCEL, null);
		errorObject    = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_FAILED),ISystemMessageObject.MSGTYPE_ERROR, null);
	}
	
    /**
     * In getChildren, return checkForNull(children, true/false) vs your array directly.
     * This method checks for a null array which not allow and replaces it with an empty array.
     * If true is passed then it returns the "Empty list" message object if the array is null or empty
     */
    protected Object[] checkForNull(Object[] children, boolean returnNullMsg)
    {
	   if ((children == null) || (children.length==0))
	   {
	   	 if (!returnNullMsg)
           return emptyList;
         else
         {
	 	   if (nullObject == null)
	 	     initMsgObjects();
	 	   msgList[0] = nullObject;
	 	   return msgList;
         }
	   }
       else
         return children;
    }

    /**
     * Return the "Operation cancelled by user" msg as an object array so can be used to answer getChildren()
     */
    protected Object[] getCancelledMessageObject()
    {    	
		 if (canceledObject == null)
		   initMsgObjects();
		 msgList[0] = canceledObject;
		 return msgList;
    }    

    /**
     * Return the "Operation failed" msg as an object array so can be used to answer getChildren()
     */
    protected Object[] getFailedMessageObject()
    {    	
		 if (errorObject == null)
		   initMsgObjects();
		 msgList[0] = errorObject;
		 return msgList;
    }    

	/**
	 * Return true if we are listing connections or not, so we know whether we are interested in 
	 *  connection-add events
	 */
	public boolean showingConnections()
	{
		return false;
	}

	// ------------------
	// HELPER METHODS...
	// ------------------	
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
}