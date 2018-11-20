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

import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.service.commands.LLDBCommandFactory;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A service factor specific to LLDB that replaces some services in order to
 * offer a more tailored experience and mostly work around some issues with
 * LLDB-MI.
 */
public class LLDBServiceFactory extends GdbDebugServicesFactory {

	/**
	 * Constructs the {@link LLDBServiceFactory}.
	 *
	 * @param version the GDB-equivalent version of LLDB
	 * @param config the launch configuration
	 */
	public LLDBServiceFactory(String version, ILaunchConfiguration config) {
		super(version, config);
	}

	@Override
	protected ICommandControl createCommandControl(DsfSession session, ILaunchConfiguration config) {
		return new LLDBControl(session, config, new LLDBCommandFactory(session));
	}

	@Override
	protected IBreakpoints createBreakpointService(DsfSession session) {
		return new LLDBBreakpoints(session);
	}

	@Override
	protected IRunControl createRunControlService(DsfSession session) {
		return new LLDBRunControl(session);
	}

	@Override
	protected IProcesses createProcessesService(DsfSession session) {
		return new LLDBProcesses(session);
	}
}
