/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.internal.remote.jsch.core.messages;

import org.eclipse.osgi.util.NLS;

/**
 * @author Daniel Felix Ferber
 * 
 * @since 3.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_ID = "org.eclipse.internal.remote.jsch.core.messages.messages"; //$NON-NLS-1$

	public static String AuthInfo_Authentication_message;
	public static String JSchConnection_connectionNotOpen;
	public static String JSchConnection_remote_address_must_be_set;
	public static String JSchConnection_remotePort;
	public static String RemoteToolsConnection_open;
	public static String JSchConnection_forwarding;
	public static String JSchConnection_username_must_be_set;
	public static String JSchConnectionManager_connection_with_this_name_exists;
	public static String JSchConnectionManager_cannotRemoveOpenConnection;
	public static String JSchConnectionManager_invalidConnectionType;
	public static String RemoteToolsFileStore_0;
	public static String RemoteToolsFileStore_1;
	public static String RemoteToolsFileStore_2;
	public static String RemoteToolsFileStore_3;
	public static String RemoteToolsFileStore_4;
	public static String RemoteToolsFileStore_5;
	public static String RemoteToolsFileStore_6;
	public static String RemoteToolsFileStore_7;
	public static String RemoteToolsFileStore_8;
	public static String RemoteToolsFileStore_10;
	public static String RemoteToolsFileStore_12;
	public static String RemoteToolsFileStore_13;
	public static String RemoteToolsFileStore_14;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, Messages.class);
	}

	private Messages() {
		// cannot create new instance
	}
}
