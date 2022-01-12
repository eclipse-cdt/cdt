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

package org.eclipse.cdt.llvm.dsf.lldb.core.internal;

/**
 * Constants related to the LLDB debugger itself.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ILLDBConstants {

	/**
	 * The executable name for lldb-mi
	 */
	public static final String LLDB_MI_EXECUTABLE_NAME = "lldb-mi"; //$NON-NLS-1$

	/**
	 * The executable name for lldb
	 */
	public static final String LLDB_EXECUTABLE_NAME = "lldb"; //$NON-NLS-1$
}
