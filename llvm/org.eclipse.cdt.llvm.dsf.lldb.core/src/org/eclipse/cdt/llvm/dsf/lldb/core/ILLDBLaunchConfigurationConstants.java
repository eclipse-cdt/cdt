/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core;

import org.eclipse.cdt.llvm.dsf.lldb.core.internal.ILLDBConstants;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.LLDBCorePlugin;

/**
 * Preference constants that are used in some ways by the LLDB launch
 * configuration.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ILLDBLaunchConfigurationConstants {

	/**
	 * Launch configuration attribute key. The value is the name of
	 * the Debuger associated with a C/C++ launch configuration.
	 */
	public static final String ATTR_DEBUG_NAME = LLDBCorePlugin.PLUGIN_ID + ".DEBUG_NAME"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUG_NAME.
	 */
	public static final String DEBUGGER_DEBUG_NAME_DEFAULT = ILLDBConstants.LLDB_MI_EXECUTABLE_NAME;
}
