/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;

public interface IMakeHelpContextIds {
	public static final String PREFIX = MakeUIPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$
	
	public static final String MAKE_PATH_SYMBOL_SETTINGS = PREFIX + "cdt_paths_symbols_page"; //$NON-NLS-1$
	public static final String MAKE_BUILDER_SETTINGS = PREFIX + "newproj_buildset"; //$NON-NLS-1$
	public static final String MAKE_PROP_BUILDER_SETTINGS = PREFIX + "std_prop_build"; //$NON-NLS-1$
	public static final String SCANNER_CONFIG_DISCOVERY_OPTIONS =  PREFIX + "discovery_preferences"; //$NON-NLS-1$
	public static final String MAKE_PROP_DISCOVERY = PREFIX + "std_prop_discovery"; //$NON-NLS-1$
	public static final String MAKE_VIEW = PREFIX + "make_targets_view"; //$NON-NLS-1$
	public static final String MAKE_PROJ_WIZ_NAME_PAGE = PREFIX + "new_proj_wiz_s_name";  //$NON-NLS-1$
	public static final String MAKE_PROJ_WIZ_PROJECTS_TAB = PREFIX + "new_proj_wiz_s_proj"; //$NON-NLS-1$
	public static final String MAKE_PROJ_WIZ_MAKEBUILDER_TAB = PREFIX + "new_proj_wiz_s_mbuilder"; //$NON-NLS-1$
	public static final String MAKE_PROJ_WIZ_ERRORPARSER_TAB = PREFIX + "new_proj_wiz_s_errorp"; //$NON-NLS-1$
	public static final String MAKE_PROJ_WIZ_BINARYPARSER_TAB = PREFIX + "new_proj_wiz_s_binary"; //$NON-NLS-1$
	public static final String MAKE_PROJ_WIZ_DISCOVERY_TAB = PREFIX + "new_proj_wiz_s_discovery";  //$NON-NLS-1$
	public static final String MAKE_PROJ_WIZ_INDEXER_TAB = PREFIX + "new_proj_wiz_s_cindexer";  //$NON-NLS-1$
	
	public static final String MAKE_PROP_ERROR_PARSER = PREFIX + "std_prop_error"; //$NON-NLS-1$
	public static final String MAKE_PROP_BINARY_PARSER = PREFIX + "std_prop_binary"; //$NON-NLS-1$
	public static final String MAKE_PREF_ERROR_PARSER = PREFIX + "newproj_parser_error"; //$NON-NLS-1$
	public static final String MAKE_PREF_BINARY_PARSER = PREFIX + "newproj_parser_binary"; //$NON-NLS-1$

	public static final String MAKE_EDITOR_PREFERENCE_PAGE = PREFIX + "make_editor_pref"; //$NON-NLS-1$

}
