/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - adapted to work with platform Modules view (bug 210558)
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
	 * Executables view identifier (value <code>"org.eclipse.cdt.debug.ui.executablesView"</code>).
	 */
	public static final String ID_EXECUTABLES_VIEW = PREFIX + "executablesView"; //$NON-NLS-1$

    /**
     * Disassembly view identifier (value <code>"org.eclipse.cdt.debug.ui.DisassemblyView"</code>).
     * @deprecated As of CDT 7.0 replaced by the DSF Disassembly view ("org.eclipse.cdt.dsf.debug.ui.disassembly.view")
     */
    @Deprecated
    public static final String ID_DISASSEMBLY_VIEW = PREFIX + "DisassemblyView"; //$NON-NLS-1$

    /**
     * Disassembly view identifier (value <code>"org.eclipse.cdt.dsf.debug.ui.disassembly.view"</code>).
     * @since 7.0
     */
    public static final String ID_DSF_DISASSEMBLY_VIEW = "org.eclipse.cdt.dsf.debug.ui.disassembly.view"; //$NON-NLS-1$

    /**
     * Signals view identifier (value <code>"org.eclipse.cdt.debug.ui.SignalsView"</code>).
     * @since 6.0
     */
    public static final String ID_SIGNALS_VIEW = PREFIX + "SignalsView"; //$NON-NLS-1$

    /**
     * Deafult disassembly editor identifier (value <code>"org.eclipse.cdt.debug.ui.disassemblyEditor"</code>).
     */
    public static final String ID_DEFAULT_DISASSEMBLY_EDITOR = PREFIX + "disassemblyEditor"; //$NON-NLS-1$

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
	 * modules group in a menu (value <code>"emptyModulesGroup"</code>).
	 */
	public static final String EMPTY_MODULES_GROUP = "emptyModulesGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a shared libraries group in a menu (value <code>"modulesGroup"</code>).
	 */
	public static final String MODULES_GROUP = "modulesGroup"; //$NON-NLS-1$
	
	/**
	 * Editor ID for the CSourceNotFoundEditor.
	 */
	public static final String CSOURCENOTFOUND_EDITOR_ID = PREFIX + "SourceNotFoundEditor"; //$NON-NLS-1$
	
}
