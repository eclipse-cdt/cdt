/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core;
 
import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointChangedEvent;
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
			session.fireEvent(new MISignalChangedEvent(""));
		}
	}

	int getSteppingOperationKind(String operation) {
		int type = -1;
		/* execution commands: n, next, s, step, si, stepi, u, until, finish,
		   c, continue, fg */
		if (operation.equals("n") || operation.equals("next")) {
			type = MIRunningEvent.NEXT;
		} else if (operation.equals("ni") || operation.equals("nexti")) {
			type = MIRunningEvent.NEXTI;
		} else if (operation.equals("s") || operation.equals("step")) {
			type = MIRunningEvent.STEP;
		} else if (operation.equals("si") || operation.equals("stepi")) {
			type = MIRunningEvent.STEPI;
		} else if (operation.equals("u") ||
			   (operation.startsWith("unt") &&  "until".indexOf(operation) != -1)) {
				type = MIRunningEvent.UNTIL;
		} else if (operation.startsWith("fin") && "finish".indexOf(operation) != -1) {
			type = MIRunningEvent.FINISH;
		} else if (operation.equals("c") || operation.equals("fg") ||
			   (operation.startsWith("cont") && "continue".indexOf(operation) != -1)) {
			type = MIRunningEvent.CONTINUE;
		} else if (operation.startsWith("sig") && "signal".indexOf(operation) != -1) {
			type = MIRunningEvent.CONTINUE;
		} else if (operation.startsWith("j") && "jump".indexOf(operation) != -1) {
			type = MIRunningEvent.CONTINUE;
		}
		return type;
	}

	boolean isSettingBreakpoint(String operation) {
		boolean isbreak = false;
		/* breakpoints: b, break, hbreak, tbreak, rbreak, thbreak */
		/* watchpoints: watch, rwatch, awatch, tbreak, rbreak, thbreak */
		if ((operation.startsWith("b")   && "break".indexOf(operation)   != -1) ||
		    (operation.startsWith("tb")  && "tbreak".indexOf(operation)  != -1) ||
		    (operation.startsWith("hb")  && "hbreak".indexOf(operation)  != -1) ||
		    (operation.startsWith("thb") && "thbreak".indexOf(operation) != -1) ||
		    (operation.startsWith("rb")  && "rbreak".indexOf(operation)  != -1)) {
			isbreak = true;
		}
		return isbreak;
	}

	boolean isSettingWatchpoint(String operation) {
		boolean isWatch = false;
		/* watchpoints: watch, rwatch, awatch, tbreak, rbreak, thbreak */
		if ((operation.startsWith("wa")  && "watch".indexOf(operation)   != -1) ||
		    (operation.startsWith("rw")  && "rwatch".indexOf(operation)  != -1) ||
		    (operation.startsWith("aw")  && "awatch".indexOf(operation)  != -1)) {
			isWatch = true;
		}
		return  isWatch;
	}

	boolean isDeletingBreakpoint(String operation) {
		boolean isDelete = false;
		/* deleting breaks: clear, delete */
		if ((operation.startsWith("cl")  && "clear".indexOf(operation)   != -1) ||
		    (operation.equals("d") || (operation.startsWith("del") && "delete".indexOf(operation)  != -1))) {
			isDelete = true;
		}
		return isDelete;
	}

	boolean isChangeBreakpoint(String operation) {
		boolean isChange = false;
		/* changing breaks: enable, disable */
		if ((operation.equals("dis") || operation.equals("disa") ||
			(operation.startsWith("disa")  && "disable".indexOf(operation) != -1)) ||
		    (operation.equals("en") || (operation.startsWith("en") && "enable".indexOf(operation) != -1)) ||
		    (operation.startsWith("ig") && "ignore".indexOf(operation) != -1) ||
		    (operation.startsWith("cond") && "condition".indexOf(operation) != -1)) {
			isChange = true;
		}
		return isChange;
	}

	boolean isSettingSignal(String operation) {
		boolean isChange = false;
		/* changing signal: handle, signal */
		if (operation.startsWith("ha")  && "handle".indexOf(operation) != -1) {
			isChange = true;
		}
		return isChange;
	}

}
