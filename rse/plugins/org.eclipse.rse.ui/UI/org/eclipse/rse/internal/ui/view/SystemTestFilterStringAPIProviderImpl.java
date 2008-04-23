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
 * Tobias Schwarz   (Wind River) - [173267] "empty list" should not be displayed 
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.view.SystemAbstractAPIProvider;


/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is used when the contents are used to show the resolution of a single filter string.
 */
public class SystemTestFilterStringAPIProviderImpl 
       extends SystemAbstractAPIProvider
{


	protected String filterString = null;
	protected ISubSystem subsystem = null;

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
	 	   children = subsystem.resolveFilterString(filterString, new NullProgressMonitor());
	 	   children = checkForEmptyList(children, null, true);
		} catch (InterruptedException exc)
		{
		   children = getCancelledMessageObject();
		} catch (Exception exc)
		{
		   children = getFailedMessageObject();			
		   SystemBasePlugin.logError("Error in SystemTestFilterStringAPIProviderImpl#getSystemViewRoots()",exc); //$NON-NLS-1$
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


    // ----------------------------------
    // OUR OWN METHODS...    
    // ----------------------------------
     
}