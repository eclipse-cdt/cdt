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
	public static final String ROOT = "org.eclipse.rse.preferences."; //$NON-NLS-1$
	
    // keys
    public static final String SYSTEMTYPE              = ROOT + "systemtype";  //$NON-NLS-1$
    public static final String SYSTEMTYPE_VALUES       = ROOT + "systemtype.info"; //$NON-NLS-1$
    public static final String USERIDPERKEY            = ROOT + "useridperkey"; //$NON-NLS-1$
    public static final String USERIDKEYS              = ROOT + "userid.keys";     //$NON-NLS-1$
    public static final String SHOWFILTERPOOLS         = ROOT + "filterpools.show"; //$NON-NLS-1$
    public static final String ACTIVEUSERPROFILES      = ROOT + "activeuserprofiles";     //$NON-NLS-1$
    public static final String QUALIFY_CONNECTION_NAMES= ROOT + "qualifyconnectionnames"; //$NON-NLS-1$
    public static final String ORDER_CONNECTIONS       = ROOT + "order.connections"; //$NON-NLS-1$
    public static final String HISTORY_FOLDER          = ROOT + "history.folder"; //$NON-NLS-1$
    public static final String HISTORY_QUALIFIED_FOLDER= ROOT + "history.qualified.folder"; //$NON-NLS-1$
    public static final String SHOWNEWCONNECTIONPROMPT = ROOT + "shownewconnection"; //$NON-NLS-1$
    public static final String REMEMBER_STATE          = ROOT + "rememberState";     //$NON-NLS-1$
    public static final String USE_DEFERRED_QUERIES    = ROOT + "useDeferredQueries";   //$NON-NLS-1$
	public static final String RESTORE_STATE_FROM_CACHE = ROOT + "restoreStateFromCache"; //$NON-NLS-1$
    public static final String CASCADE_UDAS_BYPROFILE  = ROOT + "uda.cascade";     //$NON-NLS-1$

	public static final String DAEMON_AUTOSTART 	   = ROOT + "daemon.autostart"; //$NON-NLS-1$
	public static final String DAEMON_PORT 			   = ROOT + "daemon.port"; //$NON-NLS-1$


    
    public static final String VERIFY_CONNECTION       = ROOT + "verify.connection"; //$NON-NLS-1$
    
    public static final String ALERT_SSL       = ROOT + "alert.ssl"; //$NON-NLS-1$
    public static final String ALERT_NONSSL    = ROOT + "alert.nonssl"; //$NON-NLS-1$

    // DEFAULTS
    public static final boolean DEFAULT_SHOWFILTERPOOLS          = false;
    public static final boolean DEFAULT_QUALIFY_CONNECTION_NAMES = false;
    public static final String  DEFAULT_SYSTEMTYPE               = ""; //$NON-NLS-1$
    public static final String  DEFAULT_USERID                   = ""; //$NON-NLS-1$
    //DKM public static final String  DEFAULT_ACTIVEUSERPROFILES       = "Team;Private";
    public static final String  DEFAULT_ACTIVEUSERPROFILES       = "Team"; //$NON-NLS-1$
    
    public static final String  DEFAULT_ORDER_CONNECTIONS        = "";     //$NON-NLS-1$
    public static final String  DEFAULT_HISTORY_FOLDER           = ""; //$NON-NLS-1$

    public static final boolean DEFAULT_SHOWNEWCONNECTIONPROMPT  = false;
    public static final boolean DEFAULT_REMEMBER_STATE           = true; // changed in R2. Phil
	public static final boolean DEFAULT_RESTORE_STATE_FROM_CACHE = true; // yantzi: artemis 6.0      
    public static final boolean DEFAULT_CASCADE_UDAS_BYPROFILE   = false;
    public static final boolean DEFAULT_USE_DEFERRED_QUERIES     = true;
    
    public static final String DEFAULT_TEAMPROFILE    = "Team"; //$NON-NLS-1$


	public static final boolean DEFAULT_DAEMON_AUTOSTART		= false;
	public static final int     DEFAULT_DAEMON_PORT				= 4300;   
	

	
	
    public static final boolean DEFAULT_VERIFY_CONNECTION = true;	
    
    public static final boolean DEFAULT_ALERT_SSL = true;
    public static final boolean DEFAULT_ALERT_NON_SSL = true;

}