/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.ui.view.SystemAbstractAPIProvider;

/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is used when the contents are the children of a particular connection. 
 * Used when user right clicks on a connection and selects Open In New Perspective.
 */
public class SystemViewAPIProviderForConnections 
       extends SystemAbstractAPIProvider
{


	protected IHost connection = null;
	
	/**
	 * Constructor 
	 * @param connection The connection object we are drilling down on.
	 */
	public SystemViewAPIProviderForConnections(IHost connection)
	{
		super();
		this.connection = connection;
	}
	
	/**
	 * Get the input connection object. 
	 */
	public IHost getConnection()
	{
		return connection;
	}
	/**
	 * Reset the input connection object.
	 */
	public void setConnection(IHost connection)
	{
		this.connection = connection;
	}

    // ----------------------------------
    // SYSTEMVIEWINPUTPROVIDER METHODS...
    // ----------------------------------
	/**
	 * Return the children objects to consistute the root elements in the system view tree.
	 * We return all subsystems for this connection
	 */
	public Object[] getSystemViewRoots()
	{
		//return sr.getSubSystems(connection);
		return getViewAdapter(connection).getChildren(connection, new NullProgressMonitor()); // pc42690
	}
	/**
	 * Return true if {@link #getSystemViewRoots()} will return a non-empty list
	 * We return true, assuming there is at least one subsystem object
	 */
	public boolean hasSystemViewRoots()
	{
		return true;
	}
	/**
	 * This method is called by the connection adapter when the user expands
	 *  a connection. This method must return the child objects to show for that
	 *  connection.
	 * <p>Not applicable for us.
	 */
	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		//return sr.getSubSystems(selectedConnection);
		return getViewAdapter(selectedConnection).getChildren(selectedConnection, new NullProgressMonitor()); // pc42690
	}
	/**
	 * This method is called by the connection adapter when deciding to show a plus-sign
	 * or not beside a connection. Return true if this connection has children to be shown.
	 * <p>Not applicable for us.
	 */
	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		return true;
	}
}