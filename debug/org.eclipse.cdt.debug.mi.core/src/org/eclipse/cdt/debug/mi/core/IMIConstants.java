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
package org.eclipse.cdt.debug.mi.core;

/**
 * 
 * Constant definitions for GDB MI plug-in.
 * 
 * @since Oct 4, 2002
 */
public interface IMIConstants
{
	/**
	 * MI plug-in identifier (value <code>"org.eclipse.cdt.debug.mi"</code>).
	 */
	public static final String PLUGIN_ID = MIPlugin.getUniqueIdentifier();

	/**
	 * Preference key for default MI request timeout value.
	 */
	public static final String PREF_REQUEST_TIMEOUT = PLUGIN_ID + ".PREF_REQUEST_TIMEOUT"; //$NON-NLS-1$

	/**
	 * Preference key for default MI launch request timeout value.
	 */
	public static final String PREF_REQUEST_LAUNCH_TIMEOUT = PLUGIN_ID + ".PREF_REQUEST_LAUNCH_TIMEOUT"; //$NON-NLS-1$

	/**
	 * The default MI request timeout when no preference is set.
	 */
	public static final int DEF_REQUEST_LAUNCH_TIMEOUT = 30000;
	
	/**
	 * The default MI request timeout when no preference is set.
	 */
	public static final int DEF_REQUEST_TIMEOUT = 10000;

	/**
	 * The minimum value the MI request timeout can have.
	 */
	public static final int MIN_REQUEST_TIMEOUT = 100;

	/**
	 * The maximum value the MI request timeout can have.
	 */
	public static final int MAX_REQUEST_TIMEOUT = Integer.MAX_VALUE;

	/**
	 * Boolean preference controlling whether the shared library manager will be
	 * refreshed every time when the execution of program stops.
	 */
	public static final String PREF_SHARED_LIBRARIES_AUTO_REFRESH = PLUGIN_ID + ".SharedLibraries.auto_refresh"; //$NON-NLS-1$

	/**
	 * The default value of the for <code>PREF_SHARED_LIBRARIES_AUTO_REFRESH</code> property
	 */
	public static final boolean DEF_PREF_SHARED_LIBRARIES_AUTO_REFRESH = true;

	/**
	 * Boolean preference controlling whether the register manager will be
	 * refreshed every time when the execution of program stops.
	 */
	public static final String PREF_REGISTERS_AUTO_REFRESH = PLUGIN_ID + ".Registers.auto_refresh"; //$NON-NLS-1$

	/**
	 * The default value of the for <code>PREF_REGISTERS_AUTO_REFRESH</code> property
	 */
	public static final boolean DEF_PREF_REGISTERS_AUTO_REFRESH = true;
}
