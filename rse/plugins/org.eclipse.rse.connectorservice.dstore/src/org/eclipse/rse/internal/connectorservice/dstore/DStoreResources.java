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
 * David McKnight   (IBM)        - [216596] dstore preferences (timeout, and others)
 * David McKnight  (IBM)         - [220123][dstore] Configurable timeout on irresponsiveness
 ********************************************************************************/
package org.eclipse.rse.internal.connectorservice.dstore;

import org.eclipse.osgi.util.NLS;

public class DStoreResources extends NLS {

	private static String BUNDLE_NAME = "org.eclipse.rse.internal.connectorservice.dstore.DStoreResources";  //$NON-NLS-1$

	public static String RESID_PREFERENCE_CONNECTION_TIMEOUT_LABEL;
	public static String RESID_PREFERENCE_CONNECTION_TIMEOUT_TOOLTIP;
	
	public static String RESID_PREFERENCE_DO_KEEPALIVE_LABEL;
	public static String RESID_PREFERENCE_DO_KEEPALIVE_TOOLTIP;
	
	public static String RESID_PREFERENCE_KEEPALIVE_SOCKET_READ_TIMEOUT_LABEL;
	public static String RESID_PREFERENCE_KEEPALIVE_SOCKET_READ_TIMEOUT_TOOLTIP;
	
	public static String RESID_PREFERENCE_KEEPALIVE_RESPONSE_TIMEOUT_LABEL;
	public static String RESID_PREFERENCE_KEEPALIVE_RESPONSE_TIMEOUT_TOOLTIP;
	
	public static String RESID_PREFERENCE_CACHE_REMOTE_CLASSES_LABEL;
	public static String RESID_PREFERENCE_CACHE_REMOTE_CLASSES_TOOLTIP;
	
	public static String RESID_PREFERENCE_SHOW_MISMATCHED_SERVER_LABEL;
	public static String RESID_PREFERENCE_SHOW_MISMATCHED_SERVER_TOOLTIP;
		
	public static String RESID_PREFERENCE_KEEPALIVE_LABEL;
	
	static 
	{
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, DStoreResources.class);
	}
	
}