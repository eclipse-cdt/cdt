/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Observable;

import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBExit;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
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

	TxThread txThread;
	RxThread rxThread;
	EventThread eventThread;

	CommandQueue txQueue;
	CommandQueue rxQueue;
	Queue eventQueue;

	PipedInputStream miInPipe;
	PipedOutputStream miOutPipe;
	PipedInputStream targetInPipe;
	PipedOutputStream targetOutPipe;
	PipedInputStream logInPipe;
	PipedOutputStream logOutPipe;

	CommandFactory factory;

	MIParser parser;

	long cmdTimeout = 10000; // 10 * 1000 (~ 10 secs);

	MIInferior inferior;

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
		txQueue = new CommandQueue();
		rxQueue = new CommandQueue();
		eventQueue = new Queue();
		txThread = new TxThread(this);
		rxThread = new RxThread(this);
		eventThread = new EventThread(this);
		txThread.start();
		rxThread.start();
		eventThread.start();

		try {
			miOutPipe = new PipedOutputStream();
			miInPipe = new PipedInputStream(miOutPipe);
			targetOutPipe = new PipedOutputStream();
			targetInPipe = new PipedInputStream(targetOutPipe);
			logOutPipe = new PipedOutputStream();
			logInPipe = new PipedInputStream(logOutPipe);
		} catch (IOException e) {
		}

		inferior = new MIInferior(this);
		try {
			postCommand(new MIGDBSet(new String[]{"confirm", "off"}));
		} catch (MIException e) {
		}
	}

	/**
	 * get Console Stream.
	 */
	public InputStream getMIStream() {
		return miInPipe;
	}


	/**
	 * Get Target Stream.
	 */
	public InputStream getTargetStream() {
		return targetInPipe;
	}

	/**
	 * Get Log Stream
	 */
	public InputStream getLogStream() {
		return logInPipe;
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
	 * Return the default Command Timeout, default 10 secs.
	 */
	public long getCommandTimeout() {
		return cmdTimeout;
	}

	/**
	 * postCommand(cmd, 10 secs) 
	 */
	public void postCommand(Command cmd) throws MIException {
		postCommand(cmd, cmdTimeout);
	}

	/**
	 * Sends a command to gdb.
	 */
	public void postCommand(Command cmd, long timeout) throws MIException {

		MIPlugin.getDefault().debugLog(cmd.toString());

		// Test if we in a sane state.
		if (!txThread.isAlive() || !rxThread.isAlive()) {
			throw new MIException("{R,T}xThread terminated");
		}

		txQueue.addCommand(cmd);

		// Wait for the response or timedout
		synchronized (cmd) {
			// Do not wait for command if time out is 0
			if (timeout > 0) {
				// RxThread will set the MIOutput on the cmd
				// when the response arrive.
				while (cmd.getMIOutput() == null) {
					try {
						cmd.wait(timeout);
						if (cmd.getMIOutput() == null) {
							throw new MIException("Timedout");
						}
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	public MIInferior getMIInferior() {
		return inferior;
	}

	public boolean isTerminated() {
		return (!txThread.isAlive() || !rxThread.isAlive());
	}
	
	/**
	 * Close the MISession.
	 */
	public void terminate() {

		// Destroy any MI Inferior
		inferior.destroy();

		// send the exit.
		try {
			MIGDBExit exit = factory.createMIGDBExit();
			postCommand(exit);
		} catch (MIException e) {
		}

		// Explicitely close the channels
		try {
			inChannel.close();
		} catch (IOException e) {
		}
		inChannel = null;

		try {
			outChannel.close();
		} catch (IOException e) {
		}
		// This is __needed__ to stop the txThread and eventThread.
		outChannel = null;
		
		// Make sure all threads are gone.
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

		try {
			if (eventThread.isAlive()) {
				eventThread.interrupt();
			}
			eventThread.join();
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Notify the observers of new MI OOB events.
	 */
	public void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}


	OutputStream getConsolePipe() {
		return miOutPipe;
	}

	OutputStream getTargetPipe() {
		return targetOutPipe;
	}

	OutputStream getLogPipe() {
		return logOutPipe;
	}

	CommandQueue getTxQueue() {
		return txQueue;
	}

	CommandQueue getRxQueue() {
		return rxQueue;
	}

	Queue getEventQueue() {
		return eventQueue;
	}

	RxThread getRxThread() {
		return rxThread;
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
