package org.eclipse.cdt.debug.mi.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import org.eclipse.cdt.debug.mi.core.output.MIOutput;


/**
 */
public class MISession {

	InputStream in;
	OutputStream out;

	Thread txThread;
	Thread rxThread;

	Queue txQueue;
	Queue rxQueue;

	Reader consoleStream = null;
	Reader targetStream = null;
	Reader logStream = null;

	/**
	 * The constructor.
	 */
	MISession(InputStream i, OutputStream o) {
		in = i;
		out = o;
		txQueue = new Queue();
		rxQueue = new Queue();
		txThread = new TxThread(this);
		rxThread = new RxThread(this);
		txThread.start();
		rxThread.start();
	}

	/**
	 * Set Console Stream.
	 */
	public void setConsoleStream(Reader console) {
		consoleStream = console;
	}

	/**
	 * Set Target Stream.
	 */
	public void setTargetStreamOutput(Reader target) {
		targetStream = target;
	}

	/**
	* Set Log Stream
	*/
	public void setLogStreamOutput(Reader log) {
		logStream = log;
	}


	Queue getTxQueue() {
		return txQueue;
	}

	Queue getRxQueue() {
		return rxQueue;
	}

	InputStream getInputStream() {
		return in;
	}

	OutputStream getOutputStream() {
		return out;
	}

	MIOutput parse(String buffer) {
		return null;
	}
}
