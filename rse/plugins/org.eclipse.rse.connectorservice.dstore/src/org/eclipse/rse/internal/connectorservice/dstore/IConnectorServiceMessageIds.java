/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 ********************************************************************************/
package org.eclipse.rse.internal.connectorservice.dstore;

public interface IConnectorServiceMessageIds {

	public static final String MSG_CONNECT_DAEMON_FAILED = "RSEG1242"; //MSG_CONNECT_PREFIX + "Failed";			 //$NON-NLS-1$
	public static final String MSG_CONNECT_DAEMON_FAILED_EXCEPTION = "RSEG1243"; //MSG_CONNECT_PREFIX + "Failed";	 //$NON-NLS-1$
	public static final String MSG_CONNECT_SSL_EXCEPTION = "RSEC2307"; //MSG_CONNECT_PREFIX + "Failed";	 //$NON-NLS-1$
		
	public static final String MSG_STARTING_SERVER_VIA_REXEC = "RSEC2310"; //$NON-NLS-1$
	public static final String MSG_STARTING_SERVER_VIA_DAEMON = "RSEC2311"; //$NON-NLS-1$
	public static final String MSG_CONNECTING_TO_SERVER= "RSEC2312"; //$NON-NLS-1$
	public static final String MSG_INITIALIZING_SERVER= "RSEC2313"; //$NON-NLS-1$
	public static final String MSG_PORT_OUT_RANGE = "RSEC2316"; //$NON-NLS-1$

	public static final String MSG_COMM_CONNECT_FAILED 		= "RSEC1001"; //$NON-NLS-1$

	public static final String MSG_COMM_PWD_EXISTS			= "RSEC2101"; //$NON-NLS-1$
	public static final String MSG_COMM_PWD_MISMATCH		= "RSEC2102"; //$NON-NLS-1$
	public static final String MSG_COMM_PWD_BLANKFIELD		= "RSEC2103"; //$NON-NLS-1$

	public static final String MSG_COMM_ENVVAR_DUPLICATE	= "RSEC2001"; //$NON-NLS-1$
	public static final String MSG_COMM_ENVVAR_NONAME		= "RSEC2002"; //$NON-NLS-1$
	public static final String MSG_COMM_ENVVAR_INVALIDCHAR	= "RSEC2004"; //$NON-NLS-1$
	
	public static final String MSG_COMM_SERVER_NOTSTARTED	= "RSEC2301"; //$NON-NLS-1$
	public static final String MSG_COMM_INVALID_LOGIN		= "RSEC2302"; //$NON-NLS-1$
	
	public static final String MSG_COMM_INCOMPATIBLE_PROTOCOL = "RSEC2303"; //$NON-NLS-1$
	public static final String MSG_COMM_INCOMPATIBLE_UPDATE   = "RSEC2304"; //$NON-NLS-1$

	
	public static final String MSG_COMM_REXEC_NOTSTARTED      = "RSEC2305"; //$NON-NLS-1$
	
	public static final String MSG_COMM_PORT_WARNING          = "RSEC2306"; //$NON-NLS-1$
	
	public static final String MSG_COMM_SERVER_OLDER_WARNING  = "RSEC2308"; //$NON-NLS-1$
	public static final String MSG_COMM_CLIENT_OLDER_WARNING  = "RSEC2309"; //$NON-NLS-1$
	
	public static final String MSG_COMM_USING_SSL  = "RSEC2314"; //$NON-NLS-1$
	public static final String MSG_COMM_NOT_USING_SSL  = "RSEC2315"; //$NON-NLS-1$
	
	public static final String MSG_VALIDATE_PASSWORD_EMPTY   = "RSEG1035"; //MSG_VALIDATE_PREFIX + "PasswordRequired"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_PASSWORD_EXPIRED = "RSEG1036"; //MSG_VALIDATE_PREFIX + "PasswordExpired";	 //$NON-NLS-1$
	public static final String MSG_VALIDATE_PASSWORD_INVALID = "RSEG1297"; //$NON-NLS-1$

}
