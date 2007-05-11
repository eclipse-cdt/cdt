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

	public static String PropertySet_Description;
	
	public static String TelnetConnectorService_ErrorDisconnecting;

}
