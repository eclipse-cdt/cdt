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
package org.eclipse.rse.internal.subsystems.files.core;

public interface ISystemFileMessageIds {

	public static final String MSG_FILE_CANNOT_BE_SAVED = "RSEF5003"; //$NON-NLS-1$
	public static final String MSG_FOLDER_UNREADABLE = "RSEF5020"; //$NON-NLS-1$
	
	public static final String MSG_VALIDATE_NAME_EMPTY    = "RSEG1006"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_NAME_NOTUNIQUE= "RSEG1007"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_NAME_NOTVALID = "RSEG1008"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_RENAME_EMPTY    = "RSEG1012"; //MSG_VALIDATE_PREFIX + "ReName.Required"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_RENAME_NOTUNIQUE= "RSEG1010"; //MSG_VALIDATE_PREFIX + "ReName.NotUnique"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_RENAME_NOTVALID = "RSEG1011"; //MSG_VALIDATE_PREFIX + "ReName.NotValid"; //$NON-NLS-1$
 	public static final String MSG_VALIDATE_RENAME_OLDEQUALSNEW = "RSEG1009"; //MSG_VALIDATE_PREFIX+"ReName.OldEqualsNew"; //$NON-NLS-1$  	
	
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_EMPTY    = "RSEF1011"; //$NON-NLS-1$
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE= "RSEF1007"; //$NON-NLS-1$
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOTVALID = "RSEF1008"; //$NON-NLS-1$
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOINCLUDES = "RSEF1009";	 //$NON-NLS-1$
	public static final String FILEMSG_FILE_NOTFOUND   = "RSEF1006";  	 //$NON-NLS-1$

}
