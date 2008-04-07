/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *******************************************************************************/
package org.eclipse.rse.internal.importexport;

/**
 * Interface containing contstants required for import and export.
 */
public interface IRemoteImportExportConstants {
	/**
	 * Remote file import description file extension, "rimpfd".
	 */
	public static final String REMOTE_FILE_IMPORT_DESCRIPTION_FILE_EXTENSION = "rimpfd"; //$NON-NLS-1$
	/**
	 * An array of import description file extensions.
	 */
	public static final String[] REMOTE_IMPORT_DESCRIPTION_FILE_EXTENSIONS = { REMOTE_FILE_IMPORT_DESCRIPTION_FILE_EXTENSION };
	/**
	 * Remote file export description file extension, "rexpfd".
	 */
	public static final String REMOTE_FILE_EXPORT_DESCRIPTION_FILE_EXTENSION = "rexpfd"; //$NON-NLS-1$
	/**
	 * Remote jar export description file extension, "rmtjardesc".
	 */
	public static final String REMOTE_JAR_EXPORT_DESCRIPTION_FILE_EXTENSION = "rmtjardesc"; //$NON-NLS-1$
	/**
	 * An array of export description file extensions.
	 */
	public static final String[] REMOTE_EXPORT_DESCRIPTION_FILE_EXTENSIONS = { REMOTE_FILE_EXPORT_DESCRIPTION_FILE_EXTENSION, REMOTE_JAR_EXPORT_DESCRIPTION_FILE_EXTENSION };
	
	
	// message ids
	// -------------------------	
	// IMPORT/EXPORT MESSAGES...
	// -------------------------
	public static final String FILEMSG_COPY_ROOT = "RSEF8050"; //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_ERROR = "RSEF8052"; //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_PROBLEMS = "RSEF8054"; //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_SELF = "RSEF8056";	 //$NON-NLS-1$
	public static final String FILEMSG_EXPORT_ERROR = "RSEF8057"; //$NON-NLS-1$
	public static final String FILEMSG_EXPORT_PROBLEMS = "RSEF8058";	 //$NON-NLS-1$
	public static final String FILEMSG_NOT_WRITABLE = "RSEF8059"; //$NON-NLS-1$
		
	public static final String FILEMSG_TARGET_EXISTS = "RSEF8060"; //$NON-NLS-1$
	public static final String FILEMSG_FOLDER_IS_FILE = "RSEF8061";	 //$NON-NLS-1$
	public static final String FILEMSG_DESTINATION_CONFLICTING = "RSEF8062";	 //$NON-NLS-1$
	public static final String FILEMSG_SOURCE_IS_FILE = "RSEF8063";	 //$NON-NLS-1$
	public static final String FILEMSG_SOURCE_EMPTY = "RSEF8066";	 //$NON-NLS-1$
	public static final String FILEMSG_EXPORT_FAILED = "RSEF8067";	 //$NON-NLS-1$
	public static final String FILEMSG_EXPORT_NONE_SELECTED = "RSEF8068";		 //$NON-NLS-1$
	public static final String FILEMSG_DESTINATION_EMPTY = "RSEF8069";	 //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_FAILED = "RSEF8070";		 //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_NONE_SELECTED = "RSEF8071";	 //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_FILTERING = "RSEF8072";	 //$NON-NLS-1$
	
	// file import/export messages
	public static final String MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION = "RSEF5101";  //$NON-NLS-1$
	public static final String MSG_IMPORT_EXPORT_UNEXPECTED_EXCEPTION = "RSEF5102"; //$NON-NLS-1$
	
	
    public static final String FILEMSG_CREATE_FOLDER_FAILED  = "RSEF1304"; //$NON-NLS-1$
    
    
}
