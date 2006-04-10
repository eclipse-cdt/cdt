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


public class CommunicationsEvent {
	
	
	// Communications event types
	public static final int BEFORE_CONNECT = 1;
	public static final int AFTER_CONNECT = 2;
	public static final int BEFORE_DISCONNECT = 3;
	public static final int AFTER_DISCONNECT = 4;
	public static final int CONNECTION_ERROR = 5;
	
	private IConnectorService system;
	private int state;
	
	public CommunicationsEvent(IConnectorService system, int state) {
		this.system = system;
		this.state = state;
	}
	
	public int getState() {
		return state;
	}
	
	public IConnectorService getSystem() {
		return system;
	}

}