/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.io.OutputStream;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess_7_3;
import org.eclipse.cdt.utils.pty.PTY;

/**
 * Specialization for GDB >= 7.3
 * @since 4.7
 */
public class StartOrRestartProcessSequence_7_3 extends StartOrRestartProcessSequence_7_0 {

	public StartOrRestartProcessSequence_7_3(DsfExecutor executor, IContainerDMContext containerDmc,
			Map<String, Object> attributes, boolean restart, DataRequestMonitor<IContainerDMContext> rm) {
		super(executor, containerDmc, attributes, restart, rm);
	}

	/** @deprecated The creation of MIInferiorProcess has been moved to the IGDBProcesses service */
	@Deprecated
	@Override
	protected MIInferiorProcess createInferiorProcess(IContainerDMContext container, OutputStream outputStream) {
		return new MIInferiorProcess_7_3(container, outputStream);
	}

	/** @deprecated The creation of MIInferiorProcess has been moved to the IGDBProcesses service */
	@Deprecated
	@Override
	protected MIInferiorProcess createInferiorProcess(IContainerDMContext container, PTY pty) {
		return new MIInferiorProcess_7_3(container, pty);
	}
}
