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
 * 
 * Constant definitions for C/C++ Debug UI plug-in.
 * 
 * @since Jul 23, 2002
 */
public interface ICDebugUIConstants
{
	/**
	 * C/C++ Debug UI plug-in identifier (value <code>"org.eclipse.cdt.debug.ui"</code>).
	 */
	public static final String PLUGIN_ID = CDebugUIPlugin.getUniqueIdentifier();

	// Debug views
	
	/**
	 * Registers view identifier (value <code>"org.eclipse.cdt.debug.ui.RegitersView"</code>).
	 */
	public static final String ID_REGISTERS_VIEW = "org.eclipse.cdt.debug.ui.RegitersView"; //$NON-NLS-1$
	
	/**
	 * Memory view identifier (value <code>"org.eclipse.cdt.debug.ui.MemoryView"</code>).
	 */
	public static final String ID_MEMORY_VIEW = "org.eclipse.cdt.debug.ui.MemoryView"; //$NON-NLS-1$

	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 150;

	/** 
	 * Identifier for an empty group preceeding a
	 * register group in a menu (value <code>"emptyRegisterGroup"</code>).
	 */
	public static final String EMPTY_REGISTER_GROUP = "emptyRegisterGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a register group in a menu (value <code>"registerGroup"</code>).
	 */
	public static final String REGISTER_GROUP = "registerGroup"; //$NON-NLS-1$

	/** 
	 * Identifier for an empty group preceeding a
	 * memory group in a menu (value <code>"emptyMemoryGroup"</code>).
	 */
	public static final String EMPTY_MEMORY_GROUP = "emptyMemoryGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a memory group in a menu (value <code>"memoryGroup"</code>).
	 */
	public static final String MEMORY_GROUP = "memoryGroup"; //$NON-NLS-1$

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
}
