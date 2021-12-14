/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial contribution
 *******************************************************************************/
package org.eclipse.remote.telnet.internal.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.remote.telnet.internal.core.messages.messages"; //$NON-NLS-1$

	public static String TelnetCommandShell_0;
	public static String TelnetProtocol_0;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
