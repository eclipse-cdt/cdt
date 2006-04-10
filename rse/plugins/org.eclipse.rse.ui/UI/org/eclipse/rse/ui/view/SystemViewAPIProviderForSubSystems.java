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
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;


/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is used when the contents are the children of a particular subsystem. 
 * Used when user right clicks on a subsystem and selects Open In New Perspective.
 */
public class SystemViewAPIProviderForSubSystems 
       extends SystemAbstractAPIProvider
{


	protected ISubSystem subsystem = null;
	
	/**
	 * Constructor 
	 * @param subsystem The subsystem object we are drilling down on.
	 */
	public SystemViewAPIProviderForSubSystems(ISubSystem subsystem)
	{
		super();
		setSubSystem(subsystem);
	}
	
	/**
	 * Get the input subsystem object. 
	 */
	public ISubSystem getSubSystem()
	{
		return subsystem;
	}
	/**
	 * Reset the input subsystem object.
	 */
	public void setSubSystem(ISubSystem subsystem)
	{
		this.subsystem = subsystem;
	}

    // ----------------------------------
    // SYSTEMVIEWINPUTPROVIDER METHODS...
    // ----------------------------------
	/**
	 * Return the children objects to consistute the root elements in the system view tree.
	 * We return all filter pools for this subsystem
	 */
	public Object[] getSystemViewRoots()
	{
		return subsystem.getChildren();
	}
	/**
	 * Return true if {@link #getSystemViewRoots()} will return a non-empty list
	 * We return subsystem.hasChildren()
	 */
	public boolean hasSystemViewRoots()
	{
		return subsystem.hasChildren();
	}
	/**
	 * This method is called by the connection adapter when the user expands
	 *  a connection. This method must return the child objects to show for that
	 *  connection.
	 * <p>Not applicable for us.
	 */
	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		return null;
	}
	/**
	 * This method is called by the connection adapter when deciding to show a plus-sign
	 * or not beside a connection. Return true if this connection has children to be shown.
	 * <p>Not applicable for us.
	 */
	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		return false;
	}

    /**
     * Return true to show right-click popup actions on objects in the tree.
     * We return true.
     */
    public boolean showActions()
    {
    	return true;
    }

}