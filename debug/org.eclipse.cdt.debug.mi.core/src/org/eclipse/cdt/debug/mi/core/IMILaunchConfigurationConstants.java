package org.eclipse.cdt.debug.mi.core;

/*
 * (c) Copyright QNX Software System 2002.
 * All Rights Reserved.
 */


public interface IMILaunchConfigurationConstants {
	/**
	 * Launch configuration attribute key. The value is the name of
	 * the Debuger associated with a C/C++ launch configuration.
	 */
	public static final String ATTR_DEBUG_NAME = MIPlugin.getUniqueIdentifier() + ".DEBUG_NAME"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a List (array of String) directories for solib-search-path
	 * the Debuger associated with a C/C++ launch configuration.
	 */
	public static final String ATTR_SOLIB_PATH = MIPlugin.getUniqueIdentifier() + ".SOLIB_PATH"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the auto-solib-add
	 * Debuger/gdb/MI property.
	 */
	public static final String ATTR_AUTO_SOLIB = MIPlugin.getUniqueIdentifier() + ".AUTO_SOLIB"; //$NON-NLS-1$
}
