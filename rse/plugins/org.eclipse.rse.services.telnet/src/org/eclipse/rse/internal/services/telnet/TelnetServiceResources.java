/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0 
 * which accompanies this distribution, and is available at 
 * https://www.eclipse.org/legal/epl-2.0/ 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 * Sheldon D'souza  (Celunite)   - adapted form SshServiceResources
 * Anna Dushistova  (MontaVista) - [267226] Wrong name and description in TelnetTerminalService 
 *******************************************************************************/
package org.eclipse.rse.internal.services.telnet;

import org.eclipse.osgi.util.NLS;

public class TelnetServiceResources extends NLS {
	
	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.services.telnet.TelnetServiceResources"; //$NON-NLS-1$

	public static String TelnetPlugin_Unexpected_Exception;
	
	public static String TelnetTerminalService_Description;

	public static String TelnetTerminalService_Name;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, TelnetServiceResources.class);
	}

	private TelnetServiceResources(){
		
	}
}
