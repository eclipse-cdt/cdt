/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;



/**
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 2.0
 */
public interface IGdbDebugPreferenceConstants {
	
	/**
	 * Boolean preference whether to enable GDB traces. Default is <code>true</code>. 
	 */
	public static final String PREF_TRACES_ENABLE = "tracesEnable"; //$NON-NLS-1$

	/**
	 * Boolean preference whether to automatically terminate GDB when the inferior exists. Default is <code>true</code>. 
	 */
	public static final String PREF_AUTO_TERMINATE_GDB = "autoTerminateGdb"; //$NON-NLS-1$

	/**
	 * Boolean preference whether to use the advanced Inspect debug text hover. Default is <code>true</code>. 
	 * @since 3.0
	 */
	public static final String PREF_USE_INSPECTOR_HOVER = "useInspectorHover"; //$NON-NLS-1$

	/**
	 * Boolean preference whether to enable pretty printers for MI variable
	 * objects. Default is <code>true</code>.
	 * @since 4.0
	 */
	public static final String PREF_ENABLE_PRETTY_PRINTING = "enablePrettyPrinting"; //$NON-NLS-1$

	/**
	 * The maximum limit of children to be initially fetched by GDB for
	 * collections. Default is 100.
	 * @since 4.0
	 */
	public static final String PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS = "initialChildCountLimitForCollections"; //$NON-NLS-1$
	
	/**
	 * The default command for gdb
	 * @since 4.0
	 */
	public static final String PREF_DEFAULT_GDB_COMMAND = "defaultGdbCommand"; //$NON-NLS-1$
	
	/**
	 * The default command file for gdb
	 * @since 4.0
	 */
	public static final String PREF_DEFAULT_GDB_INIT = "defaultGdbInit"; //$NON-NLS-1$

	/**
     * Help prefixes.
     */
    public static final String PREFIX = GdbPlugin.PLUGIN_ID + "."; //$NON-NLS-1$
}

