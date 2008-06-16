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
 * David McKnight   (IBM)        - [207100] adding SystemRemoteChangeEventManager.isRegisteredSystemRemoteChangeListener
 * Martin Oberhuber (Wind River) - [218659] Make *EventManager, *ChangeManager thread-safe
 * David Dykstal (IBM) - [227750] add a test for registered listeners
 ********************************************************************************/

package org.eclipse.rse.internal.core.model;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvent;
import org.eclipse.rse.core.events.ISystemRemoteChangeListener;

/**
 * Manages the list of registered remote resource change listeners.
 */
public class SystemRemoteChangeEventManager
{
    private ISystemRemoteChangeListener[] listeners = new ISystemRemoteChangeListener[0];
    private Object lockObject = new Object();

    /**
     * Constructor
     */
    public SystemRemoteChangeEventManager()
    {
    }
    
    /**
     * Query if the given listener is already listening for SystemRemoteChange events.
     * @param l the listener to check
     * @return <code>true</code> if the listener is already registered
     */
    public boolean isRegisteredSystemRemoteChangeListener(ISystemRemoteChangeListener l)
    {
    	synchronized(lockObject) {
    		for(int i=0; i<listeners.length; i++) {
    			if(listeners[i].equals(l)) {
    				return true;
    			}
    		}
        	return false;
    	}
    }

    /**
     * Add a listener to list of listeners.
     * If this object is already in the list, this does nothing.
     * @param l the new listener to add.
     */
    public void addSystemRemoteChangeListener(ISystemRemoteChangeListener l)
    {
    	if (l==null) throw new IllegalArgumentException();
    	synchronized(lockObject) {
    		if (!isRegisteredSystemRemoteChangeListener(l)) {
    			int len = listeners.length;
    			ISystemRemoteChangeListener[] oldListeners = listeners;
    			listeners = new ISystemRemoteChangeListener[len+1];
    			System.arraycopy(oldListeners, 0, listeners, 0, len);
    			listeners[len] = l;
    		}
    	}
    }

    /**
     * Remove a listener from the list of listeners.
     * If this object is not in the list, this does nothing.
     * @param l the listener to remove
     */
    public void removeSystemRemoteChangeListener(ISystemRemoteChangeListener l)
    {
    	synchronized(lockObject) {
    		if (isRegisteredSystemRemoteChangeListener(l)) {
        		//Thread-safety: create a new List when removing, to avoid problems in notify()
    			int len = listeners.length;
    			ISystemRemoteChangeListener[] oldListeners = listeners;
    			listeners = new ISystemRemoteChangeListener[len-1];
    			for (int i=0, j=0; i<len; i++) {
    				if (!oldListeners[i].equals(l)) {
    					listeners[j++] = oldListeners[i];
    				}
    			}
    		}
    	}
    }

    /**
     * Notify all registered listeners of the given event.
     * TODO document on which thread the event is being sent.
     * @param event the event to send.
     */
    public void notify(ISystemRemoteChangeEvent event)
    {
    	//Thread-safe event firing: fire events on a current snapshot of the list.
    	//If not done that way, and a thread removes a listener while event firing 
    	//is in progress, an ArrayIndexOutOfBoundException might occur.
    	ISystemRemoteChangeListener[] currentListeners;
    	synchronized(lockObject) {
    		currentListeners = listeners;
    	}
    	for (int idx=0; idx<currentListeners.length; idx++) {
    	   currentListeners[idx].systemRemoteResourceChanged(event);
    	}
    }

    /**
     * Test if a manager has any listeners registered.
     * @return true if there are any listeners, false if not.
     */
    public boolean hasListeners() {
    	boolean result = false;
    	synchronized(lockObject) {
    		result = listeners.length > 0;
    	}
    	return result;
    }


}