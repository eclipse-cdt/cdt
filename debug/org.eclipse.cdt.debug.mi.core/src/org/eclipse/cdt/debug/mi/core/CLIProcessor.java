/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core;
 
import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIDetachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalChangedEvent;

/**
 * Transmission command thread blocks on the command Queue
 * and wake cmd are available and push them to gdb out channel.
 */
public class CLIProcessor {

	MISession session;

	public CLIProcessor(MISession s) {
		session = s;
	}

	/**
	 * An attempt to discover the command type and
	 * fire an event if necessary.
	 */
	void process(CLICommand cmd) {
		String operation = cmd.getOperation().trim();
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
			// if it was a step instruction set state running
			session.getMIInferior().setRunning();
			MIEvent event = new MIRunningEvent(cmd.getToken(), type);
			session.fireEvent(event);
		} else if (isSettingBreakpoint(operation) ||
				   isSettingWatchpoint(operation) ||
				   isChangeBreakpoint(operation) ||
				   isDeletingBreakpoint(operation)) {
			// We know something change, we just do not know what.
			// So the easiest way is to let the top layer handle it. 
			session.fireEvent(new MIBreakpointChangedEvent(0));
		} else if (isSettingSignal(operation)) {
			// We do no know which signal let the upper layer find it.
			session.fireEvent(new MISignalChangedEvent("")); //$NON-NLS-1$
		} else if (isDetach(operation)) {
			// if it was a "detach" command change the state.
			session.getMIInferior().setDisconnected();
			MIEvent event = new MIDetachedEvent(cmd.getToken());
			session.fireEvent(event);
		}
	}

	int getSteppingOperationKind(String operation) {
		int type = -1;
		/* execution commands: n, next, s, step, si, stepi, u, until, finish,
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

	boolean isSettingBreakpoint(String operation) {
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

	boolean isSettingWatchpoint(String operation) {
		boolean isWatch = false;
		/* watchpoints: watch, rwatch, awatch, tbreak, rbreak, thbreak */
		if ((operation.startsWith("wa")  && "watch".indexOf(operation)   != -1) || //$NON-NLS-1$ //$NON-NLS-2$
		    (operation.startsWith("rw")  && "rwatch".indexOf(operation)  != -1) || //$NON-NLS-1$ //$NON-NLS-2$
		    (operation.startsWith("aw")  && "awatch".indexOf(operation)  != -1)) { //$NON-NLS-1$ //$NON-NLS-2$
			isWatch = true;
		}
		return  isWatch;
	}

	boolean isDeletingBreakpoint(String operation) {
		boolean isDelete = false;
		/* deleting breaks: clear, delete */
		if ((operation.startsWith("cl")  && "clear".indexOf(operation)   != -1) || //$NON-NLS-1$ //$NON-NLS-2$
		    (operation.equals("d") || (operation.startsWith("del") && "delete".indexOf(operation)  != -1))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			isDelete = true;
		}
		return isDelete;
	}

	boolean isChangeBreakpoint(String operation) {
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

	boolean isSettingSignal(String operation) {
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
	boolean isDetach(String operation) {
		return (operation.startsWith("det")  && "detach".indexOf(operation) != -1); //$NON-NLS-1$ //$NON-NLS-2$
	}


}
