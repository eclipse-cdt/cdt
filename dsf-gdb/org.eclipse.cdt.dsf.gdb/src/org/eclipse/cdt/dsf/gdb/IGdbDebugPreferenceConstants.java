/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial implementation
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
     * Help prefixes.
     */
    public static final String PREFIX = GdbPlugin.PLUGIN_ID + "."; //$NON-NLS-1$
    
    public static final String PREFERENCE_PAGE= PREFIX + "preference_page_context"; //$NON-NLS-1$
}

