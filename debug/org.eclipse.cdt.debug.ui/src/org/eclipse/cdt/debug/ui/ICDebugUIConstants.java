/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui;

/**
 * Constant definitions for C/C++ Debug UI plug-in.
 */
public interface ICDebugUIConstants {
	/**
	 * C/C++ Debug UI plug-in identifier (value <code>"org.eclipse.cdt.debug.ui"</code>).
	 */
	public static final String PLUGIN_ID = CDebugUIPlugin.getUniqueIdentifier();

	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$
	
	/**
	 * Modules view identifier (value <code>"org.eclipse.cdt.debug.ui.ModulesView"</code>).
	 */
	public static final String ID_MODULES_VIEW = PREFIX + "ModulesView"; //$NON-NLS-1$

	/**
	 * Id for the popup menu associated with the variables (tree viewer) part of the VariableView
	 */
	public static final String MODULES_VIEW_MODULES_ID = PREFIX + "ModulesView.modules"; //$NON-NLS-1$

	/**
	 * Id for the popup menu associated with the detail (text viewer) part of the Modules view
	 */
	public static final String MODULES_VIEW_DETAIL_ID = PREFIX + "ModulesView.detail"; //$NON-NLS-1$

	/** 
	 * Identifier for an empty group preceeding a
	 * format group in a menu (value <code>"emptyFormatGroup"</code>).
	 */
	public static final String EMPTY_FORMAT_GROUP = "emptyFormatGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a format group in a menu (value <code>"formatGroup"</code>).
	 */
	public static final String FORMAT_GROUP = "formatGroup"; //$NON-NLS-1$

	/** 
	 * Identifier for an empty group preceeding a
	 * refresh group in a menu (value <code>"emptyRefreshGroup"</code>).
	 */
	public static final String EMPTY_REFRESH_GROUP = "emptyRefreshGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a refresh group in a menu (value <code>"refreshGroup"
	 * </code>).
	 */
	public static final String REFRESH_GROUP = "refreshGroup"; //$NON-NLS-1$

	/** 
	 * Identifier for an empty group preceeding a
	 * shared libraries group  in a menu (value <code>"
	 * emptySharedLibrariesGroup"
	 * </code>).
	 */
	public static final String EMPTY_SHARED_LIBRARIES_GROUP = "emptySharedLibrariesGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a shared libraries group in a menu (value <code>"
	 * sharedLibrariesGroup"
	 * </code>).
	 */
	public static final String SHARED_LIBRARIES_GROUP = "sharedLibrariesGroup"; //$NON-NLS-1$

	/** 
	 * Identifier for an empty group preceeding a
	 * modules group in a menu (value <code>"emptyModulesGroup"</code>).
	 */
	public static final String EMPTY_MODULES_GROUP = "emptyModulesGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a shared libraries group in a menu (value <code>"modulesGroup"</code>).
	 */
	public static final String MODULES_GROUP = "modulesGroup"; //$NON-NLS-1$
}
