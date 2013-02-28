/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConsoleStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * @since 4.2
 */
@ConfinedToDsfExecutor("fConnection#getExecutor")
public class CLIEventProcessor_7_5 extends CLIEventProcessor_7_0
    implements IEventProcessor
{
    private final ICommandControlService fControl;

    public CLIEventProcessor_7_5(ICommandControlService connection, ICommandControlDMContext controlDmc) {
    	super(connection, controlDmc);
    	fControl = connection;
    }

	@Override
    public void eventReceived(Object output) {
        for (MIOOBRecord oobr : ((MIOutput)output).getMIOOBRecords()) {
            if (oobr instanceof MIConsoleStreamOutput) {
            	MIConsoleStreamOutput exec = (MIConsoleStreamOutput) oobr;

            	// Look for a printout that indicates that we cannot call inferior methods.
            	// This affects Ubuntu 32bit OS only
            	if (exec.getCString().indexOf("Cannot call inferior functions") != -1) { //$NON-NLS-1$
            		// In this case, make sure we use the 'gdb' style of dprintf
            		// and not the 'call' one.  We don't do that for remote sessions since they
            		// use the 'agent' style, which works ok.
            		if (fControl instanceof IMICommandControl) {
            			DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fControl.getSession().getId());
            			IGDBBackend backend = tracker.getService(IGDBBackend.class);
            			tracker.dispose();
            			if (backend != null && backend.getSessionType() != SessionType.REMOTE) {
            				fControl.queueCommand(
            						((IMICommandControl)fControl).getCommandFactory().createMIGDBSetDPrintfStyle(fControl.getContext(), "gdb"),//$NON-NLS-1$
            						new ImmediateDataRequestMonitor<MIInfo>() {
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
    }
}
