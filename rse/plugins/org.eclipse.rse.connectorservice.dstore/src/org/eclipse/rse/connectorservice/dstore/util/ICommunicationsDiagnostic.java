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

package org.eclipse.rse.connectorservice.dstore.util;

import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 * An interface for ICommunicationsDiagnostic class
 */
public interface ICommunicationsDiagnostic extends Runnable {
	
	
			
	public static final int CANCEL_WAIT_REQUESTED = 1;
		
	
	/**
	 * Setup for the diagnostic
	 * 
	 * @param
	 *  String id: assign an ID for this diagnostic instance
	 *  boolean quiet: true if user to be prompted for a dialog
	 *  String server: the host network name
	 *  ISystem system: the connection to be investigated
	 *  String str1, str2, str3: optional strings 
	 */ 
	public void setUp(String id, boolean quiet, String server, IConnectorService system, String str1, String str2, String str3);		
			
	/**
	 * Log an error in the .log file
	 * 
	 * @param
	 *  String text: message text to be logged
	 */ 
	public void logError(String text);
	 
	/**
	 * Check if network is down
	 * @param None
	 * @return true or false
	 * 
	 */ 
    public boolean isNetworkDown();
    
    /**
     * Check if host server is still active
     * @param None
     * @return true or false
     */
    public boolean isServerActive();
    
    /**
     * Dispaly a message dialog
     * 
     * @param
     *  int id: message to be displayed.
     */
    public void displayMessage(String id);
    
    /**
     * diagnosticStatus
     * 
     * @return status
     */
    public int diagnosticStatus();
    
        
}