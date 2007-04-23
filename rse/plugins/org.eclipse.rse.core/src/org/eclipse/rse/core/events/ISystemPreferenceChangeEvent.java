/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 ********************************************************************************/

package org.eclipse.rse.core.events;

/**
 * Interface of event sent when a remote system preference changes.
 * @see org.eclipse.rse.core.events.ISystemPreferenceChangeEvents
 */
public interface ISystemPreferenceChangeEvent {

	/**
	 * Returns the type of the event.
	 * @see org.eclipse.rse.core.events.ISystemPreferenceChangeEvents
	 * @return a type that is one of the constants in ISystemPreferenceChangeEvents.
	 */
	public int getType();

	/**
	 * Set the type of the event.
	 * @see org.eclipse.rse.core.events.ISystemPreferenceChangeEvents
	 * @param type the type of the event.
	 */
	public void setType(int type);

	/**
	 * Get the old value of the Preference.
	 * For boolean will be a Boolean object.
	 * @return the old value of the Preference.
	 */
	public Object getOldValue();

	/**
	 * Get the new value of the Preference.
	 * For boolean will be a Boolean object.
	 * @return the new value of the Preference.
	 */
	public Object getNewValue();

}