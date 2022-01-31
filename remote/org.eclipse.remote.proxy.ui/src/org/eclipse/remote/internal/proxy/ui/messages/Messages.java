/**
 * Copyright (c) 2013 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.remote.internal.proxy.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_ID = "org.eclipse.remote.internal.proxy.ui.messages.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, Messages.class);
	}

	public static String ProxyConnectionPage_0;
	public static String ProxyConnectionPage_1;
	public static String ProxyConnectionPage_2;
	public static String ProxyConnectionPage_A_connection_with_that_name_already_exists;
	public static String ProxyConnectionPage_Edit_Connection;
	public static String ProxyConnectionPage_Edit_properties_of_an_existing_connection;
	public static String ProxyConnectionPage_Initial_Message;
	public static String ProxyConnectionPage_Proxy;
	public static String ProxyConnectionPage_Help;
	public static String ProxyConnectionPage_KeysAtSSH2;
	public static String ProxyConnectionPage_SelectCommand;
	public static String ProxyConnectionPage_SelectConnection;
	public static String ProxyConnectionPage_Settings0;
	public static String ProxyConnectionPage_selectProxyConnection;
	public static String ProxyFileSystemContributor_0;
	public static String ProxyNewConnectionPage_Advanced;
	public static String ProxyNewConnectionPage_Connection_name;
	public static String ProxyNewConnectionPage_File_with_private_key;
	public static String ProxyNewConnectionPage_Host;
	public static String ProxyNewConnectionPage_Host_information;
	public static String ProxyNewConnectionPage_Host_name_cannot_be_empty;
	public static String ProxyNewConnectionPage_New_Connection;
	public static String ProxyNewConnectionPage_New_connection_properties;
	public static String ProxyNewConnectionPage_Passphrase;
	public static String ProxyNewConnectionPage_Password;
	public static String ProxyNewConnectionPage_Password_based_authentication;
	public static String ProxyNewConnectionPage_Please_enter_a_connection_name;
	public static String ProxyNewConnectionPage_Port;
	public static String ProxyNewConnectionPage_Port_is_not_valid;
	public static String ProxyNewConnectionPage_Private_key_file_cannot_be_read;
	public static String ProxyNewConnectionPage_Private_key_file_does_not_exist;
	public static String ProxyNewConnectionPage_Private_key_file_is_invalid;
	public static String ProxyNewConnectionPage_Private_key_path_cannot_be_empty;
	public static String ProxyNewConnectionPage_Public_key_based_authentication;
	public static String ProxyNewConnectionPage_Timeout;
	public static String ProxyNewConnectionPage_Timeout_is_not_valid;
	public static String ProxyNewConnectionPage_User;
	public static String ProxyNewConnectionPage_User_name_cannot_be_empty;
	public static String ProxyUIConnectionManager_Connection_Error;
	public static String ProxyUIConnectionManager_Could_not_open_connection;

	private Messages() {
		// cannot create new instance
	}
}
