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

package org.eclipse.rse.ui;
/**
 * Keys into preferences bundle.
 */
public interface ISystemPreferencesConstants 
{
	
	// root
	public static final String ROOT = "org.eclipse.rse.preferences.";
	
    // keys
    public static final String SYSTEMTYPE              = ROOT + "systemtype"; 
    public static final String SYSTEMTYPE_VALUES       = ROOT + "systemtype.info";
    public static final String USERIDPERKEY            = ROOT + "useridperkey";
    public static final String USERIDKEYS              = ROOT + "userid.keys";    
    public static final String SHOWFILTERPOOLS         = ROOT + "filterpools.show";
    public static final String ACTIVEUSERPROFILES      = ROOT + "activeuserprofiles";    
    public static final String QUALIFY_CONNECTION_NAMES= ROOT + "qualifyconnectionnames";
    public static final String ORDER_CONNECTIONS       = ROOT + "order.connections";
    public static final String HISTORY_FOLDER          = ROOT + "history.folder";
    public static final String HISTORY_QUALIFIED_FOLDER= ROOT + "history.qualified.folder";
    public static final String SHOWHIDDEN              = ROOT + "showhidden";
    public static final String SHOWNEWCONNECTIONPROMPT = ROOT + "shownewconnection";
    public static final String REMEMBER_STATE          = ROOT + "rememberState";    
    public static final String USE_DEFERRED_QUERIES    = ROOT + "useDeferredQueries";  
	public static final String RESTORE_STATE_FROM_CACHE = ROOT + "restoreStateFromCache";
    public static final String CASCADE_UDAS_BYPROFILE  = ROOT + "uda.cascade";    
    public static final String FILETRANSFERMODEDEFAULT = ROOT + "filetransfermodedefault";
	public static final String DAEMON_AUTOSTART 	   = ROOT + "daemon.autostart";
	public static final String DAEMON_PORT 			   = ROOT + "daemon.port";

	public static final String LIMIT_CACHE             = ROOT + "limit.cache";
	public static final String MAX_CACHE_SIZE          = ROOT + "max.cache.size";

    public static final String DOSUPERTRANSFER		   = ROOT + "dosupertransfer";
    public static final String SUPERTRANSFER_ARC_TYPE  = ROOT + "supertransfer.archivetype";
    
    public static final String DOWNLOAD_BUFFER_SIZE    = ROOT + "download.buffer.size";
    public static final String UPLOAD_BUFFER_SIZE      = ROOT + "upload.buffer.size";
    
    public static final String VERIFY_CONNECTION       = ROOT + "verify.connection";
    
    public static final String ALERT_SSL       = ROOT + "alert.ssl";
    public static final String ALERT_NONSSL    = ROOT + "alert.nonssl";

    // DEFAULTS
    public static final boolean DEFAULT_SHOWFILTERPOOLS          = false;
    public static final boolean DEFAULT_QUALIFY_CONNECTION_NAMES = false;
    public static final String  DEFAULT_SYSTEMTYPE               = "";
    public static final String  DEFAULT_USERID                   = "";
    //DKM public static final String  DEFAULT_ACTIVEUSERPROFILES       = "Team;Private";
    public static final String  DEFAULT_ACTIVEUSERPROFILES       = "Team";
    
    public static final String  DEFAULT_ORDER_CONNECTIONS        = "";    
    public static final String  DEFAULT_HISTORY_FOLDER           = "";
    public static final boolean DEFAULT_SHOW_HIDDEN              = true;
    public static final boolean DEFAULT_SHOWNEWCONNECTIONPROMPT  = false;
    public static final boolean DEFAULT_REMEMBER_STATE           = true; // changed in R2. Phil
	public static final boolean DEFAULT_RESTORE_STATE_FROM_CACHE = true; // yantzi: artemis 6.0      
    public static final boolean DEFAULT_CASCADE_UDAS_BYPROFILE   = false;
    public static final int     DEFAULT_FILETRANSFERMODE         = 0;
    
    public static final String DEFAULT_TEAMPROFILE    = "Team";

	public static final int FILETRANSFERMODE_BINARY 			= 0;
	public static final int FILETRANSFERMODE_TEXT 				= 1;

	public static final boolean DEFAULT_DAEMON_AUTOSTART		= false;
	public static final int     DEFAULT_DAEMON_PORT				= 4300;   
	
	public static final boolean DEFAULT_LIMIT_CACHE             = false;
	public static final String  DEFAULT_MAX_CACHE_SIZE          = "512";
	
	public static final String DEFAULT_SUPERTRANSFER_ARCHIVE_TYPE 	= "zip";
	public static final boolean DEFAULT_DOSUPERTRANSFER 			= true;
	
	public static final int DEFAULT_DOWNLOAD_BUFFER_SIZE        = 40;
	
    public static final boolean DEFAULT_VERIFY_CONNECTION = true;	
    
    public static final boolean DEFAULT_ALERT_SSL = true;
    public static final boolean DEFAULT_ALERT_NON_SSL = true;

}