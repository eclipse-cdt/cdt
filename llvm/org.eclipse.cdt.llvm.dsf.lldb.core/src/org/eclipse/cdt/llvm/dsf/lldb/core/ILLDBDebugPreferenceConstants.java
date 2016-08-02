/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.llvm.dsf.lldb.core;

/**
 * Preference constants that affect behavior in the core LLDB plugin.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ILLDBDebugPreferenceConstants {

	/**
	 * The default command for lldb-mi
	 */
	public static final String PREF_DEFAULT_LLDB_COMMAND = "defaultLLDBCommand"; //$NON-NLS-1$

	/**
	 * The value is a boolean specifying the default for whether to stop at main().
	 */
	public static final String PREF_DEFAULT_STOP_AT_MAIN = "defaultStopAtMain"; //$NON-NLS-1$

	/**
	 * The value is a string specifying the default symbol to use for the main breakpoint.
	 */
	public static final String PREF_DEFAULT_STOP_AT_MAIN_SYMBOL = "defaultStopAtMainSymbol"; //$NON-NLS-1$
}

