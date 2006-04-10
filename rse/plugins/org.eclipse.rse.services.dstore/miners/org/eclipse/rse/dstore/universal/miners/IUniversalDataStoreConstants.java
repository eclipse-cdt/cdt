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

package org.eclipse.rse.dstore.universal.miners;

import org.eclipse.rse.services.clientserver.IServiceConstants;

public interface IUniversalDataStoreConstants extends IServiceConstants
{



	/*
	 * Miner names, used for logging
	 */
	public static final String UNIVERSAL_FILESYSTEM_MINER = "UniversalFileSystemMiner";
	
	//
	// Universal File descriptors for DataStore DataElements
	//
	public static final String MESSAGE_DESCRIPTOR = "universal.message";

    public static final String UNIVERSAL_NODE_DESCRIPTOR = "universal.node";
    public static final String UNIVERSAL_TEMP_DESCRIPTOR = "universal.temp";
	public static final String UNIVERSAL_FILTER_DESCRIPTOR = "universal.FilterObject";
	public static final String UNIVERSAL_FILE_DESCRIPTOR = "universal.FileObject";
	public static final String UNIVERSAL_FOLDER_DESCRIPTOR = "universal.FolderObject";
	
	public static final String UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR = "universal.ArchiveFileObject";
	public static final String UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR = "universal.VirtualFileObject";
	public static final String UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR = "universal.VirtualFolderObject";

	
	//
	// Universal File Miner Commands
	//

	public static final String C_QUERY_ROOTS = "C_QUERY_ROOTS";
	public static final String C_QUERY_VIEW_ALL = "C_QUERY_VIEW_ALL";
	public static final String C_QUERY_VIEW_FILES = "C_QUERY_VIEW_FILES";
	public static final String C_QUERY_VIEW_FOLDERS = "C_QUERY_VIEW_FOLDERS";
	public static final String C_CREATE_FILE = "C_CREATE_FILE";
	public static final String C_CREATE_FOLDER = "C_CREATE_FOLDER";
	public static final String C_QUERY_GET_REMOTE_OBJECT="C_QUERY_GET_REMOTE_OBJECT";
	
	public static final String C_DELETE = "C_DELETE";
	public static final String C_DELETE_BATCH = "C_DELETE_BATCH";
	public static final String C_RENAME = "C_RENAME";
	public static final String C_COPY = "C_COPY";
	public static final String C_COPY_BATCH = "C_COPY_BATCH";
	
	// Download file command
	public static final String C_DOWNLOAD_FILE = "C_DOWNLOAD_FILE";
	
	// Query system encoding command
	public static final String C_SYSTEM_ENCODING = "C_SYSTEM_ENCODING";
	
	// Query unused port
	public static final String C_QUERY_UNUSED_PORT = "C_QUERY_UNUSED_PORT";
	
	// Qualified class name command and return type
	public static final String C_QUERY_QUALIFIED_CLASSNAME = "C_QUERY_QUALIFIED_CLASSNAME";
	public static final String TYPE_QUALIFIED_CLASSNAME = "fullClassName";
	

    	
	// Mode of transfer: text or binary
	public static final int TEXT_MODE = -1;
	public static final int BINARY_MODE = -2;
	
	
	// Download result types and download messages
	public static final String DOWNLOAD_RESULT_SUCCESS_TYPE = "universal.download.success";
	public static final String DOWNLOAD_RESULT_SUCCESS_MESSAGE = "successful";
	public static final String DOWNLOAD_RESULT_FILE_NOT_FOUND_EXCEPTION = "FileNotFoundException";
	public static final String DOWNLOAD_RESULT_UNSUPPORTED_ENCODING_EXCEPTION = "UnsupportedEncodingException";
	public static final String DOWNLOAD_RESULT_IO_EXCEPTION = "IOException";
	public static final String DOWNLOAD_RESULT_EXCEPTION = "Exception";
	public static final String DOWNLOAD_RESULT_UNEXPECTED_ERROR = "UnexpectedError";
	
	
	// Number of bytes in a kilobyte
	public static final int KB_IN_BYTES = 1024;
	
	// Number of kilobytes we want
	public static final int NUM_OF_KB = 40;
	
	// The size of file segments to read and send across connection (in bytes)
	public static final int BUFFER_SIZE = NUM_OF_KB * KB_IN_BYTES;
}