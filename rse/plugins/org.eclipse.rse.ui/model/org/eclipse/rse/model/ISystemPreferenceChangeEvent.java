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

package org.eclipse.rse.model;
/**
 * Interface of event sent when a remote system preference changes.
 * @see org.eclipse.rse.model.ISystemPreferenceChangeEvents
 */
public interface ISystemPreferenceChangeEvent extends ISystemResourceChangeEvents
{	

    /**
     * Returns the type of the event.
     * @see org.eclipse.rse.model.ISystemPreferenceChangeEvents
     * @return a type that is one of the constants in this interface
     */
    public int getType();
    /**
     * Set the type
     * @see org.eclipse.rse.model.ISystemPreferenceChangeEvents
     */
    public void setType(int type);	
	/**
	 * Get the old value. For boolean will be a Boolean object
	 */
	public Object getOldValue();
	/**
	 * Get the new value. For boolean will be a Boolean object
	 */
	public Object getNewValue();
	
}