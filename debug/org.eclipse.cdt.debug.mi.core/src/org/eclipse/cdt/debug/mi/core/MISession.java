package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Observable;

import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIParser;

/**
 * Represents a GDB/MI session.
 * Note that on GNU/Linux the target stream is not
 * preceded by the token '@' until this is fix, on GNU/Linux
 * there a good change to confuse the parser.
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

	long cmdTimeout = 0000; // 20 * 1000 (~ 20 secs);

	final int STOPPED = 0;
	final int RUNNING = 1;
	final int SUSPENDED = 1;
	int state = STOPPED;

	/**
	 * Create the gdb session.
	 *
	 * @param i the gdb input channel.
	 * @param o gdb output channel.
	 */
	public MISession(InputStream i, OutputStream o) {
		inChannel = i;
		outChannel = o;
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
	 * get Console Stream.
	 */
	OutputStream getConsoleStream() {
		return consoleStream;
	}

	/**
	 * Set Target Stream.
	 */
	public void setTargetStream(OutputStream target) {
		targetStream = target;
	}

	/**
	 * Get Target Stream.
	 */
	OutputStream getTargetStream() {
		return targetStream;
	}

	/**
	 * Set Log Stream
	 */
	public void setLogStream(OutputStream log) {
		logStream = log;
	}

	/**
	 * Get Log Stream
	 */
	OutputStream getLogStream() {
		return logStream;
	}

	/**
	 * For example the CDI/MI adapters uses the command
	 * factory to create MI commands this allow overloading.
	 */
	public CommandFactory getCommandFactory() {
		return factory;
	}

	/**
	 * Set a new factory to use in CDI/MI adapters.
	 */
	public void setCommandFactory(CommandFactory f) {
		factory = f;
	}

	/**
	 * Return the MI main parser.
	 */
	public MIParser getMIParser() {
		return parser;
	}

	/**
	 * Reset the parser.
	 */
	public void setMIParser(MIParser p) {
		parser = p;
	}

	/**
	 * Reset the default Command Timeout.
	 */
	public void setCommandTimeout(long timeout) {
		cmdTimeout = timeout;
	}

	/**
	 * Return the default Command Timeout, default 20 secs.
	 */
	public long getCommandTimeout() {
		return cmdTimeout;
	}

	/**
	 * postCommand(cmd, 20 secs) 
	 */
	public void postCommand(Command cmd) throws MIException {
		postCommand(cmd, cmdTimeout);
	}

	/**
	 * Sends a command to gdb.
	 */
	public void postCommand(Command cmd, long timeout) throws MIException {

		if (!txThread.isAlive() || !rxThread.isAlive()) {
			throw new MIException("{R,T}xThread terminated");
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

	/**
	 * Close the MISession.
	 */
	public void terminate() {

		// Closing the channel will kill the RxThread.
		try {
			inChannel.close();
		} catch (IOException e) {
		}
		inChannel = null;

		try {
			outChannel.close();
		} catch (IOException e) {
		}
		outChannel = null; // This is needed to stop the txThread.
		
		try {
			if (txThread.isAlive()) {
				txThread.interrupt();
			}
			txThread.join();
		} catch (InterruptedException e) {
		}
		
		try {
			if (rxThread.isAlive()) {
				rxThread.interrupt();
			}
			rxThread.join();
		} catch (InterruptedException e) {
		}
	}

	/**
	 * The session is in STOPPED state.
	 * It means the 'run/-exec-run' command was not issued.
	 * Or the program exited, via a signal or normally.
	 * It is not the same as gdb/MI *stopped async-class
	 * gdb/MI stopped means suspended here.
	 */
	public boolean isStopped() {
		return state == STOPPED;
	}

	/**
	 * The session is in SUSPENDED state.
	 * State after hitting a breakpoint or after attach.
	 */
	public boolean isSuspended() {
		return state == SUSPENDED;
	}

	/**
	 * The session is in RUNNING state.
	 */
	public boolean isRunning() {
		return state == RUNNING;
	}

	/**
	 * Set the state STOPPED.
	 */
	public void setStopped() {
		state = STOPPED;
	}

	/**
	 * Set the state SUSPENDED.
	 */
	public void setSuspended() {
		state = SUSPENDED;
	}

	/**
	 * Set the state STOPPED.
	 */
	public void setRunning() {
		state = RUNNING;
	}

	/**
	 * Notify the observers of new MI OOB events.
	 */
	public void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
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
