package org.eclipse.cdt.debug.mi.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import org.eclipse.cdt.debug.mi.core.output.MIOutput;


/**
 */
public class MISession {

	Process process;
	Writer consoleStreamOutput = null;
	Writer targetStreamOutput = null;
	Writer logStreamOutput = null;

	/**
	 * The constructor.
	 */
	MISession(Process proc) {
		process = proc;
	}

	/**
	 * Set Console Stream.
	 */
	public void setConsoleStreamOutput(Writer consoleOutput) {
		consoleStreamOutput = consoleOutput;
	}

	/**
	 * Set Target Stream.
	 */
	public void setTargetStreamOutput(Writer targetOutput) {
		targetStreamOutput = targetOutput;
	}

	/**
	* Set Log Stream
	*/
	public void setLogStreamOutput(Writer logOutput) {
		logStreamOutput = logOutput;
	}

	MIOutput parse(String buffer) {
		return null;
	}

	OutputStream getSessionInputStream() {
		if (process != null) {
			process.getOutputStream();
		}
		return null;
	}

	InputStream getSessionOutputStream() {
		if (process != null) {
			process.getInputStream();
		}
		return null;
	}
}
