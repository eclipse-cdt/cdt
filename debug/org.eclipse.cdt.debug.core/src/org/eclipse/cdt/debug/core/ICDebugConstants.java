/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

/**
 * 
 * Constant definitions for C/C++ debug plug-in.
 * 
 * @since: Oct 15, 2002
 */
public interface ICDebugConstants
{
	/**
	 * C/C++ debug plug-in identifier (value <code>"org.eclipse.cdt.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = CDebugCorePlugin.getDefault().getDescriptor().getUniqueIdentifier();

	/**
	 * Boolean preference controlling whether the debugger automatically 
	 * switchs to disassembly mode when can not find the source file . 
	 * When <code>true</code> the debugger will automatically switch to 
	 * disassembly mode.
	 */
	public static final String PREF_AUTO_DISASSEMBLY = PLUGIN_ID + "cDebug.auto_disassembly"; //$NON-NLS-1$

	/**
	 * The identifier of the default variable format to use in the variables view
	 */
	public static final String PREF_DEFAULT_VARIABLE_FORMAT = PLUGIN_ID + "cDebug.default_variable_format"; //$NON-NLS-1$

	/**
	 * The identifier of the default register format to use in the registers view
	 */
	public static final String PREF_DEFAULT_REGISTER_FORMAT = PLUGIN_ID + "cDebug.default_register_format"; //$NON-NLS-1$

	/**
	 * The identifier of the default expression format to use in the expressions views
	 */
	public static final String PREF_DEFAULT_EXPRESSION_FORMAT = PLUGIN_ID + "cDebug.default_expression_format"; //$NON-NLS-1$
}
