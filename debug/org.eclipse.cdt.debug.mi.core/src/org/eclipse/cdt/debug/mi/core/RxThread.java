package org.eclipse.cdt.debug.mi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.output.MIAsyncRecord;
import org.eclipse.cdt.debug.mi.core.output.MIOOBRecord;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 


public class RxThread extends Thread {

	MISession session;

	public RxThread(MISession s) {
		super("MI RX Thread");
		session = s;
		setDaemon(true);
	}

	/*
	 * Get the response, parse the output, dispatch for OOB
	 * search for the corresponding token in rxQueue.
	 */
	public void run () {
		BufferedReader reader =
			new BufferedReader(new InputStreamReader(session.getChannelInputStream()));
		StringBuffer buffer = new StringBuffer();
		try {
			while (true) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("(gdb)")) {
						processMIOutput(buffer.toString());
						buffer = new StringBuffer();
					} else {
						buffer.append(line).append('\n');
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void processMIOutput(String buffer) {
		MIOutput response = session.parse(buffer);
		if (response != null) {
			Queue rxQueue = session.getRxQueue();

			// Notify any command waiting for a ResultRecord.
			MIResultRecord rr = response.getMIResultRecord();
			if (rr != null) {
				int id = rr.geToken();
				Command cmd = rxQueue.removeCommand(id);
				if (cmd != null) {
					synchronized (cmd) {
						cmd.setMIOutput(response);
						cmd.notifyAll();
					}
				}
			}

			// A command may wait on a specific oob, like breakpointhit
			MIOOBRecord[] oobs = response.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIAsyncRecord) {
					int id = ((MIAsyncRecord)oobs[i]).getToken();
					Command cmd = rxQueue.removeCommand(id);
					if (cmd != null) {
						cmd.setMIOutput(response);
						cmd.notifyAll();
					}
				}
				processMIOOBRecord(oobs[i]);
			}
		}
	}

	void processMIOOBRecord(MIOOBRecord oob) {
		// Dispatch a thread to deal with the listeners.
	}
}
