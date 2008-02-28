/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 ********************************************************************************/
package org.eclipse.rse.internal.services.dstore;

public interface IDStoreMessageIds {

	   // Remote File Exception Messages
  	public static final String FILEMSG_SECURITY_ERROR = "RSEF1001"; //$NON-NLS-1$
  	public static final String FILEMSG_IO_ERROR = "RSEF1002"; //$NON-NLS-1$
  	
  	public static final String FILEMSG_FOLDER_NOTEMPTY = "RSEF1003"; //$NON-NLS-1$
  	public static final String FILEMSG_FOLDER_NOTFOUND = "RSEF1004"; //$NON-NLS-1$
  	public static final String FILEMSG_FOLDER_NOTFOUND_WANTTOCREATE = "RSEF1005";  	 //$NON-NLS-1$
  	public static final String FILEMSG_FILE_NOTFOUND   = "RSEF1006";  	 //$NON-NLS-1$

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
	
}
