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

package org.eclipse.rse.internal.files.ui;

// TODO is this file still used or necessary?

/**
 * Constants used throughout the System plugin
 */
public interface ISystemFileConstants 
{
	public static final String PLUGIN_ID ="org.eclipse.rse.files.ui"; //$NON-NLS-1$
	public static final String PREFIX = PLUGIN_ID+".files."; //$NON-NLS-1$
	// Icons
	public static final String ICON_DIR = "icons"; //$NON-NLS-1$
	public static final String ICON_PATH = java.io.File.separator + ICON_DIR + java.io.File.separator;
	public static final String ICON_SUFFIX = "Icon";	 //$NON-NLS-1$
	public static final String ICON_EXT = ".gif";	 //$NON-NLS-1$
		
	// Resource Bundle ids
	public static final String RESID_PREFIX = PREFIX+"ui.";	 //$NON-NLS-1$

	// -------------------
	// DEFAULT FILTERS...
	// -------------------
	public static final String RESID_FILTER_ROOTS      = RESID_PREFIX + "RootsFileFilter.label"; //$NON-NLS-1$
	public static final String RESID_FILTER_ROOTFILES  = RESID_PREFIX + "RootFileFilter.label"; //$NON-NLS-1$
	public static final String RESID_FILTER_ROOTFOLDERS= RESID_PREFIX + "RootFolderFilter.label"; //$NON-NLS-1$
	public static final String RESID_FILTER_DRIVES     = RESID_PREFIX + "DrivesFileFilter.label"; //$NON-NLS-1$
	public static final String RESID_FILTER_HOME       = RESID_PREFIX + "HomeFileFilter.label"; //$NON-NLS-1$
	public static final String RESID_FILTER_USERHOME   = RESID_PREFIX + "UserHomeFileFilter.label"; //$NON-NLS-1$
	public static final String RESID_FILTER_MYHOME     = RESID_PREFIX + "MyHomeFileFilter.label"; //$NON-NLS-1$
    // -------------------------
	// MISCELLANEOUS...
	// -------------------------	
	public static final String RESID_PROPERTY_FILE_FILTER_VALUE = RESID_PREFIX + "FileFilterProperty.value"; //$NON-NLS-1$

    // -------------------------
	// WIZARDS...
	// -------------------------	

	// New System File Filter wizard...
	public static final String RESID_NEWFILEFILTER_PAGE1_TITLE       = RESID_PREFIX+"NewFileFilter.page1.title"; //$NON-NLS-1$
	public static final String RESID_NEWFILEFILTER_PAGE1_DESCRIPTION = RESID_PREFIX+"NewFileFilter.page1.description"; //$NON-NLS-1$
	public static final String RESID_FILEFILTER_ROOT      = RESID_PREFIX+"filefilter."; //$NON-NLS-1$
	public static final String RESID_FILEFILTER_NAME_ROOT = RESID_FILEFILTER_ROOT+"name."; //$NON-NLS-1$
	public static final String RESID_FILEFILTER_FOLDER_ROOT = RESID_FILEFILTER_ROOT+"folder."; //$NON-NLS-1$
	public static final String RESID_FILEFILTER_FILE_ROOT   = RESID_FILEFILTER_ROOT+"file.";	 //$NON-NLS-1$
	public static final String RESID_FILEFILTER_STRING_ROOT = RESID_FILEFILTER_ROOT+"strings."; //$NON-NLS-1$

	// New Filter String wizard...
	public static final String RESID_NEWFILTERSTRING_TITLE             = RESID_PREFIX+"NewFilterString.title"; //$NON-NLS-1$
	public static final String RESID_NEWFILTERSTRING_PAGE1_TITLE       = RESID_PREFIX+"NewFilterString.page1.title"; //$NON-NLS-1$
	public static final String RESID_NEWFILTERSTRING_PAGE1_DESCRIPTION = RESID_PREFIX+"NewFilterString.page1.description"; //$NON-NLS-1$

	// File Filter String Re-Usable form (used in dialog and wizard)
	public static final String RESID_FILEFILTERSTRING_ROOT            = RESID_PREFIX+"filefilterstring."; //$NON-NLS-1$
	public static final String RESID_FILEFILTERSTRING_FOLDER_ROOT     = RESID_FILEFILTERSTRING_ROOT+"folder."; //$NON-NLS-1$
	public static final String RESID_FILEFILTERSTRING_FILE_ROOT       = RESID_FILEFILTERSTRING_ROOT+"file.";	 //$NON-NLS-1$
	public static final String RESID_FILEFILTERSTRING_INCFOLDERS_ROOT = RESID_FILEFILTERSTRING_ROOT+"include.folders."; //$NON-NLS-1$
	public static final String RESID_FILEFILTERSTRING_INCFILES_ROOT   = RESID_FILEFILTERSTRING_ROOT+"include.files.";	 //$NON-NLS-1$
	public static final String RESID_FILEFILTERSTRING_INCFILESONLY_ROOT = RESID_FILEFILTERSTRING_ROOT+"include.filesonly.";	 //$NON-NLS-1$
	public static final String RESID_FILEFILTERSTRING_BYFILENAME_ROOT = RESID_FILEFILTERSTRING_ROOT+"include.byfilename.";	 //$NON-NLS-1$
	public static final String RESID_FILEFILTERSTRING_BYFILETYPES_ROOT= RESID_FILEFILTERSTRING_ROOT+"include.byfiletypes.";	 //$NON-NLS-1$
	public static final String RESID_FILEFILTERSTRING_TYPES_ROOT      = RESID_FILEFILTERSTRING_ROOT+"types.";	 //$NON-NLS-1$
	public static final String RESID_FILEFILTERSTRING_SELECTTYPES_ROOT= RESID_FILEFILTERSTRING_ROOT+"selectTypes.";	 //$NON-NLS-1$
	public static final String RESID_FILEFILTERSTRING_TEST_ROOT       = RESID_FILEFILTERSTRING_ROOT+"test.";	 //$NON-NLS-1$
    // -------------------------
	// DIALOGS...
	// -------------------------	
	// Change System File Filter dialog...
	public static final String RESID_CHGFILEFILTER_TITLE = RESID_PREFIX+"ChgFileFilter.title"; //$NON-NLS-1$
	// Change System File Filter String dialog...
	public static final String RESID_CHGFILEFILTERSTRING_TITLE = RESID_PREFIX+"ChgFileFilterString.title"; //$NON-NLS-1$

	// Select Directory dialog...
	public static final String RESID_SELECTDIRECTORY_TITLE = RESID_PREFIX+"SelectDirectory.title"; //$NON-NLS-1$
	public static final String RESID_SELECTDIRECTORY_VERBIAGE = RESID_PREFIX+"SelectDirectory.verbiage."; //$NON-NLS-1$
	public static final String RESID_SELECTDIRECTORY_SELECT  = RESID_PREFIX+"SelectDirectory.select."; //$NON-NLS-1$
	// Select File dialog...
	public static final String RESID_SELECTFILE_TITLE = RESID_PREFIX+"SelectFile.title"; //$NON-NLS-1$
	public static final String RESID_SELECTFILE_VERBIAGE = RESID_PREFIX+"SelectFile.verbiage."; //$NON-NLS-1$
	public static final String RESID_SELECTFILE_SELECT  = RESID_PREFIX+"SelectFile.select."; //$NON-NLS-1$

	// Prompt for home folder dialog...
	public static final String RESID_HOMEPROMPT_TITLE   = RESID_PREFIX+"HomePrompt.title"; //$NON-NLS-1$
	public static final String RESID_HOMEPROMPT_VERBIAGE = RESID_PREFIX+"HomePrompt.verbiage."; //$NON-NLS-1$
	public static final String RESID_HOMEPROMPT_PROMPT_ROOT  = RESID_PREFIX+"HomePrompt.prompt."; //$NON-NLS-1$
		
    // -------------------------
	// ACTIONS...
	// -------------------------
	public static final String ACTION_ID = RESID_PREFIX + "action."; //$NON-NLS-1$

    public static final String ACTION_NEWFILEFILTER      = ACTION_ID + "NewFilter"; //$NON-NLS-1$
    public static final String ACTION_NEWNESTEDFILEFILTER= ACTION_ID + "NewNestedFilter"; //$NON-NLS-1$
    public static final String ACTION_UPDATEFILEFILTER   = ACTION_ID + "UpdateFilter"; //$NON-NLS-1$

    public static final String ACTION_NEWFILEFILTERSTRING = ACTION_ID + "NewFilterString"; //$NON-NLS-1$
    public static final String ACTION_ADDFILEFILTERSTRING = ACTION_ID + "AddFilterString";     //$NON-NLS-1$
    public static final String ACTION_UPDATEFILEFILTERSTRING = ACTION_ID + "UpdateFilterString"; //$NON-NLS-1$
    public static final String ACTION_SELECTFILETYPES = ACTION_ID + "SelectFileTypes"; //$NON-NLS-1$
    
    // -------------------------
	// WIDGETS...
	// -------------------------
	public static final String WIDGET_ID = RESID_PREFIX + "widget."; //$NON-NLS-1$
	
	public static final String WIDGET_FOLDER_ROOT = WIDGET_ID + "directory."; //$NON-NLS-1$
	public static final String WIDGET_BROWSE_ROOT = WIDGET_ID + "browse.";	 //$NON-NLS-1$
	

    // -------------------------
	// PROPERTY PAGES...
	// -------------------------	
    public static final String RESID_PP_FILE_ROOT       = RESID_PREFIX+"pp.file."; //$NON-NLS-1$
    public static final String RESID_PP_FILE_TITLE      = RESID_PP_FILE_ROOT+"title"; //$NON-NLS-1$
    public static final String RESID_PP_FILE_TYPE_ROOT  = RESID_PP_FILE_ROOT+"type.";     //$NON-NLS-1$
    public static final String RESID_PP_FILE_TYPE_FILE_VALUE   = RESID_PP_FILE_ROOT+"type.file.value"; //$NON-NLS-1$
    public static final String RESID_PP_FILE_TYPE_FOLDER_VALUE = RESID_PP_FILE_ROOT+"type.folder.value"; //$NON-NLS-1$
    public static final String RESID_PP_FILE_TYPE_ROOT_VALUE   = RESID_PP_FILE_ROOT+"type.root.value";     //$NON-NLS-1$
    public static final String RESID_PP_FILE_NAME_ROOT     = RESID_PP_FILE_ROOT+"name.";    //$NON-NLS-1$
    public static final String RESID_PP_FILE_PATH_ROOT     = RESID_PP_FILE_ROOT+"path.";  //$NON-NLS-1$
    public static final String RESID_PP_FILE_SIZE_ROOT     = RESID_PP_FILE_ROOT+"size.";  //$NON-NLS-1$
    public static final String RESID_PP_FILE_SIZE_VALUE    = RESID_PP_FILE_ROOT+"size.value";  //$NON-NLS-1$
    public static final String RESID_PP_FILE_MODIFIED_ROOT = RESID_PP_FILE_ROOT+"modified."; 	 //$NON-NLS-1$
    public static final String RESID_PP_FILE_READONLY_ROOT = RESID_PP_FILE_ROOT+"readonly."; 	 //$NON-NLS-1$
    public static final String RESID_PP_FILE_READABLE_ROOT = RESID_PP_FILE_ROOT+"readable."; 	 //$NON-NLS-1$
    public static final String RESID_PP_FILE_WRITABLE_ROOT = RESID_PP_FILE_ROOT+"writable."; 	 //$NON-NLS-1$
    public static final String RESID_PP_FILE_HIDDEN_ROOT   = RESID_PP_FILE_ROOT+"hidden."; //$NON-NLS-1$
    
    
    // -------------------------------
    // Enter or select file form
    // -------------------------------
	public static final String RESID_ENTER_OR_SELECT_FILE_PREFIX = RESID_PREFIX + "EnterOrSelectFile."; //$NON-NLS-1$
	public static final String RESID_ENTER_OR_SELECT_FILE_TITLE = RESID_ENTER_OR_SELECT_FILE_PREFIX + "title"; //$NON-NLS-1$
	public static final String RESID_ENTER_OR_SELECT_FILE_VERBIAGE_LABEL = RESID_ENTER_OR_SELECT_FILE_PREFIX + "verbiage.label"; //$NON-NLS-1$
	public static final String RESID_ENTER_OR_SELECT_FILE_ENTER_LABEL = RESID_ENTER_OR_SELECT_FILE_PREFIX + "enter.label"; //$NON-NLS-1$
	public static final String RESID_ENTER_OR_SELECT_FILE_ENTER_TOOLTIP = RESID_ENTER_OR_SELECT_FILE_PREFIX + "enter.tooltip"; //$NON-NLS-1$
	public static final String RESID_ENTER_OR_SELECT_FILE_SELECT_TOOLTIP = RESID_ENTER_OR_SELECT_FILE_PREFIX + "select.tooltip"; //$NON-NLS-1$
	public static final String RESID_ENTER_OR_SELECT_FILE_FILE_LABEL = RESID_ENTER_OR_SELECT_FILE_PREFIX + "file.label"; //$NON-NLS-1$
	public static final String RESID_ENTER_OR_SELECT_FILE_FILE_TOOLTIP = RESID_ENTER_OR_SELECT_FILE_PREFIX + "file.tooltip"; //$NON-NLS-1$
}