package org.eclipse.cdt.managedbuilder.internal.ui;

import org.eclipse.cdt.ui.CUIPlugin;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

public interface ManagedBuilderHelpContextIds {
	public static final String PREFIX= CUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	// Wizard pages
	public static final String MAN_PROJ_PLATFORM_HELP = PREFIX + "new_proj_wiz_m_target"; //$NON-NLS-1$
	public static final String MAN_PROJ_WIZ_NAME_PAGE = PREFIX + "new_proj_wiz_m_name"; //$NON-NLS-1$
	public static final String MAN_PROJ_WIZ_PROJECTS_TAB = PREFIX + "new_proj_wiz_m_proj"; //$NON-NLS-1$
	public static final String MAN_PROJ_WIZ_ERRORPARSERS_TAB = PREFIX + "new_proj_wiz_m_errorp"; //$NON-NLS-1$
	public static final String MAN_PROJ_WIZ_INDEXER_TAB = PREFIX + "new_proj_wiz_m_cindexer"; //$NON-NLS-1$
	public static final String MAN_PROJ_WIZ_BINARYPARSER_TAB = PREFIX + "new_proj_wiz_s_binary"; //$NON-NLS-1$
	
	public static final String MAN_PROJ_BUILD_PROP = PREFIX + "man_prop_build"; //$NON-NLS-1$
	public static final String MAN_PROJ_ERROR_PARSER = PREFIX + "man_prop_error"; //$NON-NLS-1$

}
