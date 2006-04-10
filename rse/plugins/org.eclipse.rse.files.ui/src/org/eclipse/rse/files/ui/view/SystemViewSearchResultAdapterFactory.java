/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.view;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.services.search.IHostSearchResult;
import org.eclipse.rse.ui.view.ISystemDragDropAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;



/**
 * This factory maps requests for an adapter object from a given
 * element object. A search result adapter factory maps a search 
 * result object to a search result adapter.
 */
public class SystemViewSearchResultAdapterFactory implements IAdapterFactory 
{
	private SystemViewRemoteSearchResultAdapter searchResultAdapter = new SystemViewRemoteSearchResultAdapter();

	
	/**
	 * @see IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() 
	{
	    return new Class[] {ISystemViewElementAdapter.class, ISystemDragDropAdapter.class, ISystemRemoteElementAdapter.class, IPropertySource.class, IWorkbenchAdapter.class, IActionFilter.class};		
	}
	/**
	 * Called by our plugin's startup method to register our adaptable object types 
	 * with the platform. We prefer to do it here to isolate/encapsulate all factory
	 * logic in this one place.
	 */
	public void registerWithManager(IAdapterManager manager)
	{
		manager.registerAdapters(this, IHostSearchResult.class);
	}
	/**
	 * @see IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) 
	{
	    Object adapter = null;
	    if (adaptableObject instanceof IHostSearchResult)
	    	adapter = searchResultAdapter;
	  
	    if ((adapter != null) && (adapterType == IPropertySource.class))
	    {	
	        ((ISystemViewElementAdapter)adapter).setPropertySourceInput(adaptableObject);
	    }		
	    else if (adapter == null)
	    {
	    	SystemBasePlugin.logWarning("No adapter found for object of type: " + adaptableObject.getClass().getName());
	    }	      	    
		return adapter;
	}


}