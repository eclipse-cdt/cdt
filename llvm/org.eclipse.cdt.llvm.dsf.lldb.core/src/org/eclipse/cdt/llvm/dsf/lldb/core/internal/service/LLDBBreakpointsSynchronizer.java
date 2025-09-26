/*******************************************************************************
 * Copyright (c) 2025 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *     John Dallaway - Initial implementation (#1319)
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.service;

import org.eclipse.cdt.dsf.mi.service.MIBreakpointsSynchronizer;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.service.DsfSession;

public class LLDBBreakpointsSynchronizer extends MIBreakpointsSynchronizer {

	public LLDBBreakpointsSynchronizer(DsfSession session) {
		super(session);
	}

	@Override
	protected boolean isTargetBreakpointConditionModified(MIBreakpoint miBpt, String condition) {
		// assume not modified due to =breakpoint-modified async record issue:
		// https://github.com/lldb-tools/lldb-mi/issues/125
		return false;
	}

}
