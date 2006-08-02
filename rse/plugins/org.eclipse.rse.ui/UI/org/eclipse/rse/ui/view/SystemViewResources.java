/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;

import org.eclipse.osgi.util.NLS;


/**
 * Constants used throughout the SystemView plugin
 */
public class SystemViewResources extends NLS {
	private static String	BUNDLE_NAME	= "org.eclipse.rse.ui.view.SystemViewResources";	//$NON-NLS-1$

	// -------------------------
	// Property names...
	// -------------------------
	// Property sheet values: Common	
	public static String	RESID_PROPERTY_NBRCHILDREN_LABEL;
	public static String	RESID_PROPERTY_NBRCHILDREN_TOOLTIP;

	public static String	RESID_PROPERTY_NBRCHILDRENRETRIEVED_LABEL;
	public static String	RESID_PROPERTY_NBRCHILDRENRETRIEVED_TOOLTIP;
	
	// Property sheet values: Connections
	public static String	RESID_PROPERTY_PROFILE_TYPE_VALUE;

	public static String	RESID_PROPERTY_PROFILESTATUS_LABEL;
 	public static String	RESID_PROPERTY_PROFILESTATUS_TOOLTIP;

	public static String	RESID_PROPERTY_PROFILESTATUS_ACTIVE_LABEL;

	public static String	RESID_PROPERTY_PROFILESTATUS_NOTACTIVE_LABEL;

	public static String	RESID_PROPERTY_CONNECTION_TYPE_VALUE;

	public static String	RESID_PROPERTY_SYSTEMTYPE_LABEL;
	public static String	RESID_PROPERTY_SYSTEMTYPE_TOOLTIP;

	public static String    RESID_PROPERTY_CONNECTIONSTATUS_LABEL;
	public static String    RESID_PROPERTY_CONNECTIONSTATUS_TOOLTIP;
	public static String	RESID_PROPERTY_CONNECTIONSTATUS_CONNECTED_VALUE;

	public static String	RESID_PROPERTY_CONNECTIONSTATUS_DISCONNECTED_VALUE;

	
	public static String	RESID_PROPERTY_ALIASNAME_LABEL;
	public static String	RESID_PROPERTY_ALIASNAME_TOOLTIP;

	public static String	RESID_PROPERTY_HOSTNAME_LABEL;
	public static String	RESID_PROPERTY_HOSTNAME_TOOLTIP;

	public static String	RESID_PROPERTY_DEFAULTUSERID_LABEL;
	public static String	RESID_PROPERTY_DEFAULTUSERID_TOOLTIP;

	public static String	RESID_PROPERTY_CONNDESCRIPTION_LABEL;
	public static String	RESID_PROPERTY_CONNDESCRIPTION_TOOLTIP;

	public static String	RESID_PROPERTY_PROFILE_LABEL;
	public static String	RESID_PROPERTY_PROFILE_TOOLTIP;
	

	// Property sheet values: SubSystems
	public static String	RESID_PROPERTY_SUBSYSTEM_TYPE_VALUE;

	public static String	RESID_PROPERTY_USERID_LABEL;
	public static String	RESID_PROPERTY_USERID_TOOLTIP;
	
	public static String	RESID_PROPERTY_PORT_LABEL;
	public static String	RESID_PROPERTY_PORT_TOOLTIP;

	public static String	RESID_PROPERTY_CONNECTED_TOOLTIP;
	public static String	RESID_PROPERTY_CONNECTED_LABEL;

	public static String	RESID_PROPERTY_VRM_LABEL;
	public static String	RESID_PROPERTY_VRM_TOOLTIP;

	// Property sheet values: Filter Pools
	public static String	RESID_PROPERTY_FILTERPOOL_TYPE_VALUE;

	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_TYPE_VALUE;

	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPOOL_LABEL;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPOOL_TOOLTIP;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPROFILE_LABEL;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPROFILE_TOOLTIP;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_RELATEDCONNECTION_LABEL;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_RELATEDCONNECTION_TOOLTIP;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_IS_CONNECTIONPRIVATE_LABEL;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_IS_CONNECTIONPRIVATE_TOOLTIP;

	// Property sheet values: Filters
	public static String	RESID_PROPERTY_FILTERTYPE_LABEL;
	public static String	RESID_PROPERTY_FILTERTYPE_VALUE;
	public static String	RESID_PROPERTY_FILTERTYPE_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILTERSTRING_LABEL;
	public static String	RESID_PROPERTY_FILTERSTRING_VALUE;
	public static String	RESID_PROPERTY_FILTERSTRING_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILTERSTRINGS_LABEL;
	public static String	RESID_PROPERTY_FILTERSTRINGS_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILTERSTRINGS_COUNT_LABEL;
	public static String	RESID_PROPERTY_FILTERSTRINGS_COUNT_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILTERPARENTFILTER_LABEL;
	public static String	RESID_PROPERTY_FILTERPARENTFILTER_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILTERPARENTPOOL_LABEL;
	public static String	RESID_PROPERTY_FILTERPARENTPOOL_TOOLTIP;

	public static String	RESID_PROPERTY_FILTERS_LABEL;
	public static String	RESID_PROPERTY_FILTERS_DESCRIPTION;
	
	// Property sheet values: Files
	public static String	RESID_PROPERTY_FILE_TYPE_FILE_VALUE;
	public static String	RESID_PROPERTY_FILE_TYPE_FOLDER_VALUE;
	public static String	RESID_PROPERTY_FILE_TYPE_ROOT_VALUE;

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

	public static String	RESID_PROPERTY_FILE_PATH_LABEL;
	public static String	RESID_PROPERTY_FILE_PATH_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILE_LASTMODIFIED_LABEL;
	public static String	RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILE_SIZE_LABEL;
	public static String	RESID_PROPERTY_FILE_SIZE_TOOLTIP;

	public static String	RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL;
	public static String	RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP;

	public static String	RESID_PROPERTY_FILE_CLASSIFICATION_LABEL;
	public static String	RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP;

	public static String	RESID_PROPERTY_FILE_READONLY_LABEL;
	public static String	RESID_PROPERTY_FILE_READONLY_TOOLTIP;

	public static String	RESID_PROPERTY_FILE_READABLE_LABEL;
	public static String	RESID_PROPERTY_FILE_READABLE_TOOLTIP;

	public static String	RESID_PROPERTY_FILE_WRITABLE_LABEL;
	public static String	RESID_PROPERTY_FILE_WRITABLE_TOOLTIP;

	public static String	RESID_PROPERTY_FILE_HIDDEN_LABEL;
	public static String	RESID_PROPERTY_FILE_HIDDEN_TOOLTIP;

	// search result properties
	public static String	RESID_PROPERTY_SEARCH_LINE_LABEL;
	public static String	RESID_PROPERTY_SEARCH_LINE_TOOLTIP;
	//public static String	RESID_PROPERTY_SEARCH_CHAR_END_LABEL;
	//public static String	RESID_PROPERTY_SEARCH_CHAR_END_TOOLTIP;
	
	

	// shell status properties
	public static String	RESID_PROPERTY_SHELL_STATUS_LABEL;
	public static String	RESID_PROPERTY_SHELL_STATUS_TOOLTIP;
	public static String	RESID_PROPERTY_SHELL_CONTEXT_LABEL;
	public static String	RESID_PROPERTY_SHELL_CONTEXT_TOOLTIP;
	
	public static String	RESID_PROPERTY_SHELL_STATUS_ACTIVE_VALUE;
	public static String	RESID_PROPERTY_SHELL_STATUS_INACTIVE_VALUE;

	// error properties
	public static String	RESID_PROPERTY_ERROR_FILENAME_LABEL;
	public static String	RESID_PROPERTY_ERROR_FILENAME_TOOLTIP;

	public static String	RESID_PROPERTY_ERROR_LINENO_LABEL;
	public static String	RESID_PROPERTY_ERROR_LINENO_TOOLTIP;

	// Property sheet values: Messages
	public static String	RESID_PROPERTY_MESSAGE_TYPE_VALUE;

	// Property sheet values: Categories in Team view
	public static String	RESID_PROPERTY_TEAM_CATEGORY_TYPE_VALUE;
	public static String	RESID_PROPERTY_TEAM_SSFACTORY_TYPE_VALUE;
	public static String	RESID_PROPERTY_TEAM_USERACTION_TYPE_VALUE;
	public static String	RESID_PROPERTY_TEAM_COMPILETYPE_TYPE_VALUE;
	public static String	RESID_PROPERTY_TEAM_COMPILECMD_TYPE_VALUE;

	// Property sheet values: Compile types
	public static String	RESID_PROPERTY_COMPILETYPE_TYPES_LABEL;
	public static String	RESID_PROPERTY_COMPILETYPE_TYPES_DESCRIPTION;

	// Miscellaneous / common
	public static String	RESID_PROPERTY_ORIGIN_LABEL;
	public static String	RESID_PROPERTY_ORIGIN_TOOLTIP;
	public static String	RESID_PROPERTY_COMMAND_LABEL;
	public static String	RESID_PROPERTY_COMMAND_TOOLTIP;
	public static String	RESID_PROPERTY_COMMENT_LABEL;
	public static String	RESID_PROPERTY_COMMENT_TOOLTIP;
	
	public static String	RESID_PROPERTY_LINKINGACTION_TEXT;
	public static String	RESID_PROPERTY_LINKINGACTION_TOOLTIP;

	public static String    RESID_SCRATCHPAD;
	public static String    RESID_REMOTE_SCRATCHPAD;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, SystemViewResources.class);
	}
}