/********************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others. All rights reserved.
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
 * David McKnight   (IBM)        - [223204] [cleanup] fix broken nls strings in files.ui and others
 * Martin Oberhuber (Wind River) - [226374] [api] Need default SystemMessageException specialisations
 * Martin Oberhuber (Wind River) - [227135] Cryptic exception when sftp-server is missing
 ********************************************************************************/
package org.eclipse.rse.services.clientserver.messages;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized Strings for common messages that all clients can use.
 * @since 3.0
 */
public class CommonMessages extends NLS {
	private static String BUNDLE_NAME = "org.eclipse.rse.services.clientserver.messages.CommonMessages";//$NON-NLS-1$


	public static String MSG_EXCEPTION_OCCURRED;
	public static String MSG_ERROR_UNEXPECTED;
	/**
	 * General message format for concatenating a message with a cause
	 *
	 * @since 3.1
	 */
	public static String MSG_FAILURE_WITH_CAUSE;

	public static String MSG_COMM_AUTH_FAILED;
	public static String MSG_COMM_AUTH_FAILED_DETAILS;
	public static String MSG_COMM_NETWORK_ERROR;
	public static String MSG_LOCK_TIMEOUT;

	public static String MSG_EXPAND_FAILED;
	public static String MSG_EXPAND_CANCELLED;

	// operation status
	public static String MSG_OPERATION_RUNNING;
	public static String MSG_OPERATION_FINISHED;
	public static String MSG_OPERTION_STOPPED;
	public static String MSG_OPERATION_DISCONNECTED;

	public static String MSG_CONNECT_CANCELLED;
	public static String MSG_CONNECT_PROGRESS;
	public static String MSG_CONNECTWITHPORT_PROGRESS;
	public static String MSG_CONNECT_FAILED;
	public static String MSG_CONNECT_UNKNOWNHOST;

	public static String MSG_DISCONNECT_PROGRESS;
	public static String MSG_DISCONNECTWITHPORT_PROGRESS;
	public static String MSG_DISCONNECT_FAILED;
	public static String MSG_DISCONNECT_CANCELLED;

	public static String MSG_OPERATION_FAILED;
	public static String MSG_OPERATION_CANCELLED;
	public static String MSG_OPERATION_UNSUPPORTED;
	public static String MSG_OPERATION_SECURITY_VIOLATION;

	public static String MSG_ELEMENT_NOT_FOUND;

	public static String MSG_RESOLVE_PROGRESS;

	public static String MSG_QUERY_PROGRESS;
	public static String MSG_QUERY_PROPERTIES_PROGRESS;

	public static String MSG_SET_PROGRESS;
	public static String MSG_SET_PROPERTIES_PROGRESS;

	public static String MSG_RUN_PROGRESS;
	public static String MSG_COPY_PROGRESS;


	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, CommonMessages.class);
	}
}
