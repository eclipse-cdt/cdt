/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved.
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
 * Javier Montalvo Orus (Symbian) - Added Externalized Strings
 * Javier Montalvo Orus (Symbian) - [191048] Remote files locally listed and being removed by other users should be reported as missing
 ********************************************************************************/

package org.eclipse.rse.internal.services.files.ftp;

import org.eclipse.osgi.util.NLS;

public class FTPServiceResources extends NLS 
{
	private static String BUNDLE_NAME = "org.eclipse.rse.internal.services.files.ftp.FTPServiceResources";//$NON-NLS-1$
	public static String FTP_File_Service_Copy_Not_Supported;
	public static String FTP_File_Service_Deleting_Task;
	public static String FTP_File_Service_Listing_Job;
	public static String FTP_File_Service_Listing_Job_Success;
	public static String FTP_File_Service_Monitor_Format;
	public static String FTP_File_Service_Name;
	public static String FTP_File_Service_Description;
	public static String FTPService_FTP_File_Service_Not_Found;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, FTPServiceResources.class);
	}
}