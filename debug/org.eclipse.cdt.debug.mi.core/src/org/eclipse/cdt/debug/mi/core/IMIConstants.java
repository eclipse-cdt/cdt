/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
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
	public static final String PLUGIN_ID = MIPlugin.getDefault().getDescriptor().getUniqueIdentifier();

	/**
	 * Preference key for default MI request timeout value.
	 */
	public static final String PREF_REQUEST_TIMEOUT = PLUGIN_ID + ".PREF_REQUEST_TIMEOUT"; //$NON-NLS-1$
	
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
}
