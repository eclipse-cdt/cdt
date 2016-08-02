/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.ui.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Messages related to preferences and launch configuration.
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.llvm.dsf.lldb.ui.internal.messages"; //$NON-NLS-1$
	public static String LLDBCDebuggerPage_browse;
	public static String LLDBCDebuggerPage_browse_dialog_title;
	public static String LLDBCDebuggerPage_tab_name;
	public static String LLDBCDebuggerPage_debugger_command;
	public static String LLDBDebugPreferencePage_Stop_on_startup_at;
	public static String LLDBDebugPreferencePage_description;
	public static String LLDBDebugPreferencePage_defaults_label;
	public static String LLDBDebugPreferencePage_see_gdb_preferences;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
