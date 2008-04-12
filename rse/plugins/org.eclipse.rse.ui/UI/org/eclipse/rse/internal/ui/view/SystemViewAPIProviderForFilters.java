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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Tobias Schwarz   (Wind River) - [173267] "empty list" should not be displayed 
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * Martin Oberhuber (Wind River) - [218524][api] Remove deprecated ISystemViewInputProvider#getShell()
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemHelpers;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;
import org.eclipse.rse.ui.view.SystemAbstractAPIProvider;



/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is used when the contents are the children of a particular subsystem. 
 * Used when user right clicks on a filter and selects Open In New Perspective.
 */
public class SystemViewAPIProviderForFilters 
       extends SystemAbstractAPIProvider
{


	protected ISubSystem subsystem = null;
	protected ISystemFilterPool filterPool = null;
	protected ISystemFilterPoolReference filterPoolReference = null;
	protected ISystemFilterReference filterReference = null;
	protected ISystemFilter filter = null;
		
	/**
	 * Constructor 
	 * @param filterReference The filter reference object we are drilling down on.
	 */
	public SystemViewAPIProviderForFilters(ISystemFilterReference filterReference)
	{
		super();
		setFilterReference(filterReference);
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
	 * Get the input filter reference object.
	 */
	public ISystemFilterReference getSystemFilterReference()
	{
		return filterReference;
	}
	/**
	 * Get the filter referenced by the input filter reference object. 
	 */
	public ISystemFilter getSystemFilter()
	{
		return filter;
	}

	/**
	 * Reset the input filter reference object.
	 */
	public void setFilterReference(ISystemFilterReference filterReference)
	{
		this.filterReference = filterReference;
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
		// see getChildren() OF SystemViewFilterReferenceAdapter. TODO: RE-USE VS COPY!
    	Object[] children = null;		
		ISystemFilterReference fRef = filterReference;
		Object element = fRef;
		//Object[] children = fRef.getChildren(getShell()); 
    	ISystemFilter referencedFilter = fRef.getReferencedFilter();
    	
    	ISubSystemConfiguration ssf = SubSystemHelpers.getParentSubSystemConfiguration(referencedFilter);    	
    	boolean promptable = referencedFilter.isPromptable();
    	//System.out.println("Promptable? " + promptable);
    	if (promptable)
    	{
		   children = new SystemMessageObject[1];
    	   try {
    		   ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssf.getAdapter(ISubSystemConfigurationAdapter.class);
             ISystemFilter newFilter = adapter.createFilterByPrompting(ssf, fRef, RSEUIPlugin.getTheSystemRegistryUI().getShell());
             if (newFilter == null)
             {
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_CANCELLED),
		                                              ISystemMessageObject.MSGTYPE_CANCEL,element);
             }
             else // filter successfully created!
             {
             	// return "filter created successfully" message object for this node
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FILTERCREATED),
		                                              ISystemMessageObject.MSGTYPE_OBJECTCREATED,element);
		        // select the new filter reference...
		        ISubSystem ss = fRef.getSubSystem();
		        ISystemFilterReference sfr = fRef.getParentSystemFilterReferencePool().getExistingSystemFilterReference(ss, newFilter);
		        ISystemViewInputProvider inputProvider = this;
		        if ((sfr != null)  && (inputProvider.getViewer()!=null))
		        {
		          SystemResourceChangeEvent event = new SystemResourceChangeEvent(sfr, ISystemResourceChangeEvents.EVENT_SELECT_EXPAND, null);
                  Viewer v = (Viewer)inputProvider.getViewer();
                  if (v instanceof ISystemResourceChangeListener)
                  {
                    //sr.fireEvent((ISystemResourceChangeListener)v, event); // only expand in the current viewer, not all viewers!
                	RSEUIPlugin.getTheSystemRegistryUI().postEvent((ISystemResourceChangeListener)v, event); // only expand in the current viewer, not all viewers!
                  }
		        }
             }
    	   } catch (Exception exc) {
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FAILED),
		                                              ISystemMessageObject.MSGTYPE_ERROR, element);
		     	SystemBasePlugin.logError("Exception prompting for filter ",exc);           //$NON-NLS-1$
    	   }
    	   //RSEUIPlugin.logDebugMessage(this.getClass().getName(),"returning children");
    	   return children;
    	}
    	ISubSystem ss = fRef.getSubSystem();
    	Object[] nestedFilterReferences = fRef.getSystemFilterReferences(ss);    	
    	int nbrFilterStrings = referencedFilter.getFilterStringCount();
    	if (nbrFilterStrings == 0)
    	  return nestedFilterReferences;
    	else
    	{

    	   { 
    	     String[] filterStrings = referencedFilter.getFilterStrings();    	   	 
		     try
		     {
		    	 if (nestedFilterReferences != null)
		    	 {
		     	Object[] allChildren = ss.resolveFilterStrings(filterStrings, new NullProgressMonitor());
                int nbrNestedFilters = nestedFilterReferences.length;
                children = new Object[nbrNestedFilters + allChildren.length];
                int idx = 0;
                for (idx=0; idx<nbrNestedFilters; idx++)
                   children[idx] = nestedFilterReferences[idx];
                for (int jdx=0; jdx<allChildren.length; jdx++)
                   children[idx++] = allChildren[jdx];
		    	 }
		     }
		     catch (InterruptedException exc)
		     {
		     	children = new SystemMessageObject[1];
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_CANCELLED),
		                                              ISystemMessageObject.MSGTYPE_CANCEL,element);
		     	SystemBasePlugin.logDebugMessage(this.getClass().getName(),"Filter resolving cancelled by user."); //$NON-NLS-1$
		     }
		     catch (Exception exc)
		     {
		     	children = new SystemMessageObject[1];
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FAILED),
		                                              ISystemMessageObject.MSGTYPE_ERROR, element);
		     	SystemBasePlugin.logError("Exception resolving filters' strings ",exc); //$NON-NLS-1$
		     } // message already issued

    	   	 return checkForEmptyList(children, element, true);
    	   }	
    	}		
	}
	/**
	 * Return true if {@link #getSystemViewRoots()} will return a non-empty list
	 * We return true if the referenced filter has nested filters or has filter strings.
	 */
	public boolean hasSystemViewRoots()
	{
    	int nbrNestedFilters = filter.getSystemFilterCount();
    	int nbrFilterStrings = filter.getFilterStringCount();
		return (nbrNestedFilters > 0) || (nbrFilterStrings > 0);				
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