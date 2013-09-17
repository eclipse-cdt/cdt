/**
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.internal.remote.jsch.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_ID = "org.eclipse.internal.remote.jsch.ui.messages.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, Messages.class);
	}

	private Messages() {
		// cannot create new instance
	}

	public static String JSchConnectionPage_A_connection_with_that_name_already_exists;
	public static String JSchConnectionPage_Edit_Connection;
	public static String JSchConnectionPage_Edit_properties_of_an_existing_connection;
	public static String JSchConnectionPage_Please_enter_name_for_connection;
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
}
