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
 *******************************************************************************/

package org.eclipse.rse.internal.services.local;

import org.eclipse.osgi.util.NLS;

public class LocalServiceResources extends NLS 
{
	private static String BUNDLE_NAME = "org.eclipse.rse.internal.services.local.LocalServiceResources";//$NON-NLS-1$

	public static String Local_File_Service_Name;
	public static String Local_Process_Service_Name;
	public static String Local_Shell_Service_Name;
	
	public static String Local_File_Service_Description;
	public static String Local_Process_Service_Description;
	public static String Local_Shell_Service_Description;
	

	public static String FILEMSG_FILE_NOT_SAVED;
	public static String FILEMSG_FILE_NOT_SAVED_DETAILS;
	
	public static String FILEMSG_ARCHIVE_CORRUPTED;
	public static String FILEMSG_ARCHIVE_CORRUPTED_DETAILS;
	
	public static String FILEMSG_CREATE_VIRTUAL_FAILED;
	public static String FILEMSG_CREATE_VIRTUAL_FAILED_DETAILS;
	
	public static String FILEMSG_DELETE_VIRTUAL_FAILED;
	public static String FILEMSG_DELETE_VIRTUAL_FAILED_DETAILS;
	
    public static  String FILEMSG_RENAME_FILE_FAILED;
    public static  String FILEMSG_RENAME_FILE_FAILED_DETAILS;
    
    public static  String FILEMSG_COPY_FILE_FAILED;
    public static  String FILEMSG_COPY_FILE_FAILED_DETAILS;
	
	public static String FILEMSG_DELETING;
	public static String FILEMSG_COPYING;
	
	public static String FILEMSG_ERROR_DOWNLOAD_SIZE;
	public static String FILEMSG_ERROR_UPLOAD_SIZE;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, LocalServiceResources.class);
	}
}
