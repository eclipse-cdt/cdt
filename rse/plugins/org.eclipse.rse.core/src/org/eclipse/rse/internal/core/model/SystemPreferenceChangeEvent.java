/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 *******************************************************************************/

package org.eclipse.rse.internal.core.model;
import java.util.EventObject;

import org.eclipse.rse.core.events.ISystemPreferenceChangeEvent;
import org.eclipse.rse.core.events.ISystemPreferenceChangeEvents;


/**
 * Event object sent to ISystemPreferenceChangeListeners when a
 * remote system preference is changed.
 * @see org.eclipse.rse.core.events.ISystemPreferenceChangeEvents
 */
public class SystemPreferenceChangeEvent
	   extends EventObject
	   implements ISystemPreferenceChangeEvent
{
	private static final long serialVersionUID = 1;
	private int type;
    private Object oldValue, newValue;
	
	/**
	 * Constructor 
     * @see org.eclipse.rse.core.events.ISystemPreferenceChangeEvents
	 * @param type The type of event, one of {@link ISystemPreferenceChangeEvents} constants.
	 * @param oldValue the old value of the Preference
	 * @param newValue the old value of the Preference
	 */
	public SystemPreferenceChangeEvent(int type, Object oldValue, Object newValue)
	{
		super(newValue);
		setType(type);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * Return the type of the event 
     * @see org.eclipse.rse.core.events.ISystemPreferenceChangeEvents
	 */
	public int getType()
	{
		return type;
	}
    /**
     * Return the old value prior to the change
     */
    public Object getOldValue()
    {
    	return oldValue;
    }
    /**
     * Return the new value after the change
     */
    public Object getNewValue()
    {
    	return newValue;
    }

    /**
     * Set the type
     * @see org.eclipse.rse.core.events.ISystemPreferenceChangeEvents
     */
    public void setType(int type)
    {
    	this.type = type;
    }
    /**
     * Set the old value prior to the change
     */
    public void setOldValue(Object value)
    {
    	this.oldValue = value;
    }
    /**
     * Return the new value after the change
     */
    public void setNewValue(Object value)
    {
    	this.newValue = value;
    }

}
