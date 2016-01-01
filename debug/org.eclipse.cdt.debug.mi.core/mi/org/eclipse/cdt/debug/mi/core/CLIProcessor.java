/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core;
 
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.cdt.debug.mi.core.command.MIInterpreterExecConsole;
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
	void processStateChanges(CLICommand cmd) {
		String operation = cmd.getOperation().trim();
		processStateChanges(cmd.getToken(), operation);
	}

	void processStateChanges(MIInterpreterExecConsole exec) {
		String[] operations = exec.getParameters();
		if (operations != null && operations.length > 0) {
			processStateChanges(exec.getToken(), operations[0]);
		}
	}

	void processStateChanges(int token, String op) {
		String operation = op;
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
			MIEvent event = new MIRunningEvent(session, token, type);
			session.fireEvent(event);
		}
	}

	/**
	 * An attempt to discover the command type and
	 * fire an event if necessary.
	 */
	void processSettingChanges(CLICommand cmd) {
		String operation = cmd.getOperation().trim();
		processSettingChanges(cmd.getToken(), operation);
	}

	void processSettingChanges(MIInterpreterExecConsole exec) {
		String[] operations = exec.getParameters();
		if (operations != null && operations.length > 0) {
			processSettingChanges(exec.getToken(), operations[0]);
		}
	}

	void processSettingChanges(int token, String command) {
		// Get the command name.
		String operation = command;
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
				isDeletingBreakpoint(operation)) {
			// We know something change, we just do not know what.
			// So the easiest way is to let the top layer handle it.
			// But we can parse the command line to hint the top layer 
			// on the breakpoint type.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=135250
			int hint = MIBreakpointChangedEvent.HINT_NONE;
			if (isSettingBreakpoint(operation)) {
				hint = getBreakpointHint(command);
			}
			session.fireEvent(new MIBreakpointChangedEvent(session, 0, hint));
		} else if (isSettingSignal(operation)) {
			// We do no know which signal let the upper layer find it.
			session.fireEvent(new MISignalChangedEvent(session, "")); //$NON-NLS-1$
		} else if (isDetach(operation)) {
			// if it was a "detach" command change the state.
			session.getMIInferior().setDisconnected();
			MIEvent event = new MIDetachedEvent(session, token);
			session.fireEvent(event);
		}
	}

	static int getSteppingOperationKind(String operation) {
		int type = -1;
		/* execution commands: n, next, s, step, si, stepi, u, until, finish, return,
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

	boolean isSettingBreakpoint(String operation) {
		boolean isbreak = false;
		/* breakpoints: b, break, hbreak, tbreak, rbreak, thbreak */
		/* watchpoints: watch, rwatch, awatch, tbreak, rbreak, thbreak */
		if ((operation.startsWith("b")   && "break".indexOf(operation)   != -1) || //$NON-NLS-1$ //$NON-NLS-2$
		    (operation.startsWith("tb")  && "tbreak".indexOf(operation)  != -1) || //$NON-NLS-1$ //$NON-NLS-2$
		    (operation.startsWith("hb")  && "hbreak".indexOf(operation)  != -1) || //$NON-NLS-1$ //$NON-NLS-2$
		    (operation.startsWith("thb") && "thbreak".indexOf(operation) != -1) || //$NON-NLS-1$ //$NON-NLS-2$
		    (operation.startsWith("rb")  && "rbreak".indexOf(operation)  != -1) ||  //$NON-NLS-1$ //$NON-NLS-2$
		    (operation.startsWith("catch"))) {  //$NON-NLS-1$
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

	int getBreakpointHint(String command) {
		StringTokenizer st = new StringTokenizer(command);
		// get operation
		String op = st.nextToken();
	    if (op.startsWith("rb")  && "rbreak".indexOf(op) != -1) { //$NON-NLS-1$ //$NON-NLS-2$
	    	// only function breakpoints can be set using rbreak
	    	return MIBreakpointChangedEvent.HINT_NEW_FUNCTION_BREAKPOINT;
	    }
	    if (op.equals("catch")) { //$NON-NLS-1$
	    	return MIBreakpointChangedEvent.HINT_NEW_EVENTBREAKPOINT;
	    }
	    if ( !st.hasMoreTokens() ) {
	    	// "break" with no arguments
	    	return MIBreakpointChangedEvent.HINT_NEW_LINE_BREAKPOINT;
	    }
	    String token = st.nextToken();
	    if ("if".equals(token) || "ignore".equals(token) || token.charAt(0) == '+' || token.charAt(0) == '-') { //$NON-NLS-1$ //$NON-NLS-2$
	    	// conditional "break" with no location argument
	    	// or "break +/- offset"
	    	return MIBreakpointChangedEvent.HINT_NEW_LINE_BREAKPOINT;
	    }
	    if (token.charAt(0) == '*') {
	    	return MIBreakpointChangedEvent.HINT_NEW_ADDRESS_BREAKPOINT;
	    }
	    int index = token.lastIndexOf( ':' );
	    String lineNumber = token;
	    if (index != -1 && index+1 < token.length()) {
	    	lineNumber = token.substring(index+1, token.length());
	    }
	    try {
	    	Integer.parseInt( lineNumber );
	    }
	    catch(NumberFormatException e) {
	    	return MIBreakpointChangedEvent.HINT_NEW_FUNCTION_BREAKPOINT;
	    }
    	return MIBreakpointChangedEvent.HINT_NEW_LINE_BREAKPOINT;
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
