/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.ui.internal.model.SystemNewConnectionPromptObject;


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
	private IRSESystemType[] systemTypes;
	
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
	public void setSystemTypes(IRSESystemType[] systemTypes)
	{
		this.systemTypes = systemTypes;
	}
	/**
	 * Return the system types we are restricted by
	 */
	public IRSESystemType[] getSystemTypes()
	{
		return systemTypes;
	}
	
	// REQUIRED METHODS...
	
	/**
	 * @see org.eclipse.rse.core.model.ISystemViewInputProvider#getSystemViewRoots()
	 */
	public Object[] getSystemViewRoots()
	{
		//System.out.println("Inside getSystemViewRoots. showNew = "+showNew);
		IHost[] conns = null;
		if (systemTypes == null)
			conns = RSECorePlugin.getTheSystemRegistry().getHosts();
		else
			conns = RSECorePlugin.getTheSystemRegistry().getHostsBySystemTypes(systemTypes);			
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
	 * @see org.eclipse.rse.core.model.ISystemViewInputProvider#hasSystemViewRoots()
	 */
	public boolean hasSystemViewRoots()
	{
		return true;
	}
	/**
	 * @see org.eclipse.rse.core.model.ISystemViewInputProvider#showingConnections()
	 */
	public boolean showingConnections()
	{
		return true;
	}
	/**
	 * @see org.eclipse.rse.core.model.ISystemViewInputProvider#getConnectionChildren(org.eclipse.rse.core.model.IHost)
	 */
	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		return null;
	}
	/**
	 * @see org.eclipse.rse.core.model.ISystemViewInputProvider#hasConnectionChildren(org.eclipse.rse.core.model.IHost)
	 */
	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		return false;
	}
}