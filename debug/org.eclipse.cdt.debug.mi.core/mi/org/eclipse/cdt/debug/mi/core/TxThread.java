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
 
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.command.MIInterpreterExecConsole;
import org.eclipse.cdt.debug.mi.core.command.RawCommand;

/**
 * Transmission command thread blocks on the command Queue
 * and wake cmd are available and push them to gdb out channel.
 */
public class TxThread extends Thread {

	MISession session;
	CLIProcessor cli;

	public TxThread(MISession s) {
		super("MI TX Thread"); //$NON-NLS-1$
		session = s;
		cli = new CLIProcessor(session);
	}

	public void run () {
		try {
			RxThread rxThread = session.getRxThread();

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
					String str = cmd.toString();
					// if string is empty consider as a noop
					if (str.length() > 0) {
						// Move to the RxQueue only if RxThread is alive.
						Thread rx = session.getRxThread();
						if (rx != null && rx.isAlive() && !(cmd instanceof RawCommand)) {
							CommandQueue rxQueue = session.getRxQueue();
							rxQueue.addCommand(cmd);
						} else {
							// The RxThread is not running
							synchronized (cmd) {
								cmd.notifyAll();
							}
						}
					
						// Process the Command line to recognise patterns we may need to fire event.
						if (cmd instanceof CLICommand) {
							cli.processStateChanges((CLICommand)cmd);
						} else if (cmd instanceof MIInterpreterExecConsole) {
							cli.processStateChanges((MIInterpreterExecConsole)cmd);
						}

						// shove in the pipe
						if (out != null) {
							out.write(str.getBytes());
							out.flush();
						}
					} else {
						// String is empty consider as a noop
						synchronized (cmd) {
							cmd.notifyAll();
						}
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

}
