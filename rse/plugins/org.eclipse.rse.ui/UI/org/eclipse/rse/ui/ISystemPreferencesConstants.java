/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.rse.ui;

/**
 * Keys into preferences bundle.
 */
public interface ISystemPreferencesConstants {
	/* 
	 * root
	 */
	public static final String ROOT = "org.eclipse.rse.preferences."; //$NON-NLS-1$
	/*
	 * uda preference keys
	 */
	public static final String CASCADE_UDAS_BYPROFILE = ROOT + "uda.cascade"; //$NON-NLS-1$
	/*
	 * uda preference default values
	 */
	public static final boolean DEFAULT_CASCADE_UDAS_BYPROFILE = false;
	/*
	 * ui preference keys
	 */
	public static final String RESTORE_STATE_FROM_CACHE = ROOT + "restoreStateFromCache"; //$NON-NLS-1$
	public static final String SYSTEMTYPE_VALUES = ROOT + "systemtype.info"; //$NON-NLS-1$
	public static final String SHOWFILTERPOOLS = ROOT + "filterpools.show"; //$NON-NLS-1$
	public static final String QUALIFY_CONNECTION_NAMES = ROOT + "qualifyconnectionnames"; //$NON-NLS-1$
	public static final String ORDER_CONNECTIONS = ROOT + "order.connections"; //$NON-NLS-1$
	public static final String SHOWNEWCONNECTIONPROMPT = ROOT + "shownewconnection"; //$NON-NLS-1$
	public static final String VERIFY_CONNECTION = ROOT + "verify.connection"; //$NON-NLS-1$
	public static final String ALERT_SSL = ROOT + "alert.ssl"; //$NON-NLS-1$
	public static final String ALERT_NONSSL = ROOT + "alert.nonssl"; //$NON-NLS-1$
	public static final String HISTORY_FOLDER = ROOT + "history.folder"; //$NON-NLS-1$
	public static final String HISTORY_QUALIFIED_FOLDER = ROOT + "history.qualified.folder"; //$NON-NLS-1$
	public static final String REMEMBER_STATE = ROOT + "rememberState"; //$NON-NLS-1$
	
	/**
	 * The SHOW_EMPTY_LISTS preference. Value is "SHOW_EMPTY_LISTS".
	 * The default value is true.
	 * This may be used in the product's plug-in initialization.
	 * Example:
	 * <code>
	 * org.eclipse.rse.ui/SHOW_EMPTY_LISTS=false
	 * </code>
	 * To use this preference in code do the following:
	 * <code>
	 * Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
	 * boolean showLists = store.getBoolean(ISystemPreferencesConstants.SHOW_EMPTY_LISTS);
	 * </code>
	 */
	public static final String SHOW_EMPTY_LISTS = "SHOW_EMPTY_LISTS"; //$NON-NLS-1$
	/*
	 * ui preference default values
	 */
	public static final boolean DEFAULT_RESTORE_STATE_FROM_CACHE = true; // yantzi: artemis 6.0      
	public static final boolean DEFAULT_SHOWFILTERPOOLS = false;
	public static final boolean DEFAULT_QUALIFY_CONNECTION_NAMES = false;
	public static final String DEFAULT_ORDER_CONNECTIONS = ""; //$NON-NLS-1$
	public static final boolean DEFAULT_SHOWNEWCONNECTIONPROMPT = false;
	public static final boolean DEFAULT_VERIFY_CONNECTION = true;
	public static final boolean DEFAULT_ALERT_SSL = true;
	public static final boolean DEFAULT_ALERT_NON_SSL = true;
	public static final String DEFAULT_HISTORY_FOLDER = ""; //$NON-NLS-1$
	public static final boolean DEFAULT_REMEMBER_STATE = true; // changed in R2. Phil
	public static final boolean DEFAULT_SHOW_EMPTY_LISTS = true;
}
