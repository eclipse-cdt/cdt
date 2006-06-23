/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

public interface IMILaunchConfigurationConstants {
	/**
	 * Launch configuration attribute key. The value is the name of
	 * the Debuger associated with a C/C++ launch configuration.
	 */
	public static final String ATTR_DEBUG_NAME = MIPlugin.getUniqueIdentifier() + ".DEBUG_NAME"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the gdb command file
	 * Debuger/gdb/MI property.
	 */
	public static final String ATTR_GDB_INIT = MIPlugin.getUniqueIdentifier() + ".GDB_INIT"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the 'automatically load shared library symbols' flag of the debugger.
	 */
	public static final String ATTR_DEBUGGER_AUTO_SOLIB = MIPlugin.getUniqueIdentifier() + ".AUTO_SOLIB"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the 'stop on shared library events' flag of the debugger.
	 */
	public static final String ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS = MIPlugin.getUniqueIdentifier() + ".STOP_ON_SOLIB_EVENTS"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a List (array of String) of directories for the search path of shared libraries.
	 */
	public static final String ATTR_DEBUGGER_SOLIB_PATH = MIPlugin.getUniqueIdentifier() + ".SOLIB_PATH"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a List (array of String) of shared libraries to load symbols automatically.
	 */
	public static final String ATTR_DEBUGGER_AUTO_SOLIB_LIST = MIPlugin.getUniqueIdentifier() + ".AUTO_SOLIB_LIST"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUG_NAME.
	 */
	public static final String DEBUGGER_DEBUG_NAME_DEFAULT = "gdb"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is ATTR_GDB_INIT.
	 */
	public static final String DEBUGGER_GDB_INIT_DEFAULT = ".gdbinit"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_AUTO_SOLIB.
	 */
	public static final boolean DEBUGGER_AUTO_SOLIB_DEFAULT = true;

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS.
	 */
	public static final boolean DEBUGGER_STOP_ON_SOLIB_EVENTS_DEFAULT = false;

	/**
	 * Launch configuration attribute key.  The value is a string specifying the identifier of the command factory to use.
	 */
	public static final String ATTR_DEBUGGER_COMMAND_FACTORY = MIPlugin.getUniqueIdentifier() + ".commandFactory"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key.  The value is a string specifying the protocol to
	 * use.  For now only "mi", "mi1", "m2", "mi3" are supported.
	 */
	public static final String ATTR_DEBUGGER_PROTOCOL = MIPlugin.getUniqueIdentifier() + ".protocol"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key.  The value is a boolean specifying the mode of the gdb console.
	 */
	public static final String ATTR_DEBUGGER_VERBOSE_MODE = MIPlugin.getUniqueIdentifier() + ".verboseMode"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_VERBOSE_MODE.
	 */
	public static final boolean DEBUGGER_VERBOSE_MODE_DEFAULT = false;
}
