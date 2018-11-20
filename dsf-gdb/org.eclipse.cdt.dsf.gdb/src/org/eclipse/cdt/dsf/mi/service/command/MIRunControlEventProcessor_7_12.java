/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command;

import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.RawCommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConsoleStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * Listens to events on the MI channel and takes proper action.
 * Specialization for GDB 7.12.
 *
 * @since 5.3
 */
public class MIRunControlEventProcessor_7_12 extends MIRunControlEventProcessor_7_0

{
	private final AbstractMIControl fCommandControl;
	private final ICommandControlDMContext fControlDmc;

	public MIRunControlEventProcessor_7_12(AbstractMIControl connection, ICommandControlDMContext controlDmc) {
		super(connection, controlDmc);
		fCommandControl = connection;
		fControlDmc = controlDmc;
	}

	@Override
	public void eventReceived(Object output) {
		for (MIOOBRecord oobr : ((MIOutput) output).getMIOOBRecords()) {
			if (oobr instanceof MIConsoleStreamOutput) {
				MIConsoleStreamOutput stream = (MIConsoleStreamOutput) oobr;
				if (stream.getCString().indexOf("(y or n)") != -1 && //$NON-NLS-1$
						stream.getCString().indexOf("[answered ") == -1) {//$NON-NLS-1$
					// We have a query on MI that was not automatically answered by GDB!.
					// That is not something GDB should do.
					// The user cannot answer since it is on MI, so we need to answer
					// ourselves.  If we don't GDB will hang forever, waiting for that
					// answer. We always answer 'yes' although
					// we can't be sure it is the right answer, but it is better
					// than simply hanging there forever.
					fCommandControl.queueCommand(new RawCommand(fControlDmc, "y"), //$NON-NLS-1$
							new ImmediateDataRequestMonitor<MIInfo>());
				}
			}
		}

		super.eventReceived(output);
	}
}
