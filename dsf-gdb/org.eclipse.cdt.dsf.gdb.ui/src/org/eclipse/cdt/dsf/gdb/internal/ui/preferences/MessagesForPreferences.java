/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * Preference strings.
 */
class MessagesForPreferences extends NLS {
	private static final String BUNDLE_NAME= "org.eclipse.cdt.dsf.gdb.internal.ui.preferences.messages"; //$NON-NLS-1$

	public static String GdbDebugPreferencePage_description;
	public static String GdbDebugPreferencePage_traces_label;
	public static String GdbDebugPreferencePage_enableTraces_label;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, MessagesForPreferences.class);
	}

	private MessagesForPreferences() {
	}
}
