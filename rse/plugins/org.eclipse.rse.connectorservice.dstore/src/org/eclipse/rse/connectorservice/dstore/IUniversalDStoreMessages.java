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

package org.eclipse.rse.connectorservice.dstore;

/**
 * Message IDs
 */
public interface IUniversalDStoreMessages 
{


	public static final String PLUGIN_ID ="org.eclipse.rse.connectorservice.dstore";
	public static final String PREFIX = PLUGIN_ID+".";
	// Resource Bundle ids
	public static final String RESID_PREFIX = PREFIX+"ui.";	
	// Messages prefixes
	public static final String MSG_PREFIX = RESID_PREFIX+"msg.";	
    public static final String MSG_TITLE = MSG_PREFIX + "Title";

	// Messages    
	public static final String MSG_CONNECTION_PREFIX = MSG_PREFIX + "Connection.";
	public static final String MSG_CONNECTION_FAILED = MSG_CONNECTION_PREFIX + "Failed";
	public static final String MSG_CONNECTION_UNKNOWN_HOST = MSG_CONNECTION_PREFIX + "UnknownHost";	
	public static final String MSG_CONNECTION_VERIFY = MSG_CONNECTION_PREFIX + "Verify";
	public static final String MSG_CONNECTION_COMMPROPERTIES = MSG_CONNECTION_PREFIX + "CommProperties";
    
	// RSE Server Connection Messages
	public static final String MSG_SIGNON_PREFIX = MSG_PREFIX + "Signon.";
	public static final String MSG_SIGNON_PASSWORD_ERROR = MSG_SIGNON_PREFIX + "PasswordError";
	public static final String MSG_SIGNON_PASSWORD_INCORRECT = MSG_SIGNON_PREFIX + "PasswordIncorrect";
	public static final String MSG_SIGNON_PASSWORD_INCORRECT_USER_DISABLED= MSG_SIGNON_PREFIX + "PasswordIncorrectUserDisabled";
	public static final String MSG_SIGNON_PASSWORD_EXPIRED = MSG_SIGNON_PREFIX + "PasswordExpired";
	public static final String MSG_SIGNON_USERID_INVALID = MSG_SIGNON_PREFIX + "UserIDInvalid";
	public static final String MSG_SIGNON_USERID_DISABLED = MSG_SIGNON_PREFIX + "UserIDDisabled";
	public static final String MSG_SIGNON_USERID_ERROR = MSG_SIGNON_PREFIX + "UserIDError";
	
	public static final String MSG_DATASTORE_PREFIX = MSG_PREFIX + "DataStore.";
	public static final String MSG_DATASTORE_STARTSERVER = MSG_DATASTORE_PREFIX + "StartServer";
	public static final String MSG_DATASTORE_CONNECTSERVER = MSG_DATASTORE_PREFIX + "ConnectServer";
	public static final String MSG_DATASTORE_INITIALIZESERVER = MSG_DATASTORE_PREFIX + "InitializeServer";
	public static final String MSG_DATASTORE_INITIALIZECODESERVER = MSG_DATASTORE_PREFIX + "InitializeCODEServer";

	public static final String MSG_CMD_PREFIX       = MSG_PREFIX + "Command.";
	public static final String MSG_CMDNAME_EMPTY    = MSG_CMD_PREFIX + "Required";    
	public static final String MSG_CMDNAME_NOTVALID = MSG_CMD_PREFIX + "NotValid";		
}