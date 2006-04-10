/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.files.core.subsystems;
/**
 * Keys into the core plugin's resource bundle for error messages related
 *  to exceptions thrown by the remote file system framework.
 */
public interface IRemoteFileExceptionMessages
{
	//public static final String PLUGIN_ID ="org.eclipse.rse.systems";
	//public static final String PLUGIN_ID =ISystemMessages.PLUGIN_ID;
	// Message prefix
	//public static final String FILEMSG_PREFIX = PLUGIN_ID+".ui.filemsg.";	

    // Messages
  	public static final String FILEMSG_IO_ERROR = "RSEF1001";            
  	public static final String FILEMSG_SECURITY_ERROR = "RSEF1002";        
    
  	public static final String FILEMSG_FOLDER_NOTEMPTY = "RSEF1003";    
  	public static final String FILEMSG_FOLDER_NOTFOUND = "RSEF1004";    
  	
  	public static final String FILEMSG_FILE_NOTFOUND   = "RSEF1005";  	
}