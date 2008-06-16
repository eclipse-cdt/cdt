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
 * Martin Oberhuber (Wind River) - [218659] Make *EventManager, *ChangeManager thread-safe
 * David Dykstal (IBM) - [227750] add a test for registered listeners
 ********************************************************************************/

package org.eclipse.rse.internal.core.model;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.events.ISystemPreferenceChangeEvent;
import org.eclipse.rse.core.events.ISystemPreferenceChangeListener;


/**
 * Manages the list of registered preference change listeners.
 */
public class SystemPreferenceChangeManager
{
    private List listeners = new ArrayList();
    private Object lockObject = new Object();

    /**
     * Constructor
     */
    public SystemPreferenceChangeManager()
    {
    }

    /**
     * Add a listener to list of listeners.
     * If this object is already in the list, this does nothing.
     * @param l the listener to add
     */
    public void addSystemPreferenceChangeListener(ISystemPreferenceChangeListener l)
    {
    	synchronized(lockObject) {
        	if (!listeners.contains(l))
          	  listeners.add(l);
    	}
    }

    /**
     * Remove a listener from the list of listeners.
     * If this object is not in the list, this does nothing.
     * @param l the listener to remove
     */
    public void removeSystemPreferenceChangeListener(ISystemPreferenceChangeListener l)
    {
    	synchronized(lockObject) {
    		//Thread-safety: create a new List when removing, to avoid problems in notify()
    		listeners = new ArrayList(listeners);
    		listeners.remove(l);
    	}
    }

    /**
     * Notify all registered listeners of the given event.
     * @param event the event to send
     */
    public void notify(ISystemPreferenceChangeEvent event)
    {
    	//Thread-safe event firing: fire events on a current snapshot of the list.
    	//If not done that way, and a thread removes a listener while event firing 
    	//is in progress, an ArrayIndexOutOfBoundException might occur.
    	List currentListeners;
    	synchronized(lockObject) {
    		currentListeners = listeners;
    	}
    	for (int idx=0; idx<currentListeners.size(); idx++) {
     	   ISystemPreferenceChangeListener l = (ISystemPreferenceChangeListener)currentListeners.get(idx);
    	   l.systemPreferenceChanged(event);
    	}
    }

    /**
     * Test if a manager has any listeners registered.
     * @return true if there are any listeners, false if not.
     */
    public boolean hasListeners() {
    	boolean result = false;
    	synchronized(lockObject) {
    		result = listeners.size() > 0;
    	}
    	return result;
    }

}