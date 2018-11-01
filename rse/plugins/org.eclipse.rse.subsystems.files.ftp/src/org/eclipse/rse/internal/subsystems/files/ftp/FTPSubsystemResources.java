/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0 
 * which accompanies this distribution, and is available at 
 * https://www.eclipse.org/legal/epl-2.0/ 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 * David Dykstal (IBM) - added RESID_FTP_SETTINGS_LABEL
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.ftp;

import org.eclipse.osgi.util.NLS;

public class FTPSubsystemResources extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.subsystems.files.ftp.FTPSubsystemResources"; //$NON-NLS-1$
	static {
		NLS.initializeMessages(BUNDLE_NAME, FTPSubsystemResources.class);
	}
	private FTPSubsystemResources() {
	}

	public static String	RESID_FTP_CONNECTORSERVICE_NAME;
	public static String	RESID_FTP_CONNECTORSERVICE_DESCRIPTION;
	public static String RESID_FTP_SETTINGS_LABEL;
	
}
