/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *  David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 *  David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *  David McKnight   (IBM)        - [223204] [cleanup] fix broken nls strings in files.ui and others
 *******************************************************************************/
package org.eclipse.rse.internal.importexport;

import org.eclipse.osgi.util.NLS;

public class RemoteImportExportResources extends NLS {
	private static String BUNDLE_NAME = "org.eclipse.rse.internal.importexport.RemoteImportExportResources"; //$NON-NLS-1$
	public static String IMPORT_EXPORT_DESCRIPTION_FILE_DIALOG_TITLE;
	public static String IMPORT_EXPORT_DESCRIPTION_FILE_DIALOG_MESSAGE;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_ABSOLUTE;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_EXISTING_CONTAINER;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_NO_CONTAINER;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_INVALID_EXTENSION;
	public static String IMPORT_EXPORT_ERROR_CREATE_FILES_FAILED;
	public static String IMPORT_EXPORT_ERROR_CREATE_FILE_FAILED;
	public static String IMPORT_EXPORT_EXPORT_ACTION_DELEGATE_TITLE;
	public static String IMPORT_EXPORT_IMPORT_ACTION_DELEGATE_TITLE;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_READ;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_CLOSE;
	public static String WizardDataTransfer_existsQuestion;
	public static String WizardDataTransfer_overwriteNameAndPathQuestion;
	public static String Question;
	
	
	public static  String FILEMSG_COPY_ROOT;
	
	public static  String FILEMSG_IMPORT_ERROR;
	public static  String FILEMSG_IMPORT_PROBLEMS;
	public static  String FILEMSG_IMPORT_SELF;
	public static  String FILEMSG_EXPORT_ERROR;
	public static  String FILEMSG_EXPORT_PROBLEMS;
	public static  String FILEMSG_NOT_WRITABLE;
		
	public static  String FILEMSG_TARGET_EXISTS;
	public static  String FILEMSG_TARGET_EXISTS_DETAILS;
	public static  String FILEMSG_FOLDER_IS_FILE;
	public static  String FILEMSG_FOLDER_IS_FILE_DETAILS;
	public static  String FILEMSG_DESTINATION_CONFLICTING;
	public static  String FILEMSG_DESTINATION_CONFLICTING_DETAILS;
	public static  String FILEMSG_SOURCE_IS_FILE;
	public static  String FILEMSG_SOURCE_IS_FILE_DETAILS;
	public static  String FILEMSG_SOURCE_EMPTY;
	public static  String FILEMSG_SOURCE_EMPTY_DETAILS;
	public static  String FILEMSG_EXPORT_FAILED;
	public static  String FILEMSG_EXPORT_NONE_SELECTED;
	public static  String FILEMSG_EXPORT_NONE_SELECTED_DETAILS;
	public static  String FILEMSG_DESTINATION_EMPTY;
	public static  String FILEMSG_DESTINATION_EMPTY_DETAILS;
	public static  String FILEMSG_IMPORT_FAILED;
	public static  String FILEMSG_IMPORT_NONE_SELECTED;
	public static  String FILEMSG_IMPORT_NONE_SELECTED_DETAILS;
	public static  String FILEMSG_IMPORT_FILTERING;
	
    public static String FILEMSG_CREATE_FOLDER_FAILED;
    public static String FILEMSG_CREATE_FOLDER_FAILED_DETAILS;
    
	// file import/export messages
	public static String MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION;
	public static String MSG_IMPORT_EXPORT_UNEXPECTED_EXCEPTION;
	public static String MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION_DETAILS;
	public static String MSG_IMPORT_EXPORT_UNEXPECTED_EXCEPTION_DETAILS;

    
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, RemoteImportExportResources.class);
	}
}
