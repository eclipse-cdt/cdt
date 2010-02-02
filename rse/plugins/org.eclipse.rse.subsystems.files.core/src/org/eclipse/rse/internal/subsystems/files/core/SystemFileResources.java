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
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David McKnight   (IBM)        - [223204] [cleanup] fix broken nls strings in files.ui and others
 * David McKnight (IBM)  - [283033] remoteFileTypes extension point should include "xml" type
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.core;

import org.eclipse.osgi.util.NLS;


public class SystemFileResources extends NLS 
{
	private static String	BUNDLE_NAME	= "org.eclipse.rse.internal.subsystems.files.core.SystemFileResources";	//$NON-NLS-1$

	// -------------------
	// DEFAULT FILTERS...
	// -------------------
	public static String	RESID_FILTER_ROOTS;
	public static String	RESID_FILTER_ROOTFILES;
	public static String	RESID_FILTER_ROOTFOLDERS;
	public static String	RESID_FILTER_DRIVES;
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

	// File Filter String Re-Usable form (used in dialog and wizard)
	public static String	RESID_FILEFILTERSTRING_FILE_LABEL;
	public static String	RESID_FILEFILTERSTRING_INCFILESONLY_LABEL; 
	public static String	RESID_FILEFILTERSTRING_BYFILENAME_LABEL; 
	public static String	RESID_FILEFILTERSTRING_BYFILETYPES_LABEL; 
	public static String	RESID_FILEFILTERSTRING_TYPES_LABEL; 
	public static String	RESID_FILEFILTERSTRING_SELECTTYPES_LABEL; 

	public static String	RESID_FILEFILTERSTRING_FILE_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_INCFILESONLY_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_BYFILENAME_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_BYFILETYPES_TOOLTIP;
	public static String	RESID_FILEFILTERSTRING_TYPES_TOOLTIP; 
	public static String	RESID_FILEFILTERSTRING_SELECTTYPES_TOOLTIP; 

	// -------------------------
	// DIALOGS...
	// -------------------------
	// Change System File Filter dialog...
	public static String	RESID_CHGFILEFILTER_TITLE;


	// Select Directory dialog...
	public static String	RESID_SELECTDIRECTORY_TITLE;
	public static String	RESID_SELECTDIRECTORY_VERBIAGE;

	public static String	RESID_SELECTDIRECTORY_SELECT_TOOLTIP;

	// Select File dialog...
	public static String	RESID_SELECTFILE_TITLE;
	public static String	RESID_SELECTFILE_VERBIAGE;
	public static String	RESID_SELECTFILE_SELECT_TOOLTIP;

	// Prompt for home folder dialog...
	public static String	RESID_HOMEPROMPT_TITLE;

	// -------------------------
	// ACTIONS...
	// -------------------------
	public static String	ACTION_NEWFILTER_LABEL;
	public static String	ACTION_NEWFILTER_TOOLTIP;

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
	public static String	RESID_PP_FILE_TYPE_LABEL; 
	public static String	RESID_PP_FILE_TYPE_TOOLTIP;
	public static String	RESID_PP_FILE_TYPE_FILE_VALUE;
	public static String	RESID_PP_FILE_TYPE_FOLDER_VALUE;
	public static String	RESID_PP_FILE_TYPE_ROOT_VALUE;
	public static String	RESID_PP_FILE_NAME_LABEL;
	public static String	RESID_PP_FILE_PATH_LABEL; 
	public static String	RESID_PP_FILE_SIZE_LABEL;
	public static String	RESID_PP_FILE_MODIFIED_LABEL;
	public static String	RESID_PP_FILE_READONLY_LABEL;
	public static String	RESID_PP_FILE_HIDDEN_LABEL;

	public static String	RESID_PP_FILE_NAME_TOOLTIP;
	public static String	RESID_PP_FILE_PATH_TOOLTIP;
	public static String	RESID_PP_FILE_SIZE_TOOLTIP;
	public static String	RESID_PP_FILE_MODIFIED_TOOLTIP; 
	public static String	RESID_PP_FILE_READONLY_TOOLTIP; 
	public static String	RESID_PP_FILE_HIDDEN_TOOLTIP;
	
	public static String	RESID_PP_FILE_ENCODING_GROUP_LABEL;
	public static String	RESID_PP_FILE_ENCODING_DEFAULT_LABEL;
	public static String	RESID_PP_FILE_ENCODING_DEFAULT_TOOLTIP;
	public static String	RESID_PP_FILE_ENCODING_OTHER_LABEL;
	public static String	RESID_PP_FILE_ENCODING_OTHER_TOOLTIP;
	public static String	RESID_PP_FILE_ENCODING_ENTER_TOOLTIP;
	

	public static String	RESID_ENTER_OR_SELECT_FILE_TITLE;

	public static String	RESID_JOB_SEARCH_NAME;
	public static String	RESID_JOB_DECORATEFILES_NAME;
	

	public static String FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE;
	public static String FILEMSG_VALIDATE_FILEFILTERSTRING_NOTVALID;
	public static String MSG_VALIDATE_NAME_EMPTY;
	public static String MSG_VALIDATE_NAME_EMPTY_DETAILS;
	public static String MSG_VALIDATE_NAME_NOTUNIQUE;
	public static String MSG_VALIDATE_NAME_NOTUNIQUE_DETAILS;


  	public static String FILEMSG_FILE_NOTFOUND;
  	

	
	public static String MSG_FOLDER_UNREADABLE;
	
	public static String MSG_FILE_CANNOT_BE_SAVED;
	public static String MSG_FILE_CANNOT_BE_SAVED_DETAILS;	
	
	public static String RESID_JOB_SETUP_ENCODING_PROVIDER;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, SystemFileResources.class);
	}
}
