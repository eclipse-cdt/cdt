/*******************************************************************************
 * Copyright (c) 2008, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson AB          - Additional handling of events 
 *     Ericsson             - Version 7.0  
 *     Mikhail Khodjaiants (Mentor Graphics) - Refactor common code in GDBControl* classes (bug 372795)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.ISignals.ISignalsDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInterpreterExecConsole;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointChangedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIDetachedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISignalChangedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * GDB debugger output listener.
 * @since 1.1
 */
@ConfinedToDsfExecutor("fConnection#getExecutor")
public class CLIEventProcessor_7_0
    implements IEventProcessor
{
    private final ICommandControlService fCommandControl;
    
    private final DsfServicesTracker fServicesTracker;
    
    public CLIEventProcessor_7_0(ICommandControlService connection, ICommandControlDMContext controlDmc) {
        fCommandControl = connection;
        fServicesTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fCommandControl.getSession().getId());
        fCommandControl.addCommandListener(this);
        fCommandControl.addEventListener(this);
    }

	@Override
	public void dispose() {
        fCommandControl.removeCommandListener(this);
        fCommandControl.removeEventListener(this);
        fServicesTracker.dispose();
    }
    
	@Override
    public void commandSent(ICommandToken token) {
        if (token.getCommand() instanceof CLICommand<?>) {
            processStateChanges( (CLICommand<?>)token.getCommand() );
        }
        else if (token.getCommand() instanceof MIInterpreterExecConsole<?>) {
            processStateChanges( (MIInterpreterExecConsole<?>)token.getCommand() );
        }
    }
    
	@Override
    public void commandDone(ICommandToken token, ICommandResult result) {
        if (token.getCommand() instanceof CLICommand<?>) {
            processSettingChanges( (CLICommand<?>)token.getCommand() );
        }
        else if (token.getCommand() instanceof MIInterpreterExecConsole<?>) {
            processSettingChanges( (MIInterpreterExecConsole<?>)token.getCommand() );
        }
    }
    
	@Override
    public void commandQueued(ICommandToken token) {
        // No action 
    }
    
	@Override
    public void commandRemoved(ICommandToken token) {
        // No action 
    }
    
	@Override
    public void eventReceived(Object output) {
    }


    private void processStateChanges(CLICommand<? extends ICommandResult> cmd) {
        String operation = cmd.getOperation().trim();
        // In refactoring we are no longer generating the token id as
        // part of the command. It is passed here and stored away  and
        // then never really used. So it has just been changed to 0.
        processStateChanges(0, operation);
    }

    private void processStateChanges(MIInterpreterExecConsole<? extends ICommandResult> exec) {
        String[] operations = exec.getParameters();
        if (operations != null && operations.length > 0) {
        	// In refactoring we are no longer generating the token id as
            // part of the command. It is passed here and stored away  and
            // then never really used. So it has just been changed to 0.
            processStateChanges(0, operations[0]);
        }
    }

    private void processStateChanges(int token, String operation) {
        // Get the command name.
        int indx = operation.indexOf(' ');
        if (indx != -1) {
            operation = operation.substring(0, indx).trim();
        } else {
            operation = operation.trim();
        }

        // Check the type of command
        int type = getSteppingOperationKind(operation);
        if (type != -1) {
        	// Should set MIrunControlEventProcessor_7_0.fLastRunningCmdType
        }
    }

    /**
     * An attempt to discover the command type and
     * fire an event if necessary.
     */
    private void processSettingChanges(CLICommand<?> cmd) {
        String operation = cmd.getOperation().trim();
        // In refactoring we are no longer genwerating the token id as
        // part of the command. It is passed here and stored away  and
        // then never really used. So it has just been changed to 0.
        processSettingChanges(cmd.getContext(), 0, operation);
    }

    private void processSettingChanges(MIInterpreterExecConsole<?> exec) {
        String[] operations = exec.getParameters();
        if (operations != null && operations.length > 0) {
        	// In refactoring we are no longer genwerating the token id as
            // part of the command. It is passed here and stored away  and
            // then never really used. So it has just been changed to 0.
            processSettingChanges(exec.getContext(), 0, operations[0]);
        }
    }

    private void processSettingChanges(IDMContext dmc, int token, String operation) {
        // Get the command name.
        int indx = operation.indexOf(' ');
        if (indx != -1) {
            operation = operation.substring(0, indx).trim();
        } else {
            operation = operation.trim();
        }

        // Check the type of command

        if (isSettingBreakpoint(operation) ||
                isSettingWatchpoint(operation) ||
                isChangeBreakpoint(operation) ||
                isDeletingBreakpoint(operation)) 
        {
            // We know something change, we just do not know what.
            // So the easiest way is to let the top layer handle it. 
        	IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(dmc, IBreakpointsTargetDMContext.class);
        	if (bpTargetDmc != null) {
        		MIEvent<?> event = new MIBreakpointChangedEvent(bpTargetDmc, 0);
            	fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
            }
        } else if (isSettingSignal(operation)) {
        	// We do no know which signal let the upper layer find it.
        	ISignalsDMContext signalDmc = DMContexts.getAncestorOfType(dmc, ISignalsDMContext.class);
        	if (signalDmc != null) {
        		MIEvent<?> event = new MISignalChangedEvent(signalDmc, ""); //$NON-NLS-1$
        		fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
        	}
        } else if (isDetach(operation)) {
            // if it was a "detach" command change the state.
        	ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
        	if (controlDmc != null) {
        		MIEvent<?> event = new MIDetachedEvent(controlDmc, token);
        		fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
        	}
        }
    }

    private static int getSteppingOperationKind(String operation) {
        int type = -1;
        /* execution commands: n, next, s, step, si, stepi, u, until, finish, rerurn,
           c, continue, fg */
        if (operation.equals("n") || operation.equals("next")) { //$NON-NLS-1$ //$NON-NLS-2$
            type = MIRunningEvent.NEXT;
        } else if (operation.equals("ni") || operation.equals("nexti")) { //$NON-NLS-1$ //$NON-NLS-2$
            type = MIRunningEvent.NEXTI;
        } else if (operation.equals("s") || operation.equals("step")) { //$NON-NLS-1$ //$NON-NLS-2$
            type = MIRunningEvent.STEP;
        } else if (operation.equals("si") || operation.equals("stepi")) { //$NON-NLS-1$ //$NON-NLS-2$
            type = MIRunningEvent.STEPI;
        } else if (operation.equals("u") || //$NON-NLS-1$
               (operation.startsWith("unt") &&  "until".indexOf(operation) != -1)) { //$NON-NLS-1$ //$NON-NLS-2$
                type = MIRunningEvent.UNTIL;
        } else if (operation.startsWith("fin") && "finish".indexOf(operation) != -1) { //$NON-NLS-1$ //$NON-NLS-2$
            type = MIRunningEvent.FINISH;
		} else if (operation.startsWith("ret") && "return".indexOf(operation) != -1) { //$NON-NLS-1$ //$NON-NLS-2$
			type = MIRunningEvent.RETURN;
        } else if (operation.equals("c") || operation.equals("fg") || //$NON-NLS-1$ //$NON-NLS-2$
               (operation.startsWith("cont") && "continue".indexOf(operation) != -1)) { //$NON-NLS-1$ //$NON-NLS-2$
            type = MIRunningEvent.CONTINUE;
        } else if (operation.startsWith("sig") && "signal".indexOf(operation) != -1) { //$NON-NLS-1$ //$NON-NLS-2$
            type = MIRunningEvent.CONTINUE;
        } else if (operation.startsWith("j") && "jump".indexOf(operation) != -1) { //$NON-NLS-1$ //$NON-NLS-2$
            type = MIRunningEvent.CONTINUE;
        } else if (operation.equals("r") || operation.equals("run")) { //$NON-NLS-1$ //$NON-NLS-2$
            type = MIRunningEvent.CONTINUE;
        }
        return type;
    }

    /**
     * Return true if the operation is a stepping operation.
     * 
     * @param operation
     * @return
     */
    public static boolean isSteppingOperation(String operation) {
        int type = getSteppingOperationKind(operation);
        return type != -1;
    }

    private boolean isSettingBreakpoint(String operation) {
        boolean isbreak = false;
        /* breakpoints: b, break, hbreak, tbreak, rbreak, thbreak */
        /* watchpoints: watch, rwatch, awatch, tbreak, rbreak, thbreak */
        if ((operation.startsWith("b")   && "break".indexOf(operation)   != -1) || //$NON-NLS-1$ //$NON-NLS-2$
            (operation.startsWith("tb")  && "tbreak".indexOf(operation)  != -1) || //$NON-NLS-1$ //$NON-NLS-2$
            (operation.startsWith("hb")  && "hbreak".indexOf(operation)  != -1) || //$NON-NLS-1$ //$NON-NLS-2$
            (operation.startsWith("thb") && "thbreak".indexOf(operation) != -1) || //$NON-NLS-1$ //$NON-NLS-2$
            (operation.startsWith("rb")  && "rbreak".indexOf(operation)  != -1)) { //$NON-NLS-1$ //$NON-NLS-2$
            isbreak = true;
        }
        return isbreak;
    }

    private boolean isSettingWatchpoint(String operation) {
        boolean isWatch = false;
        /* watchpoints: watch, rwatch, awatch, tbreak, rbreak, thbreak */
        if ((operation.startsWith("wa")  && "watch".indexOf(operation)   != -1) || //$NON-NLS-1$ //$NON-NLS-2$
            (operation.startsWith("rw")  && "rwatch".indexOf(operation)  != -1) || //$NON-NLS-1$ //$NON-NLS-2$
            (operation.startsWith("aw")  && "awatch".indexOf(operation)  != -1)) { //$NON-NLS-1$ //$NON-NLS-2$
            isWatch = true;
        }
        return  isWatch;
    }

    private boolean isDeletingBreakpoint(String operation) {
        boolean isDelete = false;
        /* deleting breaks: clear, delete */
        if ((operation.startsWith("cl")  && "clear".indexOf(operation)   != -1) || //$NON-NLS-1$ //$NON-NLS-2$
            (operation.equals("d") || (operation.startsWith("del") && "delete".indexOf(operation)  != -1))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            isDelete = true;
        }
        return isDelete;
    }

    private boolean isChangeBreakpoint(String operation) {
        boolean isChange = false;
        /* changing breaks: enable, disable */
        if ((operation.equals("dis") || operation.equals("disa") || //$NON-NLS-1$ //$NON-NLS-2$
            (operation.startsWith("disa")  && "disable".indexOf(operation) != -1)) || //$NON-NLS-1$ //$NON-NLS-2$
            (operation.equals("en") || (operation.startsWith("en") && "enable".indexOf(operation) != -1)) || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            (operation.startsWith("ig") && "ignore".indexOf(operation) != -1) || //$NON-NLS-1$ //$NON-NLS-2$
            (operation.startsWith("cond") && "condition".indexOf(operation) != -1)) { //$NON-NLS-1$ //$NON-NLS-2$
            isChange = true;
        }
        return isChange;
    }

    private boolean isSettingSignal(String operation) {
        boolean isChange = false;
        /* changing signal: handle, signal */
        if (operation.startsWith("ha")  && "handle".indexOf(operation) != -1) { //$NON-NLS-1$ //$NON-NLS-2$
            isChange = true;
        }
        return isChange;
    }

    /**
     * @param operation
     * @return
     */
    private boolean isDetach(String operation) {
        return (operation.startsWith("det")  && "detach".indexOf(operation) != -1); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
