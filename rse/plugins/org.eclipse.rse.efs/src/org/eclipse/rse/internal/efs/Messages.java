/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: Kevin Doyle.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.internal.efs;

import org.eclipse.osgi.util.NLS;

public class Messages {
	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.efs.messages"; //$NON-NLS-1$

	public static String RESOURCES_NOT_LOADED;
	public static String CONNECTION_NOT_FOUND;
	public static String NO_FILE_SUBSYSTEM;
	public static String COULD_NOT_CONNECT;
	public static String COULD_NOT_GET_REMOTE_FILE;
	public static String FILE_STORE_DOES_NOT_EXIST;
	public static String UNKNOWN_EXCEPTION;
	public static String FILE_NAME_EXISTS;
	public static String CANNOT_OPEN_STREAM_ON_FOLDER;
	public static String DELETE_FAILED;
	
	static {
		// initialize resource bundles
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
