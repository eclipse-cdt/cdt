/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core;
 
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;

/**
 * Transmission command thread blocks on the command Queue
 * and wake cmd are available and push them to gdb out channel.
 */
public class TxThread extends Thread {

	MISession session;
	int token;

	public TxThread(MISession s) {
		super("MI TX Thread");
		session = s;
		// start at one, zero is special means no token.
		token = 1;
	}

	public void run () {
		try {
			// signal by the session of time to die.
			OutputStream out;
			while ((out = session.getChannelOutputStream()) != null) {
				Command cmd = null;
				CommandQueue txQueue = session.getTxQueue();
				// removeCommand() will block until a command is available.
				try {
					cmd = txQueue.removeCommand();
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}

				if (cmd != null) {
					// Give the command a token and increment. 
					cmd.setToken(token++);
					// Move to the RxQueue only if we have
					// a valid token, this is to permit input(HACK!)
					// or commands that do not want to wait for responses.
					Thread rx = session.getRxThread();
					if (cmd.getToken() > 0 && rx != null && rx.isAlive()) {
						CommandQueue rxQueue = session.getRxQueue();
						rxQueue.addCommand(cmd);
					} else {
						synchronized (cmd) {
							cmd.notifyAll();
						}
					}
					
					// May need to fire event.
					if (cmd instanceof CLICommand) {
						processCLICommand((CLICommand)cmd);
					}
				
					// shove in the pipe
					String str = cmd.toString();
					if (out != null) {
						out.write(str.getBytes());
						out.flush();
					}
				}
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}

		// Clear the queue and notify any command waiting, we are going down.
                CommandQueue txQueue = session.getTxQueue();
                if (txQueue != null) {
                        Command[] cmds = txQueue.clearCommands();
                        for (int i = 0; i < cmds.length; i++) {
                                synchronized (cmds[i]) {
                                        cmds[i].notifyAll();
                                }
                        }
                }
	}
	
	/**
	 * An attempt to discover the command type and
	 * fire an event if necessary.
	 */
	void processCLICommand(CLICommand cmd) {
		String operation = cmd.getOperation();
		int indx = operation.indexOf(' ');
		if (indx != -1) {
			operation = operation.substring(0, indx).trim();
		} else {
			operation = operation.trim();
		}

		// Check the type of command
		int type = -1;
		// if it was a step instruction set state running
		if (operation.equals("n") || operation.equals("next")) {
			type = MIRunningEvent.NEXT;
		} else if (operation.equals("ni") || operation.equals("nexti")) {
			type = MIRunningEvent.NEXTI;
		} else if (operation.equals("s") || operation.equals("step")) {
			type = MIRunningEvent.STEP;
		} else if (operation.equals("si") || operation.equals("stepi")) {
			type = MIRunningEvent.STEPI;
		} else if (operation.equals("u") || operation.startsWith("unt")) {
			type = MIRunningEvent.UNTIL;
		} else if (operation.startsWith("fin")) {
			type = MIRunningEvent.FINISH;
		} else if (operation.equals("c") || operation.equals("fg") || operation.startsWith("cont")) {
			type = MIRunningEvent.CONTINUE;
		}
		if (type != -1) {
			session.getMIInferior().setRunning();
			MIEvent event = new MIRunningEvent(type);
			session.fireEvent(event);
		}
	}
}
