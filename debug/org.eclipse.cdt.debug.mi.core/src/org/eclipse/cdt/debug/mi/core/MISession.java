package org.eclipse.cdt.debug.mi.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Observable;

import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIParser;


/**
 */
public class MISession extends Observable {

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

	long cmdTimeout  = 0000; // 20 * 1000 (~ 20 secs);

	final int STOPPED = 0;
	final int RUNNING = 1;
	int state = STOPPED;
	
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
	 * postCommand(cmd, 20 secs) 
	 */
	public void postCommand(Command cmd) throws MIException {
		postCommand(cmd, cmdTimeout);
	}

	public void setCommandTimeout(long timeout) {
		cmdTimeout = timeout;
	}
	
	public long getCommandTimeout() {
		return cmdTimeout;
	}
	
	/**
	 * 
	 */
	public void postCommand(Command cmd, long timeout) throws MIException {

		if (!txThread.isAlive()) {
			throw new MIException("TxThread terminated");
		}
		txQueue.addCommand(cmd);
		synchronized (cmd) {
			// RxThread will set the MIOutput on the cmd
			// when the response arrive.
			while (cmd.getMIOutput() == null) {
				try {
					cmd.wait(timeout);
					break; // Timeout or Notify
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public boolean isStopped() {
		return state == STOPPED;
	}

	public boolean isRunning() {
		return state == RUNNING;
	}

	void setStopped() {
		state = STOPPED;
	}

	void setRunning() {
		state = RUNNING;
	} 

	public void setDirty() {
		setChanged();
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
