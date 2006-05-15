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

package org.eclipse.dstore.core.server;

/**
 * This class contains a list of server return codes that are used
 * to negociate server communication with a client
 */
public class ServerReturnCodes
{
	public static final String RC_DSTORE_SERVER_MAGIC = "Dstore Server Starting..."; //$NON-NLS-1$

	public static final String RC_SUCCESS = "Server Started Successfully";

	public static final String RC_UNKNOWN_HOST_ERROR = "Unknown host error";
	public static final String RC_BIND_ERROR = "Error binding socket";
	public static final String RC_GENERAL_IO_ERROR = "General IO error creating socket";
	public static final String RC_CONNECTION_ERROR = "Connection error";

	public static final String RC_SECURITY_ERROR = "Security error creating socket";

	public static final String RC_FINISHED = "Server Finished";
	
	public static final String RC_JRE_VERSION_ERROR = "JRE 1.4 or higher required";
}