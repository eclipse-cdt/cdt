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
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIGDBExitEvent;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIParser;
import org.eclipse.cdt.utils.pty.PTY;

/**
 * Represents a GDB/MI session.
 * Note that on GNU/Linux the target stream is not
 * preceded by the token '@' until this is fix, on GNU/Linux
 * there a good change to confuse the parser.
 */
public class MISession extends Observable {

	/**
	 * Normal program debuging.
	 */
	public final static int PROGRAM = 0;
	/**
	 * Attach to a running process debuging.
	 */
	public final static int ATTACH = 1;
	/**
	 * PostMortem analysis.
	 */
	public final static int CORE = 2;

	/**
	 * Default wait() period for an answer after a query, 10 seconds.
	 */
	public static long REQUEST_TIMEOUT = 10000; // 10 * 1000 (~ 10 secs);

	boolean terminated;

	// hold the type of the session(post-mortem, attach etc ..)
	int sessionType;

	Process miProcess;
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

	CommandFactory factory;

	MIParser parser;

	long cmdTimeout;

	MIInferior inferior;

	/**
	 * Create the gdb session.
	 *
	 * @param Process gdb Process.
	 * @param pty Terminal to use for the inferior.
	 * @param timeout time in milliseconds to wait for command response.
	 * @param type the type of debugin session.
	 */
	public MISession(Process process, PTY pty, int timeout, int type) throws MIException {

		miProcess = process;
		inChannel = process.getInputStream();
		outChannel = process.getOutputStream();

		cmdTimeout = timeout;

		sessionType = type;

		factory = new CommandFactory();

		parser = new MIParser();

		inferior = new MIInferior(this, pty);

		txQueue = new CommandQueue();
		rxQueue = new CommandQueue();
		eventQueue = new Queue();

		txThread = new TxThread(this);
		rxThread = new RxThread(this);
		eventThread = new EventThread(this);

		txThread.start();
		rxThread.start();
		eventThread.start();

		// Disable a certain number of irritations from gdb.
		// Like confirmation and screen size.

		MIGDBSet confirm = new MIGDBSet(new String[]{"confirm", "off"});
		postCommand(confirm);
		confirm.getMIInfo(); 

		MIGDBSet width = new MIGDBSet(new String[]{"width", "0"});
		postCommand(width);
		confirm.getMIInfo(); 

		MIGDBSet height = new MIGDBSet(new String[]{"height", "0"});
		postCommand(height);
		confirm.getMIInfo(); 
	}

	/**
	 * get MI Console Stream.
	 * The parser will make available the MI console stream output.
	 */
	public InputStream getMIStream() {
		if (miInPipe == null) {
			try {
				miOutPipe = new PipedOutputStream();
				miInPipe = new PipedInputStream(miOutPipe);
			} catch (IOException e) {
			}
		}
		return miInPipe;
	}


	/**
	 * For example the CDI/MI bridge uses the command
	 * factory to create MI commands this allow overloading.
	 */
	public CommandFactory getCommandFactory() {
		return factory;
	}

	/**
	 * Set a new factory to use for command.
	 */
	public void setCommandFactory(CommandFactory f) {
		factory = f;
	}

	/**
	 * Return the MI parser.
	 */
	public MIParser getMIParser() {
		return parser;
	}

	/**
	 * Reset the MI parser.
	 */
	public void setMIParser(MIParser p) {
		parser = p;
	}

	/**
	 * Set the type of session this is.
	 * Certain action will base on that, for example
	 * the inferior will not try to kill/destroy a
	 * attach session disconnected.
	 */
	public int getSessionType() {
		return sessionType;
	}

	public void setSessionType(int type) {
		sessionType = type;
	}

	/**
	 * The debug session is a program being debug.
	 */
	public boolean isProgramSession() {
		return sessionType == PROGRAM;
	}

	/**
	 * The debug session is a program being attach to.
	 */
	public boolean isAttachSession() {
		return sessionType == ATTACH;
	}

	/**
	 * The debug session is a core being analysed.
	 */
	public boolean isCoreSession() {
		return sessionType == CORE;
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
	 * equivalent to:
	 * postCommand(cmd, cmdTimeout) 
	 */
	public void postCommand(Command cmd) throws MIException {
		postCommand(cmd, cmdTimeout);
	}

	/**
	 * Sends a command to gdb, and wait(timeout) for a response.
	 */
	static int number = 1;
	public synchronized void postCommand(Command cmd, long timeout) throws MIException {

//MIPlugin.getDefault().debugLog(number++ + " " + cmd.toString());

		// Test if we are in a sane state.
		if (!txThread.isAlive() || !rxThread.isAlive()) {
			throw new MIException("{R,T}xThread terminated");
		}

		// Test if we are in the right state?
		if (inferior.isRunning()) {
			// REMINDER: if we support -exec-interrupt
			// Let it throught:
			// if (cmd instanceof MIExecInterrupt) { }
			throw new MIException("Target running");
		}

		txQueue.addCommand(cmd);

		// Wait for the response or timedout
		synchronized (cmd) {
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

	/**
	 * Return the inferior "Process".
	 */
	public MIInferior getMIInferior() {
		return inferior;
	}

	/**
	 * Return the "gdb/mi" Process.
	 */
	public Process getMIProcess() {
		return miProcess;
	}

	/**
	 * Check if the gdb session is terminated.
	 */
	public boolean isTerminated() {
		return terminated;
	}
	
	/**
	 * Terminate the MISession.
	 */
	public void terminate() {

		// Sanity check.
		if (isTerminated()) {
			return;
		}
		
		terminated = true;
		
		// Destroy any MI Inferior(Process) and streams.
		inferior.destroy();
		
		// Tell the observers that the session
		// is finish, but we can not use the Event Thread.
		// The Event Thread is being kill below.
		notifyObservers(new MIGDBExitEvent());
		
		// {in,out}Channel is use as predicate/condition
		// in the {RX,TX,Event}Thread to detect termination
		// and bail out.  So they are set to null.
		InputStream inGDB = inChannel;
		inChannel = null;
		OutputStream outGDB = outChannel;
		outChannel = null;

		// send the exit(-gdb-exit).
		try {
			MIGDBExit exit = factory.createMIGDBExit();
			postCommand(exit);
		} catch (MIException e) {
		}
		
		// Make sure gdb is killed.
		// FIX: the destroy() must be call before closing gdb streams
		// on windows if the order is not follow the close() will hang.
		if (miProcess != null) {
			miProcess.destroy();
		}

		// Close the input GDB prompt
		try {
			if (inGDB != null)
				inGDB.close();
		} catch (IOException e) {
		}

		// Close the output GDB prompt
		try {
			if (outGDB != null)
				outGDB.close();
		} catch (IOException e) {
		}

		// Destroy the MI console stream.
		try {
			miInPipe = null;
			if (miOutPipe != null) {
				miOutPipe.close();
			}
		} catch (IOException e) {
		}
		
		// Kill the Transmition thread.
		try {
			if (txThread.isAlive()) {
				txThread.interrupt();
				txThread.join(cmdTimeout);
			}
		} catch (InterruptedException e) {
		}
		
		// Kill the Receiving Thread.
		try {
			if (rxThread.isAlive()) {
				rxThread.interrupt();
				rxThread.join(cmdTimeout);
			}
		} catch (InterruptedException e) {
		}

		// Kill the event Thread.
		try {
			if (eventThread.isAlive()) {
				eventThread.interrupt();
				eventThread.join(cmdTimeout);
			}
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

	public void fireEvents(MIEvent[] events) {
		if (events != null && events.length > 0) {
			for (int i = 0; i < events.length; i++) {
				fireEvent(events[i]);
			}
		}
	}

	public void fireEvent(MIEvent event) {
		if (event != null) {
			getEventQueue().addItem(event);
		}
	}

}
