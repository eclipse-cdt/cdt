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

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.launching;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence_7_2;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * A LLDB-specific launch sequence that was initially created to work around the
 * fact that LLDB always has to run in async mode, even in all-stop.
 */
public class LLDBFinalLaunchSequence extends FinalLaunchSequence_7_2 {

	/**
	 * Constructs the {@link LLDBFinalLaunchSequence}.
	 *
	 * @param session
	 *            The debugging session
	 * @param attributes
	 *            the launch configuration attributes
	 * @param rm
	 *            a request monitor that will indicate when the sequence is
	 *            completed
	 */
	public LLDBFinalLaunchSequence(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
	}

	@Execute
	@Override
	public void stepSetNonStop(RequestMonitor requestMonitor) {
		// LLDB doesn't support non-stop and target-async cannot be disabled so
		// do not do anything in this step
		requestMonitor.done();
	}

}
