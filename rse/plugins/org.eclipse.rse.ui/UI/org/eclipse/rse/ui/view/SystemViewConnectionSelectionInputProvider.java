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

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.internal.model.SystemNewConnectionPromptObject;
import org.eclipse.rse.model.IHost;


/**
 * This input provider for the System View is used when we want to merely present a
 *  list of existing connections for the user to select from, and optionally include
 *  the New Connection prompting connection. <br>
 * Used in the {@link org.eclipse.rse.ui.widgets.SystemSelectConnectionForm} class.
 */
public class SystemViewConnectionSelectionInputProvider extends SystemAbstractAPIProvider
{
	private boolean showNew = true;
	private SystemNewConnectionPromptObject newConnPrompt;
	private Object[] newConnPromptArray;
	private String[] systemTypes;
	
	/**
	 * Constructor
	 */
	public SystemViewConnectionSelectionInputProvider()
	{
		super();
	}
	
	/**
	 * Specify if the New Connection prompt is to be shown.
	 * Default is true.
	 */
	public void setShowNewConnectionPrompt(boolean show)
	{
		this.showNew = show;
	}
	/**
	 * Query whether the New Connection prompt is to be shown or not.
	 */
	public boolean getShowNewConnectionPrompt()
	{
		return showNew;
	}
	/**
	 * Set the system types to restrict by
	 */
	public void setSystemTypes(String[] systemTypes)
	{
		this.systemTypes = systemTypes;
	}
	/**
	 * Return the system types we are restricted by
	 */
	public String[] getSystemTypes()
	{
		return systemTypes;
	}
	
	// REQUIRED METHODS...
	
	/**
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#getSystemViewRoots()
	 */
	public Object[] getSystemViewRoots()
	{
		//System.out.println("Inside getSystemViewRoots. showNew = "+showNew);
		IHost[] conns = null;
		if (systemTypes == null)
			conns = SystemPlugin.getTheSystemRegistry().getHosts();
		else
			conns = SystemPlugin.getTheSystemRegistry().getHostsBySystemTypes(systemTypes);			
		if (showNew)
		{
			if ((conns == null) || (conns.length == 0))
			{
		  		return getNewConnectionPromptObjectAsArray();
			}
			else
			{
		  		Object[] allChildren = new Object[conns.length+1];
		  		allChildren[0] = getNewConnectionPromptObject();
		  		for (int idx=0; idx<conns.length; idx++)
			 		allChildren[idx+1] = conns[idx];
		  		return allChildren;
			}
		}
		else
			return conns;
	}
	private SystemNewConnectionPromptObject getNewConnectionPromptObject()
	{
		if (newConnPrompt == null)
		  	newConnPrompt = new SystemNewConnectionPromptObject();
		if (systemTypes != null)
			newConnPrompt.setSystemTypes(systemTypes);
		return newConnPrompt;
	}

	private Object[] getNewConnectionPromptObjectAsArray()
	{
		if (newConnPromptArray == null)
		  	newConnPromptArray = new Object[1];
		newConnPromptArray[0] = getNewConnectionPromptObject();
		return newConnPromptArray;
	}

	/**
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#hasSystemViewRoots()
	 */
	public boolean hasSystemViewRoots()
	{
		return true;
	}
	/**
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#showingConnections()
	 */
	public boolean showingConnections()
	{
		return true;
	}
	/**
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#getConnectionChildren(org.eclipse.rse.model.IHost)
	 */
	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		return null;
	}
	/**
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#hasConnectionChildren(org.eclipse.rse.model.IHost)
	 */
	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		return false;
	}
}