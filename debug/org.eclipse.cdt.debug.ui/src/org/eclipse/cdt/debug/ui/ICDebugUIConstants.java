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
}
