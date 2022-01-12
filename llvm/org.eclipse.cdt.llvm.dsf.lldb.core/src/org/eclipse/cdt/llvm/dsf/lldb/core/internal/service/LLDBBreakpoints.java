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

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.service;

import java.util.Map;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.gdb.service.GDBBreakpoints_7_4;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.LLDBTrait;
import org.eclipse.core.runtime.Path;

/**
 * Provides service for breakpoint operations. See {@link IBreakpoints}
 *
 * This LLDB specific implementation was initially created in order to work
 * around a bug with absolute paths when specifying the breakpoint location.
 */
public class LLDBBreakpoints extends GDBBreakpoints_7_4 {

	/**
	 * Constructs the {@link LLDBBreakpoints} service.
	 *
	 * @param session
	 *            The debugging session
	 */
	public LLDBBreakpoints(DsfSession session) {
		super(session);
	}

	@Override
	protected String formatLocation(Map<String, Object> attributes) {
		if (LLDBTrait.BROKEN_BREAKPOINT_INSERT_FULL_PATH_LLVM_BUG_28709.isTraitOf(getSession())) {
			// FIXME: ***Big hack*** lldb-mi's -breakpoint-insert doesn't handle
			// locations that look like absolute paths (leading /). This will have
			// to be fixed upstream because the work-around is not ideal: we only
			// use the last segment to insert the breakpoint. This is not good if
			// there are two files of the same name in the inferior.
			// See https://llvm.org/bugs/show_bug.cgi?id=28709
			String location = super.formatLocation(attributes);
			return adjustDebuggerPath(location);
		}

		return super.formatLocation(attributes);
	}

	@Override
	public String adjustDebuggerPath(String originalPath) {
		if (LLDBTrait.BROKEN_BREAKPOINT_INSERT_FULL_PATH_LLVM_BUG_28709.isTraitOf(getSession())) {
			// FIXME: See also #formatLocation for hack explanation. This one needs
			// to be overridden for moveToLine, runToLine and stepIntoSelection
			// (Once this hack is removed, they should be tested).
			Path path = new Path(originalPath);
			if (path.isAbsolute()) {
				return path.lastSegment();
			}
		}

		return super.adjustDebuggerPath(originalPath);
	}
}
