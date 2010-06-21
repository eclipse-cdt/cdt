/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public static String ConsoleMessages_trace_console_terminated;

	public static String ConsoleMessages_save_action_tooltip;
	public static String ConsoleMessages_save_confirm_overwrite_title;
	public static String ConsoleMessages_save_confirm_overwrite_desc;
	public static String ConsoleMessages_save_info_io_error_title;
	public static String ConsoleMessages_save_info_io_error_desc;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(ConsoleMessages.class.getName(), ConsoleMessages.class);
	}
	
	private ConsoleMessages() {
	}
}
