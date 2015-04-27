/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.remote.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.tm.terminal.connector.remote.nls.Messages"; //$NON-NLS-1$

	public static String RemoteConnectionManager_0;

	public static String RemoteConnectionManager_1;

	public static String RemoteTerminalPreferencePage_0;

	public static String TERMINAL_EXCEPTION;
	
	public static String RemoteLauncherDelegate_terminalTitle;
	public static String RemoteLauncherDelegate_terminalTitle_default;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
