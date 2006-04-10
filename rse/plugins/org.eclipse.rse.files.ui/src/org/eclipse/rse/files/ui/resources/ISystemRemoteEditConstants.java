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

package org.eclipse.rse.files.ui.resources;

public interface ISystemRemoteEditConstants {

	
	
	// Constants for remote editing
	public static final String REMOTE_FILE_OBJECT_KEY 	= "remote_file_object_key";

	public static final String REMOTE_FILE_SUBSYSTEM_KEY  = "remote_file_subsystem_key";
	public static final String REMOTE_FILE_PATH_KEY 		= "remote_file_path_key";

	public static final String REMOTE_FILE_MODIFIED_STAMP  = "remote_file_modified_stamp";
	public static final String REMOTE_FILE_BINARY_TRANSFER = "remote_file_binary_transfer";
	public static final String TEMP_FILE_DIRTY 			 = "temp_file_dirty";
	public static final String TEMP_FILE_READONLY        = "temp_file_readonly";

	public static final String DOWNLOAD_FILE_MODIFIED_STAMP = "download_file_modified_stamp";
	// for mounted mappings
	public static final String REMOTE_FILE_MOUNTED = "remote_file_mounted";
	public static final String RESOLVED_MOUNTED_REMOTE_FILE_PATH_KEY = "resolved_mounted_remote_file_path_key";
	public static final String RESOLVED_MOUNTED_REMOTE_FILE_HOST_KEY = "resolved_mounted_remote_file_host_key";
	
	
	// Constants related to how the editor will set the document content
	public static final String LOAD_TYPE_KEY = "load_type_key";
	public static final String LOAD_TYPE_USE_STRING = "load_type_use_string";
	
	
	// Universal remote editing profile
	public static final String DEFAULT_EDITOR_PROFILE = "default";
	public static final String UNIVERSAL_EDITOR_PROFILE = "universal";
	public static final String UNIVERSAL_LOCAL_EDITOR_PROFILE = "universallocal";
	
	
	// Local relative directory for various editor actions
	public static final String EDITOR_COMPARE_LOCATION	=	"/compare/";
	public static final String EDITOR_GET_FILE_LOCATION	=	"/get/";
}