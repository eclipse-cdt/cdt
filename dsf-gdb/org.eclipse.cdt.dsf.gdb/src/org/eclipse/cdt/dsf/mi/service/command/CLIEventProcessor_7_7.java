/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetDPrintfStyle;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConsoleStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @since 4.4
 */
@ConfinedToDsfExecutor("fConnection#getExecutor")
public class CLIEventProcessor_7_7 extends CLIEventProcessor_7_0 {
	private final ICommandControlService fControl;
	private boolean fResetDPrintfStyle;

	public CLIEventProcessor_7_7(ICommandControlService connection, ICommandControlDMContext controlDmc) {
		super(connection, controlDmc);
		fControl = connection;
	}

	@Override
	public void eventReceived(Object output) {
		if (!fResetDPrintfStyle) {
			// Only do this if we haven't already reset the dprintf style
			for (MIOOBRecord oobr : ((MIOutput) output).getMIOOBRecords()) {
				if (oobr instanceof MIConsoleStreamOutput) {
					MIConsoleStreamOutput exec = (MIConsoleStreamOutput) oobr;

					// Look for a printout that indicates that we cannot call inferior methods.
					// This affects Ubuntu 32bit OS
					if (exec.getCString().indexOf("Cannot call inferior functions") != -1) { //$NON-NLS-1$
						// In this case, make sure we use the 'gdb' style of dprintf
						// and not the 'call' one.
						fResetDPrintfStyle = true;
						if (fControl instanceof IMICommandControl) {
							CommandFactory factory = ((IMICommandControl) fControl).getCommandFactory();
							fControl.queueCommand(factory.createMIGDBSetDPrintfStyle(fControl.getContext(),
									MIGDBSetDPrintfStyle.GDB_STYLE), new ImmediateDataRequestMonitor<MIInfo>() {
										@Override
										protected void handleCompleted() {
											// We accept errors
										}
									});
						}
					}
				}
			}
		}
		super.eventReceived(output);
	}
}
