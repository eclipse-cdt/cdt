/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [180519][api] declaratively register adapter factories
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui.view;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.services.search.IHostSearchResult;
import org.eclipse.rse.ui.SystemBasePlugin;
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
	
//	/**
//	 * Register this factory with the Platform's Adapter Manager.
//	 * Can be used for explicit registration, but we prefer doing it 
//	 * declaratively in plugin.xml so this is currently not used. 
//	 */
//	public void registerWithManager(IAdapterManager manager)
//	{
//		manager.registerAdapters(this, IHostSearchResult.class);
//	}
	
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
	    	SystemBasePlugin.logWarning("No adapter found for object of type: " + adaptableObject.getClass().getName()); //$NON-NLS-1$
	    }	      	    
		return adapter;
	}


}