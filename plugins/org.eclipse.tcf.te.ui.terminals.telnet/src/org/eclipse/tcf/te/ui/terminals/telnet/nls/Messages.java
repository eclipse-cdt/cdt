/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [366374] [TERMINALS][TELNET] Add Telnet terminal support
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.telnet.nls;

import org.eclipse.osgi.util.NLS;

/**
 * Target Explorer TCF terminals extensions UI plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.ui.terminals.telnet.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String TelnetLauncherDelegate_terminalTitle;
	public static String TelnetLauncherDelegate_terminalTitle_default;
}
