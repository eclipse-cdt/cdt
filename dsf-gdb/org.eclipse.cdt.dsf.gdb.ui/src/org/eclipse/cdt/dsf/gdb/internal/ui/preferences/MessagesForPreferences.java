/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
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
	public static String GdbDebugPreferencePage_description;
	public static String GdbDebugPreferencePage_traces_label;
	public static String GdbDebugPreferencePage_enableTraces_label;
	public static String GdbDebugPreferencePage_termination_label;
	public static String GdbDebugPreferencePage_autoTerminateGdb_label;
	public static String GdbDebugPreferencePage_hover_label;
	public static String GdbDebugPreferencePage_useInspectorHover_label;

	static {
		// initialize resource bundle
		NLS.initializeMessages(MessagesForPreferences.class.getName(), MessagesForPreferences.class);
	}

	private MessagesForPreferences() {
	}
}
