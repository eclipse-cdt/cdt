/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Yu-Fen Kuo       (MontaVista) - [170910] Integrate the TM Terminal View with RSE
 * Anna Dushistova  (MontaVista) - [261478] Remove SshShellService, SshHostShell (or deprecate and schedule for removal in 3.2)
 * Martin Oberhuber (Wind River) - [227135] Cryptic exception when sftp-server is missing
 * Anna Dushistova  (MontaVista) - [331213] [scp] Provide UI-less scp IFileService in org.eclipse.rse.services.ssh
 *******************************************************************************/

package org.eclipse.rse.internal.services.ssh;

import org.eclipse.osgi.util.NLS;

public class SshServiceResources extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.services.ssh.SshServiceResources"; //$NON-NLS-1$

	public static String ScpFileService_AttributesError;

	public static String ScpFileService_Description;

	public static String ScpFileService_DownloadFileTaskName;

	public static String ScpFileService_LastModifiedNotSupportedError;

	public static String ScpFileService_Name;

	public static String ScpFileService_ReadAckError;

	public static String ScpFileService_UploadFileTaskName;
	
	public static String ScpFileService_DownloadException;

	public static String ScpFileService_DownloadException1;

	public static String ScpFileService_UploadException;
	
	public static String SftpFileService_Name;
	public static String SftpFileService_Description;
	public static String SftpFileService_Error_JschSessionLost;
	public static String SftpFileService_Error_download_size;
	public static String SftpFileService_Error_upload_size;
	public static String SftpFileService_Error_no_sftp;
	public static String SftpFileService_Msg_Progress;

	public static String SshPlugin_Unexpected_Exception;

	public static String SshTerminalService_Name;
	public static String SshTerminalService_Description;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, SshServiceResources.class);
	}

	private SshServiceResources() {
	}
}
