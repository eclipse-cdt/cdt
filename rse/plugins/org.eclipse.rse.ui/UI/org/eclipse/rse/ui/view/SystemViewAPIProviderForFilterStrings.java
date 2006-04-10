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
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.filters.ISystemFilterStringReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.ui.ISystemMessages;


/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is used when the contents are the children of a particular subsystem. 
 * Used when user right clicks on a filter string and selects Open In New Perspective.
 */
public class SystemViewAPIProviderForFilterStrings 
       extends SystemAbstractAPIProvider implements ISystemMessages
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
			children = ss.resolveFilterString(filterStringReference.getString(),getShell());
			if ((children == null) || (children.length==0))
			{
		      children = new SystemMessageObject[1];
		      children[0] = new SystemMessageObject(SystemPlugin.getPluginMessage(MSG_EXPAND_EMPTY),
		                                            ISystemMessageObject.MSGTYPE_EMPTY, element);
			}
		}
		catch (InterruptedException exc)
		{
		    children = new SystemMessageObject[1];
		    children[0] = new SystemMessageObject(SystemPlugin.getPluginMessage(MSG_EXPAND_CANCELLED),
		                                          ISystemMessageObject.MSGTYPE_CANCEL, element);
		 	System.out.println("Canceled.");
		}
		catch (Exception exc)
		{
		    children = new SystemMessageObject[1];
		    children[0] = new SystemMessageObject(SystemPlugin.getPluginMessage(MSG_EXPAND_FAILED),
		                                          ISystemMessageObject.MSGTYPE_ERROR, element);
		    System.out.println("Exception resolving filter strings: " + exc.getClass().getName() + ", " + exc.getMessage());			
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

    /**
     * Return true to show right-click popup actions on objects in the tree.
     * We return true.
     */
    public boolean showActions()
    {
    	return true;
    }

}