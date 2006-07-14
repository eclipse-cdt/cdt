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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui;

// TODO is this file still used or necessary?

/**
 * Constants used throughout the System plugin
 */
public interface ISystemFileConstants 
{
	public static final String PLUGIN_ID ="org.eclipse.rse.files.ui";
	public static final String PREFIX = PLUGIN_ID+".files.";
	// Icons
	public static final String ICON_DIR = "icons";
	public static final String ICON_PATH = java.io.File.separator + ICON_DIR + java.io.File.separator;
	public static final String ICON_SUFFIX = "Icon";	
	public static final String ICON_EXT = ".gif";	
		
	// Resource Bundle ids
	public static final String RESID_PREFIX = PREFIX+"ui.";	

	// -------------------
	// DEFAULT FILTERS...
	// -------------------
	public static final String RESID_FILTER_ROOTS      = RESID_PREFIX + "RootsFileFilter.label";
	public static final String RESID_FILTER_ROOTFILES  = RESID_PREFIX + "RootFileFilter.label";
	public static final String RESID_FILTER_ROOTFOLDERS= RESID_PREFIX + "RootFolderFilter.label";
	public static final String RESID_FILTER_DRIVES     = RESID_PREFIX + "DrivesFileFilter.label";
	public static final String RESID_FILTER_HOME       = RESID_PREFIX + "HomeFileFilter.label";
	public static final String RESID_FILTER_USERHOME   = RESID_PREFIX + "UserHomeFileFilter.label";
	public static final String RESID_FILTER_MYHOME     = RESID_PREFIX + "MyHomeFileFilter.label";
    // -------------------------
	// MISCELLANEOUS...
	// -------------------------	
	public static final String RESID_PROPERTY_FILE_FILTER_VALUE = RESID_PREFIX + "FileFilterProperty.value";

    // -------------------------
	// WIZARDS...
	// -------------------------	

	// New System File Filter wizard...
	public static final String RESID_NEWFILEFILTER_PAGE1_TITLE       = RESID_PREFIX+"NewFileFilter.page1.title";
	public static final String RESID_NEWFILEFILTER_PAGE1_DESCRIPTION = RESID_PREFIX+"NewFileFilter.page1.description";
	public static final String RESID_FILEFILTER_ROOT      = RESID_PREFIX+"filefilter.";
	public static final String RESID_FILEFILTER_NAME_ROOT = RESID_FILEFILTER_ROOT+"name.";
	public static final String RESID_FILEFILTER_FOLDER_ROOT = RESID_FILEFILTER_ROOT+"folder.";
	public static final String RESID_FILEFILTER_FILE_ROOT   = RESID_FILEFILTER_ROOT+"file.";	
	public static final String RESID_FILEFILTER_STRING_ROOT = RESID_FILEFILTER_ROOT+"strings.";

	// New Filter String wizard...
	public static final String RESID_NEWFILTERSTRING_TITLE             = RESID_PREFIX+"NewFilterString.title";
	public static final String RESID_NEWFILTERSTRING_PAGE1_TITLE       = RESID_PREFIX+"NewFilterString.page1.title";
	public static final String RESID_NEWFILTERSTRING_PAGE1_DESCRIPTION = RESID_PREFIX+"NewFilterString.page1.description";

	// File Filter String Re-Usable form (used in dialog and wizard)
	public static final String RESID_FILEFILTERSTRING_ROOT            = RESID_PREFIX+"filefilterstring.";
	public static final String RESID_FILEFILTERSTRING_FOLDER_ROOT     = RESID_FILEFILTERSTRING_ROOT+"folder.";
	public static final String RESID_FILEFILTERSTRING_FILE_ROOT       = RESID_FILEFILTERSTRING_ROOT+"file.";	
	public static final String RESID_FILEFILTERSTRING_INCFOLDERS_ROOT = RESID_FILEFILTERSTRING_ROOT+"include.folders.";
	public static final String RESID_FILEFILTERSTRING_INCFILES_ROOT   = RESID_FILEFILTERSTRING_ROOT+"include.files.";	
	public static final String RESID_FILEFILTERSTRING_INCFILESONLY_ROOT = RESID_FILEFILTERSTRING_ROOT+"include.filesonly.";	
	public static final String RESID_FILEFILTERSTRING_BYFILENAME_ROOT = RESID_FILEFILTERSTRING_ROOT+"include.byfilename.";	
	public static final String RESID_FILEFILTERSTRING_BYFILETYPES_ROOT= RESID_FILEFILTERSTRING_ROOT+"include.byfiletypes.";	
	public static final String RESID_FILEFILTERSTRING_TYPES_ROOT      = RESID_FILEFILTERSTRING_ROOT+"types.";	
	public static final String RESID_FILEFILTERSTRING_SELECTTYPES_ROOT= RESID_FILEFILTERSTRING_ROOT+"selectTypes.";	
	public static final String RESID_FILEFILTERSTRING_TEST_ROOT       = RESID_FILEFILTERSTRING_ROOT+"test.";	
    // -------------------------
	// DIALOGS...
	// -------------------------	
	// Change System File Filter dialog...
	public static final String RESID_CHGFILEFILTER_TITLE = RESID_PREFIX+"ChgFileFilter.title";
	// Change System File Filter String dialog...
	public static final String RESID_CHGFILEFILTERSTRING_TITLE = RESID_PREFIX+"ChgFileFilterString.title";

	// Select Directory dialog...
	public static final String RESID_SELECTDIRECTORY_TITLE = RESID_PREFIX+"SelectDirectory.title";
	public static final String RESID_SELECTDIRECTORY_VERBAGE = RESID_PREFIX+"SelectDirectory.verbage.";
	public static final String RESID_SELECTDIRECTORY_SELECT  = RESID_PREFIX+"SelectDirectory.select.";
	// Select File dialog...
	public static final String RESID_SELECTFILE_TITLE = RESID_PREFIX+"SelectFile.title";
	public static final String RESID_SELECTFILE_VERBAGE = RESID_PREFIX+"SelectFile.verbage.";
	public static final String RESID_SELECTFILE_SELECT  = RESID_PREFIX+"SelectFile.select.";

	// Prompt for home folder dialog...
	public static final String RESID_HOMEPROMPT_TITLE   = RESID_PREFIX+"HomePrompt.title";
	public static final String RESID_HOMEPROMPT_VERBAGE = RESID_PREFIX+"HomePrompt.verbage.";
	public static final String RESID_HOMEPROMPT_PROMPT_ROOT  = RESID_PREFIX+"HomePrompt.prompt.";
		
    // -------------------------
	// ACTIONS...
	// -------------------------
	public static final String ACTION_ID = RESID_PREFIX + "action.";

    public static final String ACTION_NEWFILEFILTER      = ACTION_ID + "NewFilter";
    public static final String ACTION_NEWNESTEDFILEFILTER= ACTION_ID + "NewNestedFilter";
    public static final String ACTION_UPDATEFILEFILTER   = ACTION_ID + "UpdateFilter";

    public static final String ACTION_NEWFILEFILTERSTRING = ACTION_ID + "NewFilterString";
    public static final String ACTION_ADDFILEFILTERSTRING = ACTION_ID + "AddFilterString";    
    public static final String ACTION_UPDATEFILEFILTERSTRING = ACTION_ID + "UpdateFilterString";
    public static final String ACTION_SELECTFILETYPES = ACTION_ID + "SelectFileTypes";
    
    // -------------------------
	// WIDGETS...
	// -------------------------
	public static final String WIDGET_ID = RESID_PREFIX + "widget.";
	
	public static final String WIDGET_FOLDER_ROOT = WIDGET_ID + "directory.";
	public static final String WIDGET_BROWSE_ROOT = WIDGET_ID + "browse.";	
	

    // -------------------------
	// PROPERTY PAGES...
	// -------------------------	
    public static final String RESID_PP_FILE_ROOT       = RESID_PREFIX+"pp.file.";
    public static final String RESID_PP_FILE_TITLE      = RESID_PP_FILE_ROOT+"title";
    public static final String RESID_PP_FILE_TYPE_ROOT  = RESID_PP_FILE_ROOT+"type.";    
    public static final String RESID_PP_FILE_TYPE_FILE_VALUE   = RESID_PP_FILE_ROOT+"type.file.value";
    public static final String RESID_PP_FILE_TYPE_FOLDER_VALUE = RESID_PP_FILE_ROOT+"type.folder.value";
    public static final String RESID_PP_FILE_TYPE_ROOT_VALUE   = RESID_PP_FILE_ROOT+"type.root.value";    
    public static final String RESID_PP_FILE_NAME_ROOT     = RESID_PP_FILE_ROOT+"name.";   
    public static final String RESID_PP_FILE_PATH_ROOT     = RESID_PP_FILE_ROOT+"path."; 
    public static final String RESID_PP_FILE_SIZE_ROOT     = RESID_PP_FILE_ROOT+"size."; 
    public static final String RESID_PP_FILE_SIZE_VALUE    = RESID_PP_FILE_ROOT+"size.value"; 
    public static final String RESID_PP_FILE_MODIFIED_ROOT = RESID_PP_FILE_ROOT+"modified."; 	
    public static final String RESID_PP_FILE_READONLY_ROOT = RESID_PP_FILE_ROOT+"readonly."; 	
    public static final String RESID_PP_FILE_READABLE_ROOT = RESID_PP_FILE_ROOT+"readable."; 	
    public static final String RESID_PP_FILE_WRITABLE_ROOT = RESID_PP_FILE_ROOT+"writable."; 	
    public static final String RESID_PP_FILE_HIDDEN_ROOT   = RESID_PP_FILE_ROOT+"hidden.";
    
    
    // -------------------------------
    // Enter or select file form
    // -------------------------------
	public static final String RESID_ENTER_OR_SELECT_FILE_PREFIX = RESID_PREFIX + "EnterOrSelectFile.";
	public static final String RESID_ENTER_OR_SELECT_FILE_TITLE = RESID_ENTER_OR_SELECT_FILE_PREFIX + "title";
	public static final String RESID_ENTER_OR_SELECT_FILE_VERBAGE_LABEL = RESID_ENTER_OR_SELECT_FILE_PREFIX + "verbage.label";
	public static final String RESID_ENTER_OR_SELECT_FILE_ENTER_LABEL = RESID_ENTER_OR_SELECT_FILE_PREFIX + "enter.label";
	public static final String RESID_ENTER_OR_SELECT_FILE_ENTER_TOOLTIP = RESID_ENTER_OR_SELECT_FILE_PREFIX + "enter.tooltip";
	public static final String RESID_ENTER_OR_SELECT_FILE_SELECT_TOOLTIP = RESID_ENTER_OR_SELECT_FILE_PREFIX + "select.tooltip";
	public static final String RESID_ENTER_OR_SELECT_FILE_FILE_LABEL = RESID_ENTER_OR_SELECT_FILE_PREFIX + "file.label";
	public static final String RESID_ENTER_OR_SELECT_FILE_FILE_TOOLTIP = RESID_ENTER_OR_SELECT_FILE_PREFIX + "file.tooltip";
}