/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * David McKnight (IBM) - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * Martin Oberhuber (Wind River) - [230298][api][breaking] ISystemPropertyConstants should not extend IBasicPropertyConstants
 *******************************************************************************/

package org.eclipse.rse.ui.view;
/**
 * Constants that are the key values used to identify properties that populate the
 * Property Sheet viewer.
 */
public interface ISystemPropertyConstants
{
	public static final String P_PREFIX = "org.eclipse.rse.ui."; //$NON-NLS-1$
	// GENERIC / COMMON
	public static final String P_TYPE     = P_PREFIX+"type";	 //$NON-NLS-1$
	public static final String P_NEWNAME  = P_PREFIX+"newName";		 //$NON-NLS-1$
	public static final String P_ERROR    = P_PREFIX+"error";	 //$NON-NLS-1$
	public static final String P_OK       = P_PREFIX+"ok"; //$NON-NLS-1$
	public static final String P_FILTERSTRING = P_PREFIX+"filterString"; //$NON-NLS-1$
	public static final String P_NBRCHILDREN  = P_PREFIX+"nbrChildren"; //$NON-NLS-1$

	// CONNECTION PROPERTIES
	public static final String P_PROFILE       = P_PREFIX+"profile";	 //$NON-NLS-1$
	public static final String P_SYSTEMTYPE    = P_PREFIX+"systemType";	 //$NON-NLS-1$
	public static final String P_HOSTNAME      = P_PREFIX+"hostname"; //$NON-NLS-1$
	public static final String P_DEFAULTUSERID = P_PREFIX+"defaultuserid";	 //$NON-NLS-1$
	public static final String P_DESCRIPTION   = P_PREFIX+"description";		 //$NON-NLS-1$

	// FILTER POOL PROPERTIES
	//public static final String P_IS_SHARABLE = P_PREFIX+"sharable"; // transient

	// FILTER PROPERTIES
	public static final String P_FILTERSTRINGS = P_PREFIX+"filterstrings"; //$NON-NLS-1$
	public static final String P_FILTERSTRINGS_COUNT = P_PREFIX+"filterstringsCount";	 //$NON-NLS-1$
	public static final String P_PARENT_FILTER       = P_PREFIX+"filterParent";		 //$NON-NLS-1$
	public static final String P_PARENT_FILTERPOOL   = P_PREFIX+"filterParentPool";			 //$NON-NLS-1$
	public static final String P_RELATED_CONNECTION  = P_PREFIX+"filterRelatedConnection"; //$NON-NLS-1$
	public static final String P_IS_CONNECTION_PRIVATE  = P_PREFIX+"filterConnectionPrivate"; //$NON-NLS-1$

	// FILE PROPERTIES
	public static final String P_FILE_LASTMODIFIED = P_PREFIX+"file.lastmodified"; //$NON-NLS-1$
	public static final String P_FILE_SIZE         = P_PREFIX+"file.size";	 //$NON-NLS-1$
	public static final String P_FILE_PATH         = P_PREFIX+"file.path";	 //$NON-NLS-1$
	public static final String P_FILE_CANONICAL_PATH = P_PREFIX+"file.canonicalpath";	 //$NON-NLS-1$
	public static final String P_FILE_CLASSIFICATION= P_PREFIX+"file.classification"; //$NON-NLS-1$
	/**
	 * @since 3.0
	 */
	public static final String P_FILE_EXTENSION = P_PREFIX+"file.extension"; //$NON-NLS-1$
	public static final String P_FILE_READONLY     = P_PREFIX+"file.readonly"; //$NON-NLS-1$
	public static final String P_FILE_READABLE     = P_PREFIX+"file.readable"; //$NON-NLS-1$
	public static final String P_FILE_WRITABLE     = P_PREFIX+"file.writable"; //$NON-NLS-1$
	public static final String P_FILE_HIDDEN       = P_PREFIX+"file.hidden"; //$NON-NLS-1$

	/**
	 * @since 3.0
	 */
	public static final String P_FILE_PERMISSIONS  = P_PREFIX+"file.permissions"; //$NON-NLS-1$
	/**
	 * @since 3.0
	 */
	public static final String P_FILE_OWNER        = P_PREFIX+"file.owner"; //$NON-NLS-1$
	/**
	 * @since 3.0
	 */
	public static final String P_FILE_GROUP        = P_PREFIX+"file.group"; //$NON-NLS-1$




	// SEARCH LOCATION PROPERTIES
	public static final String P_SEARCH_LINE  = P_PREFIX+"search.line"; //$NON-NLS-1$
	//public static final String P_SEARCH_CHAR_END    = P_PREFIX+"search.char.end";

	// ARCHIVE FILE PROPERTIES
	public static final String P_ARCHIVE_EXPANDEDSIZE 	= P_PREFIX+"archive.expandedsize"; //$NON-NLS-1$
	public static final String P_ARCHIVE_COMMENT 		= P_PREFIX+"archive.comment"; //$NON-NLS-1$

	// VIRTUAL FILE PROPERTIES
	public static final String P_VIRTUAL_COMPRESSEDSIZE 	= P_PREFIX+"virtual.compressedsize"; //$NON-NLS-1$
	public static final String P_VIRTUAL_COMMENT 			= P_PREFIX+"virtual.comment"; //$NON-NLS-1$
	public static final String P_VIRTUAL_COMPRESSIONRATIO 	= P_PREFIX+"virtual.compressionratio"; //$NON-NLS-1$
	public static final String P_VIRTUAL_COMPRESSIONMETHOD 	= P_PREFIX+"virtual.compressionmethod"; //$NON-NLS-1$

	// SHELL PROPERTIES
	public static final String P_SHELL_STATUS = P_PREFIX+"shell.status"; //$NON-NLS-1$
	public static final String P_SHELL_CONTEXT = P_PREFIX+"shell.context"; //$NON-NLS-1$

	// ERROR PROPERTIES
	public static final String P_ERROR_FILENAME    = P_PREFIX+"error.filename"; //$NON-NLS-1$
	public static final String P_ERROR_LINENO      = P_PREFIX+"error.lineno"; //$NON-NLS-1$

	// USER ACTION PROPERTIES
	public static final String P_USERACTION_DOMAIN = P_PREFIX+"action.domain"; //$NON-NLS-1$

	// COMPILE TYPE PROPERTIES
	public static final String P_COMPILETYPE_TYPES = P_PREFIX+"compiletypes.types"; //$NON-NLS-1$

	// MISCELLANEOUS PROPERTIES
	public static final String P_USERID   = P_PREFIX+"userid"; //$NON-NLS-1$
	public static final String P_PASSWORD = P_PREFIX+"password"; //$NON-NLS-1$
	public static final String P_CCSID    = P_PREFIX+"ccsid"; //$NON-NLS-1$
	public static final String P_VRM      = P_PREFIX+"vrm"; //$NON-NLS-1$
	public static final String P_ENVLIST  = P_PREFIX+"envlist"; // indexed //$NON-NLS-1$
	public static final String P_FILTERS  = P_PREFIX+"filters"; // indexed //$NON-NLS-1$
	public static final String P_FILTER   = P_PREFIX+"filter"; // scalar //$NON-NLS-1$
	public static final String P_IS_CONNECTED = P_PREFIX+"connected"; // transient //$NON-NLS-1$
	public static final String P_IS_ACTIVE    = P_PREFIX+"active";    // for profiles //$NON-NLS-1$
	public static final String P_HAS_CHILDREN = P_PREFIX+"hasChildren"; // see SystemElementViewerAdapter //$NON-NLS-1$
	public static final String P_PORT     = P_PREFIX+"port"; //$NON-NLS-1$
	public static final String P_ORIGIN   = P_PREFIX+"origin"; //$NON-NLS-1$
	public static final String P_VENDOR   = P_PREFIX+"vendor"; //$NON-NLS-1$
	public static final String P_COMMAND  = P_PREFIX+"command"; //$NON-NLS-1$
	public static final String P_COMMENT  = P_PREFIX+"comment"; //$NON-NLS-1$
}
