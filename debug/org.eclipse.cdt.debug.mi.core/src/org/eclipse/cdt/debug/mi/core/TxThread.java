/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core;
 
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.debug.mi.core.command.Command;

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
			while (session.getChannelOutputStream() != null) {
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
					if (cmd.getToken() > 0) {
						CommandQueue rxQueue = session.getRxQueue();
						rxQueue.addCommand(cmd);
					}
					// shove in the pipe
					String str = cmd.toString();
					OutputStream out = session.getChannelOutputStream();
					if (out != null) {
						out.write(str.getBytes());
						out.flush();
					}
				}
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
}
