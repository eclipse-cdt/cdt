/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMakeHelpContextIds {
	public static final String PREFIX = MakeUIPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$

	public static final String MAKE_PATH_SYMBOL_SETTINGS = PREFIX + "cdt_paths_symbols_page"; //$NON-NLS-1$
	public static final String MAKE_BUILDER_SETTINGS = PREFIX + "newproj_buildset"; //$NON-NLS-1$
	public static final String MAKE_PROP_BUILDER_SETTINGS = PREFIX + "std_prop_build"; //$NON-NLS-1$
	public static final String SCANNER_CONFIG_DISCOVERY_OPTIONS = PREFIX + "discovery_preferences"; //$NON-NLS-1$
	public static final String MAKE_PROP_DISCOVERY = PREFIX + "std_prop_discovery"; //$NON-NLS-1$
	public static final String MAKE_VIEW = PREFIX + "make_targets_view"; //$NON-NLS-1$

	public static final String MAKE_PROP_ERROR_PARSER = PREFIX + "std_prop_error"; //$NON-NLS-1$
	public static final String MAKE_PROP_BINARY_PARSER = PREFIX + "std_prop_binary"; //$NON-NLS-1$
	public static final String MAKE_PREF_ERROR_PARSER = PREFIX + "newproj_parser_error"; //$NON-NLS-1$
	public static final String MAKE_PREF_BINARY_PARSER = PREFIX + "newproj_parser_binary"; //$NON-NLS-1$

	public static final String MAKE_EDITOR_PREFERENCE_PAGE = PREFIX + "make_editor_pref"; //$NON-NLS-1$
	/**
	 * @since 7.1
	 */
	public static final String MAKE_TARGETS_PREFERENCE_PAGE = PREFIX + "make_targets_pref"; //$NON-NLS-1$
	/**
	 * @since 7.1
	 */
	public static final String MAKE_SETTINGS_PREFERENCE_PAGE = PREFIX + "make_settings_pref"; //$NON-NLS-1$

}
