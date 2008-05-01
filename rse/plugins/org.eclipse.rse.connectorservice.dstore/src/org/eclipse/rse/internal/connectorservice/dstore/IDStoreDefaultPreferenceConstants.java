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
 * David McKnight   (IBM)        - [228334][api][breaking][dstore] Default DataStore connection timeout is too short
 ********************************************************************************/
package org.eclipse.rse.internal.connectorservice.dstore;

public interface IDStoreDefaultPreferenceConstants {
	public static final boolean DEFAULT_PREF_CACHE_REMOTE_CLASSES = true;
	public static final int DEFAULT_PREF_SOCKET_TIMEOUT = 5000;
	public static final boolean DEFAULT_PREF_DO_KEEPALIVE = true;
	public static final int DEFAULT_PREF_KEEPALIVE_RESPONSE_TIMEOUT = 60000;
	public static final int DEFAULT_PREF_SOCKET_READ_TIMEOUT = 3600000; 
	public static final boolean DEFAULT_ALERT_MISMATCHED_SERVER = true;
}
