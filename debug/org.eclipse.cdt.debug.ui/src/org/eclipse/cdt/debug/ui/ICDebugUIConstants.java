/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

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
}
