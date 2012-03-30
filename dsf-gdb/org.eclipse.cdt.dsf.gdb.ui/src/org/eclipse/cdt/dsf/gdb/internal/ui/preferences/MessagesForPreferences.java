/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
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
	public static String GdbDebugPreferencePage_Anvanced_Timeout_Settings;
	public static String GdbDebugPreferencePage_description;
	public static String GdbDebugPreferencePage_traces_label;
	public static String GdbDebugPreferencePage_enableTraces_label;
	/** @since 2.2 */
	public static String GdbDebugPreferencePage_maxGdbTraces_label;
	public static String GdbDebugPreferencePage_termination_label;
	public static String GdbDebugPreferencePage_autoTerminateGdb_label;
	public static String GdbDebugPreferencePage_Command_column_name;
	public static String GdbDebugPreferencePage_Command_field_can_not_be_empty;
	public static String GdbDebugPreferencePage_Command_timeout;
	public static String GdbDebugPreferencePage_hover_label;
	public static String GdbDebugPreferencePage_useInspectorHover_label;
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
	public static String GdbDebugPreferencePage_Invalid_timeout_value;
	public static String GdbDebugPreferencePage_Timeout_column_name;
	public static String GdbDebugPreferencePage_Timeout_value_can_not_be_negative;
	static {
		// initialize resource bundle
		NLS.initializeMessages(MessagesForPreferences.class.getName(), MessagesForPreferences.class);
	}

	private MessagesForPreferences() {
	}
}
