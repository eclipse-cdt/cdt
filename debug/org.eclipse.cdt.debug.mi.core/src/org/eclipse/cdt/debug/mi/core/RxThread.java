package org.eclipse.cdt.debug.mi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.cdt.debug.mi.core.command.Command;
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
		BufferedReader reader = new BufferedReader(new InputStreamReader(session.getInputStream()));
		StringBuffer buffer = new StringBuffer();
		try {
			while (true) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.equals("(gdb)")) {
						processMIOutput(buffer.toString());
						buffer = new StringBuffer();
					}
					buffer.append(line).append('\n');
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void processMIOutput(String buffer) {
		MIOutput response = session.parse(buffer);
		if (response != null) {
			String id = response.getToken();
			Queue rxQueue = session.getRxQueue();
			Command cmd = rxQueue.removeCommand(id);
			if (cmd != null) {
				cmd.setMIOutput(response);
				cmd.notifyAll();
			}
			MIOOBRecord[] oobs = response.getMIOOBRecords();
			if (oobs != null && oobs.length > 0) {
				processMIOOBRecords(oobs);
			}
		}
	}

	void processMIOOBRecords(MIOOBRecord[] oobs) {
	}
}
