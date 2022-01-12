/*******************************************************************************
 * Copyright (c) 2010, 2016 Marc-Andre Laperle and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.1
 */
public class ConsoleMessages extends NLS {
	public static String ConsoleMessages_trace_console_name;
	public static String ConsoleMessages_console_terminated;

	public static String ConsoleMessages_save_action_tooltip;
	public static String ConsoleMessages_save_confirm_overwrite_title;
	public static String ConsoleMessages_save_confirm_overwrite_desc;
	public static String ConsoleMessages_save_info_io_error_title;
	public static String ConsoleMessages_save_info_io_error_desc;

	public static String ConsoleTerminateLaunchAction_name;
	public static String ConsoleTerminateLaunchAction_description;

	public static String ConsoleClearAction_name;
	public static String ConsoleClearAction_description;

	public static String ConsoleScrollLockAction_name;
	public static String ConsoleScrollLockAction_description;

	public static String ConsoleCopyAction_name;
	public static String ConsoleCopyAction_description;

	public static String ConsolePasteAction_name;
	public static String ConsolePasteAction_description;

	public static String ConsoleSelectAllAction_name;
	public static String ConsoleSelectAllAction_description;

	public static String ConsoleAutoTerminateAction_name;
	public static String ConsoleAutoTerminateAction_description;

	public static String GdbConsolePreferences_name;

	static {
		// initialize resource bundle
		NLS.initializeMessages(ConsoleMessages.class.getName(), ConsoleMessages.class);
	}

	private ConsoleMessages() {
	}
}
