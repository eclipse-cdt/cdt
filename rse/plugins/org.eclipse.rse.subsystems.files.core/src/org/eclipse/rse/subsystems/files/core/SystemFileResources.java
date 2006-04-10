/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.files.core;

import org.eclipse.osgi.util.NLS;


public class SystemFileResources extends NLS 
{
	private static String	BUNDLE_NAME	= "org.eclipse.rse.subsystems.files.core.SystemFileResources";	//$NON-NLS-1$

	// -------------------
	// DEFAULT FILTERS...
	// -------------------
	public static String	RESID_FILTER_ROOTS;
	public static String	RESID_FILTER_ROOTFILES;
	public static String	RESID_FILTER_ROOTFOLDERS;
	public static String	RESID_FILTER_DRIVES;
	public static String	RESID_FILTER_HOME;
	public static String	RESID_FILTER_USERHOME;
	public static String	RESID_FILTER_MYHOME;

	// -------------------------
	// MISCELLANEOUS...
	// -------------------------
	public static String	RESID_PROPERTY_FILE_FILTER_VALUE;

	// -------------------------
	// WIZARDS...
	// -------------------------

	// New System File Filter wizard...
	public static String	RESID_NEWFILEFILTER_PAGE1_TITLE;
	public static String	RESID_NEWFILEFILTER_PAGE1_DESCRIPTION;

	// New Filter String wizard...
	public static String	RESID_NEWFILTERSTRING_TITLE;
	public static String	RESID_NEWFILTERSTRING_PAGE1_TITLE;
	public static String	RESID_NEWFILTERSTRING_PAGE1_DESCRIPTION;

	// File Filter String Re-Usable form (used in dialog and wizard)

	public static String	RESID_FILEFILTERSTRING_FOLDER_LABEL;
	public static String	RESID_FILEFILTERSTRING_FILE_LABEL;
	public static String	RESID_FILEFILTERSTRING_INCFOLDERS_LABEL; 
	public static String	RESID_FILEFILTERSTRING_INCFILES_LABEL;
	public static String	RESID_FILEFILTERSTRING_INCFILESONLY_LABEL; 
	public static String	RESID_FILEFILTERSTRING_BYFILENAME_LABEL; 
	public static String	RESID_FILEFILTERSTRING_BYFILETYPES_LABEL; 
	public static String	RESID_FILEFILTERSTRING_TYPES_LABEL; 
	public static String	RESID_FILEFILTERSTRING_SELECTTYPES_LABEL; 
	public static String	RESID_FILEFILTERSTRING_TEST_LABEL;
	
	public static String	RESID_FILEFILTERSTRING_FOLDER_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_FILE_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_INCFOLDERS_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_INCFILES_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_INCFILESONLY_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_BYFILENAME_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_BYFILETYPES_TOOLTIP;
	public static String	RESID_FILEFILTERSTRING_TYPES_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_SELECTTYPES_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_TEST_TOOLTIP; 

	// -------------------------
	// DIALOGS...
	// -------------------------
	// Change System File Filter dialog...
	public static String	RESID_CHGFILEFILTER_TITLE;

	// Change System File Filter String dialog...
	public static String	RESID_CHGFILEFILTERSTRING_TITLE;

	// Select Directory dialog...
	public static String	RESID_SELECTDIRECTORY_TITLE;
	public static String	RESID_SELECTDIRECTORY_VERBAGE;
	public static String	RESID_SELECTDIRECTORY_SELECT_LABEL;
	public static String	RESID_SELECTDIRECTORY_SELECT_TOOLTIP;

	// Select File dialog...
	public static String	RESID_SELECTFILE_TITLE;
	public static String	RESID_SELECTFILE_VERBAGE;
	public static String	RESID_SELECTFILE_SELECT_LABEL;
	public static String	RESID_SELECTFILE_SELECT_TOOLTIP;

	// Prompt for home folder dialog...
	public static String	RESID_HOMEPROMPT_TITLE;
	public static String	RESID_HOMEPROMPT_VERBAGE;
	public static String	RESID_HOMEPROMPT_PROMPT_LABEL;
	public static String	RESID_HOMEPROMPT_PROMPT_TOOLTIP;

	// -------------------------
	// ACTIONS...
	// -------------------------
	public static String	ACTION_NEWFILTER_LABEL;
	public static String	ACTION_NEWFILTER_TOOLTIP;

	public static String	ACTION_NEWNESTEDFILTER_LABEL;
	public static String	ACTION_NEWNESTEDFILTER_TOOLTIP;


	public static String	ACTION_NEWFILTERSTRING_LABEL;
	public static String	ACTION_NEWFILTERSTRING_TOOLTIP;

	public static String	ACTION_ADDFILTERSTRING_LABEL;
	public static String	ACTION_AddFILTERSTRING_TOOLTIP;

	public static String	ACTION_UPDATEFILTERSTRING_LABEL;
	public static String	ACTION_UPDATEFILTERSTRING_TOOLTIP;

	public static String	ACTION_SELECTFILETYPES_LABEL;
	public static String	ACTION_SELECTFILETYPES_TOOLTIP;
	
	// -------------------------
	// WIDGETS...
	// -------------------------
	public static String	WIDGET_FOLDER_LABEL; 
	public static String	WIDGET_FOLDER_TOOLTIP; 

	public static String	WIDGET_BROWSE_LABEL; 
	public static String	WIDGET_BROWSE_TOOLTIP;

	// -------------------------
	// PROPERTY PAGES...
	// -------------------------
	public static String	RESID_PP_FILE_TITLE;
	public static String	RESID_PP_FILE_TYPE_LABEL; 
	public static String	RESID_PP_FILE_TYPE_TOOLTIP;
	public static String	RESID_PP_FILE_TYPE_FILE_VALUE;
	public static String	RESID_PP_FILE_TYPE_FOLDER_VALUE;
	public static String	RESID_PP_FILE_TYPE_ROOT_VALUE;
	public static String	RESID_PP_FILE_NAME_LABEL;
	public static String	RESID_PP_FILE_PATH_LABEL; 
	public static String	RESID_PP_FILE_SIZE_LABEL;
	public static String	RESID_PP_FILE_SIZE_VALUE;
	public static String	RESID_PP_FILE_MODIFIED_LABEL;
	public static String	RESID_PP_FILE_READONLY_LABEL;
	public static String	RESID_PP_FILE_WRITABLE_LABEL;
	public static String	RESID_PP_FILE_HIDDEN_LABEL;

	public static String	RESID_PP_FILE_NAME_TOOLTIP;
	public static String	RESID_PP_FILE_PATH_TOOLTIP;
	public static String	RESID_PP_FILE_SIZE_TOOLTIP;
	public static String	RESID_PP_FILE_MODIFIED_TOOLTIP; 
	public static String	RESID_PP_FILE_READONLY_TOOLTIP; 
	public static String	RESID_PP_FILE_READABLE_TOOLTIP;
	public static String	RESID_PP_FILE_WRITABLE_TOOLTIP;
	public static String	RESID_PP_FILE_HIDDEN_TOOLTIP;
	
	// -------------------------------
	// Enter or select file form
	// -------------------------------
	public static String	RESID_ENTER_OR_SELECT_FILE_TITLE;
	public static String	RESID_ENTER_OR_SELECT_FILE_VERBAGE_LABEL;
	public static String	RESID_ENTER_OR_SELECT_FILE_ENTER_LABEL;
	public static String	RESID_ENTER_OR_SELECT_FILE_ENTER_TOOLTIP;
	public static String	RESID_ENTER_OR_SELECT_FILE_SELECT_TOOLTIP;
	public static String	RESID_ENTER_OR_SELECT_FILE_FILE_LABEL;
	public static String	RESID_ENTER_OR_SELECT_FILE_FILE_TOOLTIP;

	public static String	RESID_JOB_SEARCH_NAME;
	public static String	RESID_JOB_DECORATEFILES_NAME;
	
	public static String	RESID_FTP_CONNECTORSERVICE_NAME;
	public static String	RESID_FTP_CONNECTORSERVICE_DESCRIPTION;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, SystemFileResources.class);
	}
}