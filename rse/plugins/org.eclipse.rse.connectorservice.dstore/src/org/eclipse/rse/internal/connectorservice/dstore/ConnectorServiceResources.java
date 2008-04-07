/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *******************************************************************************/

package org.eclipse.rse.internal.connectorservice.dstore;

import org.eclipse.osgi.util.NLS;

public class ConnectorServiceResources extends NLS
{
	private static String BUNDLE_NAME = "org.eclipse.rse.internal.connectorservice.dstore.ConnectorServiceResources";//$NON-NLS-1$

	public static String DStore_ConnectorService_Label;
	public static String DStore_ConnectorService_Description;
	
	
	public static String MSG_CONNECT_SSL_EXCEPTION;
	public static String MSG_CONNECT_SSL_EXCEPTION_DETAILS;
	public static String MSG_STARTING_SERVER_VIA_REXEC;
	public static String MSG_STARTING_SERVER_VIA_DAEMON;

	public static String MSG_CONNECTING_TO_SERVER;
	public static String MSG_INITIALIZING_SERVER;
	public static String MSG_PORT_OUT_RANGE;
	
	public static String MSG_COMM_CONNECT_FAILED;
	public static String MSG_COMM_CONNECT_FAILED_DETAILS;

	public static String MSG_VALIDATE_PASSWORD_EXPIRED;
	public static String MSG_VALIDATE_PASSWORD_INVALID;
	
	public static String MSG_VALIDATE_PASSWORD_EXPIRED_DETAILS;
	public static String MSG_VALIDATE_PASSWORD_INVALID_DETAILS;
	
	public static String MSG_COMM_USING_SSL;
	public static String MSG_COMM_NOT_USING_SSL;
	
	public static String MSG_COMM_SERVER_OLDER_WARNING;
	public static String MSG_COMM_CLIENT_OLDER_WARNING;
	public static String MSG_COMM_SERVER_OLDER_WARNING_DETAILS;
	public static String MSG_COMM_CLIENT_OLDER_WARNING_DETAILS;
	
	public static String MSG_CONNECT_DAEMON_FAILED;
	public static String MSG_CONNECT_DAEMON_FAILED_EXCEPTION;
	
	public static String MSG_COMM_INCOMPATIBLE_PROTOCOL;
	public static String MSG_COMM_INCOMPATIBLE_UPDATE;
	public static String MSG_COMM_INCOMPATIBLE_PROTOCOL_DETAILS;
	public static String MSG_COMM_INCOMPATIBLE_UPDATE_DETAILS;
	
	public static String MSG_COMM_INVALID_LOGIN; 
	public static String MSG_COMM_INVALID_LOGIN_DETAILS;
	
	
	public static String MSG_COMM_REXEC_NOTSTARTED;
	public static String MSG_COMM_REXEC_NOTSTARTED_DETAILS;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ConnectorServiceResources.class);
	}
}
