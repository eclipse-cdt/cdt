package org.eclipse.cdt.debug.mi.core;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.debug.mi.core.command.Command;

public class TxThread extends Thread {

	MISession session;
	int token;

	public TxThread(MISession s) {
		super("MI TX Thread");
		session = s;
		token = 1;
		setDaemon(true);
	}

	public void run () {
		try {
			while (true) {
				Command cmd = null;
				Queue txQueue = session.getTxQueue();
				// removeCommand() will block until a command is available.
				try {
					cmd = txQueue.removeCommand();
				} catch (Exception e) {
					//e.printStackTrace();
				}

				if (cmd != null) {
					// Give the command a token and increment. 
					cmd.setToken(token++);
					// shove in the pipe
					String str = cmd.toString();
					OutputStream out = session.getChannelOutputStream();
					out.write(str.getBytes());
					out.flush();
					// Move to the RxQueue
					Queue rxQueue = session.getRxQueue();
					rxQueue.addCommand(cmd);
				}
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
}
