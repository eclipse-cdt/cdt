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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Kevin Doyle (IBM) - [160280] Added a check to see if the item to be added already exists
 * Kevin Doyle (IBM) - [189421] Scratchpad not updated after Rename
 ********************************************************************************/

package org.eclipse.rse.ui.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.internal.ui.view.SystemViewScratchpadAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.ui.views.properties.IPropertySource;


/**
 * This is the root object for the Remote Scratchpad view. 
 */
public class SystemScratchpad implements IAdaptable
{
    public List _children;
    public static SystemViewScratchpadAdapter _adapter;
    
    public SystemScratchpad()
    {
        _children = new ArrayList();
    }
    
    public boolean hasChildren()
    {
        return !_children.isEmpty();
    }
    
    public boolean contains(Object obj)
    {
        return _children.contains(obj);
    }
    
    public Object[] getChildren()
    {
        Object[] children = new Object[_children.size()];
        for (int i = 0; i < _children.size(); i++)
        {
            children[i] = _children.get(i);
        }
        return children;
    }
    
    public void addChild(Object child)
    {
    	if (!contains(child))
    		_children.add(child);
    }
    
    public void removeChild(Object child)
    {
        _children.remove(child);
    }
    
    public void clearChildren()
    {
        _children.clear();
    }
    
    public Object getAdapter(Class adapterType)
    {
        if (adapterType == IPropertySource.class || 
                adapterType == ISystemViewElementAdapter.class ||
                adapterType == ISystemRemoteElementAdapter.class ||
                adapterType == ISystemDragDropAdapter.class)
        {
            if (_adapter == null)
        	{
            	_adapter = new SystemViewScratchpadAdapter();
        	}
        	return _adapter;
        }
        return Platform.getAdapterManager().getAdapter(this, adapterType);	
    }
    
    public void replace(String oldName, Object newItem) {
    	int index = -1;
    	Object adapterObj = Platform.getAdapterManager().getAdapter(newItem, ISystemDragDropAdapter.class);
    	ISystemDragDropAdapter adapter;
    	ISubSystem subSystemNewItem;
    	if (adapterObj != null && adapterObj instanceof ISystemDragDropAdapter) { 
    		adapter = (ISystemDragDropAdapter) adapterObj;
    		subSystemNewItem = adapter.getSubSystem(newItem);
    		
	    	// try to find the old item by oldName
	    	for (int i = 0; i < _children.size() && index == -1; i++) {
	    		Object element = _children.get(i);
	    		if (adapter.getSubSystem(element) == subSystemNewItem) {
	    			if (adapter.getAbsoluteName(element).equals(oldName)) {
	    				index = i;
	    			}
	    		}
	    	}
	    	// We found the old item remove it and replace with new one
	    	if (index != -1) {
	    		_children.remove(index);
	    		_children.add(index, newItem);
	    	}    	
    	}
    }
    
}