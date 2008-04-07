/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * David McKnight   (IBM)        - [216252] use SimpleSystemMessage instead of getMessage()
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David McKnight   (IBM)        - [223204] [cleanup] fix broken nls strings in files.ui and others
 *******************************************************************************/

package org.eclipse.rse.internal.services.dstore;

import org.eclipse.osgi.util.NLS;

public class ServiceResources extends NLS
{
	private static String BUNDLE_NAME = "org.eclipse.rse.internal.services.dstore.ServiceResources";//$NON-NLS-1$

	public static String DStore_Shell_Service_Label;
	public static String DStore_Search_Service_Label;
	public static String DStore_File_Service_Label;
	public static String DStore_Process_Service_Label;
	
	public static String DStore_Shell_Service_Description;
	public static String DStore_Search_Service_Description;
	public static String DStore_File_Service_Description;
	public static String DStore_Process_Service_Description;
	
	public static String DStore_Service_ProgMon_Initializing_Message;
	public static String DStore_Service_Percent_Complete_Message;
	
    // Remote File Exception Messages
  	public static String FILEMSG_SECURITY_ERROR;
		
  	public static String FILEMSG_SECURITY_ERROR_DETAILS;

    // --------------------------
    // UNIVERSAL FILE MESSAGES...
    // --------------------------	
    public static  String FILEMSG_DELETE_FILE_FAILED;
    public static  String FILEMSG_RENAME_FILE_FAILED;
    public static  String FILEMSG_CREATE_FILE_FAILED;
    public static  String FILEMSG_CREATE_FILE_FAILED_EXIST;
    public static  String FILEMSG_CREATE_FOLDER_FAILED_EXIST;
    public static  String FILEMSG_COPY_FILE_FAILED;

    public static  String FILEMSG_DELETE_FILE_FAILED_DETAILS;
    public static  String FILEMSG_RENAME_FILE_FAILED_DETAILS;
    public static  String FILEMSG_CREATE_FILE_FAILED_DETAILS;
    public static  String FILEMSG_CREATE_FILE_FAILED_EXIST_DETAILS;
    public static  String FILEMSG_CREATE_FOLDER_FAILED_EXIST_DETAILS;
    public static  String FILEMSG_COPY_FILE_FAILED_DETAILS;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ServiceResources.class);
	}
}
