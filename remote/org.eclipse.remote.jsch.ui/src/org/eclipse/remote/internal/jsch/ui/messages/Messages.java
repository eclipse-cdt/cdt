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
package org.eclipse.remote.internal.jsch.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_ID = "org.eclipse.remote.internal.jsch.ui.messages.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, Messages.class);
	}

	public static String JSchConnectionPage_0;
	public static String JSchConnectionPage_1;
	public static String JSchConnectionPage_2;
	public static String JSchConnectionPage_A_connection_with_that_name_already_exists;
	public static String JSchConnectionPage_Edit_Connection;
	public static String JSchConnectionPage_Edit_properties_of_an_existing_connection;
	public static String JSchConnectionPage_Initial_Message;
	public static String JSchConnectionPage_Proxy;
	public static String JSchConnectionPage_Help;
	public static String JSchConnectionPage_KeysAtSSH2;
	public static String JSchConnectionPage_SelectCommand;
	public static String JSchConnectionPage_SelectConnection;
	public static String JSchConnectionPage_Settings0;
	public static String JSchConnectionPage_selectProxyConnection;
	public static String JSchFileSystemContributor_0;
	public static String JSchNewConnectionPage_Advanced;
	public static String JSchNewConnectionPage_Connection_name;
	public static String JSchNewConnectionPage_File_with_private_key;
	public static String JSchNewConnectionPage_Host;
	public static String JSchNewConnectionPage_Host_information;
	public static String JSchNewConnectionPage_Host_name_cannot_be_empty;
	public static String JSchNewConnectionPage_New_Connection;
	public static String JSchNewConnectionPage_New_connection_properties;
	public static String JSchNewConnectionPage_Passphrase;
	public static String JSchNewConnectionPage_Password;
	public static String JSchNewConnectionPage_Password_based_authentication;
	public static String JSchNewConnectionPage_Please_enter_a_connection_name;
	public static String JSchNewConnectionPage_Port;
	public static String JSchNewConnectionPage_Port_is_not_valid;
	public static String JSchNewConnectionPage_Private_key_file_cannot_be_read;
	public static String JSchNewConnectionPage_Private_key_file_does_not_exist;
	public static String JSchNewConnectionPage_Private_key_file_is_invalid;
	public static String JSchNewConnectionPage_Private_key_path_cannot_be_empty;
	public static String JSchNewConnectionPage_Public_key_based_authentication;
	public static String JSchNewConnectionPage_Timeout;
	public static String JSchNewConnectionPage_Timeout_is_not_valid;
	public static String JSchNewConnectionPage_User;
	public static String JSchNewConnectionPage_User_name_cannot_be_empty;
	public static String JSchUIConnectionManager_Connection_Error;
	public static String JSchUIConnectionManager_Could_not_open_connection;

	private Messages() {
		// cannot create new instance
	}
}
