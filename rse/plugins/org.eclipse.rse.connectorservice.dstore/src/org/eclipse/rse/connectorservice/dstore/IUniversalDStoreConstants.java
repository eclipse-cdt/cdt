/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 * David McKnight   (IBM)        - [216596] dstore preferences (timeout, and others)
 * David McKnight   (IBM)        - [218685] [api][breaking][dstore] Unable to connect when using SSL.
 *******************************************************************************/

package org.eclipse.rse.connectorservice.dstore;
/**
 * Constants used throughout the UniversalSystem plugin
 */
public interface IUniversalDStoreConstants 
{


	public static final String PLUGIN_ID ="org.eclipse.rse.connectorservice.dstore"; //$NON-NLS-1$
	
	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$
	
	// prefix for context sensitive help
	public static final String HELP_PREFIX = PREFIX; 
	
	// Resource Bundle ids
	public static final String RESID_PREFIX = PREFIX + "ui.";	 //$NON-NLS-1$
	
	// Icons
	public static final String ICON_DIR = "icons"; //$NON-NLS-1$
	public static final String ICON_PATH = java.io.File.separator + ICON_DIR + java.io.File.separator;
	public static final String ICON_SUFFIX = "Icon";	 //$NON-NLS-1$
	public static final String ICON_EXT = ".gif";	 //$NON-NLS-1$

    // -------------------------
	// Action prefixes.
	//  SYstemBaseAction class adds "label" to get text and "tooltip" and "description" to get hover help
	// -------------------------
	// action ids
	public static final String ACTION_PREFIX = RESID_PREFIX + "action.";         //$NON-NLS-1$
	public static final String RESID_RUN_REMOTECMD_PREFIX = ACTION_PREFIX+"RunRemoteCommand"; //$NON-NLS-1$


    // -------------------------
	// Preferences...
	// -------------------------
    public static final String RESID_PREF_PREFIX = RESID_PREFIX+"preferences.";     //$NON-NLS-1$
    public static final String RESID_PREF_ROOT_TITLE = RESID_PREF_PREFIX+"root.title"; //$NON-NLS-1$
    
    // RemoteClassLoader caching preferences
    public static final String RESID_PREF_CACHE_REMOTE_CLASSES = RESID_PREF_PREFIX + "cacheremoteclasses"; //$NON-NLS-1$
    public static final boolean DEFAULT_PREF_CACHE_REMOTE_CLASSES = true;
    
        // Socket timeout preference
    public static final String RESID_PREF_SOCKET_TIMEOUT = RESID_PREF_PREFIX + "sockettimeout"; //$NON-NLS-1$
    public static final int DEFAULT_PREF_SOCKET_TIMEOUT = 100000;

    public static final String RESID_PREF_DO_KEEPALIVE = RESID_PREF_PREFIX + "dokeepalive"; //$NON-NLS-1$
    public static final boolean DEFAULT_PREF_DO_KEEPALIVE = true;
    
	public static final String ALERT_MISMATCHED_SERVER = RESID_PREFIX + "alert.mismatched.server"; //$NON-NLS-1$
	public static final boolean DEFAULT_ALERT_MISMATCHED_SERVER = true;
}
