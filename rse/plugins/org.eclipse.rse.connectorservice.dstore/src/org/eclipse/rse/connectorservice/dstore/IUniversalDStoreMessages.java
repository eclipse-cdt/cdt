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


	public static final String PLUGIN_ID ="org.eclipse.rse.connectorservice.dstore"; //$NON-NLS-1$
	public static final String PREFIX = PLUGIN_ID+"."; //$NON-NLS-1$
	// Resource Bundle ids
	public static final String RESID_PREFIX = PREFIX+"ui.";	 //$NON-NLS-1$
	// Messages prefixes
	public static final String MSG_PREFIX = RESID_PREFIX+"msg.";	 //$NON-NLS-1$
    public static final String MSG_TITLE = MSG_PREFIX + "Title"; //$NON-NLS-1$

	// Messages    
	public static final String MSG_CONNECTION_PREFIX = MSG_PREFIX + "Connection."; //$NON-NLS-1$
	public static final String MSG_CONNECTION_FAILED = MSG_CONNECTION_PREFIX + "Failed"; //$NON-NLS-1$
	public static final String MSG_CONNECTION_UNKNOWN_HOST = MSG_CONNECTION_PREFIX + "UnknownHost";	 //$NON-NLS-1$
	public static final String MSG_CONNECTION_VERIFY = MSG_CONNECTION_PREFIX + "Verify"; //$NON-NLS-1$
	public static final String MSG_CONNECTION_COMMPROPERTIES = MSG_CONNECTION_PREFIX + "CommProperties"; //$NON-NLS-1$
    
	// RSE Server Connection Messages
	public static final String MSG_SIGNON_PREFIX = MSG_PREFIX + "Signon."; //$NON-NLS-1$
	public static final String MSG_SIGNON_PASSWORD_ERROR = MSG_SIGNON_PREFIX + "PasswordError"; //$NON-NLS-1$
	public static final String MSG_SIGNON_PASSWORD_INCORRECT = MSG_SIGNON_PREFIX + "PasswordIncorrect"; //$NON-NLS-1$
	public static final String MSG_SIGNON_PASSWORD_INCORRECT_USER_DISABLED= MSG_SIGNON_PREFIX + "PasswordIncorrectUserDisabled"; //$NON-NLS-1$
	public static final String MSG_SIGNON_PASSWORD_EXPIRED = MSG_SIGNON_PREFIX + "PasswordExpired"; //$NON-NLS-1$
	public static final String MSG_SIGNON_USERID_INVALID = MSG_SIGNON_PREFIX + "UserIDInvalid"; //$NON-NLS-1$
	public static final String MSG_SIGNON_USERID_DISABLED = MSG_SIGNON_PREFIX + "UserIDDisabled"; //$NON-NLS-1$
	public static final String MSG_SIGNON_USERID_ERROR = MSG_SIGNON_PREFIX + "UserIDError"; //$NON-NLS-1$
	
	public static final String MSG_DATASTORE_PREFIX = MSG_PREFIX + "DataStore."; //$NON-NLS-1$
	public static final String MSG_DATASTORE_STARTSERVER = MSG_DATASTORE_PREFIX + "StartServer"; //$NON-NLS-1$
	public static final String MSG_DATASTORE_CONNECTSERVER = MSG_DATASTORE_PREFIX + "ConnectServer"; //$NON-NLS-1$
	public static final String MSG_DATASTORE_INITIALIZESERVER = MSG_DATASTORE_PREFIX + "InitializeServer"; //$NON-NLS-1$
	public static final String MSG_DATASTORE_INITIALIZECODESERVER = MSG_DATASTORE_PREFIX + "InitializeCODEServer"; //$NON-NLS-1$

	public static final String MSG_CMD_PREFIX       = MSG_PREFIX + "Command."; //$NON-NLS-1$
	public static final String MSG_CMDNAME_EMPTY    = MSG_CMD_PREFIX + "Required";     //$NON-NLS-1$
	public static final String MSG_CMDNAME_NOTVALID = MSG_CMD_PREFIX + "NotValid";		 //$NON-NLS-1$
}