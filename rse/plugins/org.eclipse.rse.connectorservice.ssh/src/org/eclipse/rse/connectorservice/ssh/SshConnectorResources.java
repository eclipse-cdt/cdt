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

package org.eclipse.rse.connectorservice.ssh;

import org.eclipse.osgi.util.NLS;

public class SshConnectorResources extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.rse.connectorservice.ssh.SshConnectorResources"; //$NON-NLS-1$

	public static String SshConnectorService_Name;
	public static String SshConnectorService_Description;
	
	public static String SshConnectorService_ErrorDisconnecting;
	public static String SshConnectorService_Info;
	public static String SshConnectorService_Warning;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, SshConnectorResources.class);
	}

	private SshConnectorResources() {
	}
}
