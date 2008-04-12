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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Tobias Schwarz   (Wind River) - [173267] "empty list" should not be displayed 
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.filters.ISystemFilterStringReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.view.SystemAbstractAPIProvider;


/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is used when the contents are the children of a particular subsystem. 
 * Used when user right clicks on a filter string and selects Open In New Perspective.
 */
public class SystemViewAPIProviderForFilterStrings 
       extends SystemAbstractAPIProvider
{


	protected ISubSystem subsystem = null;
	protected ISystemFilterPool filterPool = null;
	protected ISystemFilterPoolReference filterPoolReference = null;
	protected ISystemFilterReference filterReference = null;
	protected ISystemFilter filter = null;
	protected ISystemFilterString filterString = null;
	protected ISystemFilterStringReference filterStringReference = null;
		
	/**
	 * Constructor 
	 * @param filterStringReference The filter string reference object we are drilling down on.
	 */
	public SystemViewAPIProviderForFilterStrings(ISystemFilterStringReference filterStringReference)
	{
		super();
		setFilterStringReference(filterStringReference);
	}
	
	/**
	 * Get the parent subsystem object. 
	 */
	public ISubSystem getSubSystem()
	{
		return subsystem;
	}
	/**
	 * Get the parent filter pool reference object. 
	 */
	public ISystemFilterPoolReference getSystemFilterPoolReference()
	{
		return filterPoolReference;
	}
	/**
	 * Get the parent filter pool.
	 */
	public ISystemFilterPool getSystemFilterPool()
	{
		return filterPool;
	}
	/**
	 * Get the parent filter reference object.
	 */
	public ISystemFilterReference getSystemFilterReference()
	{
		return filterReference;
	}
	/**
	 * Get the parent filter
	 */
	public ISystemFilter getSystemFilter()
	{
		return filter;
	}
	/**
	 * Get the input filter string reference object.
	 */
	public ISystemFilterStringReference getSystemFilterStringReference()
	{
		return filterStringReference;
	}
	/**
	 * Get the filter referenced by the input filter string reference object. 
	 */
	public ISystemFilterString getSystemFilterString()
	{
		return filterString;
	}


	/**
	 * Reset the input filter string reference object.
	 */
	public void setFilterStringReference(ISystemFilterStringReference filterStringReference)
	{
		this.filterStringReference = filterStringReference;
		this.filterString = filterStringReference.getReferencedFilterString();
		this.filterReference = filterStringReference.getParent();
		this.filter = filterReference.getReferencedFilter();
		this.filterPoolReference = filterReference.getParentSystemFilterReferencePool();
		this.filterPool = filterPoolReference.getReferencedFilterPool();
		this.subsystem = (ISubSystem)filterPoolReference.getProvider();
	}

    // ----------------------------------
    // SYSTEMVIEWINPUTPROVIDER METHODS...
    // ----------------------------------
	/**
	 * Return the children objects to consistute the root elements in the system view tree.
	 * What we return depends on setting of Show Filter Strings.
	 */
	public Object[] getSystemViewRoots()
	{
		ISubSystem ss = subsystem;
		Object element = filterStringReference;
        Object[] children = null;
		try
		{
			children = ss.resolveFilterString(filterStringReference.getString(), new NullProgressMonitor());
			children = checkForEmptyList(children, element, true);
		}
		catch (InterruptedException exc)
		{
		    children = new SystemMessageObject[1];
		    children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_CANCELLED),
		                                          ISystemMessageObject.MSGTYPE_CANCEL, element);
		 	System.out.println("Cancelled."); //$NON-NLS-1$
		}
		catch (Exception exc)
		{
		    children = new SystemMessageObject[1];
		    children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FAILED),
		                                          ISystemMessageObject.MSGTYPE_ERROR, element);
		    System.out.println("Exception resolving filter strings: " + exc.getClass().getName() + ", " + exc.getMessage());			 //$NON-NLS-1$ //$NON-NLS-2$
		    exc.printStackTrace();
		} // message already issued        
		return children;		
	}
	/**
	 * Return true if {@link #getSystemViewRoots()} will return a non-empty list
	 * We return true
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
}
