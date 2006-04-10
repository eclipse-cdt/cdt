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

package org.eclipse.rse.internal.model;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.ISystemPromptableObject;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.rse.ui.view.ISystemViewRunnableObject;
import org.eclipse.swt.widgets.Shell;

/**
 * This class represents a special case object in the system view (when used in browse
 *  dialogs) that allows users to create a new connection. 
 * <p>
 * It shows as "New Connection..." in the tree. When expanded, they get the new connection
 *  wizard. 
 */
public class SystemNewConnectionPromptObject 
       implements  ISystemPromptableObject, ISystemViewRunnableObject, IAdaptable
{
    private Object parent;	
    private String[] systemTypes;
    private ISystemPromptableObject[] children;
    private SystemNewConnectionAction action = null;
    private boolean systemTypesSet = false;
    private String newConnText;
    private boolean isRootPrompt = false;
    
    /**
     * Constructor
     */
    public SystemNewConnectionPromptObject()
    {
    	systemTypes = SystemPlugin.getDefault().getSystemTypeNames(true); // true=>include local
    	isRootPrompt = true;
    }
    /**
     * Constructor for child instances
     */
    public SystemNewConnectionPromptObject(SystemNewConnectionPromptObject parent, String systemType)
    {
    	this.parent = parent;
    	this.systemTypes = new String[] {systemType};
    }
    
    // ----------------------------------------------------
    // METHODS FOR CONFIGURING THIS OBJECT
    // ----------------------------------------------------
    
    /**
     * Set the system types to restrict the New Connection wizard to
     */
    public void setSystemTypes(String[] systemTypes)
    {
    	this.systemTypes = systemTypes;
    	this.systemTypesSet = true;
    }
    
    /**
     * Set the parent object so that we can respond to getParent requests
     */
    public void setParent(Object parent)
    {
    	this.parent = parent;
    }
    
    // ----------------------------------------------------
    // METHODS CALLED BY THE SYSTEMVIEWPROMPTABLEADAPTER...
    // ----------------------------------------------------
    
    /**
     * Get the parent object (within tree view)
     */
    public Object getParent()
    {
    	return parent;
    }

	/**
	 * Return the child promptable objects.
	 * If this returns null, then SystemViewPromptableAdapter will subsequently
	 * call {@link #run(Shell)}.
	 */
	public ISystemPromptableObject[] getChildren()
	{
	    if (!hasChildren())
	      return null;
	    
	   
	    else if (children == null)
	    {
	    	children = new ISystemPromptableObject[systemTypes.length];
	    	for (int idx=0; idx<children.length; idx++)
	    	{
	    		children[idx] = new SystemNewConnectionPromptObject(this, systemTypes[idx]);
	    	}
	    }	    	
	    else
	    {
	    	 String[] typeNames = SystemPlugin.getDefault().getSystemTypeNames(true);
	    	 if (typeNames.length != systemTypes.length)
	    	 {
	    		 systemTypes = typeNames;
	    		 children = new ISystemPromptableObject[systemTypes.length];
	 	    	for (int idx=0; idx<children.length; idx++)
	 	    	{
	 	    		children[idx] = new SystemNewConnectionPromptObject(this, systemTypes[idx]);
	 	    	}
	    	 }
	    }
	    return children;
	}
	
	/**
	 * Return true if we have children, false if run when expanded
	 */
	public boolean hasChildren()
	{
	
		// DKM - I think we shouuld indicate children even if there's only one connection type	
		//if (systemTypes.length == 1)	
		if (systemTypes.length == 1 && !isRootPrompt)
			return false;
		else
		  return true;
	}
	    
	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * Calls getImage on the subsystem's owning factory.
	 */
	public ImageDescriptor getImageDescriptor()
	{
		if (hasChildren())
          return SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWCONNECTION_ID);
        else
          return SystemPlugin.getDefault().getSystemTypeImage(systemTypes[0], false);
	}
	
	/**
	 * Return the label for this object
	 */
	public String getText()
	{
		if (newConnText == null)
		{
		   if (hasChildren() || systemTypesSet)
		   {
			  if (hasChildren())
			     newConnText = SystemResources.RESID_NEWCONN_PROMPT_LABEL;
			  else
			     newConnText = SystemResources.RESID_NEWCONN_PROMPT_LABEL+"...";
		   }
		   else
		   {
		   	  newConnText = systemTypes[0]+"...";
		   }
		}
		return newConnText;
	}
	/**
	 * Return the type label for this object
	 */
	public String getType()
	{
		if (hasChildren())
		  return SystemResources.RESID_NEWCONN_EXPANDABLEPROMPT_VALUE;
		else
		  return SystemResources.RESID_NEWCONN_PROMPT_VALUE;		
	}	
	
	/**
	 * Run this prompt. This should return an appropriate ISystemMessageObject to show
	 *  as the child, reflecting if it ran successfully, was cancelled or failed. 
	 */
	public Object[] run(Shell shell)
	{
		if (action == null)
		{
		  action = new SystemNewConnectionAction(shell, false, false, null);
		}
		if (systemTypes!=null)
		  action.restrictSystemTypes(systemTypes);
		
		try 
		{
		  action.run();
		} catch (Exception exc)
		{
		   return new Object[] {
		       new SystemMessageObject(SystemPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FAILED),
		                                  ISystemMessageObject.MSGTYPE_ERROR,null)};
		}
		
		
		IHost newConnection = (IHost)action.getValue();
		
		// create appropriate object to return...
		ISystemMessageObject result = null;
		if (newConnection != null)
		{
		   result = new SystemMessageObject(SystemPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_CONNECTIONCREATED),
		                                    ISystemMessageObject.MSGTYPE_OBJECTCREATED,null);
	    }		  
	    else
		   result = new SystemMessageObject(SystemPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_CANCELLED),
		                                    ISystemMessageObject.MSGTYPE_CANCEL,null);
	    return new Object[] {result};
	}
     
    // ----------------------------------------------------
    // METHODS REQUIRED BY THE IADAPTABLE INTERFACE...
    // ----------------------------------------------------

    /**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
    public Object getAdapter(Class adapterType)
    {
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);	
    }               
}