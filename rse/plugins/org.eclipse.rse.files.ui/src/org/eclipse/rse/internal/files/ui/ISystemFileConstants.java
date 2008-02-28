/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 * David McKnight   (IBM)        - [216252] cleaning up message ids and strings
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui;


/**
 * Constants used throughout the System plugin
 */
public interface ISystemFileConstants 
{
    // --------------------------------
	// Message Ids
	// -------------------------------
	// Remote editing messages
	public static final String MSG_DOWNLOAD_NO_WRITE = "RSEF5002"; //$NON-NLS-1$
	public static final String MSG_DOWNLOAD_ALREADY_OPEN_IN_EDITOR = "RSEF5009"; //$NON-NLS-1$
	public static final String MSG_UPLOAD_FILE_EXISTS = "RSEF5012"; //$NON-NLS-1$
		
	public static final String MSG_FOLDER_UNREADABLE = "RSEF5020"; //$NON-NLS-1$
	public static final String MSG_ERROR_FILE_NOTFOUND = "RSEG1106"; 		 //$NON-NLS-1$
		
    // Remote File Exception Messages
  	public static final String FILEMSG_SECURITY_ERROR = "RSEF1001"; //$NON-NLS-1$
  	public static final String FILEMSG_IO_ERROR = "RSEF1002"; //$NON-NLS-1$
  	
  	public static final String FILEMSG_FOLDER_NOTEMPTY = "RSEF1003"; //$NON-NLS-1$
  	public static final String FILEMSG_FOLDER_NOTFOUND = "RSEF1004"; //$NON-NLS-1$
  	public static final String FILEMSG_FOLDER_NOTFOUND_WANTTOCREATE = "RSEF1005";  	 //$NON-NLS-1$
  	public static final String FILEMSG_FILE_NOTFOUND   = "RSEF1006";  	 //$NON-NLS-1$

	public static final String MSG_VALIDATE_PATH_EMPTY    = "RSEG1032"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_PATH_NOTUNIQUE= "RSEG1033"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_PATH_NOTVALID = "RSEG1034"; //$NON-NLS-1$
		
    // --------------------------
    // UNIVERSAL FILE MESSAGES...
    // --------------------------	
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_EMPTY    = "RSEF1011"; //$NON-NLS-1$
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE= "RSEF1007"; //$NON-NLS-1$
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOTVALID = "RSEF1008"; //$NON-NLS-1$
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOINCLUDES = "RSEF1009";	 //$NON-NLS-1$
    public static final String FILEMSG_DELETE_FILE_FAILED  = "RSEF1300"; //$NON-NLS-1$
    public static final String FILEMSG_RENAME_FILE_FAILED  = "RSEF1301"; //$NON-NLS-1$
    public static final String FILEMSG_CREATE_FILE_FAILED  = "RSEF1302"; //$NON-NLS-1$
    public static final String FILEMSG_CREATE_FILE_FAILED_EXIST  = "RSEF1303"; //$NON-NLS-1$
    public static final String FILEMSG_CREATE_FOLDER_FAILED  = "RSEF1304"; //$NON-NLS-1$
    public static final String FILEMSG_CREATE_FOLDER_FAILED_EXIST  = "RSEF1309"; //$NON-NLS-1$
    public static final String FILEMSG_CREATE_RESOURCE_NOTVISIBLE  = "RSEF1310"; //$NON-NLS-1$
    public static final String FILEMSG_RENAME_RESOURCE_NOTVISIBLE  = "RSEF1311"; //$NON-NLS-1$
	public static final String FILEMSG_ERROR_NOFILETYPES = "RSEF1010"; //$NON-NLS-1$
    public static final String FILEMSG_COPY_FILE_FAILED  = "RSEF1306"; //$NON-NLS-1$
    public static final String FILEMSG_MOVE_FILE_FAILED  = "RSEF1307"; //$NON-NLS-1$
    public static final String FILEMSG_MOVE_TARGET_EQUALS_SOURCE  = "RSEF1308"; //$NON-NLS-1$
	public static final String FILEMSG_MOVE_TARGET_DESCENDS_FROM_SOURCE = "RSEF1312"; //$NON-NLS-1$
	public static final String FILEMSG_DELETING = "RSEF1315"; //$NON-NLS-1$	
    public static final String FILEMSG_MOVE_TARGET_EQUALS_PARENT_OF_SOURCE = "RSEF1314"; //$NON-NLS-1$
	public static final String FILEMSG_MOVE_FILTER_NOT_VALID = "RSEF1313"; //$NON-NLS-1$
	
	public static final String FILEMSG_RENAME_INTERRUPTED = "RSEG1246"; //$NON-NLS-1$
	public static final String FILEMSG_DELETE_INTERRUPTED = "RSEG1247"; //$NON-NLS-1$
	public static final String FILEMSG_COPY_INTERRUPTED = "RSEG1248"; //$NON-NLS-1$
	public static final String FILEMSG_MOVE_INTERRUPTED = "RSEG1245"; //$NON-NLS-1$

	// cache preferences
	public static final String MSG_CACHE_UPLOAD_BEFORE_DELETE = "RSEF6101"; //$NON-NLS-1$
	public static final String MSG_CACHE_UNABLE_TO_SYNCH = "RSEF6102"; //$NON-NLS-1$
	
	public static final String MSG_ERROR_FILENAME_INVALID = "RSEF6002"; //$NON-NLS-1$
	
	
	// remote search messages
	public static final String MSG_REMOTE_SEARCH_INVALID_REGEX = "RSEG1601"; //$NON-NLS-1$
	
	
    // --------------------------------
	// INFO-POPS FOR UNIVERSAL FILE
	// -------------------------------
	
	public static final String NEW_FILE_WIZARD     = "ufwf0000"; //$NON-NLS-1$
	public static final String NEW_FOLDER_WIZARD   = "ufwr0000"; //$NON-NLS-1$
	public static final String NEW_FILE_ACTION     = "ufaf0000"; //$NON-NLS-1$
	public static final String NEW_FOLDER_ACTION   = "ufar0000"; //$NON-NLS-1$

}
