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

package org.eclipse.rse.core.comm;

/**
 * Event data for the SystemCommunicationsDaemon.
 * 
 * @author yantzi
 */
public class SystemCommunicationsDaemonEvent 
{
	// Communications daemon event types
	public static final int STARTED = 1;
	public static final int STOPPED = 2;
	public static final int STOPPED_IN_ERROR = 3;
	
	private int state;
	
	/**
	 * Constructor for SystemCommunicationsDaemonEvent
	 * 
	 * @state The new state for the daemon.
	 */
	public SystemCommunicationsDaemonEvent(int state)
	{
		this.state = state;
	}
	
	/**
	 * Get the new state for the communications daemon.  
	 */
	public int getState()
	{
		return state;	
	}
}