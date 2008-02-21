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
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.internal.ui.subsystems;

import org.eclipse.osgi.util.NLS;

public class SubSystemResources extends NLS {

	private static String BUNDLE_NAME = "org.eclipse.rse.internal.ui.subsystems.SubSystemResources";//$NON-NLS-1$
			
	public static String MSG_CONNECT_CANCELED;
	public static String MSG_CONNECT_PROGRESS;
	public static String MSG_CONNECTWITHPORT_PROGRESS;
	public static String MSG_CONNECT_FAILED;
	public static String MSG_CONNECT_UNKNOWNHOST;
	
	public static String MSG_DISCONNECT_PROGRESS;
	public static String MSG_DISCONNECTWITHPORT_PROGRESS;
	public static String MSG_DISCONNECT_FAILED;
	public static String MSG_DISCONNECT_CANCELED;
	
	public static String MSG_OPERATION_FAILED;
	public static String MSG_OPERATION_CANCELED;
	
	public static String MSG_LOADING_PROFILE_SHOULDBE_ACTIVATED;
	public static String MSG_LOADING_PROFILE_SHOULDNOTBE_DEACTIVATED;
	
	// Connection doesn't exist
	public static String MSG_CONNECTION_DELETED;
	public static String MSG_CONNECTION_DELETED_DETAILS; 
	
	// yantzi: artemis 6.0, offline messages
	public static String MSG_OFFLINE_CANT_CONNECT;
	public static String MSG_OFFLINE_CANT_CONNECT_DETAILS;
	
	public static String MSG_RESOLVE_PROGRESS;

	public static String MSG_QUERY_PROGRESS;
	public static String MSG_QUERY_PROPERTIES_PROGRESS;

	public static String MSG_SET_PROGRESS;
	public static String MSG_SET_PROPERTIES_PROGRESS;

	public static String MSG_RUN_PROGRESS;

	public static String MSG_COPY_PROGRESS;

	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, SubSystemResources.class);
	}
}
