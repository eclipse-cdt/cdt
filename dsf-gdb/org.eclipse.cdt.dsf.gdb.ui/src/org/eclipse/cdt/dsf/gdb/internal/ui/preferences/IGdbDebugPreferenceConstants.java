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
package org.eclipse.cdt.dsf.gdb.internal.ui.preferences;

import org.eclipse.debug.ui.IDebugUIConstants;


/**
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 2.0
 * @deprecated Has been replaced with org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants
 */
@Deprecated
public interface IGdbDebugPreferenceConstants {

	/**
	 * Debug UI plug-in identifier (value <code>"org.eclipse.cdt.dsf.gdb.ui"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.cdt.dsf.gdb.ui"; //$NON-NLS-1$;
	
	/**
	 * Boolean preference whether to enable GDB traces. Default is <code>true</code>.
	 * 
	 */
	public static final String PREF_TRACES_ENABLE = "tracesEnable"; //$NON-NLS-1$
	
    /**
     * Help prefixes.
     */
    public static final String PREFIX = IDebugUIConstants.PLUGIN_ID + "."; //$NON-NLS-1$
    
    public static final String PREFERENCE_PAGE= PREFIX + "preference_page_context"; //$NON-NLS-1$
}
