/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia (QNX)- Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.breakpoint.actions;

import org.eclipse.cdt.debug.core.breakpointactions.ICLIDebugActionEnabler;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.CLIEventProcessor;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInterpreterExecConsole;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 *
 * This class permits to execute custom user debugger commands through cli/mi bridge
 *
 * @since 5.0
 */
public class CLIDebugActionEnabler implements ICLIDebugActionEnabler {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fServiceTracker;
	private final ICommandControlDMContext fContext;

	/**
	 * @param executor
	 * @param serviceTracker
	 * @param context
	 */
	public CLIDebugActionEnabler(DsfExecutor executor, DsfServicesTracker serviceTracker, IDMContext context) {
		fExecutor = executor;
		fServiceTracker = serviceTracker;
		fContext = DMContexts.getAncestorOfType(context, ICommandControlDMContext.class);
		assert fContext != null;
	}

	@Override
	public void execute(String commandmulti) throws Exception {
		String[] commands = commandmulti.split("\\r?\\n"); //$NON-NLS-1$
		for (int j = 0; j < commands.length; ++j) {
			String single = commands[j];
			executeSingleCommand(single);
		}
	}

	private boolean isMIOperation(String operation) {
		if (operation.startsWith("-")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	private void executeSingleCommand(String str) {
		// Do not use the interpreter-exec for stepping operation the UI will fall out of step.
		// Also, do not use "interpreter-exec console" for MI commands.
		ICommand<MIInfo> cmd;
		if (!isMIOperation(str) && !CLIEventProcessor.isSteppingOperation(str)) {
			cmd = new MIInterpreterExecConsole<>(fContext, str);
		} else {
			cmd = new CLICommand<>(fContext, str);
		}
		fExecutor.execute(new DsfRunnable() {
			@Override
			public void run() {
				// TODO: for print command would be nice to redirect to gdb console
				ICommandControlService commandControl = fServiceTracker.getService(ICommandControlService.class);
				if (commandControl != null) {
					commandControl.queueCommand(cmd, new ImmediateDataRequestMonitor<>());
				} else {
					// Should not happen, so log the situation but then ignore it
					GdbPlugin.log(new Status(IStatus.INFO, GdbPlugin.PLUGIN_ID,
							"Unable to find service to execute breakpoint command")); //$NON-NLS-1$
				}
			}
		});
	}
}
