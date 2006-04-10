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
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.model.IHost;


/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is used when the contents are the children of a particular subsystem. 
 * Used when user right clicks on a filter pool and selects Open In New Perspective.
 */
public class SystemViewAPIProviderForFilterPools 
       extends SystemAbstractAPIProvider
{


	protected ISubSystem subsystem = null;
	protected ISystemFilterPool filterPool = null;
	protected ISystemFilterPoolReference filterPoolReference = null;
		
	/**
	 * Constructor 
	 * @param filterPoolReference The filterpool reference object we are drilling down on.
	 */
	public SystemViewAPIProviderForFilterPools(ISystemFilterPoolReference filterPoolReference)
	{
		super();
		setFilterPoolReference(filterPoolReference);
	}
	
	/**
	 * Get the parent subsystem object. 
	 */
	public ISubSystem getSubSystem()
	{
		return subsystem;
	}
	/**
	 * Get the input filter pool reference object. 
	 */
	public ISystemFilterPoolReference getSystemFilterPoolReference()
	{
		return filterPoolReference;
	}
	/**
	 * Get the filter pool referenced by the input filter pool reference object. 
	 */
	public ISystemFilterPool getSystemFilterPool()
	{
		return filterPool;
	}

	/**
	 * Reset the input filter pool reference object.
	 */
	public void setFilterPoolReference(ISystemFilterPoolReference filterPoolReference)
	{
		this.filterPoolReference = filterPoolReference;
		this.filterPool = filterPoolReference.getReferencedFilterPool();
		this.subsystem = (ISubSystem)filterPoolReference.getProvider();
	}

    // ----------------------------------
    // SYSTEMVIEWINPUTPROVIDER METHODS...
    // ----------------------------------
	/**
	 * Return the children objects to consistute the root elements in the system view tree.
	 * We return all filters for this filter pool
	 */
	public Object[] getSystemViewRoots()
	{
		return filterPoolReference.getSystemFilterReferences(getSubSystem());
	}
	/**
	 * Return true if {@link #getSystemViewRoots()} will return a non-empty list
	 * We return true if the referenced filter pool has any filters
	 */
	public boolean hasSystemViewRoots()
	{
		return (filterPool.getSystemFilterCount() > 0);
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