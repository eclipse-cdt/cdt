/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal;

import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.launching.LLDBLaunch;
import org.eclipse.debug.core.ILaunch;

/**
 * Constants related to traits of LLDB.
 *
 * A trait describe what LLDB can or cannot do. This is used to create a mapping
 * between LLDB versions and what they can do so that the code can do the proper
 * thing. For example, earlier versions would not be able to work with
 * breakpoints inserted with a full path. The trait
 * {@link #BROKEN_BREAKPOINT_INSERT_FULL_PATH_LLVM_BUG_28709} was introduced
 * because of bug https://llvm.org/bugs/show_bug.cgi?id=28709
 *
 * The code can call {@link #isTraitOf(DsfSession)} to know whether or know the
 * session (i.e. running LLDB) has that trait.
 *
 * @noreference This enum is not intended to be referenced by clients.
 */
public enum LLDBTrait {
	/**
	 * Trait for LLDBs affected by https://llvm.org/bugs/show_bug.cgi?id=28709.
	 * Inserting a breakpoint with full path would not work.
	 * TODO: Remove for versions < 4.0.0 eventually
	 */
	BROKEN_BREAKPOINT_INSERT_FULL_PATH_LLVM_BUG_28709,
	/**
	 * Trait for LLDBs that do not have "-gdb-set breakpoint pending"
	 * implemented. It was added in LLDB 8.0 as of r345563.
	 * See https://reviews.llvm.org/D52953
	 */
	MISSING_GDB_SET_BREAKPOINT_PENDING;

	/**
	 * Returns whether or not the given session has this trait.
	 *
	 * @param session the debug session
	 * @return true if the given session has this trait for the LLDB, false otherwise
	 */
	public boolean isTraitOf(DsfSession session) {
		Object launch = session.getModelAdapter(ILaunch.class);
		if (!(launch instanceof LLDBLaunch)) {
			LLDBCorePlugin.log(
					"Could not determine if session has LLDB trait " + this + " because launch is of unexpected type " //$NON-NLS-1$ //$NON-NLS-2$
							+ launch.getClass() + ". Some features might not work as expected.", //$NON-NLS-1$
					new Throwable());
			return false;
		}
		LLDBLaunch lldbLaunch = (LLDBLaunch) launch;
		return lldbLaunch.hasTrait(this);
	}
}
