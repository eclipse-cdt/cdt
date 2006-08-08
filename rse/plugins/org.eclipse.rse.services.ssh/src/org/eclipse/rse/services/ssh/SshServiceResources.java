/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.services.ssh;

import org.eclipse.osgi.util.NLS;

public class SshServiceResources extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.rse.services.ssh.SshServiceResources"; //$NON-NLS-1$

	public static String SshPlugin_Unexpected_Exception;
	
	public static String SftpFileService_Description;

	public static String SftpFileService_Error_JschSessionLost;

	public static String SftpFileService_Msg_Progress;

	public static String SftpFileService_Name;

	public static String SshShellService_Description;

	public static String SshShellService_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, SshServiceResources.class);
	}

	private SshServiceResources() {
	}
}
