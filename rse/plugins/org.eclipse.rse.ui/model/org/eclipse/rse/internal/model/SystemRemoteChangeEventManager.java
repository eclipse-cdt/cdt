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

package org.eclipse.rse.internal.model;
import java.util.Vector;

import org.eclipse.rse.model.ISystemRemoteChangeEvent;
import org.eclipse.rse.model.ISystemRemoteChangeListener;


/**
 * Manages the list of registered remote resource change listeners.
 */
public class SystemRemoteChangeEventManager
{
    private Vector listeners = new Vector();

    /**
     * Constructor
     */
    public SystemRemoteChangeEventManager()
    {
    }

    /**
     * Add a listener to list of listeners. If this object is already in
     *  the list, this does nothing.
     */
    public void addSystemRemoteChangeListener(ISystemRemoteChangeListener l)
    {
    	if (!listeners.contains(l))
    	  listeners.addElement(l);
    }

    /**
     * Remove a listener to list of listeners. If this object is not in
     *  the list, this does nothing.
     */
    public void removeSystemRemoteChangeListener(ISystemRemoteChangeListener l)
    {
    	if (listeners.contains(l))
    	  listeners.removeElement(l);
    }

    /**
     * Notify all registered listeners of the given event
     */
    public void notify(ISystemRemoteChangeEvent event)
    {
    	for (int idx=0; idx<listeners.size(); idx++)
    	{
    	   ISystemRemoteChangeListener l = (ISystemRemoteChangeListener)listeners.elementAt(idx);
    	   l.systemRemoteResourceChanged(event);
    	}
    }

}