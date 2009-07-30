/********************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others. All rights reserved.
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
 * Michael Berger (IBM) - 146339 Added refresh action graphic.
 * Martin Oberhuber (Wind River) - [174945] split importexport icons from rse.ui
 * Martin Oberhuber (Wind River) - [174945] Remove obsolete icons from rse.shells.ui
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 ********************************************************************************/

package org.eclipse.rse.ui;

/**
 * Constants used throughout the System plugin.
 */
public interface ISystemIconConstants
{
	public static final String PLUGIN_ID ="org.eclipse.rse.ui"; //$NON-NLS-1$
	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

	// Icons
	public static final String ICON_SUFFIX = "Icon";	 //$NON-NLS-1$
	public static final String ICON_BANNER_SUFFIX = "BannerIcon";	 //$NON-NLS-1$
	public static final String ICON_EXT = ".gif";	 //$NON-NLS-1$

    // WIZARD ICONS...
    public static final String ICON_WIZARD_DIR = "full/wizban/"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWPROFILEWIZARD_ROOT = "newprofile_wiz";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWPROFILEWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWPROFILEWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWPROFILEWIZARD_ID = PREFIX + ICON_SYSTEM_NEWPROFILEWIZARD_ROOT + ICON_BANNER_SUFFIX;

	public static final String ICON_SYSTEM_NEWCONNECTIONWIZARD_ROOT = "newconnection_wiz";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWCONNECTIONWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWCONNECTIONWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWCONNECTIONWIZARD_ID = PREFIX + ICON_SYSTEM_NEWCONNECTIONWIZARD_ROOT + ICON_BANNER_SUFFIX;

	public static final String ICON_SYSTEM_NEWFILTERWIZARD_ROOT = "newfilter_wiz";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWFILTERWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWFILTERWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILTERWIZARD_ID = PREFIX + ICON_SYSTEM_NEWFILTERWIZARD_ROOT + ICON_BANNER_SUFFIX;

	public static final String ICON_SYSTEM_NEWFILTERPOOLWIZARD_ROOT = "newfilterpool_wiz";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWFILTERPOOLWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWFILTERPOOLWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILTERPOOLWIZARD_ID = PREFIX + ICON_SYSTEM_NEWFILTERPOOLWIZARD_ROOT + ICON_BANNER_SUFFIX;

    public static final String ICON_SYSTEM_NEWFILEWIZARD_ROOT = "newfile_wiz"; //$NON-NLS-1$
    public static final String ICON_SYSTEM_NEWFILEWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWFILEWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILEWIZARD_ID = PREFIX + ICON_SYSTEM_NEWFILEWIZARD_ROOT + ICON_BANNER_SUFFIX;

	public static final String ICON_SYSTEM_NEWFOLDERWIZARD_ROOT = "newfolder_wiz"; //$NON-NLS-1$
    public static final String ICON_SYSTEM_NEWFOLDERWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWFOLDERWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWFOLDERWIZARD_ID = PREFIX + ICON_SYSTEM_NEWFOLDERWIZARD_ROOT + ICON_BANNER_SUFFIX;


    // THING ICONS...
    public static final String ICON_MODEL_DIR = "full/obj16/";	 //$NON-NLS-1$

	public static final String ICON_SYSTEM_PROFILE_ROOT = "systemprofile";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_PROFILE      = ICON_MODEL_DIR + ICON_SYSTEM_PROFILE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_PROFILE_ID   = PREFIX+ICON_SYSTEM_PROFILE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_PROFILE_ACTIVE_ROOT = "systemprofile_active";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_PROFILE_ACTIVE      = ICON_MODEL_DIR + ICON_SYSTEM_PROFILE_ACTIVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_PROFILE_ACTIVE_ID   = PREFIX+ICON_SYSTEM_PROFILE_ACTIVE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_CONNECTION_ROOT = "systemconnection";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_CONNECTION      = ICON_MODEL_DIR + ICON_SYSTEM_CONNECTION_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CONNECTION_ID   = PREFIX+ICON_SYSTEM_CONNECTION_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_CONNECTIONLIVE_ROOT = "systemconnectionlive"; // not currently used	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_CONNECTIONLIVE      = ICON_MODEL_DIR + ICON_SYSTEM_CONNECTIONLIVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CONNECTIONLIVE_ID   = PREFIX+ICON_SYSTEM_CONNECTIONLIVE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_FILTERPOOL_ROOT = "systemfilterpool";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_FILTERPOOL      = ICON_MODEL_DIR + ICON_SYSTEM_FILTERPOOL_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_FILTERPOOL_ID   = PREFIX+ICON_SYSTEM_FILTERPOOL_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_FILTER_ROOT = "systemfilter"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_FILTER_ID = PREFIX + ICON_SYSTEM_FILTER_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_FILTER    = ICON_MODEL_DIR + ICON_SYSTEM_FILTER_ROOT + ICON_EXT;

	public static final String ICON_SYSTEM_FILTERSTRING_ROOT = "systemfilterstring"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_FILTERSTRING_ID = PREFIX + ICON_SYSTEM_FILTERSTRING_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_FILTERSTRING    = ICON_MODEL_DIR + ICON_SYSTEM_FILTERSTRING_ROOT + ICON_EXT;

	public static final String ICON_SYSTEM_ROOTDRIVE_ROOT = "systemrootdrive";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_ROOTDRIVE      = ICON_MODEL_DIR + ICON_SYSTEM_ROOTDRIVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ROOTDRIVE_ID   = PREFIX+ICON_SYSTEM_ROOTDRIVE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_ROOTDRIVEOPEN_ROOT = "systemrootdriveopen";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_ROOTDRIVEOPEN      = ICON_MODEL_DIR + ICON_SYSTEM_ROOTDRIVEOPEN_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ROOTDRIVEOPEN_ID   = PREFIX+ICON_SYSTEM_ROOTDRIVEOPEN_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_FOLDER_ROOT = "systemfolder";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_FOLDER      = ICON_MODEL_DIR + ICON_SYSTEM_FOLDER_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_FOLDER_ID   = PREFIX+ICON_SYSTEM_FOLDER_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_PROCESS_ROOT = "systemprocess";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_PROCESS      = ICON_MODEL_DIR + ICON_SYSTEM_PROCESS_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_PROCESS_ID   = PREFIX+ICON_SYSTEM_PROCESS+ICON_SUFFIX;

	public static final String ICON_SYSTEM_TARGET_ROOT = "systemTarget";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_TARGET      = ICON_MODEL_DIR + ICON_SYSTEM_TARGET_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_TARGET_ID   = PREFIX+ICON_SYSTEM_TARGET_ROOT+ICON_SUFFIX;

    // NEW ACTION ICONS...
    public static final String ICON_NEWACTIONS_DIR = "full/ctool16/";	 //$NON-NLS-1$

	public static final String ICON_SYSTEM_NEW_ROOT = "new";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEW      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEW_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEW_ID   = PREFIX+ICON_SYSTEM_NEW_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWPROFILE_ROOT = "newprofile_wiz";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWPROFILE      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWPROFILE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWPROFILE_ID   = PREFIX+ICON_SYSTEM_NEWPROFILE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWCONNECTION_ROOT = "newconnection_wiz";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWCONNECTION      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWCONNECTION_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWCONNECTION_ID   = PREFIX+ICON_SYSTEM_NEWCONNECTION_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWFILTERPOOL_ROOT = "newfilterpool_wiz";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWFILTERPOOL      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWFILTERPOOL_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILTERPOOL_ID   = PREFIX+ICON_SYSTEM_NEWFILTERPOOL_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWFILTERPOOLREF_ROOT = "newfilterpoolref_wiz";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWFILTERPOOLREF      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWFILTERPOOLREF_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILTERPOOLREF_ID   = PREFIX+ICON_SYSTEM_NEWFILTERPOOLREF_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWFILTER_ROOT = "newfilter_wiz";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWFILTER      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWFILTER_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILTER_ID   = PREFIX+ICON_SYSTEM_NEWFILTER_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWFILE_ROOT = "newfile_wiz";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWFILE      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWFILE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILE_ID   = PREFIX+ICON_SYSTEM_NEWFILE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWFOLDER_ROOT = "newfolder_wiz";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_NEWFOLDER      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWFOLDER_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWFOLDER_ID   = PREFIX+ICON_SYSTEM_NEWFOLDER_ROOT+ICON_SUFFIX;


    // OTHER ACTION ICONS...
    public static final String ICON_ACTIONS_DIR = "full/elcl16/";	 //$NON-NLS-1$

	public static final String ICON_SYSTEM_LOCK_ROOT = "lock";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_LOCK      = ICON_ACTIONS_DIR + ICON_SYSTEM_LOCK_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_LOCK_ID   = PREFIX+ICON_SYSTEM_LOCK_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_MOVEUP_ROOT = "up";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_MOVEUP      = ICON_ACTIONS_DIR + ICON_SYSTEM_MOVEUP_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_MOVEUP_ID   = PREFIX+ICON_SYSTEM_MOVEUP_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_MOVEDOWN_ROOT = "down";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_MOVEDOWN      = ICON_ACTIONS_DIR + ICON_SYSTEM_MOVEDOWN_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_MOVEDOWN_ID   = PREFIX+ICON_SYSTEM_MOVEDOWN_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_MOVE_ROOT = "move";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_MOVE      = ICON_ACTIONS_DIR + ICON_SYSTEM_MOVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_MOVE_ID   = PREFIX+ICON_SYSTEM_MOVE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_CLEAR_ROOT = "clear";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_CLEAR      = ICON_ACTIONS_DIR + ICON_SYSTEM_CLEAR_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CLEAR_ID   = PREFIX+ICON_SYSTEM_CLEAR_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_CLEAR_ALL_ROOT = "clearall";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_CLEAR_ALL      = ICON_ACTIONS_DIR + ICON_SYSTEM_CLEAR_ALL_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CLEAR_ALL_ID   = PREFIX+ICON_SYSTEM_CLEAR_ALL_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_CLEAR_SELECTED_ROOT = "clearselected";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_CLEAR_SELECTED      = ICON_ACTIONS_DIR + ICON_SYSTEM_CLEAR_SELECTED_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CLEAR_SELECTED_ID   = PREFIX+ICON_SYSTEM_CLEAR_SELECTED_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_DELETEREF_ROOT = "deletereference";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_DELETEREF      = ICON_ACTIONS_DIR + ICON_SYSTEM_DELETEREF_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_DELETEREF_ID   = PREFIX+ICON_SYSTEM_DELETEREF_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_RUN_ROOT    = "run";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_RUN         = ICON_ACTIONS_DIR + ICON_SYSTEM_RUN_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_RUN_ID      = PREFIX+ICON_SYSTEM_RUN_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_STOP_ROOT    = "stop";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_STOP         = ICON_ACTIONS_DIR + ICON_SYSTEM_STOP_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_STOP_ID      = PREFIX+ICON_SYSTEM_STOP_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_RENAME_ROOT = "rename";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_RENAME      = ICON_ACTIONS_DIR + ICON_SYSTEM_RENAME_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_RENAME_ID   = PREFIX+ICON_SYSTEM_RENAME_ROOT+ICON_SUFFIX;

	public static final String ICON_IDE_REFRESH_ID = "elcl16/refresh_nav.gif"; //$NON-NLS-1$
	public static final String ICON_IDE_LINKTOEDITOR_ID = "elcl16/synced.gif"; //$NON-NLS-1$
	public static final String ICON_IDE_FILTER_ID = "elcl16/filter_ps.gif"; //$NON-NLS-1$

	/**
	 * @deprecated use {@link org.eclipse.ui.ISharedImages} via
	 *             PlatformUI.getWorkbench().getSharedImages()
	 */
	public static final String ICON_IDE_COLLAPSEALL_ID = "elcl16/collapseall.gif"; //$NON-NLS-1$

	public static final String ICON_SYSTEM_MAKEPROFILEACTIVE_ROOT = "makeprofileactive";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_MAKEPROFILEACTIVE      = ICON_ACTIONS_DIR + ICON_SYSTEM_MAKEPROFILEACTIVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_MAKEPROFILEACTIVE_ID   = PREFIX+ICON_SYSTEM_MAKEPROFILEACTIVE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_MAKEPROFILEINACTIVE_ROOT = "makeprofileinactive";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_MAKEPROFILEINACTIVE      = ICON_ACTIONS_DIR + ICON_SYSTEM_MAKEPROFILEINACTIVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_MAKEPROFILEINACTIVE_ID   = PREFIX+ICON_SYSTEM_MAKEPROFILEINACTIVE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_CHANGEFILTER_ROOT = "editfilter";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_CHANGEFILTER      = ICON_ACTIONS_DIR + ICON_SYSTEM_CHANGEFILTER_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CHANGEFILTER_ID   = PREFIX+ICON_SYSTEM_CHANGEFILTER_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_SELECTPROFILE_ROOT = "selectprofile";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_SELECTPROFILE      = ICON_ACTIONS_DIR + ICON_SYSTEM_SELECTPROFILE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_SELECTPROFILE_ID   = PREFIX+ICON_SYSTEM_SELECTPROFILE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_SELECTFILTERPOOLS_ROOT = "selectpool";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_SELECTFILTERPOOLS      = ICON_ACTIONS_DIR + ICON_SYSTEM_SELECTFILTERPOOLS_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_SELECTFILTERPOOLS_ID   = PREFIX+ICON_SYSTEM_SELECTFILTERPOOLS_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_WORKWITHFILTERPOOLS_ROOT = "workwithfilterpools";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_WORKWITHFILTERPOOLS      = ICON_ACTIONS_DIR + ICON_SYSTEM_WORKWITHFILTERPOOLS_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_WORKWITHFILTERPOOLS_ID   = PREFIX+ICON_SYSTEM_WORKWITHFILTERPOOLS_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_SHOW_TABLE_ROOT 	= "systemshowintable"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_SHOW_TABLE      	= ICON_ACTIONS_DIR + ICON_SYSTEM_SHOW_TABLE_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_SHOW_TABLE_ID  	= PREFIX + ICON_SYSTEM_SHOW_TABLE_ROOT + ICON_SUFFIX;

	public static final String ICON_SYSTEM_SHOW_MONITOR_ROOT = "monitor_view"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_SHOW_MONITOR    = ICON_ACTIONS_DIR + ICON_SYSTEM_SHOW_MONITOR_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_SHOW_MONITOR_ID  = PREFIX + ICON_SYSTEM_SHOW_MONITOR_ROOT + ICON_SUFFIX;

	public static final String ICON_SYSTEM_SHOW_SHELL_ROOT 	= "systemshell"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_SHOW_SHELL      	= ICON_ACTIONS_DIR + ICON_SYSTEM_SHOW_SHELL_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_SHOW_SHELL_ID  	= PREFIX + ICON_SYSTEM_SHOW_SHELL_ROOT + ICON_SUFFIX;

	public static final String ICON_SYSTEM_REFRESH_ROOT = "refresh_nav"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_REFRESH = ICON_ACTIONS_DIR + ICON_SYSTEM_REFRESH_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_REFRESH_ID = PREFIX + ICON_SYSTEM_REFRESH_ROOT + ICON_SUFFIX;

    // SPECIAL MODEL OBJECT ICONS...
    public static final String ICON_OBJS_DIR = "full/obj16/";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_ERROR_ROOT = "error"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_ERROR_ID = PREFIX + ICON_SYSTEM_ERROR_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_ERROR    = ICON_OBJS_DIR + ICON_SYSTEM_ERROR_ROOT + ICON_EXT;

	// info is to be used in dialogs
	public static final String ICON_SYSTEM_INFO_ROOT = "info"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_INFO_ID = PREFIX + ICON_SYSTEM_INFO_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_INFO    = ICON_OBJS_DIR + ICON_SYSTEM_INFO_ROOT + ICON_EXT;

	// systeminfo is to be used in tree view
	public static final String ICON_SYSTEM_INFO_TREE_ROOT = "systeminfo"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_INFO_TREE_ID   = PREFIX + ICON_SYSTEM_INFO_TREE_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_INFO_TREE      = ICON_OBJS_DIR + ICON_SYSTEM_INFO_TREE_ROOT + ICON_EXT;

	public static final String ICON_SYSTEM_HELP_ROOT = "systemhelp"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_HELP_ID   = PREFIX + ICON_SYSTEM_HELP_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_HELP      = ICON_OBJS_DIR + ICON_SYSTEM_HELP_ROOT + ICON_EXT;

	public static final String ICON_SYSTEM_CANCEL_ROOT = "systemcancel"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_CANCEL_ID = PREFIX + ICON_SYSTEM_CANCEL_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_CANCEL    = ICON_OBJS_DIR + ICON_SYSTEM_CANCEL_ROOT + ICON_EXT;

	public static final String ICON_SYSTEM_EMPTY_ROOT = "systemempty"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_EMPTY_ID = PREFIX + ICON_SYSTEM_EMPTY_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_EMPTY    = ICON_OBJS_DIR + ICON_SYSTEM_EMPTY_ROOT + ICON_EXT;

	public static final String ICON_SYSTEM_OK_ROOT = "systemok"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_OK_ID = PREFIX + ICON_SYSTEM_OK_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_OK    = ICON_OBJS_DIR + ICON_SYSTEM_OK_ROOT + ICON_EXT;

	public static final String ICON_SYSTEM_WARNING_ROOT = "warning"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_WARNING_ID = PREFIX + ICON_SYSTEM_WARNING_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_WARNING    = ICON_OBJS_DIR + ICON_SYSTEM_WARNING_ROOT + ICON_EXT;

	public static final String ICON_SYSTEM_BLANK_ROOT = "systemblank"; // not used yet //$NON-NLS-1$
	public static final String ICON_SYSTEM_BLANK_ID = PREFIX + ICON_SYSTEM_BLANK_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_BLANK    = ICON_OBJS_DIR + ICON_SYSTEM_BLANK_ROOT + ICON_EXT;

	public static final String ICON_SYSTEM_SEARCH_ROOT = "system_search"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_SEARCH_ID = PREFIX + ICON_SYSTEM_SEARCH_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_SEARCH    = ICON_OBJS_DIR + ICON_SYSTEM_SEARCH_ROOT + ICON_EXT;

	public static final String ICON_SYSTEM_SEARCH_RESULT_ROOT = "systemsearchresult"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_SEARCH_RESULT_ID = PREFIX + ICON_SYSTEM_SEARCH_RESULT_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_SEARCH_RESULT    = ICON_OBJS_DIR + ICON_SYSTEM_SEARCH_RESULT_ROOT + ICON_EXT;

	public static final String ICON_SYSTEM_PERSPECTIVE_ROOT ="system_persp"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_PERSPECTIVE_ID   = PREFIX + ICON_SYSTEM_PERSPECTIVE_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_PERSPECTIVE      = ICON_OBJS_DIR + ICON_SYSTEM_PERSPECTIVE_ROOT + ICON_EXT;



	public static final String ICON_SYSTEM_ARROW_UP_ROOT = "arrowup_obj";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_ARROW_UP      = ICON_OBJS_DIR + ICON_SYSTEM_ARROW_UP_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ARROW_UP_ID   = PREFIX+ICON_SYSTEM_ARROW_UP_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_ARROW_DOWN_ROOT = "arrowdown_obj";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_ARROW_DOWN      = ICON_OBJS_DIR + ICON_SYSTEM_ARROW_DOWN_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ARROW_DOWN_ID   = PREFIX+ICON_SYSTEM_ARROW_DOWN_ROOT+ICON_SUFFIX;


	public static final String ICON_SYSTEM_CONNECTOR_SERVICE_ROOT = "connectorservice_obj"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_CONNECTOR_SERVICE      = ICON_OBJS_DIR + ICON_SYSTEM_CONNECTOR_SERVICE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CONNECTOR_SERVICE_ID  = PREFIX+ICON_SYSTEM_CONNECTOR_SERVICE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_SERVICE_ROOT = "service_obj"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_SERVICE      = ICON_OBJS_DIR + ICON_SYSTEM_SERVICE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_SERVICE_ID  = PREFIX+ICON_SYSTEM_SERVICE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_LAUNCHER_CONFIGURATION_ROOT = "launcher_config_obj"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_LAUNCHER_CONFIGURATION      = ICON_OBJS_DIR + ICON_SYSTEM_LAUNCHER_CONFIGURATION_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_LAUNCHER_CONFIGURATION_ID  = PREFIX+ICON_SYSTEM_LAUNCHER_CONFIGURATION_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_PROPERTIES_ROOT = "properties_obj"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_PROPERTIES      = ICON_OBJS_DIR + ICON_SYSTEM_PROPERTIES_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_PROPERTIES_ID  = PREFIX+ICON_SYSTEM_PROPERTIES_ROOT+ICON_SUFFIX;

	public static final String ICON_SEARCH_REMOVE_SELECTED_MATCHES_ROOT	= "searchremoveselected";	 //$NON-NLS-1$
	public static final String ICON_SEARCH_REMOVE_SELECTED_MATCHES		= ICON_ACTIONS_DIR + ICON_SEARCH_REMOVE_SELECTED_MATCHES_ROOT + ICON_EXT;
	public static final String ICON_SEARCH_REMOVE_SELECTED_MATCHES_ID	= PREFIX + ICON_SEARCH_REMOVE_SELECTED_MATCHES_ROOT + ICON_SUFFIX;

	public static final String ICON_SEARCH_REMOVE_ALL_MATCHES_ROOT	= "searchremoveall";	 //$NON-NLS-1$
	public static final String ICON_SEARCH_REMOVE_ALL_MATCHES		= ICON_ACTIONS_DIR + ICON_SEARCH_REMOVE_ALL_MATCHES_ROOT + ICON_EXT;
	public static final String ICON_SEARCH_REMOVE_ALL_MATCHES_ID	= PREFIX + ICON_SEARCH_REMOVE_ALL_MATCHES_ROOT + ICON_SUFFIX;

	// we reuse the Remove all matches action icon
	public static final String ICON_SEARCH_CLEAR_HISTORY_ROOT = ICON_SEARCH_REMOVE_ALL_MATCHES_ROOT;
	public static final String ICON_SEARCH_CLEAR_HISTORY      = ICON_ACTIONS_DIR + ICON_SEARCH_CLEAR_HISTORY_ROOT + ICON_EXT;
	public static final String ICON_SEARCH_CLEAR_HISTORY_ID   = PREFIX + ICON_SEARCH_CLEAR_HISTORY_ROOT + ICON_SUFFIX;
}