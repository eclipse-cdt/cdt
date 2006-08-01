/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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
 ********************************************************************************/

package org.eclipse.rse.ui;

/**
 * Constants used throughout the System plugin.
 */
public interface ISystemIconConstants 
{
	public static final String PLUGIN_ID ="org.eclipse.rse.ui";
	public static final String PREFIX = PLUGIN_ID + ".";
	public static final String SEP = "/";
	
	// Icons
	public static final String ICON_DIR = "icons";
	public static final String ICON_PATH = SEP + ICON_DIR + SEP;
	public static final String ICON_SUFFIX = "Icon";	
	public static final String ICON_BANNER_SUFFIX = "BannerIcon";	
	public static final String ICON_EXT = ".gif";	
	
    // WIZARD ICONS...    		
    public static final String ICON_WIZARD_DIR = SEP + "full" + SEP + "wizban" + SEP + "";
	public static final String ICON_SYSTEM_NEWPROFILEWIZARD_ROOT = "newprofile_wiz";	
	public static final String ICON_SYSTEM_NEWPROFILEWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWPROFILEWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWPROFILEWIZARD_ID = PREFIX + ICON_SYSTEM_NEWPROFILEWIZARD_ROOT + ICON_BANNER_SUFFIX;	

	public static final String ICON_SYSTEM_NEWCONNECTIONWIZARD_ROOT = "newconnection_wiz";	
	public static final String ICON_SYSTEM_NEWCONNECTIONWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWCONNECTIONWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWCONNECTIONWIZARD_ID = PREFIX + ICON_SYSTEM_NEWCONNECTIONWIZARD_ROOT + ICON_BANNER_SUFFIX;			
	
	public static final String ICON_SYSTEM_NEWFILTERWIZARD_ROOT = "newfilter_wiz";	
	public static final String ICON_SYSTEM_NEWFILTERWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWFILTERWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILTERWIZARD_ID = PREFIX + ICON_SYSTEM_NEWFILTERWIZARD_ROOT + ICON_BANNER_SUFFIX;	

	public static final String ICON_SYSTEM_NEWFILTERPOOLWIZARD_ROOT = "newfilterpool_wiz";	
	public static final String ICON_SYSTEM_NEWFILTERPOOLWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWFILTERPOOLWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILTERPOOLWIZARD_ID = PREFIX + ICON_SYSTEM_NEWFILTERPOOLWIZARD_ROOT + ICON_BANNER_SUFFIX;	

    public static final String ICON_SYSTEM_NEWFILEWIZARD_ROOT = "newfile_wiz";
    public static final String ICON_SYSTEM_NEWFILEWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWFILEWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILEWIZARD_ID = PREFIX + ICON_SYSTEM_NEWFILEWIZARD_ROOT + ICON_BANNER_SUFFIX;	
	
	public static final String ICON_SYSTEM_NEWFOLDERWIZARD_ROOT = "newfolder_wiz";
    public static final String ICON_SYSTEM_NEWFOLDERWIZARD    = ICON_WIZARD_DIR + ICON_SYSTEM_NEWFOLDERWIZARD_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_NEWFOLDERWIZARD_ID = PREFIX + ICON_SYSTEM_NEWFOLDERWIZARD_ROOT + ICON_BANNER_SUFFIX;	
			

    // THING ICONS...
    public static final String ICON_MODEL_DIR = SEP + "full" + SEP + "obj16" + SEP + "";	

	public static final String ICON_SYSTEM_PROFILE_ROOT = "systemprofile";	
	public static final String ICON_SYSTEM_PROFILE      = ICON_MODEL_DIR + ICON_SYSTEM_PROFILE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_PROFILE_ID   = PREFIX+ICON_SYSTEM_PROFILE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_PROFILE_ACTIVE_ROOT = "systemprofile_active";	
	public static final String ICON_SYSTEM_PROFILE_ACTIVE      = ICON_MODEL_DIR + ICON_SYSTEM_PROFILE_ACTIVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_PROFILE_ACTIVE_ID   = PREFIX+ICON_SYSTEM_PROFILE_ACTIVE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_CONNECTION_ROOT = "systemconnection";	
	public static final String ICON_SYSTEM_CONNECTION      = ICON_MODEL_DIR + ICON_SYSTEM_CONNECTION_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CONNECTION_ID   = PREFIX+ICON_SYSTEM_CONNECTION_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_CONNECTIONLIVE_ROOT = "systemconnectionlive"; // not currently used	
	public static final String ICON_SYSTEM_CONNECTIONLIVE      = ICON_MODEL_DIR + ICON_SYSTEM_CONNECTIONLIVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CONNECTIONLIVE_ID   = PREFIX+ICON_SYSTEM_CONNECTIONLIVE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_FILTERPOOL_ROOT = "systemfilterpool";	
	public static final String ICON_SYSTEM_FILTERPOOL      = ICON_MODEL_DIR + ICON_SYSTEM_FILTERPOOL_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_FILTERPOOL_ID   = PREFIX+ICON_SYSTEM_FILTERPOOL_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_FILTER_ROOT = "systemfilter";
	public static final String ICON_SYSTEM_FILTER_ID = PREFIX + ICON_SYSTEM_FILTER_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_FILTER    = ICON_MODEL_DIR + ICON_SYSTEM_FILTER_ROOT + ICON_EXT;	
			
	public static final String ICON_SYSTEM_FILTERSTRING_ROOT = "systemfilterstring";
	public static final String ICON_SYSTEM_FILTERSTRING_ID = PREFIX + ICON_SYSTEM_FILTERSTRING_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_FILTERSTRING    = ICON_MODEL_DIR + ICON_SYSTEM_FILTERSTRING_ROOT + ICON_EXT;	

	public static final String ICON_SYSTEM_ROOTDRIVE_ROOT = "systemrootdrive";	
	public static final String ICON_SYSTEM_ROOTDRIVE      = ICON_MODEL_DIR + ICON_SYSTEM_ROOTDRIVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ROOTDRIVE_ID   = PREFIX+ICON_SYSTEM_ROOTDRIVE_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_ROOTDRIVEOPEN_ROOT = "systemrootdriveopen";	
	public static final String ICON_SYSTEM_ROOTDRIVEOPEN      = ICON_MODEL_DIR + ICON_SYSTEM_ROOTDRIVEOPEN_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ROOTDRIVEOPEN_ID   = PREFIX+ICON_SYSTEM_ROOTDRIVEOPEN_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_FOLDER_ROOT = "systemfolder";	
	public static final String ICON_SYSTEM_FOLDER      = ICON_MODEL_DIR + ICON_SYSTEM_FOLDER_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_FOLDER_ID   = PREFIX+ICON_SYSTEM_FOLDER_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_ENVVAR_ROOT = "systemenvvar";	
	public static final String ICON_SYSTEM_ENVVAR      = ICON_MODEL_DIR + ICON_SYSTEM_ENVVAR_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ENVVAR_ID   = PREFIX+ICON_SYSTEM_ENVVAR+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_ENVVAR_LIBPATH_ROOT = "systemenvvarlibpath";	
	public static final String ICON_SYSTEM_ENVVAR_LIBPATH      = ICON_MODEL_DIR + ICON_SYSTEM_ENVVAR_LIBPATH_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ENVVAR_LIBPATH_ID   = PREFIX+ICON_SYSTEM_ENVVAR_LIBPATH+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_ENVVAR_PATH_ROOT = "systemenvvarpath";	
	public static final String ICON_SYSTEM_ENVVAR_PATH      = ICON_MODEL_DIR + ICON_SYSTEM_ENVVAR_PATH_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ENVVAR_PATH_ID   = PREFIX+ICON_SYSTEM_ENVVAR_PATH+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_PROCESS_ROOT = "systemprocess";	
	public static final String ICON_SYSTEM_PROCESS      = ICON_MODEL_DIR + ICON_SYSTEM_PROCESS_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_PROCESS_ID   = PREFIX+ICON_SYSTEM_PROCESS+ICON_SUFFIX;

	public static final String ICON_SYSTEM_TARGET_ROOT = "systemTarget";	
	public static final String ICON_SYSTEM_TARGET      = ICON_MODEL_DIR + ICON_SYSTEM_TARGET_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_TARGET_ID   = PREFIX+ICON_SYSTEM_TARGET_ROOT+ICON_SUFFIX;

    // NEW ACTION ICONS...
    public static final String ICON_NEWACTIONS_DIR = SEP + "full" + SEP + "ctool16" + SEP + "";	

	public static final String ICON_SYSTEM_NEW_ROOT = "new";	
	public static final String ICON_SYSTEM_NEW      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEW_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEW_ID   = PREFIX+ICON_SYSTEM_NEW_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWPROFILE_ROOT = "newprofile_wiz";	
	public static final String ICON_SYSTEM_NEWPROFILE      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWPROFILE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWPROFILE_ID   = PREFIX+ICON_SYSTEM_NEWPROFILE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWCONNECTION_ROOT = "newconnection_wiz";	
	public static final String ICON_SYSTEM_NEWCONNECTION      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWCONNECTION_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWCONNECTION_ID   = PREFIX+ICON_SYSTEM_NEWCONNECTION_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWFILTERPOOL_ROOT = "newfilterpool_wiz";	
	public static final String ICON_SYSTEM_NEWFILTERPOOL      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWFILTERPOOL_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILTERPOOL_ID   = PREFIX+ICON_SYSTEM_NEWFILTERPOOL_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWFILTERPOOLREF_ROOT = "newfilterpoolref_wiz";	
	public static final String ICON_SYSTEM_NEWFILTERPOOLREF      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWFILTERPOOLREF_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILTERPOOLREF_ID   = PREFIX+ICON_SYSTEM_NEWFILTERPOOLREF_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWFILTER_ROOT = "newfilter_wiz";	
	public static final String ICON_SYSTEM_NEWFILTER      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWFILTER_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILTER_ID   = PREFIX+ICON_SYSTEM_NEWFILTER_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_NEWFILE_ROOT = "newfile_wiz";	
	public static final String ICON_SYSTEM_NEWFILE      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWFILE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWFILE_ID   = PREFIX+ICON_SYSTEM_NEWFILE_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_NEWFOLDER_ROOT = "newfolder_wiz";	
	public static final String ICON_SYSTEM_NEWFOLDER      = ICON_NEWACTIONS_DIR + ICON_SYSTEM_NEWFOLDER_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_NEWFOLDER_ID   = PREFIX+ICON_SYSTEM_NEWFOLDER_ROOT+ICON_SUFFIX;
	
				
    // OTHER ACTION ICONS...    			
    public static final String ICON_ACTIONS_DIR = SEP + "full" + SEP + "elcl16" + SEP + "";	

	public static final String ICON_SYSTEM_COMPILE_ROOT = "compile";	
	public static final String ICON_SYSTEM_COMPILE      = ICON_ACTIONS_DIR + ICON_SYSTEM_COMPILE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_COMPILE_ID   = PREFIX+ICON_SYSTEM_COMPILE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_LOCK_ROOT = "lock";	
	public static final String ICON_SYSTEM_LOCK      = ICON_ACTIONS_DIR + ICON_SYSTEM_LOCK_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_LOCK_ID   = PREFIX+ICON_SYSTEM_LOCK_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_MOVEUP_ROOT = "up";	
	public static final String ICON_SYSTEM_MOVEUP      = ICON_ACTIONS_DIR + ICON_SYSTEM_MOVEUP_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_MOVEUP_ID   = PREFIX+ICON_SYSTEM_MOVEUP_ROOT+ICON_SUFFIX;
		
	public static final String ICON_SYSTEM_MOVEDOWN_ROOT = "down";	
	public static final String ICON_SYSTEM_MOVEDOWN      = ICON_ACTIONS_DIR + ICON_SYSTEM_MOVEDOWN_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_MOVEDOWN_ID   = PREFIX+ICON_SYSTEM_MOVEDOWN_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_MOVE_ROOT = "move";	
	public static final String ICON_SYSTEM_MOVE      = ICON_ACTIONS_DIR + ICON_SYSTEM_MOVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_MOVE_ID   = PREFIX+ICON_SYSTEM_MOVE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_CLEAR_ROOT = "clear";	
	public static final String ICON_SYSTEM_CLEAR      = ICON_ACTIONS_DIR + ICON_SYSTEM_CLEAR_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CLEAR_ID   = PREFIX+ICON_SYSTEM_CLEAR_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_CLEAR_ALL_ROOT = "clearall";	
	public static final String ICON_SYSTEM_CLEAR_ALL      = ICON_ACTIONS_DIR + ICON_SYSTEM_CLEAR_ALL_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CLEAR_ALL_ID   = PREFIX+ICON_SYSTEM_CLEAR_ALL_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_CLEAR_SELECTED_ROOT = "clearselected";	
	public static final String ICON_SYSTEM_CLEAR_SELECTED      = ICON_ACTIONS_DIR + ICON_SYSTEM_CLEAR_SELECTED_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CLEAR_SELECTED_ID   = PREFIX+ICON_SYSTEM_CLEAR_SELECTED_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_DELETEREF_ROOT = "deletereference";	
	public static final String ICON_SYSTEM_DELETEREF      = ICON_ACTIONS_DIR + ICON_SYSTEM_DELETEREF_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_DELETEREF_ID   = PREFIX+ICON_SYSTEM_DELETEREF_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_RUN_ROOT    = "run";	
	public static final String ICON_SYSTEM_RUN         = ICON_ACTIONS_DIR + ICON_SYSTEM_RUN_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_RUN_ID      = PREFIX+ICON_SYSTEM_RUN_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_STOP_ROOT    = "stop";	
	public static final String ICON_SYSTEM_STOP         = ICON_ACTIONS_DIR + ICON_SYSTEM_STOP_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_STOP_ID      = PREFIX+ICON_SYSTEM_STOP_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_RENAME_ROOT = "rename";	
	public static final String ICON_SYSTEM_RENAME      = ICON_ACTIONS_DIR + ICON_SYSTEM_RENAME_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_RENAME_ID   = PREFIX+ICON_SYSTEM_RENAME_ROOT+ICON_SUFFIX;

	public static final String ICON_IDE_REFRESH_ID = "elcl16/refresh_nav.gif";
	public static final String ICON_IDE_COLLAPSEALL_ID = "elcl16/collapseall.gif";
	public static final String ICON_IDE_LINKTOEDITOR_ID = "elcl16/synced.gif";
	public static final String ICON_IDE_FILTER_ID = "elcl16/filter_ps.gif";

	public static final String ICON_SYSTEM_MAKEPROFILEACTIVE_ROOT = "makeprofileactive";	
	public static final String ICON_SYSTEM_MAKEPROFILEACTIVE      = ICON_ACTIONS_DIR + ICON_SYSTEM_MAKEPROFILEACTIVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_MAKEPROFILEACTIVE_ID   = PREFIX+ICON_SYSTEM_MAKEPROFILEACTIVE_ROOT+ICON_SUFFIX; 	

	public static final String ICON_SYSTEM_MAKEPROFILEINACTIVE_ROOT = "makeprofileinactive";	
	public static final String ICON_SYSTEM_MAKEPROFILEINACTIVE      = ICON_ACTIONS_DIR + ICON_SYSTEM_MAKEPROFILEINACTIVE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_MAKEPROFILEINACTIVE_ID   = PREFIX+ICON_SYSTEM_MAKEPROFILEINACTIVE_ROOT+ICON_SUFFIX; 	

	public static final String ICON_SYSTEM_CHANGEFILTER_ROOT = "editfilter";	
	public static final String ICON_SYSTEM_CHANGEFILTER      = ICON_ACTIONS_DIR + ICON_SYSTEM_CHANGEFILTER_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CHANGEFILTER_ID   = PREFIX+ICON_SYSTEM_CHANGEFILTER_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_SELECTPROFILE_ROOT = "selectprofile";	
	public static final String ICON_SYSTEM_SELECTPROFILE      = ICON_ACTIONS_DIR + ICON_SYSTEM_SELECTPROFILE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_SELECTPROFILE_ID   = PREFIX+ICON_SYSTEM_SELECTPROFILE_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_SELECTFILTERPOOLS_ROOT = "selectpool";	
	public static final String ICON_SYSTEM_SELECTFILTERPOOLS      = ICON_ACTIONS_DIR + ICON_SYSTEM_SELECTFILTERPOOLS_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_SELECTFILTERPOOLS_ID   = PREFIX+ICON_SYSTEM_SELECTFILTERPOOLS_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_WORKWITHFILTERPOOLS_ROOT = "workwithfilterpools";	
	public static final String ICON_SYSTEM_WORKWITHFILTERPOOLS      = ICON_ACTIONS_DIR + ICON_SYSTEM_WORKWITHFILTERPOOLS_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_WORKWITHFILTERPOOLS_ID   = PREFIX+ICON_SYSTEM_WORKWITHFILTERPOOLS_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_WORKWITHUSERACTIONS_ROOT = "workwithuseractions";	
	public static final String ICON_SYSTEM_WORKWITHUSERACTIONS      = ICON_ACTIONS_DIR + ICON_SYSTEM_WORKWITHUSERACTIONS_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_WORKWITHUSERACTIONS_ID   = PREFIX+ICON_SYSTEM_WORKWITHUSERACTIONS_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_WORKWITHNAMEDTYPES_ROOT = "workwithnamedtypes";	
	public static final String ICON_SYSTEM_WORKWITHNAMEDTYPES      = ICON_ACTIONS_DIR + ICON_SYSTEM_WORKWITHNAMEDTYPES_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_WORKWITHNAMEDTYPES_ID   = PREFIX+ICON_SYSTEM_WORKWITHNAMEDTYPES_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_WORKWITHCOMPILECMDS_ROOT = "workwithcompilecmds";	
	public static final String ICON_SYSTEM_WORKWITHCOMPILECMDS      = ICON_ACTIONS_DIR + ICON_SYSTEM_WORKWITHCOMPILECMDS_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_WORKWITHCOMPILECMDS_ID   = PREFIX+ICON_SYSTEM_WORKWITHCOMPILECMDS_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_REMOVE_SHELL_ROOT = "removeshell"; 
	public static final String ICON_SYSTEM_REMOVE_SHELL_ID = PREFIX + ICON_SYSTEM_REMOVE_SHELL_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_REMOVE_SHELL    = ICON_ACTIONS_DIR + ICON_SYSTEM_REMOVE_SHELL_ROOT + ICON_EXT;		

	public static final String ICON_SYSTEM_CANCEL_SHELL_ROOT = "cancelshell"; 
	public static final String ICON_SYSTEM_CANCEL_SHELL_ID = PREFIX + ICON_SYSTEM_CANCEL_SHELL_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_CANCEL_SHELL    = ICON_ACTIONS_DIR + ICON_SYSTEM_CANCEL_SHELL_ROOT + ICON_EXT;		

	public static final String ICON_SYSTEM_EXTRACT_ROOT = "xtrctarchv_tsk";	
	public static final String ICON_SYSTEM_EXTRACT      = ICON_ACTIONS_DIR + ICON_SYSTEM_EXTRACT_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_EXTRACT_ID   = PREFIX+ICON_SYSTEM_EXTRACT_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_EXTRACTTO_ROOT = "xtrctarchvto_tsk";	
	public static final String ICON_SYSTEM_EXTRACTTO      = ICON_ACTIONS_DIR + ICON_SYSTEM_EXTRACTTO_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_EXTRACTTO_ID   = PREFIX+ICON_SYSTEM_EXTRACTTO_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_CONVERT_ROOT = "convertarchive_tsk";	
	public static final String ICON_SYSTEM_CONVERT      = ICON_ACTIONS_DIR + ICON_SYSTEM_CONVERT_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CONVERT_ID   = PREFIX+ICON_SYSTEM_CONVERT_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_COMBINE_ROOT = "combine_tsk";	
	public static final String ICON_SYSTEM_COMBINE      = ICON_ACTIONS_DIR + ICON_SYSTEM_COMBINE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_COMBINE_ID   = PREFIX+ICON_SYSTEM_COMBINE_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_SHOW_TABLE_ROOT 	= "systemshowintable";
	public static final String ICON_SYSTEM_SHOW_TABLE      	= ICON_ACTIONS_DIR + ICON_SYSTEM_SHOW_TABLE_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_SHOW_TABLE_ID  	= PREFIX + ICON_SYSTEM_SHOW_TABLE_ROOT + ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_SHOW_MONITOR_ROOT = "monitor_view";
	public static final String ICON_SYSTEM_SHOW_MONITOR    = ICON_ACTIONS_DIR + ICON_SYSTEM_SHOW_MONITOR_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_SHOW_MONITOR_ID  = PREFIX + ICON_SYSTEM_SHOW_MONITOR_ROOT + ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_SHOW_SHELL_ROOT 	= "systemshell";
	public static final String ICON_SYSTEM_SHOW_SHELL      	= ICON_ACTIONS_DIR + ICON_SYSTEM_SHOW_SHELL_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_SHOW_SHELL_ID  	= PREFIX + ICON_SYSTEM_SHOW_SHELL_ROOT + ICON_SUFFIX;

	public static final String ICON_SYSTEM_EXPORT_SHELL_OUTPUT_ROOT 	= "exportshelloutput";
	public static final String ICON_SYSTEM_EXPORT_SHELL_OUTPUT      	= ICON_ACTIONS_DIR + ICON_SYSTEM_EXPORT_SHELL_OUTPUT_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_EXPORT_SHELL_OUTPUT_ID  	= PREFIX + ICON_SYSTEM_EXPORT_SHELL_OUTPUT_ROOT + ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_EXPORT_SHELL_HISTORY_ROOT 	= "exportshellhistory";
	public static final String ICON_SYSTEM_EXPORT_SHELL_HISTORY      	= ICON_ACTIONS_DIR + ICON_SYSTEM_EXPORT_SHELL_HISTORY_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_EXPORT_SHELL_HISTORY_ID  	= PREFIX + ICON_SYSTEM_EXPORT_SHELL_HISTORY_ROOT + ICON_SUFFIX;

	public static final String ICON_SYSTEM_REFRESH_ROOT = "refresh_nav";
	public static final String ICON_SYSTEM_REFRESH = ICON_ACTIONS_DIR + ICON_SYSTEM_REFRESH_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_REFRESH_ID = PREFIX + ICON_SYSTEM_REFRESH_ROOT + ICON_SUFFIX;

    // SPECIAL MODEL OBJECT ICONS...
    public static final String ICON_OBJS_DIR = SEP + "full" + SEP + "obj16" + SEP;	
	public static final String ICON_SYSTEM_ERROR_ROOT = "error";
	public static final String ICON_SYSTEM_ERROR_ID = PREFIX + ICON_SYSTEM_ERROR_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_ERROR    = ICON_OBJS_DIR + ICON_SYSTEM_ERROR_ROOT + ICON_EXT;		

	// info is to be used in dialogs
	public static final String ICON_SYSTEM_INFO_ROOT = "info";
	public static final String ICON_SYSTEM_INFO_ID = PREFIX + ICON_SYSTEM_INFO_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_INFO    = ICON_OBJS_DIR + ICON_SYSTEM_INFO_ROOT + ICON_EXT;		
	
	// systeminfo is to be used in tree view
	public static final String ICON_SYSTEM_INFO_TREE_ROOT = "systeminfo";
	public static final String ICON_SYSTEM_INFO_TREE_ID   = PREFIX + ICON_SYSTEM_INFO_TREE_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_INFO_TREE      = ICON_OBJS_DIR + ICON_SYSTEM_INFO_TREE_ROOT + ICON_EXT;		

	public static final String ICON_SYSTEM_HELP_ROOT = "systemhelp";
	public static final String ICON_SYSTEM_HELP_ID   = PREFIX + ICON_SYSTEM_HELP_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_HELP      = ICON_OBJS_DIR + ICON_SYSTEM_HELP_ROOT + ICON_EXT;		
		
	public static final String ICON_SYSTEM_CANCEL_ROOT = "systemcancel";
	public static final String ICON_SYSTEM_CANCEL_ID = PREFIX + ICON_SYSTEM_CANCEL_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_CANCEL    = ICON_OBJS_DIR + ICON_SYSTEM_CANCEL_ROOT + ICON_EXT;				

	public static final String ICON_SYSTEM_EMPTY_ROOT = "systemempty";
	public static final String ICON_SYSTEM_EMPTY_ID = PREFIX + ICON_SYSTEM_EMPTY_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_EMPTY    = ICON_OBJS_DIR + ICON_SYSTEM_EMPTY_ROOT + ICON_EXT;				

	public static final String ICON_SYSTEM_OK_ROOT = "systemok";
	public static final String ICON_SYSTEM_OK_ID = PREFIX + ICON_SYSTEM_OK_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_OK    = ICON_OBJS_DIR + ICON_SYSTEM_OK_ROOT + ICON_EXT;		

	public static final String ICON_SYSTEM_WARNING_ROOT = "warning";
	public static final String ICON_SYSTEM_WARNING_ID = PREFIX + ICON_SYSTEM_WARNING_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_WARNING    = ICON_OBJS_DIR + ICON_SYSTEM_WARNING_ROOT + ICON_EXT;		

	public static final String ICON_SYSTEM_FAILED_ROOT = "systemfailed"; // not used yet
	public static final String ICON_SYSTEM_FAILED_ID = PREFIX + ICON_SYSTEM_FAILED_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_FAILED    = ICON_OBJS_DIR + ICON_SYSTEM_FAILED_ROOT + ICON_EXT;	
	
	public static final String ICON_SYSTEM_BLANK_ROOT = "systemblank"; // not used yet
	public static final String ICON_SYSTEM_BLANK_ID = PREFIX + ICON_SYSTEM_BLANK_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_BLANK    = ICON_OBJS_DIR + ICON_SYSTEM_BLANK_ROOT + ICON_EXT;
	
	public static final String ICON_SYSTEM_SEARCH_ROOT = "system_search";
	public static final String ICON_SYSTEM_SEARCH_ID = PREFIX + ICON_SYSTEM_SEARCH_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_SEARCH    = ICON_OBJS_DIR + ICON_SYSTEM_SEARCH_ROOT + ICON_EXT;	
		
	public static final String ICON_SYSTEM_SEARCH_RESULT_ROOT = "systemsearchresult";
	public static final String ICON_SYSTEM_SEARCH_RESULT_ID = PREFIX + ICON_SYSTEM_SEARCH_RESULT_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_SEARCH_RESULT    = ICON_OBJS_DIR + ICON_SYSTEM_SEARCH_RESULT_ROOT + ICON_EXT;	
	
	public static final String ICON_SYSTEM_SHELL_ROOT = "systemshell"; // not used yet
	public static final String ICON_SYSTEM_SHELL_ID = PREFIX + ICON_SYSTEM_SHELL_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_SHELL    = ICON_OBJS_DIR + ICON_SYSTEM_SHELL_ROOT + ICON_EXT;		

	public static final String ICON_SYSTEM_SHELLLIVE_ROOT = "systemshelllive"; // not used yet
	public static final String ICON_SYSTEM_SHELLLIVE_ID = PREFIX + ICON_SYSTEM_SHELLLIVE_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_SHELLLIVE    = ICON_OBJS_DIR + ICON_SYSTEM_SHELLLIVE_ROOT + ICON_EXT;		

	public static final String ICON_SYSTEM_PERSPECTIVE_ROOT ="system_persp";
	public static final String ICON_SYSTEM_PERSPECTIVE_ID   = PREFIX + ICON_SYSTEM_PERSPECTIVE_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_PERSPECTIVE      = ICON_OBJS_DIR + ICON_SYSTEM_PERSPECTIVE_ROOT + ICON_EXT;



	public static final String ICON_SYSTEM_ARROW_UP_ROOT = "arrowup_obj";	
	public static final String ICON_SYSTEM_ARROW_UP      = ICON_OBJS_DIR + ICON_SYSTEM_ARROW_UP_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ARROW_UP_ID   = PREFIX+ICON_SYSTEM_ARROW_UP_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_ARROW_DOWN_ROOT = "arrowdown_obj";	
	public static final String ICON_SYSTEM_ARROW_DOWN      = ICON_OBJS_DIR + ICON_SYSTEM_ARROW_DOWN_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ARROW_DOWN_ID   = PREFIX+ICON_SYSTEM_ARROW_DOWN_ROOT+ICON_SUFFIX;
	
	
	public static final String ICON_SYSTEM_CONNECTOR_SERVICE_ROOT = "connectorservice_obj";
	public static final String ICON_SYSTEM_CONNECTOR_SERVICE      = ICON_OBJS_DIR + ICON_SYSTEM_CONNECTOR_SERVICE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_CONNECTOR_SERVICE_ID  = PREFIX+ICON_SYSTEM_CONNECTOR_SERVICE_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_SERVICE_ROOT = "service_obj";
	public static final String ICON_SYSTEM_SERVICE      = ICON_OBJS_DIR + ICON_SYSTEM_SERVICE_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_SERVICE_ID  = PREFIX+ICON_SYSTEM_SERVICE_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_LAUNCHER_CONFIGURATION_ROOT = "launcher_config_obj";
	public static final String ICON_SYSTEM_LAUNCHER_CONFIGURATION      = ICON_OBJS_DIR + ICON_SYSTEM_LAUNCHER_CONFIGURATION_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_LAUNCHER_CONFIGURATION_ID  = PREFIX+ICON_SYSTEM_LAUNCHER_CONFIGURATION_ROOT+ICON_SUFFIX;

	public static final String ICON_SYSTEM_PROPERTIES_ROOT = "properties_obj";
	public static final String ICON_SYSTEM_PROPERTIES      = ICON_OBJS_DIR + ICON_SYSTEM_PROPERTIES_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_PROPERTIES_ID  = PREFIX+ICON_SYSTEM_PROPERTIES_ROOT+ICON_SUFFIX;
	
	public static final String ICON_SEARCH_REMOVE_SELECTED_MATCHES_ROOT	= "searchremoveselected";	
	public static final String ICON_SEARCH_REMOVE_SELECTED_MATCHES		= ICON_ACTIONS_DIR + ICON_SEARCH_REMOVE_SELECTED_MATCHES_ROOT + ICON_EXT;
	public static final String ICON_SEARCH_REMOVE_SELECTED_MATCHES_ID	= PREFIX + ICON_SEARCH_REMOVE_SELECTED_MATCHES_ROOT + ICON_SUFFIX;
	
	public static final String ICON_SEARCH_REMOVE_ALL_MATCHES_ROOT	= "searchremoveall";	
	public static final String ICON_SEARCH_REMOVE_ALL_MATCHES		= ICON_ACTIONS_DIR + ICON_SEARCH_REMOVE_ALL_MATCHES_ROOT + ICON_EXT;
	public static final String ICON_SEARCH_REMOVE_ALL_MATCHES_ID	= PREFIX + ICON_SEARCH_REMOVE_ALL_MATCHES_ROOT + ICON_SUFFIX;
	
	// we reuse the Remove all matches action icon
	public static final String ICON_SEARCH_CLEAR_HISTORY_ROOT = ICON_SEARCH_REMOVE_ALL_MATCHES_ROOT;	
	public static final String ICON_SEARCH_CLEAR_HISTORY      = ICON_ACTIONS_DIR + ICON_SEARCH_CLEAR_HISTORY_ROOT + ICON_EXT;
	public static final String ICON_SEARCH_CLEAR_HISTORY_ID   = PREFIX + ICON_SEARCH_CLEAR_HISTORY_ROOT + ICON_SUFFIX;
}