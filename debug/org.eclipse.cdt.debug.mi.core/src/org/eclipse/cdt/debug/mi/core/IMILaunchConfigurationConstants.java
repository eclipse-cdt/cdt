/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
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
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_AUTO_SOLIB.
	 */
	public static boolean DEBUGGER_AUTO_SOLIB_DEFAULT = true;

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS.
	 */
	public static boolean DEBUGGER_STOP_ON_SOLIB_EVENTS_DEFAULT = false;

	/**
	 * launch configuration attribute key.  The value is a string specifying the protocol to
	 * use.  For now only "mi", "mi1", "m2", "mi3" are supported.
	 */
	public static final String ATTR_DEBUGGER_PROTOCOL = MIPlugin.getUniqueIdentifier() + ".protocol"; //$NON-NLS-1$
}
