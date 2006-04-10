/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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
import java.util.HashMap;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.ui.views.properties.IPropertyDescriptor;


/**
 * @author dmcknigh
 */
public class SystemTableViewColumnManager
{
    private Viewer _viewer;
	protected HashMap _descriptorCache;
    public SystemTableViewColumnManager(Viewer viewer)
    {
        _viewer = viewer;
        _descriptorCache = new HashMap();
    }

	protected IPropertyDescriptor[] getCachedDescriptors(ISystemViewElementAdapter adapter)
	{
	    Object descriptors = _descriptorCache.get(adapter);
	    if (descriptors != null && descriptors instanceof IPropertyDescriptor[])
	    {
	        return (IPropertyDescriptor[])descriptors;
	    }
	    return null;
	}
	
	protected void putCachedDescriptors(ISystemViewElementAdapter adapter, IPropertyDescriptor[] descriptors)
	{
	    _descriptorCache.put(adapter, descriptors);
	}
	
	public void setCustomDescriptors(ISystemViewElementAdapter adapter, IPropertyDescriptor[] descriptors)
	{
	    putCachedDescriptors(adapter, descriptors);
	    SystemPreferencesManager mgr = SystemPreferencesManager.getPreferencesManager();
	    String historyKey = getHistoryKey(adapter);
	    String[] history = new String[descriptors.length];
	    for (int i = 0; i < descriptors.length; i++)
	    {
	        history[i] = descriptors[i].getId().toString();
	    }
	    
	    mgr.setWidgetHistory(historyKey, history);
	}
	
	/**
	 * Gets the property descriptors to display as columns in the table
	 * The set of descriptors and their order may change depending on user customizations
	 * @param adapter
	 * @return
	 */
	public IPropertyDescriptor[] getVisibleDescriptors(ISystemViewElementAdapter adapter)
	{			
		if (adapter != null)
		{		   
		    IPropertyDescriptor[] descriptors = getCachedDescriptors(adapter);
		    if (descriptors == null)
		    {
		        return getCustomDescriptors(adapter);
		    }
		    else
		    {
		        return descriptors;
		    }
		}

		return new IPropertyDescriptor[0];
	}
	
	private String getHistoryKey(ISystemViewElementAdapter adapter)
	{
	    String adapterName = adapter.getClass().getName();
	    String viewName = _viewer.getClass().getName();
	    return adapterName + ":" + viewName;
	}
	
	protected IPropertyDescriptor[] getCustomDescriptors(ISystemViewElementAdapter adapter)
	{
	    IPropertyDescriptor[] uniqueDescriptors = adapter.getUniquePropertyDescriptors();
	  
	    SystemPreferencesManager mgr = SystemPreferencesManager.getPreferencesManager();
	    String historyKey = getHistoryKey(adapter);
	    String[] history = mgr.getWidgetHistory(historyKey);
	    
	    // determine the order and which of the uniqueDescriptors to use based on the history
	    if (history != null && history.length > 0)
	    {
	        int len = history.length;
	        if (uniqueDescriptors != null && uniqueDescriptors.length < len)
	        {
	            len = uniqueDescriptors.length;
	        }
	        IPropertyDescriptor[] customDescriptors = new IPropertyDescriptor[len];
	        for (int i = 0; i < len; i++)
	        {
	            String propertyName = history[i];
	            // find the associated descriptor
	            boolean found = false;
	            for (int d = 0; d < uniqueDescriptors.length && !found; d++)
	            {
	                IPropertyDescriptor descriptor = uniqueDescriptors[d];
	                if (propertyName.equals(descriptor.getId().toString()))
	                {	
	                    customDescriptors[i] = descriptor;
	                    found = true;
	                }
	            }
	            // DKM - problem here - no such descriptor exists anymore
	            if (found == false)
	            {
	                // invalidate the current history
	                setCustomDescriptors(adapter, uniqueDescriptors);
	                return uniqueDescriptors;
	            }
	        }
	        return customDescriptors;
	    }
	    else
	    {
	        setCustomDescriptors(adapter, uniqueDescriptors);
	    }
	    
	    return uniqueDescriptors;
	}
}