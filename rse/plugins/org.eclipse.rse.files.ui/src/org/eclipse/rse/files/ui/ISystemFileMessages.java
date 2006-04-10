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

package org.eclipse.rse.files.ui;
/**
 * Keys into the core plugin's resource bundle for error messages related
 *  to the remote file system framework.
 */
public interface ISystemFileMessages
{
	//public static final String PLUGIN_ID ="org.eclipse.rse.ui";
	//public static final String PLUGIN_ID =ISystemMessages.PLUGIN_ID;
	// Message prefix
	//public static final String FILEMSG_PREFIX = PLUGIN_ID+".ui.filemsg.";	

    // Messages
  	//public static final String FILEMSG_VALIDATE_PREFIX = FILEMSG_PREFIX + "Validate.";
  	
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_EMPTY    = "RSEF1006";
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE= "RSEF1007";
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOTVALID = "RSEF1008";
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOINCLUDES = "RSEF1009";	
}