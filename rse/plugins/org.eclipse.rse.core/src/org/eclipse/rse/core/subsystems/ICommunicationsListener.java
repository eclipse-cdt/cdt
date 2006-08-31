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

package org.eclipse.rse.core.subsystems;


/**
 * ICommunicationsListener - listen to communication events
 * @see CommunicationsEvent
 */
public interface ICommunicationsListener {

	
	/**
	 * This method is invoked whenever the communications state is invoked
	 * immediately before and after the state of the communications changes.
	 * The state field in CommunicationsEvent determines which state
	 * change is about to or has occured.
	 */
	public void communicationsStateChange(CommunicationsEvent e);
	
	/**
	 * This method determines if the communications listener is a passive or
	 * active listener.  Typically a passive listener registers with the communications
	 * system and responds to events as they occur.  An active listener typically 
	 * registeres with the communications system only for the duration of the task (i.e.
	 * user editing a file, or outstanding communications request.)
	 * 
	 * The user will be prompted on a disconnect if there are any active communication
	 * listeners registered.
	 * 
	 * @return false if the communications listener is an active listener, true if the 
	 * communications listener is a passive listener.
	 */
	public boolean isPassiveCommunicationsListener();
}