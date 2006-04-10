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

import org.eclipse.rse.model.ISystemModelChangeEvent;
import org.eclipse.rse.model.ISystemModelChangeListener;


/**
 * Manages the list of registered model change listeners.
 */
public class SystemModelChangeEventManager
{
    private Vector listeners = new Vector();

    /**
     * Constructor
     */
    public SystemModelChangeEventManager()
    {
    }

    /**
     * Add a listener to list of listeners. If this object is already in
     *  the list, this does nothing.
     */
    public void addSystemModelChangeListener(ISystemModelChangeListener l)
    {
    	if (!listeners.contains(l))
    	  listeners.addElement(l);
    }

    /**
     * Remove a listener to list of listeners. If this object is not in
     *  the list, this does nothing.
     */
    public void removeSystemModelChangeListener(ISystemModelChangeListener l)
    {
    	if (listeners.contains(l))
    	  listeners.removeElement(l);
    }

    /**
     * Notify all registered listeners of the given event
     */
    public void notify(ISystemModelChangeEvent event)
    {
    	for (int idx=0; idx<listeners.size(); idx++)
    	{
    	   ISystemModelChangeListener l = (ISystemModelChangeListener)listeners.elementAt(idx);
    	   l.systemModelResourceChanged(event);
    	}
    }

}