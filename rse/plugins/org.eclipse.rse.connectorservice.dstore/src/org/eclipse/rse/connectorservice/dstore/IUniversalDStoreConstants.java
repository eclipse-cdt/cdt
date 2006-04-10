/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.connectorservice.dstore;
/**
 * Constants used throughout the UniversalSystem plugin
 */
public interface IUniversalDStoreConstants 
{


	public static final String PLUGIN_ID ="com.ibm.etools.systems.universal";
	
	public static final String PREFIX = PLUGIN_ID + ".";
	
	// prefix for context sensitive help
	public static final String HELP_PREFIX = PREFIX; 
	
	// Resource Bundle ids
	public static final String RESID_PREFIX = PREFIX + "ui.";	
	
	// Icons
	public static final String ICON_DIR = "icons";
	public static final String ICON_PATH = java.io.File.separator + ICON_DIR + java.io.File.separator;
	public static final String ICON_SUFFIX = "Icon";	
	public static final String ICON_EXT = ".gif";	

	//public static final String ICON_SYSTEM_LIBRARY_ROOT = "system400Library";		
	//public static final String ICON_SYSTEM_LIBRARY      = ICON_SYSTEM_LIBRARY_ROOT + ICON_EXT;
	//public static final String ICON_SYSTEM_LIBRARY_ID   = PREFIX + ICON_SYSTEM_LIBRARY_ROOT + ICON_SUFFIX;
	
    // -------------------------
	// Action prefixes.
	//  SYstemBaseAction class adds "label" to get text and "tooltip" and "description" to get hover help
	// -------------------------
	// action ids
	public static final String ACTION_PREFIX = RESID_PREFIX + "action.";        
	public static final String RESID_RUN_REMOTECMD_PREFIX = ACTION_PREFIX+"RunRemoteCommand";


    // -------------------------
	// Preferences...
	// -------------------------
    public static final String RESID_PREF_PREFIX = RESID_PREFIX+"preferences.";    
    public static final String RESID_PREF_ROOT_TITLE = RESID_PREF_PREFIX+"root.title";
    
    // RemoteClassLoader caching preferences
    public static final String RESID_PREF_CACHE_REMOTE_CLASSES = RESID_PREF_PREFIX + "cacheremoteclasses";
    public static final boolean DEFAULT_PREF_CACHE_REMOTE_CLASSES = true;
    
        // Socket timeout preference
    public static final String RESID_PREF_SOCKET_TIMEOUT = RESID_PREF_PREFIX + "sockettimeout";
    public static final int DEFAULT_PREF_SOCKET_TIMEOUT = 300000;

    public static final String RESID_PREF_DO_KEEPALIVE = RESID_PREF_PREFIX + "dokeepalive";
    public static final boolean DEFAULT_PREF_DO_KEEPALIVE = true;
}