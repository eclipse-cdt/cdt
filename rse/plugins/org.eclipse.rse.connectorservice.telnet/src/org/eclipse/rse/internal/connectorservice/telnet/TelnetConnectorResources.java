/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 * Martin Oberhuber (Wind River) - copy dialogs from team.cvs.ui 
 * Sheldon D'souza  (Celunite) - adapted from SshConnectorResources
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 *******************************************************************************/
package org.eclipse.rse.internal.connectorservice.telnet;

import org.eclipse.osgi.util.NLS;

public class TelnetConnectorResources extends NLS {
	
	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.connectorservice.telnet.TelnetConnectorResources"; //$NON-NLS-1$
	static {
		NLS.initializeMessages(BUNDLE_NAME, TelnetConnectorResources.class);
	}
	private TelnetConnectorResources() {
	}

	public static String TelnetConnectorService_Name;
	public static String TelnetConnectorService_Description;
	
	//Telnet property set
	
	public static String PropertySet_Description;	
	
	public static String TelnetConnectorService_ErrorDisconnecting;
	
	public static String MSG_EXCEPTION_OCCURRED;
	public static String MSG_COMM_AUTH_FAILED;
	public static String MSG_COMM_AUTH_FAILED_DETAILS;


	public static String MSG_CONNECT_CANCELLED;
	public static String MSG_DISCONNECT_FAILED;
	public static String MSG_DISCONNECT_CANCELLED;
}
