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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemHelpers;
import org.eclipse.rse.core.subsystems.util.ISubsystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;



/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is used when the contents are the children of a particular subsystem. 
 * Used when user right clicks on a filter and selects Open In New Perspective.
 */
public class SystemViewAPIProviderForFilters 
       extends SystemAbstractAPIProvider implements ISystemMessages
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
    	
    	ISubSystemConfiguration ssf = SubSystemHelpers.getParentSubSystemFactory(referencedFilter);    	
    	boolean promptable = referencedFilter.isPromptable();
    	//System.out.println("Promptable? " + promptable);
    	if (promptable)
    	{
		   children = new SystemMessageObject[1];
    	   try {
    		   ISubsystemConfigurationAdapter adapter = (ISubsystemConfigurationAdapter)ssf.getAdapter(ISubsystemConfigurationAdapter.class);
             ISystemFilter newFilter = adapter.createFilterByPrompting(ssf, fRef, getShell());
             if (newFilter == null)
             {
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_CANCELLED),
		                                              ISystemMessageObject.MSGTYPE_CANCEL,element);
             }
             else // filter successfully created!
             {
             	// return "filter created successfully" message object for this node
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_FILTERCREATED),
		                                              ISystemMessageObject.MSGTYPE_OBJECTCREATED,element);
		        // select the new filter reference...
		        ISubSystem ss = fRef.getSubSystem();
		        ISystemFilterReference sfr = fRef.getParentSystemFilterReferencePool().getExistingSystemFilterReference(ss, newFilter);
		        ISystemViewInputProvider inputProvider = this;
		        if ((sfr != null) && (inputProvider != null) && (inputProvider.getViewer()!=null))
		        {
		          ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		          SystemResourceChangeEvent event = new SystemResourceChangeEvent(sfr, ISystemResourceChangeEvents.EVENT_SELECT_EXPAND, null);
                  Viewer v = inputProvider.getViewer();
                  if (v instanceof ISystemResourceChangeListener)
                  {
                    //sr.fireEvent((ISystemResourceChangeListener)v, event); // only expand in the current viewer, not all viewers!
                    sr.postEvent((ISystemResourceChangeListener)v, event); // only expand in the current viewer, not all viewers!
                  }
		        }
             }
    	   } catch (Exception exc) {
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_FAILED),
		                                              ISystemMessageObject.MSGTYPE_ERROR, element);
		     	SystemBasePlugin.logError("Exception prompting for filter ",exc);          
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
		     	Object[] allChildren = ss.resolveFilterStrings(filterStrings,getShell());
                int nbrNestedFilters = (nestedFilterReferences==null) ? 0: nestedFilterReferences.length;
                children = new Object[nbrNestedFilters + allChildren.length];
                int idx = 0;
                for (idx=0; idx<nbrNestedFilters; idx++)
                   children[idx] = nestedFilterReferences[idx];
                for (int jdx=0; jdx<allChildren.length; jdx++)
                   children[idx++] = allChildren[jdx];
		     }
		     catch (InterruptedException exc)
		     {
		     	children = new SystemMessageObject[1];
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_CANCELLED),
		                                              ISystemMessageObject.MSGTYPE_CANCEL,element);
		     	SystemBasePlugin.logDebugMessage(this.getClass().getName(),"Filter resolving canceled by user.");
		     }
		     catch (Exception exc)
		     {
		     	children = new SystemMessageObject[1];
		        children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_FAILED),
		                                              ISystemMessageObject.MSGTYPE_ERROR, element);
		     	SystemBasePlugin.logError("Exception resolving filters' strings ",exc);
		     } // message already issued

			 if ((children == null) || (children.length==0))
			 {
		       children = new SystemMessageObject[1];
		       children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_EMPTY),
		                                             ISystemMessageObject.MSGTYPE_EMPTY, element);
			 }    	     
    	   	 return children;
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

    /**
     * Return true to show right-click popup actions on objects in the tree.
     * We return true.
     */
    public boolean showActions()
    {
    	return true;
    }

}