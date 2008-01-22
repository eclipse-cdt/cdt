/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - 176488: adding some text for the cache limit checkbox
 * David McKnight(IBM) - [210142] for accessibility need transfer mode toggle button
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui;

import org.eclipse.osgi.util.NLS;

public class FileResources extends NLS 
{
	private static String BUNDLE_NAME = "org.eclipse.rse.internal.files.ui.FileResources";  //$NON-NLS-1$

	public static String RESID_FILES_DOWNLOAD;
	
	public static String RESID_FILES_PREFERENCES_BUFFER;
	public static String RESID_FILES_PREFERENCES_DOWNLOAD_BUFFER_SIZE_LABEL;
	public static String RESID_FILES_PREFERENCES_UPLOAD_BUFFER_SIZE_LABEL;
	public static String RESID_FILES_PREFERENCES_DOWNLOAD_BUFFER_SIZE_TOOLTIP;
	public static String RESID_FILES_PREFERENCES_UPLOAD_BUFFER_SIZE_TOOLTIP;
	
	public static String ResourceNavigator_openWith;
	public static String RSEOperation_message;

	// --- File Editors ---
	public static String FileEditorPreference_fileTypes;
	public static String FileEditorPreference_add;
	public static String FileEditorPreference_remove;
	public static String FileEditorPreference_associatedEditors;
	public static String FileEditorPreference_addEditor;
	public static String FileEditorPreference_removeEditor;
	public static String FileEditorPreference_default;
	public static String FileEditorPreference_existsTitle;
	public static String FileEditorPreference_existsMessage;
	public static String FileEditorPreference_defaultLabel;
    public static String FileEditorPreference_contentTypesRelatedLink;
    public static String FileEditorPreference_isLocked;

	public static String FileExtension_fileTypeMessage;
	public static String FileExtension_fileTypeLabel;
	public static String FileExtension_shellTitle;
	public static String FileExtension_dialogTitle;
	
	public static String DefaultEditorDescription_name;


	
	// NEW FILE WIZARD...
	public static String RESID_NEWFILE_TITLE;
	public static String RESID_NEWFILE_PAGE1_TITLE;
	public static String RESID_NEWFILE_PAGE1_DESCRIPTION;
	public static String RESID_NEWFILE_NAME_LABEL; 
	public static String RESID_NEWFILE_NAME_TOOLTIP; 
	public static String RESID_NEWFILE_FOLDER_LABEL;
	public static String RESID_NEWFILE_FOLDER_TIP;
	public static String RESID_NEWFILE_CONNECTIONNAME_LABEL;
	public static String RESID_NEWFILE_CONNECTIONNAME_TIP;

	// NEW FOLDER WIZARD...
	public static String RESID_NEWFOLDER_TITLE;
	public static String RESID_NEWFOLDER_PAGE1_TITLE;
	public static String RESID_NEWFOLDER_PAGE1_DESCRIPTION;
	public static String RESID_NEWFOLDER_NAME_LABEL;
	public static String RESID_NEWFOLDER_NAME_TOOLTIP; 
	public static String RESID_NEWFOLDER_FOLDER_LABEL;
	public static String RESID_NEWFOLDER_FOLDER_TIP;
	public static String RESID_NEWFOLDER_CONNECTIONNAME_LABEL;
	public static String RESID_NEWFOLDER_CONNECTIONNAME_TIP;
	
	// ---------------------------------------------------
	// PREFERENCES FOR UNIVERSAL FILE SYSTEM ...
	// ---------------------------------------------------

	public static String RESID_PREF_UNIVERSAL_FILES_TITLE;
	public static String RESID_PREF_UNIVERSAL_SHOWHIDDEN_LABEL;
	
	public static String RESID_PREF_UNIVERSAL_PRESERVE_TIMESTAMPS_LABEL;
	public static String RESID_PREF_UNIVERSAL_PRESERVE_TIMESTAMPS_TOOLTIP;

	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_TYPE_LABEL;
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_TYPE_TOOLTIP;

	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_TABLECOL_LABEL;
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_TABLECOL_TOOLTIP;

	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_ADDBUTTON_LABEL;
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_ADDBUTTON_TOOLTIP;

	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_REMOVEBUTTON_LABEL;
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_REMOVEBUTTON_TOOLTIP;

	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_TOGGLEBUTTON_LABEL;
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_TOGGLEBUTTON_TOOLTIP;

	
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_LABEL;
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TOOLTIP;

	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_BINARY_LABEL;
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_BINARY_TOOLTIP;

	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TEXT_LABEL;
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TEXT_TOOLTIP;

	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_DEFAULT_MODE_LABEL;
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_DEFAULT_MODE_TOOLTIP;

	// Search constants
	// Search dialog constants
	// search string controls
	public static String RESID_SEARCH_STRING_LABEL_LABEL;
	public static String RESID_SEARCH_STRING_LABEL_TOOLTIP;

	public static String RESID_SEARCH_STRING_COMBO_TOOLTIP;

	public static String RESID_SEARCH_CASE_BUTTON_LABEL;
	public static String RESID_SEARCH_CASE_BUTTON_TOOLTIP;

	public static String RESID_SEARCH_STRING_HINT_LABEL;
	public static String RESID_SEARCH_STRING_HINT_TOOLTIP;

	public static String RESID_SEARCH_STRING_REGEX_LABEL;
	public static String RESID_SEARCH_STRING_REGEX_TOOLTIP;

	// file name controls
	public static String RESID_SEARCH_FILENAME_LABEL_LABEL;
	public static String RESID_SEARCH_FILENAME_LABEL_TOOLTIP;
	public static String RESID_SEARCH_FILENAME_COMBO_TOOLTIP;
	public static String RESID_SEARCH_FILENAME_BROWSE_LABEL;
	public static String RESID_SEARCH_FILENAME_BROWSE_TOOLTIP;

	public static String RESID_SEARCH_FILENAME_HINT_LABEL;
	public static String RESID_SEARCH_FILENAME_HINT_TOOLTIP;

	public static String RESID_SEARCH_FILENAME_REGEX_LABEL;
	public static String RESID_SEARCH_FILENAME_REGEX_TOOLTIP;

	// folder name controls
	public static String RESID_SEARCH_FOLDERNAME_LABEL_LABEL;
	public static String RESID_SEARCH_FOLDERNAME_LABEL_TOOLTIP;

	public static String RESID_SEARCH_FOLDERNAME_COMBO_TOOLTIP;

	public static String RESID_SEARCH_FOLDERNAME_BROWSE_LABEL;
	public static String RESID_SEARCH_FOLDERNAME_BROWSE_TOOLTIP;

	// advanced search controls
	public static String RESID_SEARCH_INCLUDE_ARCHIVES_LABEL;
	public static String RESID_SEARCH_INCLUDE_ARCHIVES_TOOLTIP;

	public static String RESID_SEARCH_INCLUDE_SUBFOLDERS_LABEL;
	public static String RESID_SEARCH_INCLUDE_SUBFOLDERS_TOOLTIP;

	// advanced search filters
	public static String RESID_SEARCH_CONNECTIONNAMELABEL_LABEL;
	public static String RESID_SEARCH_CONNECTIONNAMELABEL_TOOLTIP;
	
	public static String RESID_SEARCH_TARGETGROUP_LABEL;
	public static String RESID_SEARCH_TARGETGROUP_TOOLTIP;

	public static String RESID_SEARCH_COLUMNSGROUP_LABEL;
	public static String RESID_SEARCH_COLUMNSGROUP_TOOLTIP;

	public static String RESID_SEARCH_ALLCOLUMNSLABEL_LABEL;
	public static String RESID_SEARCH_ALLCOLUMNSLABEL_TOOLTIP;

	public static String RESID_SEARCH_BETWEENLABEL_LABEL;
	public static String RESID_SEARCH_ANDLABEL_LABEL;
	public static String RESID_SEARCH_EOLLABEL_LABEL;
	public static String RESID_SEARCH_FIRSTCOLUMN_TOOLTIP;
	public static String RESID_SEARCH_SECONDCOLUMN_TOOLTIP;
	public static String RESID_SEARCH_BOTHCOLUMNSLABEL_TOOLTIP;
	public static String RESID_SEARCH_STARTCOLUMNLABEL_TOOLTIP;

	public static String RESID_SEARCH_MESSAGE_SEARCHING;
	public static String RESID_SEARCH_MESSAGE_ONEMATCH;
	public static String RESID_SEARCH_MESSAGE_MULTIPLEMATCHES;

	// Resource conflict dlg constants
	public static String RESID_CONFLICT_SAVE_TITLE;
	public static String RESID_CONFLICT_SAVE_MESSAGE;
	public static String RESID_CONFLICT_SAVE_OVERWRITEREMOTE;
	public static String RESID_CONFLICT_SAVE_REPLACELOCAL;
	public static String RESID_CONFLICT_SAVE_SAVETODIFFERENT;

	public static String RESID_CONFLICT_DOWNLOAD_TITLE;
	public static String RESID_CONFLICT_DOWNLOAD_MESSAGE_LOCALCHANGED;
	public static String RESID_CONFLICT_DOWNLOAD_MESSAGE_REMOTECHANGED;
	public static String RESID_CONFLICT_DOWNLOAD_REPLACELOCAL;
	public static String RESID_CONFLICT_DOWNLOAD_OPENWITHLOCAL;

	// RSE Cache Preferences
	public static String RESID_PREF_CACHE_DESCRIPTION;
	public static String RESID_PREF_CACHE_CLEAR;
	public static String RESID_PREF_CACHE_CLEAR_LABEL;
	public static String RESID_PREF_CACHE_CLEAR_TOOLTIP;
	public static String RESID_PREF_CACHE_LIMIT_CACHE_SIZE_LABEL;
	public static String RESID_PREF_CACHE_LIMIT_CACHE_SIZE_TOOLTIP;
	public static String RESID_PREF_CACHE_MAX_CACHE_SIZE_LABEL;
	public static String RESID_PREF_CACHE_MAX_CACHE_SIZE_DESCRIPTION;
	public static String RESID_PREF_CACHE_MAX_CACHE_SIZE_TOOLTIP;
	public static String RESID_PREF_CACHE_CLEAR_WARNING_LABEL;
	public static String RESID_PREF_CACHE_CLEAR_WARNING_DESCRIPTION;

	// SUPERTRANSFER PROGRESS MONITOR CONSTANTS
	public static String RESID_SUPERTRANSFER_PROGMON_MAIN;
	public static String RESID_SUPERTRANSFER_PROGMON_SUBTASK_CREATE;
	public static String RESID_SUPERTRANSFER_PROGMON_SUBTASK_POPULATE;

	public static String RESID_SUPERTRANSFER_PROGMON_SUBTASK_TRANSFER;
	public static String RESID_SUPERTRANSFER_PROGMON_SUBTASK_EXTRACT;
	public static String RESID_SUPERTRANSFER_PROGMON_ARCHIVE;

	// SUPERTRANSFER PREFERENCES PAGE CONSTANTS
	public static String RESID_SUPERTRANSFER_PREFS_ENABLE;
	public static String RESID_SUPERTRANSFER_PREFS_TYPE_LABEL; 
	public static String RESID_SUPERTRANSFER_PREFS_TYPE_TOOLTIP;

	// Compare with menu item
	public static String ACTION_COMPAREWITH_EACH_LABEL;
	public static String ACTION_COMPAREWITH_EACH_TOOLTIP;

	public static String ACTION_COMPAREWITH_HISTORY_LABEL;
	public static String ACTION_COMPAREWITH_HISTORY_TOOLTIP;

	// Replace with menu item
	public static String ACTION_REPLACEWITH_HISTORY_LABEL;
	public static String ACTION_REPLACEWITH_HISTORY_TOOLTIP;


	// Project menu item 
	public static String RESID_OPEN_FROM_ASSOCIATED_PROJECT;
	
	// Other Actions
	public static String ACTION_NEWFOLDER_LABEL;
	public static String ACTION_NEWFOLDER_TOOLTIP;
	
	public static String ACTION_SELECT_DIRECTORY_LABEL;
	public static String ACTION_SELECT_DIRECTORY_TOOLTIP;

	public static String ACTION_SELECT_FILE_LABEL;
	public static String ACTION_SELECT_FILE_TOOLTIP;
	
	// Property Pages
	public static String MESSAGE_ENCODING_NOT_SUPPORTED;
	
	// synchronize cache action
	public static String MESSAGE_ERROR_CACHING_REMOTE_FILES;
	public static String MESSAGE_SYNCHRONIZING_REMOTE_FILE_CACHE;
	
	// link with editor
	public static String MESSAGE_EXPANDING_FOLDER;
	public static String MESSAGE_EXPANDING_FILTER;
	public static String MESSSAGE_QUERYING_FILE;

	// file permisssions property page
	public static String MESSAGE_FILE_PERMISSIONS_NOT_SUPPORTED;
	public static String RESID_PREF_PERMISSIONS_PERMISSIONS_LABEL;
	public static String RESID_PREF_PERMISSIONS_TYPE_LABEL;
	public static String RESID_PREF_PERMISSIONS_READ_LABEL;
	public static String RESID_PREF_PERMISSIONS_WRITE_LABEL;
	public static String RESID_PREF_PERMISSIONS_EXECUTE_LABEL;
	public static String RESID_PREF_PERMISSIONS_USER_LABEL;
	public static String RESID_PREF_PERMISSIONS_GROUP_LABEL;
	public static String RESID_PREF_PERMISSIONS_OTHERS_LABEL;
	public static String RESID_PREF_PERMISSIONS_OWNERSHIP_LABEL;
	
	// file permissions messages
	public static String MESSAGE_PENDING;
	public static String MESSAGE_NOT_SUPPORTED;
	public static String MESSAGE_GETTING_PERMISSIONS;
	
	static 
	{
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, FileResources.class);
	}
	
}