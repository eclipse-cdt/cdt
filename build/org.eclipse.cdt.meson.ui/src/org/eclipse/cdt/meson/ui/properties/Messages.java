/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.meson.ui.properties;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String MesonPropertyPage_not_an_integer;
	public static String MesonPropertyPage_option_pattern;
	public static String MesonPropertyPage_option_with_values_pattern;
	public static String MesonPropertyPage_compiler_or_link_args;
	public static String MesonPropertyPage_arg_description;
	public static String MesonPropertyPage_meson_error;
	public static String MesonPropertyPage_configure_failed;
	public static String MesonPropertyPage_terminated_rc;

	public static String MesonPropertyPage_env_group;
	public static String MesonPropertyPage_env_tooltip;
	public static String MesonPropertyPage_env_label;

	public static String MesonPropertyPage_project_group;
	public static String MesonPropertyPage_project_tooltip;
	public static String MesonPropertyPage_project_label;

	public static String MesonPropertyPage_options_group;

	public static String MesonPropertyPage_prefix_tooltip;
	public static String MesonPropertyPage_libdir_tooltip;
	public static String MesonPropertyPage_libexecdir_tooltip;
	public static String MesonPropertyPage_bindir_tooltip;
	public static String MesonPropertyPage_sbindir_tooltip;
	public static String MesonPropertyPage_includedir_tooltip;
	public static String MesonPropertyPage_datadir_tooltip;
	public static String MesonPropertyPage_mandir_tooltip;
	public static String MesonPropertyPage_infodir_tooltip;
	public static String MesonPropertyPage_localedir_tooltip;
	public static String MesonPropertyPage_sysconfdir_tooltip;
	public static String MesonPropertyPage_localstatedir_tooltip;
	public static String MesonPropertyPage_sharedstatedir_tooltip;
	public static String MesonPropertyPage_buildtype_tooltip;
	public static String MesonPropertyPage_strip_tooltip;
	public static String MesonPropertyPage_unity_tooltip;
	public static String MesonPropertyPage_werror_tooltip;
	public static String MesonPropertyPage_layout_tooltip;
	public static String MesonPropertyPage_default_library_tooltip;
	public static String MesonPropertyPage_warnlevel_tooltip;
	public static String MesonPropertyPage_stdsplit_tooltip;
	public static String MesonPropertyPage_errorlogs_tooltip;
	public static String MesonPropertyPage_cross_file_tooltip;
	public static String MesonPropertyPage_wrap_mode_tooltip;

	public static String MesonPropertyPage_true;
	public static String MesonPropertyPage_false;

	static {
		// initialize resource bundle
		NLS.initializeMessages("org.eclipse.cdt.meson.ui.properties.messages", Messages.class); //$NON-NLS-1$
	}

	private Messages() {
	}
}
