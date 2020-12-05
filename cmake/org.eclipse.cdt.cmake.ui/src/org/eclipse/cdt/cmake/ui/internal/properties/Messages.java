/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.ui.internal.properties;

import org.eclipse.osgi.util.NLS;

/**
 * @author Martin Weber
 *
 */
public class Messages extends NLS {
	public static String CMakePropertyPage_lbl_browse;
	public static String CMakePropertyPage_lbl_build_type;
	public static String AbstractOverridesTab_cmakeExecutable;
	public static String AbstractOverridesTab_lbl_file;
	public static String AbstractOverridesTab_lbl_file_system;
	public static String AbstractOverridesTab_lbl_generator;
	public static String AbstractOverridesTab_lbl_select_cmake_executable;
	public static String AbstractOverridesTab_onSystemPath;
	public static String AbstractOverridesTab_tt_args_appended;

	public static String CMakePropertyPage_help_has_overrides;
	public static String CMakePropertyPage_debug_output;
	public static String CMakePropertyPage_debug_trycompile;
	public static String CMakePropertyPage_lbl_cmdline_options;
	public static String CMakePropertyPage_lbl_preprop_cache;
	public static String CMakePropertyPage_trace;
	public static String CMakePropertyPage_Wuninitialized;
	public static String CMakePropertyPage_Wunused_vars;
	public static String CMakePropertyPage_Wno_dev;
	public static String CMakePropertyPage_failed_to_load_properties;
	public static String CMakePropertyPage_failed_to_save_properties;
	public static String lbl_insert_variable;
	public static String lbl_other_cmd_args;
	public static String CMakePropertyPage_lbl_file;
	public static String CMakePropertyPage_lbl_create;
	public static String CMakePropertyPage_lbl_select_file;
	public static String OverridesPropertyPage_hlp_overrides;

	static {
		// initialize resource bundle
		NLS.initializeMessages("org.eclipse.cdt.cmake.ui.internal.properties.messages", Messages.class); //$NON-NLS-1$
	}

	private Messages() {
	}
}
