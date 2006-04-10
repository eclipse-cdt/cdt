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
import org.eclipse.jface.viewers.IBasicPropertyConstants;
/**
 * Constants that are the key values used to identify properties that populate the
 * Property Sheet viewer.
 */
public interface ISystemPropertyConstants extends IBasicPropertyConstants
{
	public static final String P_PREFIX = org.eclipse.rse.ui.ISystemIconConstants.PREFIX;
	// GENERIC / COMMON
	public static final String P_TYPE     = P_PREFIX+"type";	
	public static final String P_NEWNAME  = P_PREFIX+"newName";		
	public static final String P_ERROR    = P_PREFIX+"error";	
	public static final String P_OK       = P_PREFIX+"ok";
	public static final String P_FILTERSTRING = P_PREFIX+"filterString";
	public static final String P_NBRCHILDREN  = P_PREFIX+"nbrChildren";

	// CONNECTION PROPERTIES
	public static final String P_PROFILE       = P_PREFIX+"profile";	
	public static final String P_SYSTEMTYPE    = P_PREFIX+"systemType";	
	public static final String P_HOSTNAME      = P_PREFIX+"hostname";
	public static final String P_DEFAULTUSERID = P_PREFIX+"defaultuserid";	
	public static final String P_DESCRIPTION   = P_PREFIX+"description";		
	
	// FILTER POOL PROPERTIES
	//public static final String P_IS_SHARABLE = P_PREFIX+"sharable"; // transient	

	// FILTER PROPERTIES
	public static final String P_FILTERSTRINGS = P_PREFIX+"filterstrings";
	public static final String P_FILTERSTRINGS_COUNT = P_PREFIX+"filterstringsCount";	
	public static final String P_PARENT_FILTER       = P_PREFIX+"filterParent";		
	public static final String P_PARENT_FILTERPOOL   = P_PREFIX+"filterParentPool";			
	public static final String P_RELATED_CONNECTION  = P_PREFIX+"filterRelatedConnection";
	public static final String P_IS_CONNECTION_PRIVATE  = P_PREFIX+"filterConnectionPrivate";
		
	// FILE PROPERTIES
	public static final String P_FILE_LASTMODIFIED = P_PREFIX+"file.lastmodified";
	public static final String P_FILE_SIZE         = P_PREFIX+"file.size";	
	public static final String P_FILE_PATH         = P_PREFIX+"file.path";	
	public static final String P_FILE_CANONICAL_PATH = P_PREFIX+"file.canonicalpath";	
	public static final String P_FILE_CLASSIFICATION= P_PREFIX+"file.classification";
	public static final String P_FILE_READONLY     = P_PREFIX+"file.readonly";
	public static final String P_FILE_READABLE     = P_PREFIX+"file.readable";
	public static final String P_FILE_WRITABLE     = P_PREFIX+"file.writable";
	public static final String P_FILE_HIDDEN       = P_PREFIX+"file.hidden";
	
	// SEARCH LOCATION PROPERTIES
	public static final String P_SEARCH_LINE  = P_PREFIX+"search.line";
	//public static final String P_SEARCH_CHAR_END    = P_PREFIX+"search.char.end";

	// ARCHIVE FILE PROPERTIES
	public static final String P_ARCHIVE_EXPANDEDSIZE 	= P_PREFIX+"archive.expandedsize";
	public static final String P_ARCHIVE_COMMENT 		= P_PREFIX+"archive.comment";
	
	// VIRTUAL FILE PROPERTIES
	public static final String P_VIRTUAL_COMPRESSEDSIZE 	= P_PREFIX+"virtual.compressedsize";
	public static final String P_VIRTUAL_COMMENT 			= P_PREFIX+"virtual.comment";
	public static final String P_VIRTUAL_COMPRESSIONRATIO 	= P_PREFIX+"virtual.compressionratio";
	public static final String P_VIRTUAL_COMPRESSIONMETHOD 	= P_PREFIX+"virtual.compressionmethod";
	
	// SHELL PROPERTIES
	public static final String P_SHELL_STATUS = P_PREFIX+"shell.status";
	public static final String P_SHELL_CONTEXT = P_PREFIX+"shell.context";
	
	// ERROR PROPERTIES
	public static final String P_ERROR_FILENAME    = P_PREFIX+"error.filename";
	public static final String P_ERROR_LINENO      = P_PREFIX+"error.lineno";

	// USER ACTION PROPERTIES
	public static final String P_USERACTION_DOMAIN = P_PREFIX+"action.domain";

	// COMPILE TYPE PROPERTIES
	public static final String P_COMPILETYPE_TYPES = P_PREFIX+"compiletypes.types";
				
	// MISCELLANEOUS PROPERTIES	
	public static final String P_USERID   = P_PREFIX+"userid";
	public static final String P_PASSWORD = P_PREFIX+"password";
	public static final String P_CCSID    = P_PREFIX+"ccsid";
	public static final String P_VRM      = P_PREFIX+"vrm";
	public static final String P_ENVLIST  = P_PREFIX+"envlist"; // indexed
	public static final String P_FILTERS  = P_PREFIX+"filters"; // indexed
	public static final String P_FILTER   = P_PREFIX+"filter"; // scalar
	public static final String P_IS_CONNECTED = P_PREFIX+"connected"; // transient
	public static final String P_IS_ACTIVE    = P_PREFIX+"active";    // for profiles
	public static final String P_HAS_CHILDREN = P_PREFIX+"hasChildren"; // see SystemElementViewerAdapter
	public static final String P_PORT     = P_PREFIX+"port";
	public static final String P_ORIGIN   = P_PREFIX+"origin";
	public static final String P_VENDOR   = P_PREFIX+"vendor";
	public static final String P_COMMAND  = P_PREFIX+"command";
	public static final String P_COMMENT  = P_PREFIX+"comment";
}