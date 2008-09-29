/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [181066] NLS missing messages with DAEMON and Shells
 * Martin Oberhuber (Wind River) - [185552] Remove remoteSystemsViewPreferencesActions extension point
 * David McKnight   (IBM)        - [210229] table refresh needs unique table-specific tooltip-text
 * David McKnight   (IBM)        - [216252] [nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [223103] [cleanup] fix broken externalized strings
 * Rupen Mardirossian (IBM)      - [210682] Added BUTTON_OVERWRITE_ALL & and tooltip, also added some verbiage for new SystemCopyDialog.	
 * Xuan Chen        (IBM)        - [222263] Need to provide a PropertySet Adapter for System Team View
 * David Dykstal (IBM) - [210242] Credentials dialog should look different if password is not supported or optional
 * David Dykstal (IBM) - [216858] Need the ability to Import/Export RSE connections for sharing
 * David Dykstal (IBM) - [231943] Make "true" and "false" translatable on SystemTypeFieldEditor
 * David Dykstal (IBM) - [188150] adding "go up one level" tooltip
 * David Dykstal (IBM) - [233678] title string is constructed by concatenation, should be substituted
 * David McKnight (IBM) - [248922]  [dnd] Remote to local overwrite copy does not work
 *******************************************************************************/

package org.eclipse.rse.internal.ui;

import org.eclipse.osgi.util.NLS;


/**
 * Constants used throughout the System plugin.
 */
public class SystemResources extends NLS 
{
	private static String BUNDLE_NAME = "org.eclipse.rse.internal.ui.SystemResources";//$NON-NLS-1$

	// Buttons
	// *** NOT GOOD TO USE BUTTONS. BETTER TO USE ACTIONS WITH THEIR
	// .label,.tooltip and .description ASSOCIATIONS
	// THESE BUTTON LABELS ARE USED IN SYSTEMPROMPTDIALOG
	public static String BUTTON_BROWSE;
	public static String BUTTON_TEST;
	public static String BUTTON_CLOSE;
	public static String BUTTON_ADD;
	public static String BUTTON_CREATE_LABEL;
	public static String BUTTON_CREATE_TOOLTIP;

	public static String BUTTON_CANCEL_ALL;
	public static String BUTTON_CANCEL_ALL_TOOLTIP;
	public static String BUTTON_OVERWRITE_ALL;
	public static String BUTTON_OVERWRITE_ALL_TOOLTIP;

	// THESE TERMS ARE USED POTENTIALLY ANYWHERE
	public static String TERM_YES;
	public static String TERM_NO;

	public static String TERM_TRUE;
	public static String TERM_FALSE;


	public static String TERM_ALL;

	public static String RESID_MSGLINE_TIP;
	
	// ----------------------------------------
	// GENERIC/COMMON WIZARD AND DIALOG STRINGS
	// ----------------------------------------
	// GENERIC MULTI-SELECT RENAME DIALOG...
	public static String RESID_RENAME_TITLE;
	public static String RESID_RENAME_SINGLE_TITLE;
	public static String RESID_RENAME_VERBIAGE;
	public static String RESID_RENAME_COLHDG_OLDNAME;
	public static String RESID_RENAME_COLHDG_NEWNAME;
	public static String RESID_RENAME_COLHDG_TYPE;

	// SPECIALIZED PROMPTS FOR GENERIC RENAME DIALOG...
	public static String RESID_MULTI_RENAME_PROFILE_VERBIAGE;

	// GENERIC SINGLE-SELECT RENAME DIALOG...

	public static String RESID_SIMPLE_RENAME_PROMPT_LABEL;


	public static String RESID_SIMPLE_RENAME_RESOURCEPROMPT_LABEL;
	public static String RESID_SIMPLE_RENAME_RESOURCEPROMPT_TOOLTIP; 
	public static String RESID_SIMPLE_RENAME_RADIO_OVERWRITE_LABEL;
	public static String RESID_SIMPLE_RENAME_RADIO_OVERWRITE_TOOLTIP;
	public static String RESID_SIMPLE_RENAME_RADIO_RENAME_LABEL;
	public static String RESID_SIMPLE_RENAME_RADIO_RENAME_TOOLTIP;

	// SPECIALIZED PROMPTS FOR GENERIC RENAME DIALOG...
	public static String RESID_SIMPLE_RENAME_PROFILE_PROMPT_LABEL;

	public static String RESID_SIMPLE_RENAME_PROFILE_PROMPT_TIP;

	// GENERIC DELETE DIALOG...
	public static String RESID_DELETE_TITLE;

	public static String RESID_DELETE_PROMPT;

	public static String RESID_DELETE_PROMPT_SINGLE;


	public static String RESID_DELETE_WARNING_LABEL;
	public static String RESID_DELETE_WARNING_TOOLTIP;


	public static String RESID_DELETE_COLHDG_OLDNAME;

	public static String RESID_DELETE_COLHDG_TYPE;

	public static String RESID_DELETE_BUTTON;

	// SPECIALIZED PROMPTS FOR GENERIC DELETE DIALOG...
	public static String RESID_DELETE_PROFILES_PROMPT;

	// GENERIC COPY DIALOG...
	public static String RESID_COPY_TITLE;
	public static String RESID_COPY_SINGLE_TITLE;
	public static String RESID_COPY_PROMPT;
	public static String RESID_COPY_TARGET_PROFILE_PROMPT;
	public static String RESID_COPY_TARGET_FILTERPOOL_PROMPT;


	// GENERIC MOVE DIALOG...
	public static String RESID_MOVE_TITLE;
	public static String RESID_MOVE_SINGLE_TITLE;
	public static String RESID_MOVE_PROMPT;
	public static String RESID_MOVE_TARGET_PROFILE_PROMPT;
	public static String RESID_MOVE_TARGET_FILTERPOOL_PROMPT;

	// GENERIC RESOURCE NAME COLLISION DIALOG...
	public static String RESID_COLLISION_RENAME_TITLE;
	public static String RESID_COLLISION_RENAME_VERBIAGE;
	public static String RESID_COLLISION_RENAME_LABEL; 
	public static String RESID_COLLISION_COPY_VERBIAGE;
	public static String RESID_COLLISION_COPY_COLHDG_OLDNAME;


	// -------------------------
	// WIZARD AND DIALOG STRINGS
	// -------------------------
	// NEW PROFILE WIZARD...
	public static String RESID_NEWPROFILE_TITLE;
	public static String RESID_NEWPROFILE_PAGE1_TITLE;
	public static String RESID_NEWPROFILE_PAGE1_DESCRIPTION;
	public static String RESID_NEWPROFILE_NAME_LABEL; 
	public static String RESID_NEWPROFILE_NAME_TOOLTIP; 
	public static String RESID_NEWPROFILE_MAKEACTIVE_LABEL; 
	public static String RESID_NEWPROFILE_MAKEACTIVE_TOOLTIP;
	public static String RESID_NEWPROFILE_VERBIAGE;

	// RENAME DEFAULT PROFILE WIZARD PAGE...
	public static String RESID_PROFILE_PROFILENAME_LABEL;
	public static String RESID_PROFILE_PROFILENAME_TIP;
	public static String RESID_PROFILE_PROFILENAME_VERBIAGE;


	// COPY SYSTEM PROFILE DIALOG...
	public static String RESID_COPY_PROFILE_TITLE;

	public static String RESID_COPY_PROFILE_PROMPT_LABEL; 
	public static String RESID_COPY_PROFILE_PROMPT_TOOLTIP;

	// NEW SYSTEM CONNECTION WIZARD...
	public static String RESID_NEWCONN_PROMPT_LABEL;
	public static String RESID_NEWCONN_PROMPT_VALUE;
	public static String RESID_NEWCONN_EXPANDABLEPROMPT_VALUE;
	public static String RESID_NEWCONN_TITLE;
	public static String RESID_NEWCONN_PAGE1_TITLE;
	public static String RESID_NEWCONN_PAGE1_REMOTE_TITLE;
	public static String RESID_NEWCONN_PAGE1_LOCAL_TITLE;
	public static String RESID_NEWCONN_PAGE1_DESCRIPTION;
	public static String RESID_NEWCONN_MAIN_PAGE_TITLE;
	public static String RESID_NEWCONN_MAIN_PAGE_DESCRIPTION;
	public static String RESID_NEWCONN_SUBSYSTEMPAGE_FILES_DESCRIPTION;
	public static String RESID_NEWCONN_SUBSYSTEMPAGE_FILES_TITLE;
	public static String RESID_NEWCONN_SUBSYSTEMPAGE_FILES_VERBIAGE1;
	public static String RESID_NEWCONN_SUBSYSTEMPAGE_FILES_VERBIAGE2;
	public static String RESID_NEWCONN_SUBSYSTEMPAGE_DESCRIPTION;

	
	public static String RESID_CONNECTION_TYPE_LABEL;
	public static String RESID_CONNECTION_TYPE_VALUE;
	public static String RESID_CONNECTION_SYSTEMTYPE_LABEL;
	public static String RESID_CONNECTION_SYSTEMTYPE_TIP;
	public static String RESID_CONNECTION_SYSTEMTYPE_READONLY_TIP;

	public static String RESID_CONNECTION_CONNECTIONNAME_LABEL;
	public static String RESID_CONNECTION_CONNECTIONNAME_TIP;

	public static String RESID_CONNECTION_HOSTNAME_LABEL;
	public static String RESID_CONNECTION_HOSTNAME_TIP;


	public static String RESID_CONNECTION_VERIFYHOSTNAME_LABEL;
	public static String RESID_CONNECTION_VERIFYHOSTNAME_TOOLTIP;

	public static String RESID_CONNECTION_DEFAULTUSERID_LABEL;
	public static String RESID_CONNECTION_DEFAULTUSERID_TIP;
	public static String RESID_CONNECTION_DEFAULTUSERID_INHERITBUTTON_TIP;

	public static String RESID_CONNECTION_PORT_LABEL;
	public static String RESID_CONNECTION_PORT_TIP;

	public static String RESID_CONNECTION_DAEMON_PORT_LABEL;
	public static String RESID_CONNECTION_DAEMON_PORT_TIP;


	public static String RESID_CONNECTION_DESCRIPTION_LABEL;
	public static String RESID_CONNECTION_DESCRIPTION_TIP;

	public static String RESID_CONNECTION_PROFILE_LABEL;
	public static String RESID_CONNECTION_PROFILE_TIP;

	public static String RESID_CONNECTION_PROFILE_READONLY_TIP;



	// NEW FILTER POOL WIZARD...
	public static String RESID_NEWFILTERPOOL_TITLE;

	public static String RESID_NEWFILTERPOOL_PAGE1_TITLE;

	public static String RESID_NEWFILTERPOOL_PAGE1_DESCRIPTION;

	// WIDGETS FOR THIS WIZARD...
	public static String RESID_FILTERPOOLNAME_LABEL;
	public static String RESID_FILTERPOOLNAME_TIP;

	public static String RESID_FILTERPOOLMANAGERNAME_LABEL;
	public static String RESID_FILTERPOOLMANAGERNAME_TIP;

	// SELECT FILTER POOLS DIALOG...
	public static String RESID_SELECTFILTERPOOLS_TITLE;

	public static String RESID_SELECTFILTERPOOLS_PROMPT;

	// WORK WITH FILTER POOLS DIALOG...
	public static String RESID_WORKWITHFILTERPOOLS_TITLE;

	public static String RESID_WORKWITHFILTERPOOLS_PROMPT;

	// NEW SYSTEM FILTER WIZARD...
	public static String RESID_NEWFILTER_TITLE;

	public static String RESID_NEWFILTER_PAGE_TITLE;

	// MAIN PAGE (page 1) OF NEW FILTER WIZARD...
	public static String RESID_NEWFILTER_PAGE1_DESCRIPTION;


	// NAME PAGE (page 2) OF NEW FILTER WIZARD...
	public static String RESID_NEWFILTER_PAGE2_DESCRIPTION;

	public static String RESID_NEWFILTER_PAGE2_NAME_VERBIAGE;

	public static String RESID_NEWFILTER_PAGE2_POOL_VERBIAGE;

	public static String RESID_NEWFILTER_PAGE2_POOL_VERBIAGE_TIP;

	public static String RESID_NEWFILTER_PAGE2_PROFILE_VERBIAGE;

	public static String RESID_NEWFILTER_PAGE2_NAME_LABEL; 
	public static String RESID_NEWFILTER_PAGE2_NAME_TOOLTIP; 

	public static String RESID_NEWFILTER_PAGE2_PROFILE_LABEL; 
	public static String RESID_NEWFILTER_PAGE2_PROFILE_TOOLTIP;

	public static String RESID_NEWFILTER_PAGE2_POOL_LABEL;
	public static String RESID_NEWFILTER_PAGE2_POOL_TOOLTIP;

	public static String RESID_NEWFILTER_PAGE2_UNIQUE_LABEL; 
	public static String RESID_NEWFILTER_PAGE2_UNIQUE_TOOLTIP; 

	// INFO PAGE (page 3) OF NEW FILTER WIZARD...
	public static String RESID_NEWFILTER_PAGE3_DESCRIPTION;
	public static String RESID_NEWFILTER_PAGE3_STRINGS_VERBIAGE;
	public static String RESID_NEWFILTER_PAGE3_POOLS_VERBIAGE;


	// CHANGE SYSTEM FILTER DIALOG...
	public static String RESID_CHGFILTER_TITLE;
	public static String RESID_CHGFILTER_LIST_NEWITEM;
	public static String RESID_CHGFILTER_NAME_LABEL;
	public static String RESID_CHGFILTER_NAME_TOOLTIP;
	public static String RESID_CHGFILTER_POOL_LABEL;
	public static String RESID_CHGFILTER_POOL_TOOLTIP;
	public static String RESID_CHGFILTER_LIST_LABEL;
	public static String RESID_CHGFILTER_LIST_TOOLTIP;
	public static String RESID_CHGFILTER_FILTERSTRING_LABEL;
	public static String RESID_CHGFILTER_FILTERSTRING_TOOLTIP;
	public static String RESID_CHGFILTER_NEWFILTERSTRING_LABEL;
	public static String RESID_CHGFILTER_NEWFILTERSTRING_TOOLTIP;
	public static String RESID_CHGFILTER_BUTTON_TEST_LABEL;
	public static String RESID_CHGFILTER_BUTTON_TEST_TOOLTIP;
	public static String RESID_CHGFILTER_BUTTON_APPLY_LABEL;
	public static String RESID_CHGFILTER_BUTTON_APPLY_TOOLTIP;
	public static String RESID_CHGFILTER_BUTTON_REVERT_LABEL;
	public static String RESID_CHGFILTER_BUTTON_REVERT_TOOLTIP;

	// CREATE UNNAMED FILTER DIALOG...
	public static String RESID_CRTFILTER_TITLE;


	// NEW SYSTEM FILTER STRING WIZARD...

	public static String RESID_FILTERSTRING_STRING_LABEL;
	public static String RESID_FILTERSTRING_STRING_TIP;


	// TEST SYSTEM FILTER STRING DIALOG...
	public static String RESID_TESTFILTERSTRING_TITLE;

	public static String RESID_TESTFILTERSTRING_PROMPT_LABEL; 
	public static String RESID_TESTFILTERSTRING_PROMPT_TOOLTIP;


	// WORK WITH HISTORY DIALOG...
	public static String RESID_WORKWITHHISTORY_TITLE;
	public static String RESID_WORKWITHHISTORY_VERBIAGE;
	public static String RESID_WORKWITHHISTORY_PROMPT;
	public static String RESID_WORKWITHHISTORY_BUTTON_TIP;

	// PROMPT FOR PASSWORD DIALOG...
	public static String RESID_PASSWORD_TITLE;

	public static String RESID_PASSWORD_LABEL;
	public static String RESID_PASSWORD_LABEL_OPTIONAL;
	public static String RESID_PASSWORD_TIP;
	
	public static String RESID_PASSWORD_SYSTEMTYPE_LABEL;
	public static String RESID_PASSWORD_HOSTNAME_LABEL;

	public static String RESID_PASSWORD_USERID_LABEL;
	public static String RESID_PASSWORD_USERID_TIP;

	public static String RESID_PASSWORD_USERID_ISPERMANENT_LABEL;
	public static String RESID_PASSWORD_USERID_ISPERMANENT_TIP;

	public static String RESID_PASSWORD_SAVE_LABEL;
	public static String RESID_PASSWORD_SAVE_TOOLTIP;
	
	// CHANGE PASSWORD DIALOG
	public static String RESID_CHANGE_PASSWORD_TITLE;
	public static String RESID_CHANGE_PASSWORD_NEW_LABEL;
	public static String RESID_CHANGE_PASSWORD_NEW_TOOLTIP;
	public static String RESID_CHANGE_PASSWORD_OLD_LABEL;
	public static String RESID_CHANGE_PASSWORD_OLD_TOOLTIP;
	public static String RESID_CHANGE_PASSWORD_CONFIRM_LABEL;
	public static String RESID_CHANGE_PASSWORD_CONFIRM_TOOLTIP;

	// TABLE VIEW DIALOGS
	public static String RESID_TABLE_POSITIONTO_LABEL;
	public static String RESID_TABLE_POSITIONTO_ENTRY_TOOLTIP;

	public static String RESID_TABLE_SUBSET_LABEL;
	public static String RESID_TABLE_SUBSET_ENTRY_TOOLTIP;
	
	public static String RESID_TABLE_PRINTLIST_TITLE;

	// TABLE view column selection
	public static String RESID_TABLE_SELECT_COLUMNS_LABEL;
	public static String RESID_TABLE_SELECT_COLUMNS_TOOLTIP;
	
	public static String RESID_TABLE_SELECT_COLUMNS_ADD_LABEL;
	public static String RESID_TABLE_SELECT_COLUMNS_ADD_TOOLTIP;
	
	public static String RESID_TABLE_SELECT_COLUMNS_REMOVE_LABEL;
	public static String RESID_TABLE_SELECT_COLUMNS_REMOVE_TOOLTIP;
	
	public static String RESID_TABLE_SELECT_COLUMNS_UP_LABEL;
	public static String RESID_TABLE_SELECT_COLUMNS_UP_TOOLTIP;
	
	public static String RESID_TABLE_SELECT_COLUMNS_DOWN_LABEL;
	public static String RESID_TABLE_SELECT_COLUMNS_DOWN_TOOLTIP;
	
	public static String RESID_TABLE_SELECT_COLUMNS_AVAILABLE_LABEL;

	public static String RESID_TABLE_SELECT_COLUMNS_DISPLAYED_LABEL;
	public static String RESID_TABLE_SELECT_COLUMNS_DESCRIPTION_LABEL;

	// MONITOR VIEW DIALGOS
	public static String RESID_MONITOR_POLL_INTERVAL_LABEL;
	public static String RESID_MONITOR_POLL_INTERVAL_TOOLTIP;
	public static String RESID_MONITOR_POLL_LABEL;
	public static String RESID_MONITOR_POLL_TOOLTIP;
	public static String RESID_MONITOR_POLL_CONFIGURE_POLLING_LABEL;
	public static String RESID_MONITOR_POLL_CONFIGURE_POLLING_EXPAND_TOOLTIP;
	public static String RESID_MONITOR_POLL_CONFIGURE_POLLING_COLLAPSE_TOOLTIP;

	// TEAM VIEW
	public static String RESID_TEAMVIEW_SUBSYSFACTORY_VALUE;
	public static String RESID_TEAMVIEW_CATEGORY_VALUE;
	public static String RESID_TEAMVIEW_PROPERTYSET_VALUE;

	public static String RESID_TEAMVIEW_CATEGORY_CONNECTIONS_LABEL;
	public static String RESID_TEAMVIEW_CATEGORY_CONNECTIONS_TOOLTIP;

	public static String RESID_TEAMVIEW_CATEGORY_FILTERPOOLS_LABEL;
	public static String RESID_TEAMVIEW_CATEGORY_FILTERPOOLS_TOOLTIP;
	
	public static String RESID_TEAMVIEW_CATEGORY_PROPERTYSET_LABEL;
	public static String RESID_TEAMVIEW_CATEGORY_PROPERTYSET_TOOLTIP;

	// ------------------------------
	// REUSABLE WIDGET STRINGS...
	// ------------------------------
	// SELECT MULTIPLE REMOTE FILES WIDGET...
	public static String RESID_SELECTFILES_SELECTTYPES_BUTTON_ROOT_LABEL;
	public static String RESID_SELECTFILES_SELECTTYPES_BUTTON_ROOT_TOOLTIP;

	public static String RESID_SELECTFILES_SELECTALL_BUTTON_ROOT_LABEL;
	public static String RESID_SELECTFILES_SELECTALL_BUTTON_ROOT_TOOLTIP;

	public static String RESID_SELECTFILES_DESELECTALL_BUTTON_ROOT_LABEL;
	public static String RESID_SELECTFILES_DESELECTALL_BUTTON_ROOT_TOOLTIP;



	public static String RESID_SYSTEMREGISTRY_CONNECTIONS;

	// SUBSYSTEM PROPERTIES PAGE...
	public static String RESID_SUBSYSTEM_TYPE_LABEL;
	public static String RESID_SUBSYSTEM_TYPE_VALUE;
	public static String RESID_SUBSYSTEM_VENDOR_LABEL;
	public static String RESID_SUBSYSTEM_NAME_LABEL;
	public static String RESID_SUBSYSTEM_CONNECTION_LABEL;
	public static String RESID_SUBSYSTEM_PROFILE_LABEL;

	public static String RESID_SUBSYSTEM_PORT_LABEL;
	public static String RESID_SUBSYSTEM_PORT_TIP;
	public static String RESID_SUBSYSTEM_PORT_INHERITBUTTON_TIP;

	public static String RESID_SUBSYSTEM_USERID_LABEL;
	public static String RESID_SUBSYSTEM_USERID_TIP;

	public static String RESID_SUBSYSTEM_USERID_INHERITBUTTON_TIP;

	public static String RESID_SUBSYSTEM_SSL_LABEL;
	public static String RESID_SUBSYSTEM_SSL_TIP;
	
	public static String RESID_SUBSYSTEM_AUTODETECT_LABEL;
	public static String RESID_SUBSYSTEM_AUTODETECT_TIP;
	
	public static String RESID_SUBSYSTEM_SSL_ALERT_LABEL;
	public static String RESID_SUBSYSTEM_SSL_ALERT_TIP;
	
	public static String RESID_SUBSYSTEM_NONSSL_ALERT_LABEL;
	public static String RESID_SUBSYSTEM_NONSSL_ALERT_TIP;

	public static String RESID_SUBSYSTEM_ENVVAR_DESCRIPTION;
	public static String RESID_SUBSYSTEM_ENVVAR_TOOLTIP;

	public static String RESID_SUBSYSTEM_ENVVAR_NAME_TITLE;
	public static String RESID_SUBSYSTEM_ENVVAR_NAME_LABEL;
	public static String RESID_SUBSYSTEM_ENVVAR_NAME_TOOLTIP;

	public static String RESID_SUBSYSTEM_ENVVAR_VALUE_TITLE;
	public static String RESID_SUBSYSTEM_ENVVAR_VALUE_LABEL;
	public static String RESID_SUBSYSTEM_ENVVAR_VALUE_TOOLTIP;

	public static String RESID_SUBSYSTEM_ENVVAR_ADD_TOOLTIP;
	public static String RESID_SUBSYSTEM_ENVVAR_REMOVE_TOOLTIP;
	public static String RESID_SUBSYSTEM_ENVVAR_CHANGE_TOOLTIP;

	public static String RESID_SUBSYSTEM_ENVVAR_MOVEUP_LABEL;
	public static String RESID_SUBSYSTEM_ENVVAR_MOVEUP_TOOLTIP;
	public static String RESID_SUBSYSTEM_ENVVAR_MOVEDOWN_LABEL;
	public static String RESID_SUBSYSTEM_ENVVAR_MOVEDOWN_TOOLTIP;

	public static String RESID_SUBSYSTEM_ENVVAR_ADD_TITLE;
	public static String RESID_SUBSYSTEM_ENVVAR_CHANGE_TITLE;

	// COMMON PROPERTIES PAGE UI...
	public static String RESID_PP_PROPERTIES_TYPE_LABEL; 
	public static String RESID_PP_PROPERTIES_TYPE_TOOLTIP;

	// FILTER POOL PROPERTIES PAGE...
	public static String RESID_FILTERPOOL_TYPE_VALUE;

	public static String RESID_FILTERPOOL_NAME_LABEL; 
	public static String RESID_FILTERPOOL_NAME_TOOLTIP; 

	public static String RESID_FILTERPOOL_PROFILE_LABEL;
	public static String RESID_FILTERPOOL_PROFILE_TOOLTIP;

	public static String RESID_FILTERPOOL_REFERENCECOUNT_LABEL;
	public static String RESID_FILTERPOOL_REFERENCECOUNT_TOOLTIP; 

	public static String RESID_FILTERPOOL_RELATEDCONNECTION_LABEL; 
	public static String RESID_FILTERPOOL_RELATEDCONNECTION_TOOLTIP; 

	// FILTER POOL REFERENCE PROPERTIES PAGE...
	public static String RESID_FILTERPOOLREF_TYPE_VALUE;

	public static String RESID_FILTERPOOLREF_NAME_LABEL; 
	public static String RESID_FILTERPOOLREF_NAME_TOOLTIP; 

	public static String RESID_FILTERPOOLREF_SUBSYSTEM_LABEL; 
	public static String RESID_FILTERPOOLREF_SUBSYSTEM_TOOLTIP;

	public static String RESID_FILTERPOOLREF_CONNECTION_LABEL;
	public static String RESID_FILTERPOOLREF_CONNECTION_TOOLTIP;

	public static String RESID_FILTERPOOLREF_PROFILE_LABEL;
	public static String RESID_FILTERPOOLREF_PROFILE_TOOLTIP;

	// FILTER PROPERTIES PAGE...
	public static String RESID_PP_FILTER_TYPE_VALUE;

	public static String RESID_PP_FILTER_TYPE_PROMPTABLE_VALUE;
	public static String RESID_PP_FILTER_TYPE_PROMPTABLE_TOOLTIP;

	
	public static String RESID_PP_FILTER_NAME_LABEL; 
	public static String RESID_PP_FILTER_NAME_TOOLTIP; 

	public static String RESID_PP_FILTER_STRINGCOUNT_LABEL;
	public static String RESID_PP_FILTER_STRINGCOUNT_TOOLTIP;

	public static String RESID_PP_FILTER_FILTERPOOL_LABEL;
	public static String RESID_PP_FILTER_FILTERPOOL_TOOLTIP;

	public static String RESID_PP_FILTER_PROFILE_LABEL;
	public static String RESID_PP_FILTER_PROFILE_TOOLTIP;

	public static String RESID_PP_FILTER_ISCONNECTIONPRIVATE_LABEL;
	public static String RESID_PP_FILTER_ISCONNECTIONPRIVATE_TOOLTIP;

	// FILTER STRING PROPERTIES PAGE...
	public static String RESID_PP_FILTERSTRING_TYPE_VALUE;


	public static String RESID_PP_FILTERSTRING_FILTER_LABEL; 
	public static String RESID_PP_FILTERSTRING_FILTER_TOOLTIP; 

	public static String RESID_PP_FILTERSTRING_FILTERPOOL_LABEL;
	public static String RESID_PP_FILTERSTRING_FILTERPOOL_TOOLTIP; 


	public static String RESID_PP_FILTERSTRING_PROFILE_LABEL;
	public static String RESID_PP_FILTERSTRING_PROFILE_TOOLTIP;

	// SUBSYSTEM FACTORY PROPERTIES PAGE...
	public static String RESID_PP_SUBSYSFACTORY_ID_LABEL;
	public static String RESID_PP_SUBSYSFACTORY_ID_TOOLTIP; 

	public static String RESID_PP_SUBSYSFACTORY_VENDOR_LABEL; 
	public static String RESID_PP_SUBSYSFACTORY_VENDOR_TOOLTIP; 

	public static String RESID_PP_SUBSYSFACTORY_TYPES_LABEL;
	public static String RESID_PP_SUBSYSFACTORY_TYPES_TOOLTIP;

	public static String RESID_PP_SUBSYSFACTORY_VERBIAGE;

	// REMOTE SERVER LAUNCH PROPERTIES PAGE...
	public static String RESID_PROP_SERVERLAUNCHER_MEANS;
	public static String RESID_PROP_SERVERLAUNCHER_RADIO_DAEMON;
	public static String RESID_PROP_SERVERLAUNCHER_RADIO_REXEC;
	public static String RESID_PROP_SERVERLAUNCHER_RADIO_NONE;
	public static String RESID_PROP_SERVERLAUNCHER_RADIO_DAEMON_TOOLTIP;
	public static String RESID_PROP_SERVERLAUNCHER_RADIO_REXEC_TOOLTIP;
	public static String RESID_PROP_SERVERLAUNCHER_RADIO_NONE_TOOLTIP;
	public static String RESID_PROP_SERVERLAUNCHER_PATH;
	public static String RESID_PROP_SERVERLAUNCHER_PATH_TOOLTIP;
	public static String RESID_PROP_SERVERLAUNCHER_INVOCATION;
	public static String RESID_PROP_SERVERLAUNCHER_INVOCATION_TOOLTIP;

	
	

	// ---------------------------
	// RE-USABLE WIDGET STRINGS...
	// ---------------------------
	
	// WIDGETS IN SYSTEMCONNECTIONCOMBO.JAVA
	public static String WIDGET_CONNECTION_LABEL;
	public static String WIDGET_CONNECTION_TOOLTIP;

	public static String WIDGET_BUTTON_NEWCONNECTION_LABEL;
	public static String WIDGET_BUTTON_NEWCONNECTION_TOOLTIP;

	// -------------------------
	// PREFERENCES...
	// -------------------------
	public static String RESID_PREF_ROOT_PAGE;

	public static String RESID_PREF_SYSTYPE_COLHDG_NAME;
	public static String RESID_PREF_SYSTYPE_COLHDG_ENABLED;
	public static String RESID_PREF_SYSTYPE_COLHDG_DESC;
	public static String RESID_PREF_SYSTYPE_COLHDG_USERID;

	//
	// Signon Information Preferences Page
	//

	public static String RESID_PREF_SIGNON_HOSTNAME_TITLE;
	public static String RESID_PREF_SIGNON_HOSTNAME_LABEL;
	public static String RESID_PREF_SIGNON_HOSTNAME_TOOLTIP;

	public static String RESID_PREF_SIGNON_SYSTYPE_TITLE;
	public static String RESID_PREF_SIGNON_SYSTYPE_LABEL;
	public static String RESID_PREF_SIGNON_SYSTYPE_TOOLTIP;

	public static String RESID_PREF_SIGNON_USERID_TITLE;
	public static String RESID_PREF_SIGNON_USERID_LABEL;
	public static String RESID_PREF_SIGNON_USERID_TOOLTIP;

	public static String RESID_PREF_SIGNON_PASSWORD_LABEL;
	public static String RESID_PREF_SIGNON_PASSWORD_TOOLTIP;

	public static String RESID_PREF_SIGNON_PASSWORD_VERIFY_LABEL;
	public static String RESID_PREF_SIGNON_PASSWORD_VERIFY_TOOLTIP;

	public static String RESID_PREF_SIGNON_ADD_LABEL;
	public static String RESID_PREF_SIGNON_ADD_TOOLTIP;

	public static String RESID_PREF_SIGNON_REMOVE_LABEL;
	public static String RESID_PREF_SIGNON_REMOVE_TOOLTIP;

	public static String RESID_PREF_SIGNON_CHANGE_LABEL;
	public static String RESID_PREF_SIGNON_CHANGE_TOOLTIP;

	public static String RESID_PREF_SIGNON_ADD_DIALOG_TITLE;

	public static String RESID_PREF_SIGNON_CHANGE_DIALOG_TITLE;


	// Offline constants (yantzi:3.0)
	public static String RESID_OFFLINE_LABEL;
	public static String RESID_OFFLINE_WORKOFFLINE_LABEL;
	public static String RESID_OFFLINE_WORKOFFLINE_TOOLTIP;

	// -------------------------------------------
	// remote search view constants
	// -------------------------------------------

	// Remove selected matches action
	public static String RESID_SEARCH_REMOVE_SELECTED_MATCHES_LABEL;
	public static String RESID_SEARCH_REMOVE_SELECTED_MATCHES_TOOLTIP;

	// Remove all matches action
	public static String RESID_SEARCH_REMOVE_ALL_MATCHES_LABEL;
	public static String RESID_SEARCH_REMOVE_ALL_MATCHES_TOOLTIP;

	// Clear history action
	public static String RESID_SEARCH_CLEAR_HISTORY_LABEL;
	public static String RESID_SEARCH_CLEAR_HISTORY_TOOLTIP;

	/** ******************************************* */
	/* Generated Vars */
	/** ******************************************* */
	public static String RESID_PREF_USERID_PERTYPE_PREFIX_LABEL;
	public static String RESID_PREF_USERID_PERTYPE_PREFIX_TOOLTIP;

	public static String RESID_PREF_SHOWFILTERPOOLS_PREFIX_LABEL;
	public static String RESID_PREF_SHOWFILTERPOOLS_PREFIX_TOOLTIP;

	public static String RESID_PREF_SHOWNEWCONNECTIONPROMPT_PREFIX_LABEL;
	public static String RESID_PREF_SHOWNEWCONNECTIONPROMPT_PREFIX_TOOLTIP;

	public static String RESID_PREF_QUALIFYCONNECTIONNAMES_PREFIX_LABEL;
	public static String RESID_PREF_QUALIFYCONNECTIONNAMES_PREFIX_TOOLTIP;

	public static String RESID_PREF_REMEMBERSTATE_PREFIX_LABEL;
	public static String RESID_PREF_REMEMBERSTATE_PREFIX_TOOLTIP;


	public static String RESID_PREF_RESTOREFROMCACHE_PREFIX_LABEL;
	public static String RESID_PREF_RESTOREFROMCACHE_PREFIX_TOOLTIP;
	

	
	//
	// Actions
	//
	// Browse with menu item
	public static String ACTION_CASCADING_BROWSEWITH_LABEL;
	public static String ACTION_CASCADING_BROWSEWITH_TOOLTIP;

	// Compare with menu item
	public static String ACTION_CASCADING_COMPAREWITH_LABEL;
	public static String ACTION_CASCADING_COMPAREWITH_TOOLTIP;


	// Replace with menu item
	public static String ACTION_CASCADING_REPLACEWITH_LABEL;
	public static String ACTION_CASCADING_REPLACEWITH_TOOLTIP;
	
	public static String ACTION_RENAME_LABEL;
	public static String ACTION_RENAME_TOOLTIP;


	public static String ACTION_NEWFILE_LABEL;
	public static String ACTION_NEWFILE_TOOLTIP;

	public static String ACTION_CASCADING_NEW_LABEL;
	public static String ACTION_CASCADING_NEW_TOOLTIP;

	public static String ACTION_CASCADING_GOTO_LABEL;
	public static String ACTION_CASCADING_GOTO_TOOLTIP;

	public static String ACTION_CASCADING_GOINTO_LABEL;
	public static String ACTION_CASCADING_GOINTO_TOOLTIP;

	public static String ACTION_CASCADING_OPEN_LABEL;
	public static String ACTION_CASCADING_OPEN_TOOLTIP;

	public static String ACTION_CASCADING_OPENWITH_LABEL;
	public static String ACTION_CASCADING_OPENWITH_TOOLTIP;

	public static String ACTION_CASCADING_WORKWITH_LABEL;
	public static String ACTION_CASCADING_WORKWITH_TOOLTIP;

	public static String ACTION_CASCADING_REMOTESERVERS_LABEL;
	public static String ACTION_CASCADING_REMOTESERVERS_TOOLTIP;

	public static String ACTION_REMOTESERVER_START_LABEL;
	public static String ACTION_REMOTESERVER_START_TOOLTIP;
	
	public static String ACTION_REMOTESERVER_STOP_LABEL;
	public static String ACTION_REMOTESERVER_STOP_TOOLTIP;

	public static String ACTION_CASCADING_EXPAND_TO_LABEL;
	public static String ACTION_CASCADING_EXPAND_TO_TOOLTIP;

	public static String ACTION_CASCADING_VIEW_LABEL;
	public static String ACTION_CASCADING_VIEW_TOOLTIP;

	public static String ACTION_CASCADING_PREFERENCES_LABEL;
	//FIXME This one is OBSOLETE with https://bugs.eclipse.org/bugs/show_bug.cgi?id=186769
	public static String ACTION_CASCADING_PREFERENCES_TOOLTIP;

	public static String ACTION_CASCADING_PULLDOWN_LABEL;
	public static String ACTION_CASCADING_PULLDOWN_TOOLTIP;

	public static String ACTION_CASCADING_FILTERPOOL_NEWREFERENCE_LABEL;
	public static String ACTION_CASCADING_FILTERPOOL_NEWREFERENCE_TOOLTIP;

	public static String ACTION_TEAM_RELOAD_LABEL;
	public static String ACTION_TEAM_RELOAD_TOOLTIP;

	public static String ACTION_PROFILE_MAKEACTIVE_LABEL;
	public static String ACTION_PROFILE_MAKEACTIVE_TOOLTIP;

	public static String ACTION_PROFILE_MAKEINACTIVE_LABEL;
	public static String ACTION_PROFILE_MAKEINACTIVE_TOOLTIP;

	public static String ACTION_PROFILE_COPY_LABEL;
	public static String ACTION_PROFILE_COPY_TOOLTIP;

	public static String ACTION_NEWPROFILE_LABEL;
	public static String ACTION_NEWPROFILE_TOOLTIP;

	public static String ACTION_NEW_PROFILE_LABEL;
	public static String ACTION_NEW_PROFILE_TOOLTIP;

	public static String ACTION_QUALIFY_CONNECTION_NAMES_LABEL;
	public static String ACTION_QUALIFY_CONNECTION_NAMES_TOOLTIP;

	public static String ACTION_RESTORE_STATE_PREFERENCE_LABEL;
	public static String ACTION_RESTORE_STATE_PREFERENCE_TOOLTIP;

	public static String ACTION_PREFERENCE_SHOW_FILTERPOOLS_LABEL;
	public static String ACTION_PREFERENCE_SHOW_FILTERPOOLS_TOOLTIP;

	public static String ACTION_SHOW_PREFERENCEPAGE_LABEL;
	public static String ACTION_SHOW_PREFERENCEPAGE_TOOLTIP;

	public static String ACTION_NEWCONN_LABEL;
	public static String ACTION_NEWCONN_TOOLTIP;

	public static String ACTION_ANOTHERCONN_LABEL;
	public static String ACTION_ANOTHERCONN_TOOLTIP;

	public static String ACTION_TESTFILTERSTRING_LABEL;
	public static String ACTION_TESTFILTERSTRING_TOOLTIP;

	public static String ACTION_NEWFILTER_LABEL;
	public static String ACTION_NEWFILTER_TOOLTIP;

	public static String ACTION_UPDATEFILTER_LABEL;
	public static String ACTION_UPDATEFILTER_TOOLTIP;

	public static String ACTION_NEWFILTERPOOL_LABEL;
	public static String ACTION_NEWFILTERPOOL_TOOLTIP;

	public static String ACTION_RMVFILTERPOOLREF_LABEL;
	public static String ACTION_RMVFILTERPOOLREF_TOOLTIP;

	public static String ACTION_SELECTFILTERPOOLS_LABEL;
	public static String ACTION_SELECTFILTERPOOLS_TOOLTIP;

	public static String ACTION_WORKWITH_FILTERPOOLS_LABEL;
	public static String ACTION_WORKWITH_FILTERPOOLS_TOOLTIP;

	public static String ACTION_WORKWITH_WWFILTERPOOLS_LABEL;
	public static String ACTION_WORKWITH_WWFILTERPOOLS_TOOLTIP;

	public static String ACTION_WORKWITH_PROFILES_LABEL;
	public static String ACTION_WORKWITH_PROFILES_TOOLTIP;

	public static String ACTION_RUN_LABEL;
	public static String ACTION_RUN_TOOLTIP;

	public static String ACTION_REFRESH_ALL_LABEL;
	public static String ACTION_REFRESH_ALL_TOOLTIP;

	public static String ACTION_REFRESH_LABEL;
	public static String ACTION_REFRESH_TOOLTIP;

	public static String ACTION_REFRESH_TABLE_LABLE;
	public static String ACTION_REFRESH_TABLE_TOOLTIP;

	public static String ACTION_DELETE_LABEL;
	public static String ACTION_DELETE_TOOLTIP;

	public static String ACTION_CLEAR_LABEL;
	public static String ACTION_CLEAR_TOOLTIP;

	public static String ACTION_CLEAR_ALL_LABEL;
	public static String ACTION_CLEAR_ALL_TOOLTIP;

	public static String ACTION_CLEAR_SELECTED_LABEL;
	public static String ACTION_CLEAR_SELECTED_TOOLTIP;

	public static String ACTION_MOVEUP_LABEL;
	public static String ACTION_MOVEUP_TOOLTIP;

	public static String ACTION_MOVEDOWN_LABEL;
	public static String ACTION_MOVEDOWN_TOOLTIP;
	public static String ACTION_MOVEUPLEVEL_TOOLTIP;
	public static String ACTION_GOUPLEVEL_TOOLTIP;

	public static String ACTION_CONNECT_LABEL;
	public static String ACTION_CONNECT_TOOLTIP;

	public static String ACTION_CLEARPASSWORD_LABEL;
	public static String ACTION_CLEARPASSWORD_TOOLTIP;

	public static String ACTION_DISCONNECT_LABEL;
	public static String ACTION_DISCONNECT_TOOLTIP;

	public static String ACTION_DISCONNECTALLSUBSYSTEMS_LABEL;
	public static String ACTION_DISCONNECTALLSUBSYSTEMS_TOOLTIP;

	public static String ACTION_CONNECT_ALL_LABEL;
	public static String ACTION_CONNECT_ALL_TOOLTIP;

	public static String ACTION_CLEARPASSWORD_ALL_LABEL;
	public static String ACTION_CLEARPASSWORD_ALL_TOOLTIP;

	public static String ACTION_SET_LABEL;
	public static String ACTION_SET_TOOLTIP;

	public static String ACTION_HISTORY_DELETE_LABEL;
	public static String ACTION_HISTORY_DELETE_TOOLTIP;

	public static String ACTION_HISTORY_CLEAR_LABEL;
	public static String ACTION_HISTORY_CLEAR_TOOLTIP;

	public static String ACTION_HISTORY_MOVEUP_LABEL;
	public static String ACTION_HISTORY_MOVEUP_TOOLTIP;

	public static String ACTION_HISTORY_MOVEDOWN_LABEL;
	public static String ACTION_HISTORY_MOVEDOWN_TOOLTIP;

	public static String ACTION_HISTORY_MOVEFORWARD_LABEL;
	public static String ACTION_HISTORY_MOVEFORWARD_TOOLTIP;

	public static String ACTION_HISTORY_MOVEBACKWARD_LABEL;
	public static String ACTION_HISTORY_MOVEBACKWARD_TOOLTIP;
	
	public static String ACTION_COPY_LABEL;	
	public static String ACTION_COPY_TOOLTIP;

	public static String ACTION_CUT_LABEL;	
	public static String ACTION_CUT_TOOLTIP;
	
	public static String ACTION_UNDO_LABEL;
	public static String ACTION_UNDO_TOOLTIP;
	
	public static String ACTION_PASTE_LABEL;
	public static String ACTION_PASTE_TOOLTIP;

	public static String ACTION_COPY_CONNECTION_LABEL;
	public static String ACTION_COPY_CONNECTION_TOOLTIP;

	public static String ACTION_COPY_FILTERPOOL_LABEL;
	public static String ACTION_COPY_FILTERPOOL_TOOLTIP;

	public static String ACTION_COPY_FILTER_LABEL;
	public static String ACTION_COPY_FILTER_TOOLTIP;

	public static String ACTION_COPY_FILTERSTRING_LABEL;
	public static String ACTION_COPY_FILTERSTRING_TOOLTIP;

	public static String ACTION_MOVE_LABEL;
	public static String ACTION_MOVE_TOOLTIP;

	public static String ACTION_MOVE_CONNECTION_LABEL;
	public static String ACTION_MOVE_CONNECTION_TOOLTIP;

	public static String ACTION_MOVE_FILTERPOOL_LABEL;
	public static String ACTION_MOVE_FILTERPOOL_TOOLTIP;

	public static String ACTION_MOVE_FILTER_LABEL;
	public static String ACTION_MOVE_FILTER_TOOLTIP;

	public static String ACTION_MOVE_FILTERSTRING_LABEL;
	public static String ACTION_MOVE_FILTERSTRING_TOOLTIP;

	public static String ACTION_TABLE_LABEL;
	public static String ACTION_TABLE_TOOLTIP;
	
	public static String ACTION_MONITOR_LABEL;
	public static String ACTION_MONITOR_TOOLTIP;

	public static String ACTION_SEARCH_LABEL;
	public static String ACTION_SEARCH_TOOLTIP;

	public static String ACTION_CANCEL_SEARCH_LABEL;
	public static String ACTION_CANCEL_SEARCH_TOOLTIP;

	public static String ACTION_LOCK_LABEL;
	public static String ACTION_LOCK_TOOLTIP;

	public static String ACTION_UNLOCK_LABEL;
	public static String ACTION_UNLOCK_TOOLTIP;
	

	public static String ACTION_POSITIONTO_LABEL;
	public static String ACTION_POSITIONTO_TOOLTIP;

	public static String ACTION_SUBSET_LABEL;
	public static String ACTION_SUBSET_TOOLTIP;

	public static String ACTION_PRINTLIST_LABEL;
	public static String ACTION_PRINTLIST_TOOLTIP;

	public static String ACTION_SELECTCOLUMNS_LABEL;
	public static String ACTION_SELECTCOLUMNS_TOOLTIP;

	public static String ACTION_OPENEXPLORER_DIFFPERSP2_LABEL;
	public static String ACTION_OPENEXPLORER_DIFFPERSP2_TOOLTIP;

	public static String ACTION_EXPAND_SELECTED_LABEL;
	public static String ACTION_EXPAND_SELECTED_TOOLTIP;

	public static String ACTION_COLLAPSE_SELECTED_LABEL;
	public static String ACTION_COLLAPSE_SELECTED_TOOLTIP;

	public static String ACTION_COLLAPSE_ALL_LABEL;
	public static String ACTION_COLLAPSE_ALL_TOOLTIP;

	public static String ACTION_EXPAND_ALL_LABEL;
	public static String ACTION_EXPAND_ALL_TOOLTIP;
	
	public static String ACTION_SELECT_ALL_LABEL;
	public static String ACTION_SELECT_ALL_TOOLTIP;

	public static String ACTION_SELECT_INPUT_LABEL;
	public static String ACTION_SELECT_INPUT_DLG;
	public static String ACTION_SELECT_INPUT_TOOLTIP;

	
	
	// services and connector services property pages
	public static String RESID_PROPERTIES_SERVICES_NAME;
	public static String RESID_PROPERTIES_SERVICES_LABEL;
	public static String RESID_PROPERTIES_SERVICES_TOOLTIP;
	public static String RESID_PROPERTIES_DESCRIPTION_LABEL;
	public static String RESID_PROPERTIES_FACTORIES_LABEL;
	public static String RESID_PROPERTIES_FACTORIES_TOOLTIP;
	public static String RESID_PROPERTIES_PROPERTIES_LABEL;
	public static String RESID_PROPERTIES_PROPERTIES_TOOLTIP;


	// Services form
	public static String RESID_SERVICESFORM_CONFIGURATION_TOOLTIP;
	public static String RESID_SERVICESFORM_SERVICES_TOOLTIP;
	public static String RESID_SERVICESFORM_CONNECTORSERVICES_TOOLTIP;
	public static String RESID_SERVICESFORM_PROPERTIES_TOOLTIP;
	
	// Do not show again message
	public static String RESID_DO_NOT_SHOW_MESSAGE_AGAIN_LABEL;
	public static String RESID_DO_NOT_SHOW_MESSAGE_AGAIN_TOOLTIP;
	
	public static String RESID_EXPORT_CONNECTION_ACTIONS_TOOLTIP;

	public static String RESID_EXPORT_CONNECTIONS_ACTION_LABEL;

	// Encoding
	public static String RESID_HOST_ENCODING_GROUP_LABEL;
	public static String RESID_HOST_ENCODING_SETTING_NOTE;
	public static String RESID_HOST_ENCODING_SETTING_MSG;
	public static String RESID_HOST_ENCODING_REMOTE_LABEL;
	public static String RESID_HOST_ENCODING_REMOTE_ENCODING_LABEL;
	public static String RESID_HOST_ENCODING_REMOTE_TOOLTIP;
	public static String RESID_HOST_ENCODING_OTHER_LABEL;
	public static String RESID_HOST_ENCODING_OTHER_TOOLTIP;
	public static String RESID_HOST_ENCODING_ENTER_TOOLTIP;

	public static String RESID_IMPORT_CONNECTION_ACTION_LABEL;

	public static String RESID_IMPORT_CONNECTION_ACTION_TOOLTIP;

	public static String RESID_IMPORT_CONNECTION_LABEL_LONG;
	
	public static String SystemExportConnectionAction_CoreExceptionFound;

	public static String SystemExportConnectionAction_Error;

	public static String SystemExportConnectionAction_ExportJobName;

	public static String SystemExportConnectionAction_OverwriteFileCondition;

	public static String SystemExportConnectionAction_UnexpectedException;

	public static String SystemExportConnectionAction_Warning;

	public static String SystemExportConnectionAction_WriteProtectedFileCondition;

	public static String SystemImportConnectionAction_CoreExceptionFound;

	public static String SystemImportConnectionAction_Error;

	public static String SystemImportConnectionAction_FileNotFoundCondition;

	public static String SystemImportConnectionAction_FileNotReadableCondition;

	public static String SystemImportConnectionAction_ImportJobName;

	public static String SystemImportConnectionAction_UnexpectedException;

	public static String SystemTableViewPart_title;

	public static String SystemTypeFieldEditor_false;

	public static String SystemTypeFieldEditor_true;
	
	
	// collision dialog when copying from RSE resources to Eclipse resources
	public static String RESID_COLLISION_DUPLICATE_RESOURCE_TITLE;
	public static String RESID_COLLISION_OVERWRITE_RESOURCE_MESSAGE;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, SystemResources.class);
	}
}
