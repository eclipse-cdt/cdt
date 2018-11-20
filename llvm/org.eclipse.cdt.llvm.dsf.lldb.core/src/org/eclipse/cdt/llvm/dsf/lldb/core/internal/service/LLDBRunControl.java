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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.gdb.service.GDBRunControl_7_0;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Provides service for controlling the process. See {@link IRunControl}
 *
 * This LLDB specific implementation was initially created in order to disable
 * "move to line" and "resume at line" because the -exec-jump MI command was not
 * implemented.
 */
public class LLDBRunControl extends GDBRunControl_7_0 {

	/**
	 * Constructs the {@link LLDBRunControl} service.
	 *
	 * @param session
	 *            The debugging session
	 */
	public LLDBRunControl(DsfSession session) {
		super(session);
	}

	@Override
	public void canMoveToAddress(IExecutionDMContext context, IAddress address, boolean resume,
			DataRequestMonitor<Boolean> rm) {
		// FIXME: LLDB-MI doesn't implement -exec-jump so it will fail. So
		// disable "move to address" (disassembly) for now.
		rm.setData(false);
		rm.done();
	}

	@Override
	public void canMoveToLine(IExecutionDMContext context, String sourceFile, int lineNumber, boolean resume,
			DataRequestMonitor<Boolean> rm) {
		// FIXME: LLDB-MI doesn't implement -exec-jump so it will fail. So
		// disable "move to line" for now.
		rm.setData(false);
		rm.done();
	}
}
