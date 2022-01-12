/*******************************************************************************
 * Copyright (c) 2016 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 * Umair Sair (Mentor Graphics) - Debugging is stuck when "command aborted" occurs on step return (bug 550165)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.mi.service.command.commands.MICommand;
import org.eclipse.cdt.dsf.mi.service.command.events.MIErrorEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResultRecord;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * In some cases GDB reports 'exec-*' commands failure after the '^running' event is fired.
 * For instance, if an invalid breakpoint is set no error is reported but the consequent
 * 'exec-continue' command fails.
 *
 * 36-exec-continue --thread 1
 * 36^running
 * *running,thread-id="all"
 * (gdb)
 * &"Warning:\n"
 * &"Cannot insert breakpoint 2.\n"
 * &"Cannot access memory at address 0x0\n"
 * &"\n"
 * 36^error,msg="Command aborted."
 * (gdb)
 *
 * This class handles these type of situations by firing MIErrorEvent when such an error appears.
 *
 * @since 5.3
 */
public class MIAsyncErrorProcessor implements IEventProcessor {

	final private ICommandControlService fCommandControl;

	private Map<IExecutionDMContext, Integer> fRunCommands = new HashMap<>();

	public MIAsyncErrorProcessor(ICommandControlService commandControl) {
		super();
		fCommandControl = commandControl;
		fCommandControl.addCommandListener(this);
		fCommandControl.addEventListener(this);
	}

	@Override
	public void eventReceived(Object output) {
		MIResultRecord rr = ((MIOutput) output).getMIResultRecord();
		// Handling the asynchronous error case, i.e. when the "<token>^running" event
		// appears before "<token>^error, msg=<error_message>" for run control commands.
		if (rr != null && MIResultRecord.ERROR.equals(rr.getResultClass())) {
			handleAsyncError((MIOutput) output);
		}
	}

	@Override
	public void commandQueued(ICommandToken token) {
	}

	@Override
	public void commandSent(ICommandToken token) {
	}

	@Override
	public void commandRemoved(ICommandToken token) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void commandDone(ICommandToken token, ICommandResult result) {
		if (token.getCommand() instanceof MICommand<?> && result instanceof MIInfo && ((MIInfo) result).isRunning()) {
			IDMContext ctx = ((MICommand<MIInfo>) token.getCommand()).getContext();
			IExecutionDMContext execDMCtx = DMContexts.getAncestorOfType(ctx, IExecutionDMContext.class);
			if (execDMCtx != null) {
				MIResultRecord rr = ((MIInfo) result).getMIOutput().getMIResultRecord();
				if (rr != null) {
					fRunCommands.put(execDMCtx, Integer.valueOf(rr.getToken()));
				}
			}
		}
	}

	@Override
	public void dispose() {
		fCommandControl.removeCommandListener(this);
		fCommandControl.removeEventListener(this);
		fRunCommands.clear();
	}

	protected ICommandControlService getCommandControl() {
		return fCommandControl;
	}

	protected void handleAsyncError(MIOutput output) {
		int token = output.getMIResultRecord().getToken();
		for (Entry<IExecutionDMContext, Integer> entry : fRunCommands.entrySet()) {
			if (entry.getValue().intValue() == token && DsfSession.isSessionActive(entry.getKey().getSessionId())) {
				fireStoppedEvent(output, entry.getKey());
			}
		}
	}

	protected void fireStoppedEvent(final MIOutput output, final IExecutionDMContext ctx) {
		DsfSession session = DsfSession.getSession(ctx.getSessionId());
		int token = output.getMIResultRecord().getToken();
		session.dispatchEvent(
				MIErrorEvent.parse(ctx, token, output.getMIResultRecord().getMIResults(), output.getMIOOBRecords()),
				null);
	}
}
