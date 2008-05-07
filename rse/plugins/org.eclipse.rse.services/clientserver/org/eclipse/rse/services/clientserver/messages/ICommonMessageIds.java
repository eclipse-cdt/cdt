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
 * Martin Oberhuber (Wind River) - [226374] [api] Need default SystemMessageException specialisations
 ********************************************************************************/
package org.eclipse.rse.services.clientserver.messages;

/**
 * Common Global Message IDs to be used with the RSE SystemMessages mechanism.
 *
 * Clients may use these IDs when creating message objects, or when calling
 * {@link org.eclipse.rse.ui.SystemBasePlugin#getPluginMessage(String)}. The
 * message IDs uniquely identify a particular situation each, and are used for
 * message translation and context help lookup.
 *
 * @since org.eclipse.rse.services 3.0
 */
public interface ICommonMessageIds {

	public static final String MSG_DISCONNECT_PROGRESS = "RSEG1059"; //$NON-NLS-1$
	public static final String MSG_DISCONNECTWITHPORT_PROGRESS = "RSEG1060"; //$NON-NLS-1$
	public static final String MSG_DISCONNECT_FAILED      = "RSEG1061"; //$NON-NLS-1$
	public static final String MSG_DISCONNECT_CANCELLED   = "RSEG1062"; //$NON-NLS-1$

	public static final String MSG_CONNECT_PROGRESS    = "RSEG1054"; //$NON-NLS-1$
	public static final String MSG_CONNECTWITHPORT_PROGRESS    = "RSEG1055"; //$NON-NLS-1$
	public static final String MSG_CONNECT_FAILED      = "RSEG1056"; //$NON-NLS-1$
	public static final String MSG_CONNECT_UNKNOWNHOST = "RSEG1057"; //$NON-NLS-1$
	public static final String MSG_CONNECT_CANCELLED   = "RSEG1058"; //$NON-NLS-1$

	public static final String MSG_OPERATION_FAILED      = "RSEG1066"; 		 //$NON-NLS-1$
	public static final String MSG_OPERATION_CANCELLED   = "RSEG1067";		 //$NON-NLS-1$
	public static final String MSG_OPERATION_UNSUPPORTED = "RSEG9999";//FIXME //$NON-NLS-1$
	public static final String MSG_OPERATION_SECURITY_VIOLATION = "RSEG9999";//FIXME //$NON-NLS-1$

	public static final String MSG_EXCEPTION_OCCURRED = "RSEG1003"; //$NON-NLS-1$
	public static final String MSG_EXCEPTION_DELETING = "RSEG1063"; //$NON-NLS-1$
	public static final String MSG_EXCEPTION_RENAMING = "RSEG1064"; //$NON-NLS-1$
	public static final String MSG_EXCEPTION_MOVING   = "RSEG1065"; //$NON-NLS-1$

	public static final String MSG_ELEMENT_NOT_FOUND = "RSEG9999";//FIXME //$NON-NLS-1$

	public static final String MSG_ERROR_UNEXPECTED   = "RSEG8002"; //$NON-NLS-1$
	public static final String MSG_LOCK_TIMEOUT = "RSEG9999";//FIXME //$NON-NLS-1$

	public static final String MSG_COMM_AUTH_FAILED 		= "RSEC1002"; //$NON-NLS-1$
	public static final String MSG_COMM_PWD_INVALID			= "RSEC1004"; //$NON-NLS-1$
	public static final String MSG_COMM_NETWORK_ERROR = "RSEC9999"; //FIXME //$NON-NLS-1$

	public static final String MSG_EXPAND_FAILED    = "RSEG1098"; //$NON-NLS-1$
	public static final String MSG_EXPAND_CANCELLED = "RSEG1067"; //$NON-NLS-1$


	public static final String MSG_RUN_PROGRESS = "RSEG1071";	 //$NON-NLS-1$

	public static final String MSG_COPY_PROGRESS = "RSEG1072";	 //$NON-NLS-1$

}
