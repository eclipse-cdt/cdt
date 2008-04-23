/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.ui.view.SystemAbstractAPIProvider;


/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is a special-case provider when we want the list to be empty.
 */
public class SystemEmptyListAPIProviderImpl 
       extends SystemAbstractAPIProvider
{


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


    // ----------------------------------
    // OUR OWN METHODS...    
    // ----------------------------------
     
}
