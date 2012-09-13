/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Anton Gorenkov - A preference to use RTTI for variable types determination (Bug 377536)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * Preference strings.
 */
class MessagesForPreferences extends NLS {
	public static String GdbDebugPreferencePage_Add_button;
	public static String GdbDebugPreferencePage_Advanced_button;
	public static String GdbDebugPreferencePage_Advanced_timeout_dialog_message;
	public static String GdbDebugPreferencePage_Advanced_timeout_dialog_title;
	public static String GdbDebugPreferencePage_Advanced_timeout_settings_dialog_tooltip;
	public static String GdbDebugPreferencePage_Advanced_Timeout_Settings;
	public static String GdbDebugPreferencePage_description;
	/** @since 2.3 */
	public static String GdbDebugPreferencePage_general_behavior_label;
	public static String GdbDebugPreferencePage_enableTraces_label;
	public static String GdbDebugPreferencePage_autoTerminateGdb_label;
	public static String GdbDebugPreferencePage_Browse_button;
	public static String GdbDebugPreferencePage_Command_column_name;
	public static String GdbDebugPreferencePage_Command_field_can_not_be_empty;
	public static String GdbDebugPreferencePage_Command_timeout;
	public static String GdbDebugPreferencePage_useInspectorHover_label;
	/** @since 2.3 */
	public static String GdbDebugPreferencePage_hideRunningThreads;
	/** @since 2.2 */
	public static String GdbDebugPreferencePage_prettyPrinting_label;
	/** @since 2.2 */
	public static String GdbDebugPreferencePage_enablePrettyPrinting_label1;
	/** @since 2.2 */
	public static String GdbDebugPreferencePage_enablePrettyPrinting_label2;
	/** @since 2.2 */
	public static String GdbDebugPreferencePage_initialChildCountLimitForCollections_label;
	/** @since 2.2 */
	public static String GdbDebugPreferencePage_defaults_label;
	public static String GdbDebugPreferencePage_Delete_button;
	public static String GdbDebugPreferencePage_GDB_command_file;
	public static String GdbDebugPreferencePage_GDB_command_file_dialog_title;
	public static String GdbDebugPreferencePage_GDB_debugger;
	public static String GdbDebugPreferencePage_GDB_debugger_dialog_title;
	public static String GdbDebugPreferencePage_Invalid_timeout_value;
	public static String GdbDebugPreferencePage_Non_stop_mode;
	public static String GdbDebugPreferencePage_Timeout_column_name;
	public static String GdbDebugPreferencePage_Timeout_value_can_not_be_negative;
	public static String GdbDebugPreferencePage_Stop_on_startup_at;
	/** @since 2.3 */
	public static String GdbDebugPreferencePage_use_rtti_label1;
	/** @since 2.3 */
	public static String GdbDebugPreferencePage_use_rtti_label2;
	
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(MessagesForPreferences.class.getName(), MessagesForPreferences.class);
	}

	private MessagesForPreferences() {
	}
}
