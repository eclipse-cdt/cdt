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
package org.eclipse.rse.internal.services.local;

public interface ILocalMessageIds {
	public static final String FILEMSG_FILE_NOT_SAVED = "RSEF5006"; //$NON-NLS-1$
	
	public static final String FILEMSG_ARCHIVE_CORRUPTED = "RSEG1122"; //$NON-NLS-1$
	public static final String MSG_FOLDER_INUSE = "RSEG1150"; //$NON-NLS-1$
	public static final String MSG_FILE_INUSE = "RSEG1151"; //$NON-NLS-1$

	public static final String FILEMSG_CREATE_VIRTUAL_FAILED = "RSEF1124"; //$NON-NLS-1$
	public static final String FILEMSG_DELETE_VIRTUAL_FAILED = "RSEF1125"; //$NON-NLS-1$
	
	public static final String FILEMSG_RENAME_FILE_FAILED  = "RSEF1301"; //$NON-NLS-1$
    public static final String FILEMSG_COPY_FILE_FAILED  = "RSEF1306"; //$NON-NLS-1$
}
