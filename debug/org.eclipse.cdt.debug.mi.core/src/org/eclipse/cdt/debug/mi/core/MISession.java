package org.eclipse.cdt.debug.mi.core;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIParser;


/**
 */
public class MISession {

	InputStream inChannel;
	OutputStream outChannel;

	Thread txThread;
	Thread rxThread;

	Queue txQueue;
	Queue rxQueue;

	OutputStream consoleStream = null;
	OutputStream targetStream = null;
	OutputStream logStream = null;

	CommandFactory factory;

	MIParser parser;

	/**
	 * The constructor.
	 */
	public MISession(InputStream i, OutputStream o) {
		inChannel = i;
		outChannel= o;
		factory = new CommandFactory();
		parser = new MIParser();
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
	public void setConsoleStream(OutputStream console) {
		consoleStream = console;
	}

	/**
	 * Set Target Stream.
	 */
	public void setTargetStreamOutput(OutputStream target) {
		targetStream = target;
	}

	/**
	 * Set Log Stream
	 */
	public void setLogStreamOutput(OutputStream log) {
		logStream = log;
	}

	/**
	 *
	 */
	public CommandFactory getCommandFactory() {
		return factory;
	}

	/**
	 *
	 */
	public void setCommandFactory(CommandFactory f) {
		factory = f;
	}

	/**
	 *
	 */
	public MIParser getMIParser() {
		return parser;
	}

	/**
	 *
	 */
	public void setMIParser(MIParser p) {
		parser = p;
	}

	/**
	 * 
	 */
	public void postCommand(Command cmd) {
		txQueue.addCommand(cmd);
		synchronized (cmd) {
			try {
				// FIXME: missing the predicate
				cmd.wait();
			} catch (InterruptedException e) {
			}
		}
	}
	
	Queue getTxQueue() {
		return txQueue;
	}

	Queue getRxQueue() {
		return rxQueue;
	}

	InputStream getChannelInputStream() {
		return inChannel;
	}

	OutputStream getChannelOutputStream() {
		return outChannel;
	}

	MIOutput parse(String buffer) {
		return parser.parse(buffer);
	}
}
