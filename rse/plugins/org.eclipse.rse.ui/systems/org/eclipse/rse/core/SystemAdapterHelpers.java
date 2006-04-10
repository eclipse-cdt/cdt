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

package org.eclipse.rse.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewInputProvider;
import org.eclipse.rse.ui.view.SystemViewAdapterFactory;


/**
 * This class has static helper methods that will get an adapter given an object.
 */
public class SystemAdapterHelpers 
{
	
	
    /**
     * Returns the implementation of ISystemViewElement for the given
     * object.  Returns null if the adapter is not defined or the
     * object is not adaptable.
     */
    public static ISystemViewElementAdapter getAdapter(Object o) 
    {
    	ISystemViewElementAdapter adapter = null;
    	if (!(o instanceof IAdaptable)) 
          adapter = (ISystemViewElementAdapter)Platform.getAdapterManager().getAdapter(o,ISystemViewElementAdapter.class);
        else        
    	  adapter = (ISystemViewElementAdapter)((IAdaptable)o).getAdapter(ISystemViewElementAdapter.class);
    	return adapter;
    }
    /**
     * Overload to use when calling from a viewer. This not only finds and returns
     *  the adapter, but also sets its viewer to the given viewer. Many actions rely
     *  on this being set.
     */
    public static ISystemViewElementAdapter getAdapter(Object o, Viewer viewer) 
    {
    	ISystemViewElementAdapter adapter = getAdapter(o);
    	if (adapter != null)
    	  adapter.setViewer(viewer);
    	return adapter;
    }
    
    /**
     * Overload to use when calling from a viewer. This not only finds and returns
     * the adapter, but also sets its viewer and input provider to the given viewer.
     * Many actions rely on this being set.
     */
    public static ISystemViewElementAdapter getAdapter(Object o, Viewer viewer, ISystemViewInputProvider inputProvider) 
    {
    	ISystemViewElementAdapter adapter = getAdapter(o, viewer);
    	
    	if (adapter != null) {
    	    adapter.setInput(inputProvider);
    	}
    	
    	return adapter;
    }

    /**
     * Returns the implementation of ISystemRemoteElementAdapter for the given
     *  remote object.  Returns null if this object does not adaptable to this.
     */
    public static ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
    {
        ISystemRemoteElementAdapter adapter = null;
    	if (!(o instanceof IAdaptable)) 
          adapter = (ISystemRemoteElementAdapter)Platform.getAdapterManager().getAdapter(o,ISystemRemoteElementAdapter.class);
        else
   	      adapter = (ISystemRemoteElementAdapter)((IAdaptable)o).getAdapter(ISystemRemoteElementAdapter.class);
    	return adapter;
    }
    
    /**
     * Overload to use when calling from a viewer. This not only finds and returns
     *  the adapter, but also sets its viewer to the given viewer. Many actions rely
     *  on this being set.
     */
    public static ISystemRemoteElementAdapter getRemoteAdapter(Object o, Viewer viewer) 
    {
    	ISystemRemoteElementAdapter adapter = getRemoteAdapter(o);
    	if ((adapter != null) && (adapter instanceof ISystemViewElementAdapter))
    	  ((ISystemViewElementAdapter)adapter).setViewer(viewer);
    	return adapter;
    }

	/**
	 * For pathpath access to our adapters for non-local objects in our model. Exploits the knowledge we use singleton adapters.
	 */
	public SystemViewAdapterFactory getSystemViewAdapterFactory()
	{
		return SystemPlugin.getDefault().getSystemViewAdapterFactory();
	}
	
}