/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson and others.
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
 *     Marc Khouzam (Ericsson) - Update breakpoint handling for GDB >= 7.4 (Bug 389945)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * With GDB 7.4, the command 'maintenance set python print-stack' is not supported.
 * The new command "set python print-stack none|full|message" has replaced it.
 *
 * With GDB 7.4, breakpoints are handled globally for all of GDB, so our
 * IBreakpointsTargetDMContext becomes the GDBControlContext.
 *
 * @since 4.1
 */
public class GDBControl_7_4 extends GDBControl_7_2 {

	/**
	 * A command control context that is also a IBreakpointsTargetDMContext
	 */
	private class GDBControlDMContext_7_4 extends GDBControlDMContext implements IBreakpointsTargetDMContext {
		public GDBControlDMContext_7_4(String sessionId, String commandControlId) {
			super(sessionId, commandControlId);
		}
	}

	public GDBControl_7_4(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
		super(session, config, factory);
	}

	/** @since 4.4 */
	@Override
	protected ICommandControlDMContext createComandControlContext() {
		return new GDBControlDMContext_7_4(getSession().getId(), getId());
	}

	@Override
	public void setPrintPythonErrors(boolean enabled, RequestMonitor rm) {
		// With GDB 7.4, the command 'maintenance set python print-stack' has been replaced by
		// the new command "set python print-stack none|full|message".
		// Bug 367788
		String errorOption = enabled ? "full" : "none"; //$NON-NLS-1$ //$NON-NLS-2$
		queueCommand(getCommandFactory().createMIGDBSetPythonPrintStack(getContext(), errorOption),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}
}
