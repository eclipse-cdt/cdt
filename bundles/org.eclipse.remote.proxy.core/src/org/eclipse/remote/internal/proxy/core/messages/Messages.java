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
package org.eclipse.remote.internal.proxy.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_ID = "org.eclipse.remote.internal.proxy.core.messages.messages"; //$NON-NLS-1$

	public static String AbstractRemoteCommand_format1;
	public static String AbstractRemoteCommand_format2;
	public static String AbstractRemoteCommand_Get_symlink_target;
	public static String AbstractRemoteCommand_Operation_cancelled_by_user;
	public static String AuthInfo_Authentication_message;
	public static String ExecCommand_Exec_command;

	public static String GetInputStreamCommand_Receiving;

	public static String GetOutputStreamCommand_Sending;
	public static String JSchConnection_0;

	public static String JSchConnection_Connection_was_cancelled;
	public static String JSchConnection_connectionNotOpen;
	public static String JSchConnection_Executing_command;
	public static String JSchConnection_remote_address_must_be_set;
	public static String JSchConnection_remotePort;
	public static String JSchConnection_forwarding;
	public static String JSchConnection_Remote_host_does_not_support_sftp;
	public static String JSchConnection_Unable_to_open_sftp_channel;
	public static String JSchConnection_username_must_be_set;
	public static String JSchConnectionManager_connection_with_name_exists;
	public static String JSchConnectionManager_cannotRemoveOpenConnection;
	public static String JSchConnectionManager_invalidConnectionType;
	public static String JSchConnectionProxyFactory_failed;
	public static String JSchConnectionProxyFactory_ProxyCommandFailed;
	public static String JSchConnectionProxyFactory_timedOut;
	public static String JSchConnectionProxyFactory_wasCanceled;
	public static String JSchProcess_exitValue_exception_msg;
	public static String JSchProcessBuilder_Connection_is_not_open;
	public static String JschFileStore_Connection_is_not_open;
	public static String JschFileStore_File_doesnt_exist;
	public static String JschFileStore_Invalid_connection_for_URI;
	public static String JschFileStore_Is_a_directory;
	public static String JschFileStore_No_remote_services_found_for_URI;
	public static String JschFileStore_The_directory_could_not_be_created;

	public static String JschFileStore_A_file_of_name_already_exists;
	public static String JschFileStore_The_parent_of_directory_does_not_exist;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, Messages.class);
	}

	private Messages() {
		// cannot create new instance
	}
}
