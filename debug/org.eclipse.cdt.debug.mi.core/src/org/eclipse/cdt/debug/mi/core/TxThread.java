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
		while (true) {
			Command cmd = null;
			Queue txQueue = session.getTxQueue();
			// The removeCommand will block until a command is available.
			try {
				cmd = txQueue.removeCommand();
			} catch (Exception e) {
				//e.printStackTrace();
			}

			// The command is then:
			// - given a Id/token
			// - shove in the pipe
			// - Remove from the TxQueue
			// - Move to the RxQueue
			if (cmd != null) {
				OutputStream out = session.getChannelOutputStream();
				cmd.setToken(token);
				//System.out.println("Tx " + cmd.toString());
				try {
					String str = cmd.toString();
					out.write(str.getBytes());
					out.flush();
				} catch (IOException e) {
					//e.printStackTrace();
				}
				Queue rxQueue = session.getRxQueue();
				rxQueue.addCommand(cmd);
				token++;
			}
		}
	}
}
