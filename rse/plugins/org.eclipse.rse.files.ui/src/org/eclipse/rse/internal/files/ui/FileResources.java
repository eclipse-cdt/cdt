/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
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
 * David Dykstal (IBM) - 176488: adding some text for the cache limit checkbox
 * David McKnight(IBM) - [210142] for accessibility need transfer mode toggle button
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * David McKnight   (IBM)        - [216252] [nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David McKnight   (IBM)        - [223103] [cleanup] fix broken externalized strings
 * David McKnight   (IBM)        - [223204] [cleanup] fix broken nls strings in files.ui and others
 * David McKnight   (IBM)        - [224377] "open with" menu does not have "other" option
 * Rupen Mardirossian (IBM)		 - [227213] Added RESID_CONFLICT_COPY_PATTERN to be used for copying resources to parent folder.
 * David Dykstal (IBM) [231841] Correcting messages for folder creation
 * David McKnight   (IBM)        - [245260] Different user's connections on a single host are mapped to the same temp files cache
 * David McKnight (IBM)  - [283033] remoteFileTypes extension point should include "xml" type
 *******************************************************************************/

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

	public static String FileEditorPreference_existsTitle;
	public static String FileEditorPreference_existsMessage;

	
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

	public static String RESID_PREF_UNIVERSAL_SHOWHIDDEN_LABEL;
	
	public static String RESID_PREF_UNIVERSAL_PRESERVE_TIMESTAMPS_LABEL;
	
	public static String RESID_PREF_UNIVERSAL_SHARE_CACHED_FILES_LABEL;

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
	
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_XML_LABEL;
	public static String RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_XML_TOOLTIP;

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



	public static String RESID_SEARCH_MESSAGE_SEARCHING;

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
	
	// Resource conflict constants
	public static String RESID_CONFLICT_COPY_PATTERN;

	// RSE Cache Preferences
	public static String RESID_PREF_CACHE_CLEAR;
	public static String RESID_PREF_CACHE_CLEAR_TOOLTIP;
	public static String RESID_PREF_CACHE_LIMIT_CACHE_SIZE_LABEL;
	public static String RESID_PREF_CACHE_LIMIT_CACHE_SIZE_TOOLTIP;
	public static String RESID_PREF_CACHE_MAX_CACHE_SIZE_LABEL;
	public static String RESID_PREF_CACHE_MAX_CACHE_SIZE_TOOLTIP;
	public static String RESID_PREF_CACHE_CLEAR_WARNING_DESCRIPTION;

	// SUPERTRANSFER PROGRESS MONITOR CONSTANTS
	public static String RESID_SUPERTRANSFER_PROGMON_MAIN;
	public static String RESID_SUPERTRANSFER_PROGMON_SUBTASK_CREATE;
	public static String RESID_SUPERTRANSFER_PROGMON_SUBTASK_POPULATE;

	public static String RESID_SUPERTRANSFER_PROGMON_SUBTASK_TRANSFER;
	public static String RESID_SUPERTRANSFER_PROGMON_SUBTASK_EXTRACT;

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
	
	public static String	RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_LABEL;

	public static String	RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_VALUE;
	public static String	RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_DESCRIPTION;

	public static String	RESID_PROPERTY_ARCHIVE_COMMENT_LABEL;
	public static String	RESID_PROPERTY_ARCHIVE_COMMENT_DESCRIPTION;

	
	public static String	RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_LABEL;
	public static String	RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_VALUE;
	public static String	RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_DESCRIPTION;

	public static String	RESID_PROPERTY_VIRTUALFILE_COMMENT_LABEL;
	public static String	RESID_PROPERTY_VIRTUALFILE_COMMENT_DESCRIPTION;
	
	public static String	RESID_PROPERTY_VIRTUALFILE_COMPRESSIONRATIO_LABEL;
	public static String	RESID_PROPERTY_VIRTUALFILE_COMPRESSIONRATIO_DESCRIPTION;

	public static String	RESID_PROPERTY_VIRTUALFILE_COMPRESSIONMETHOD_LABEL;
	public static String	RESID_PROPERTY_VIRTUALFILE_COMPRESSIONMETHOD_DESCRIPTION;

	public static String	RESID_PROPERTY_FILE_SIZE_VALUE;
	
	public static String	RESID_PROPERTY_FILE_LASTMODIFIED_LABEL;
	public static String	RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILE_SIZE_LABEL;
	public static String	RESID_PROPERTY_FILE_SIZE_TOOLTIP;

	public static String	RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL;
	public static String	RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP;

	public static String	RESID_PROPERTY_FILE_EXTENSION_LABEL;
	public static String	RESID_PROPERTY_FILE_EXTENSION_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILE_PERMISSIONS_LABEL;
	public static String	RESID_PROPERTY_FILE_PERMISSIONS_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILE_OWNER_LABEL;
	public static String	RESID_PROPERTY_FILE_OWNER_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILE_GROUP_LABEL;
	public static String	RESID_PROPERTY_FILE_GROUP_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILE_CLASSIFICATION_LABEL;
	public static String	RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP;

	public static String	RESID_PROPERTY_FILE_READONLY_LABEL;
	public static String	RESID_PROPERTY_FILE_READONLY_TOOLTIP;


	public static String	RESID_PROPERTY_FILE_HIDDEN_LABEL;
	public static String	RESID_PROPERTY_FILE_HIDDEN_TOOLTIP;

	// search result properties
	public static String	RESID_PROPERTY_SEARCH_LINE_LABEL;
	public static String	RESID_PROPERTY_SEARCH_LINE_TOOLTIP;
	//public static String	RESID_PROPERTY_SEARCH_CHAR_END_LABEL;
	//public static String	RESID_PROPERTY_SEARCH_CHAR_END_TOOLTIP;
	
	public static String	RESID_PROPERTY_SEARCH_STATUS_ACTIVE_VALUE;
	public static String	RESID_PROPERTY_SEARCH_STATUS_INACTIVE_VALUE;

	
	// messages
	public static String MSG_ERROR_FILE_NOTFOUND; 

	// Remote editing messages
	public static String MSG_DOWNLOAD_NO_WRITE;
	public static String MSG_DOWNLOAD_ALREADY_OPEN_IN_EDITOR;
	public static String MSG_UPLOAD_FILE_EXISTS;	
	public static String MSG_DOWNLOAD_NO_WRITE_DETAILS;
	public static String MSG_DOWNLOAD_ALREADY_OPEN_IN_EDITOR_DETAILS;

	// file transfer messages
	public static String MSG_SYNCHRONIZE_PROGRESS;
	public static String MSG_EXTRACT_PROGRESS;
	public static String MSG_DOWNLOADING_PROGRESS;
	public static String MSG_UPLOADING_PROGRESS;
	
    // Remote File Exception Messages
  	public static String FILEMSG_SECURITY_ERROR;
  	public static String FILEMSG_IO_ERROR;
  	public static String FILEMSG_FOLDER_NOTFOUND;
  	public static String FILEMSG_FILE_NOTFOUND;
		
  	public static String FILEMSG_SECURITY_ERROR_DETAILS;
  	public static String FILEMSG_IO_ERROR_DETAILS;


  	
    // --------------------------
    // UNIVERSAL FILE MESSAGES...
    // --------------------------	
	public static  String FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE;

	
    public static  String FILEMSG_DELETE_FILE_FAILED;
    public static  String FILEMSG_CREATE_FILE_FAILED;
    public static  String FILEMSG_CREATE_FILE_FAILED_EXIST;
    public static  String FILEMSG_CREATE_FOLDER_FAILED;
    public static  String FILEMSG_CREATE_FOLDER_FAILED_EXIST;
    public static  String FILEMSG_CREATE_RESOURCE_NOTVISIBLE;
	public static  String FILEMSG_ERROR_NOFILETYPES;
    public static  String FILEMSG_COPY_FILE_FAILED;
    public static  String FILEMSG_MOVE_FILE_FAILED;
    public static  String FILEMSG_MOVE_TARGET_EQUALS_SOURCE;
    public static  String FILEMSG_MOVE_TARGET_EQUALS_PARENT_OF_SOURCE;
	public static  String FILEMSG_MOVE_TARGET_DESCENDS_FROM_SOURCE;
	public static  String FILEMSG_MOVE_FILTER_NOT_VALID;

    public static  String FILEMSG_DELETE_FILE_FAILED_DETAILS;
    public static  String FILEMSG_CREATE_FILE_FAILED_DETAILS;
    public static  String FILEMSG_CREATE_FILE_FAILED_EXIST_DETAILS;
    public static  String FILEMSG_CREATE_FOLDER_FAILED_DETAILS;
    public static  String FILEMSG_CREATE_FOLDER_FAILED_EXIST_DETAILS;
    public static  String FILEMSG_CREATE_RESOURCE_NOTVISIBLE_DETAILS;
	public static  String FILEMSG_ERROR_NOFILETYPES_DETAILS;
    public static  String FILEMSG_COPY_FILE_FAILED_DETAILS;
    public static  String FILEMSG_MOVE_FILE_FAILED_DETAILS;
    public static  String FILEMSG_MOVE_TARGET_EQUALS_SOURCE_DETAILS;
    public static  String FILEMSG_MOVE_TARGET_EQUALS_PARENT_OF_SOURCE_DETAILS;
	public static  String FILEMSG_MOVE_TARGET_DESCENDS_FROM_SOURCE_DETAILS;
	public static  String FILEMSG_MOVE_FILTER_NOT_VALID_DETAILS;
	
	public static String FILEMSG_MOVE_INTERRUPTED;
	public static String FILEMSG_COPY_INTERRUPTED;
	public static String FILEMSG_MOVE_INTERRUPTED_DETAILS;
	public static String FILEMSG_COPY_INTERRUPTED_DETAILS;
	
	// cache preferences
	public static String MSG_CACHE_UPLOAD_BEFORE_DELETE;
	public static String MSG_CACHE_UNABLE_TO_SYNCH;

	public static String MSG_ERROR_PROFILE_NOTFOUND;
	public static String MSG_ERROR_CONNECTION_NOTFOUND;
	
	public static String MSG_VALIDATE_PATH_EMPTY;

	public static String MSG_VALIDATE_PATH_EMPTY_DETAILS;

	


	// remote search messages
	public static String MSG_REMOTE_SEARCH_INVALID_REGEX;
	public static String MSG_REMOTE_SEARCH_INVALID_REGEX_DETAILS;
	

	
	public static String MSG_CREATEFILEGENERIC_PROGRESS;
	public static String MSG_CREATEFOLDERGENERIC_PROGRESS;
	
	public static String MSG_MAKE_SELECTION;
	public static String MSG_SELECT_FOLDER_NOT_VALID;
  	

	// preferences
	public static String MSG_ERROR_FILENAME_INVALID;
	
	public static String EditorManager_saveResourcesTitle;
	
	public static String OpenWithMenu_Other;
	public static String OpenWithMenu_OtherDialogDescription;
	
	static 
	{
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, FileResources.class);
	}
	
}
