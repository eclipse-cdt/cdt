package org.eclipse.cdt.debug.mi.core;

/*
 * (c) Copyright QNX Software System 2002.
 * All Rights Reserved.
 */


public interface IMILaunchConfigurationConstants {
	/**
	 * Launch configuration attribute key. The value is a name of
	 * a C/C++ project associated with a C/C++ launch configuration.
	 */
	public static final String ATTR_DEBUG_NAME = MIPlugin.getUniqueIdentifier() + ".DEBUG_NAME"; //$NON-NLS-1$

	public static final String ATTR_DEBUG_ARGS = MIPlugin.getUniqueIdentifier() + ".DEBUG_ARGS"; //$NON-NLS-1$

	public static final String ATTR_AUTO_SOLIB = MIPlugin.getUniqueIdentifier() + ".AUTO_SOLIB"; //$NON-NLS-1$
}
