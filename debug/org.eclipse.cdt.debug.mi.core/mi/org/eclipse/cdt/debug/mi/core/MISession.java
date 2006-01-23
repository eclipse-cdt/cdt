/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Observable;

import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIExecInterrupt;
import org.eclipse.cdt.debug.mi.core.command.MIGDBExit;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowExitCode;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowPrompt;
import org.eclipse.cdt.debug.mi.core.command.MIInterpreterExecConsole;
import org.eclipse.cdt.debug.mi.core.command.MIVersion;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIGDBExitEvent;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

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

	boolean terminated;
	boolean useInterpreterExecConsole;

	// hold the type of the session(post-mortem, attach etc ..)
	int sessionType;

	Process sessionProcess;
	MIProcess gdbProcess;
	InputStream inChannel;
	OutputStream outChannel;

	TxThread txThread;
	RxThread rxThread;
	EventThread eventThread;

	CommandQueue txQueue;
	CommandQueue rxQueue;
	Queue eventQueue;

	PipedInputStream miInConsolePipe;
	PipedOutputStream miOutConsolePipe;
	PipedInputStream miInLogPipe;
	PipedOutputStream miOutLogPipe;


	CommandFactory factory;

	MIParser parser;

	long cmdTimeout;

	MIInferior inferior;
	
	/**
	 * @param process
	 * @param tty
	 * @param type
	 * @param commandTimeout
	 * @param launchTimeout
	 * @param miVersion
	 * @param monitor
	 * @throws MIException
	 * @deprecated
	 */
	public MISession(MIProcess process, IMITTY tty, int type, int commandTimeout, int launchTimeout, String miVersion, IProgressMonitor monitor) throws MIException {
		this(process, tty, type, new CommandFactory(miVersion), commandTimeout, launchTimeout, monitor);
	}


	/**
	 * Create the gdb session. Assume MIVersion 1
	 *
	 * @deprecated use the other constructors with the MIVersion
	 * @param Process gdb Process.
	 * @param pty Terminal to use for the inferior.
	 * @param timeout time in milliseconds to wait for command response.
	 * @param type the type of debugin session.
	 * @deprecated
	 */
	public MISession(MIProcess process, IMITTY tty, int commandTimeout, int type, int launchTimeout) throws MIException {
		this(process, tty, type, commandTimeout, launchTimeout, MIVersion.MI1, new NullProgressMonitor());
		if (useExecConsole()) {
			// if exec console is present, assume MI2 supported
			setCommandFactory(new CommandFactory(MIVersion.MI2));
		}
	}
	
	/**
	 * Create the gdb session.
	 *
	 * @param type the type of debugging session.
	 * @param commandFactory the MI command factory
	 * @param Process gdb Process.
	 * @param pty Terminal to use for the inferior.
	 * @param timeout time in milliseconds to wait for command response.
	 * @deprecated
	 */
	public MISession(MIProcess process, IMITTY tty, int type, CommandFactory commandFactory, int commandTimeout, int launchTimeout, IProgressMonitor monitor) throws MIException {
		gdbProcess = process;
		inChannel = process.getInputStream();
		outChannel = process.getOutputStream();

		factory = commandFactory;
		cmdTimeout = commandTimeout;

		sessionType = type;

		parser = new MIParser();

		inferior = new MIInferior(this, tty);

		txQueue = new CommandQueue();
		rxQueue = new CommandQueue();
		eventQueue = new Queue();
		
		txThread = new TxThread(this);
		rxThread = new RxThread(this);
		eventThread = new EventThread(this);

		// initialize/setup
		setup(launchTimeout, new NullProgressMonitor());
	}

	/**
	 * Constructor for MISession. Creates MI wrapper for the given gdb process.
	 *
	 * @param type the type of debugging session: <code>PROGRAM</code>, <code>ATTACH</code> or <code>CORE</code>
	 * @param commandFactory the set of gdb/mi commands supported by given gdb
	 * @param Process a gdb process
	 * @param pty terminal to use for the inferior.
	 * @param timeout time in milliseconds to wait for command response.
	 * 
	 * @since 3.1
	 */
	public MISession(MIProcess process, IMITTY tty, int type, CommandFactory commandFactory, int commandTimeout) throws MIException {
		gdbProcess = process;
		inChannel = process.getInputStream();
		outChannel = process.getOutputStream();

		factory = commandFactory;
		cmdTimeout = commandTimeout;

		sessionType = type;

		parser = new MIParser();

		inferior = new MIInferior(this, tty);

		txQueue = new CommandQueue();
		rxQueue = new CommandQueue();
		eventQueue = new Queue();
		
		txThread = new TxThread(this);
		rxThread = new RxThread(this);
		eventThread = new EventThread(this);

		setup();

		txThread.start();
		rxThread.start();
		eventThread.start();	
	}

	/**
	 * No need to pass a progress monitor and a launch timeout.
	 * @since 3.1 
	 */
	protected void setup() throws MIException {
		// The Process may have terminated earlier because
		// of bad arguments etc .. check this here and bail out.
		try {
			gdbProcess.exitValue();
			InputStream err = gdbProcess.getErrorStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(err));
			String line = null;
			try {
				line = reader.readLine();
				reader.close();
			} catch (Exception e) {
				// the reader may throw a NPE.
			}
			if (line == null) {
				line = MIPlugin.getResourceString("src.MISession.Process_Terminated"); //$NON-NLS-1$
			}
			throw new MIException(line);
		} catch (IllegalThreadStateException e) {
			// Ok, it means the process is alive.
		}
	}

	/**
	 * Initializes the gdb session.
	 * @since 3.1
	 */
	public void start() throws MIException {
		try {
			initialize();
		} catch (MIException exc) {
			// Kill the Transmition thread.
			if (txThread.isAlive()) {
				txThread.interrupt();
			}		
			// Kill the Receiving Thread.
			if (rxThread.isAlive()) {
				rxThread.interrupt();
			}
			// Kill the event Thread.
			if (eventThread.isAlive()) {
				eventThread.interrupt();
			}
			// rethrow up the exception.
			throw exc;
		}
	}

	/**
	 * @deprecated use <code>setup()</code> without parameters
	 */
	protected void setup(int launchTimeout, IProgressMonitor monitor) throws MIException {
		// The Process may have terminated earlier because
		// of bad arguments etc .. check this here and bail out.
		try {
			gdbProcess.exitValue();
			InputStream err = gdbProcess.getErrorStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(err));
			String line = null;
			try {
				line = reader.readLine();
				reader.close();
			} catch (Exception e) {
				// the reader may throw a NPE.
			}
			if (line == null) {
				line = MIPlugin.getResourceString("src.MISession.Process_Terminated"); //$NON-NLS-1$
			}
			throw new MIException(line);
		} catch (IllegalThreadStateException e) {
			// Ok, it means the process is alive.
		}
		
		if (monitor.isCanceled()) {
			throw new MIException(MIPlugin.getResourceString("src.MISession.Process_Terminated")); //$NON-NLS-1$
		}

		txThread.start();
		rxThread.start();
		eventThread.start();
			
		try {
			if (monitor.isCanceled()) {
				throw new MIException(MIPlugin.getResourceString("src.MISession.Process_Terminated")); //$NON-NLS-1$
			}

			initialize(launchTimeout, monitor);
		} catch (MIException exc) {
			// Kill the Transmition thread.
			if (txThread.isAlive()) {
				txThread.interrupt();
			}		
			// Kill the Receiving Thread.
			if (rxThread.isAlive()) {
				rxThread.interrupt();
			}
			// Kill the event Thread.
			if (eventThread.isAlive()) {
				eventThread.interrupt();
			}
			// rethrow up the exception.
			throw exc;
		}
	}

	/**
	 * Turns off the "confirm" option of gdb. 
	 * Sets witdth and height of gdb session to 0.
	 * @since 3.1
	 */
	protected void initialize() throws MIException {
		// Disable a certain number of irritations from gdb.
		// Like confirmation and screen size.
		MIGDBSet confirm = getCommandFactory().createMIGDBSet(new String[]{"confirm", "off"}); //$NON-NLS-1$ //$NON-NLS-2$
		postCommand(confirm);
		confirm.getMIInfo();

		MIGDBSet width = getCommandFactory().createMIGDBSet(new String[]{"width", "0"}); //$NON-NLS-1$ //$NON-NLS-2$
		postCommand(width);
		width.getMIInfo();

		MIGDBSet height = getCommandFactory().createMIGDBSet(new String[]{"height", "0"}); //$NON-NLS-1$ //$NON-NLS-2$
		postCommand(height);
		height.getMIInfo();
		
		useInterpreterExecConsole = canUseInterpreterExecConsole();

		String prompt = getCLIPrompt();
		if (prompt != null) {
			getMIParser().cliPrompt = prompt;
		}
	}
	
	/**
	 * @deprecated use <code>initialize()</code> without parameters
	 */
	protected void initialize(int launchTimeout, IProgressMonitor monitor) throws MIException {
		// Disable a certain number of irritations from gdb.
		// Like confirmation and screen size.
		MIGDBSet confirm = getCommandFactory().createMIGDBSet(new String[]{"confirm", "off"}); //$NON-NLS-1$ //$NON-NLS-2$
		postCommand(confirm, launchTimeout);
		confirm.getMIInfo();
		if (monitor.isCanceled()) {
			throw new MIException(MIPlugin.getResourceString("src.MISession.Process_Terminated")); //$NON-NLS-1$
		}

		MIGDBSet width = getCommandFactory().createMIGDBSet(new String[]{"width", "0"}); //$NON-NLS-1$ //$NON-NLS-2$
		postCommand(width, launchTimeout);
		width.getMIInfo();
		if (monitor.isCanceled()) {
			throw new MIException(MIPlugin.getResourceString("src.MISession.Process_Terminated")); //$NON-NLS-1$
		}

		MIGDBSet height = getCommandFactory().createMIGDBSet(new String[]{"height", "0"}); //$NON-NLS-1$ //$NON-NLS-2$
		postCommand(height, launchTimeout);
		height.getMIInfo();
		if (monitor.isCanceled()) {
			throw new MIException(MIPlugin.getResourceString("src.MISession.Process_Terminated")); //$NON-NLS-1$
		}
		
		useInterpreterExecConsole = canUseInterpreterExecConsole();
		if (monitor.isCanceled()) {
			throw new MIException(MIPlugin.getResourceString("src.MISession.Process_Terminated")); //$NON-NLS-1$
		}

		String prompt = getCLIPrompt();
		if (monitor.isCanceled()) {
			throw new MIException(MIPlugin.getResourceString("src.MISession.Process_Terminated")); //$NON-NLS-1$
		}
		if (prompt != null) {
			getMIParser().cliPrompt = prompt;
		}
	}
	
	protected boolean canUseInterpreterExecConsole() {
		// Try to discover if "-interpreter-exec" is supported.
		try {
			MIInterpreterExecConsole echo = getCommandFactory().createMIInterpreterExecConsole("echo"); //$NON-NLS-1$
			postCommand(echo);
			echo.getMIInfo();
			return true;
		} catch (MIException e) {
			//
		}
		return false;
	}
	
	protected String getCLIPrompt() throws MIException {
		// Get GDB's prompt
		MIGDBShowPrompt prompt = getCommandFactory().createMIGDBShowPrompt();
		postCommand(prompt);
		MIGDBShowInfo infoPrompt = prompt.getMIGDBShowInfo();
		String value = infoPrompt.getValue();
		if (value != null && value.length() > 0) {
			return value.trim();
		}
		return null;
	}

	/**
	 * get MI Console Stream.
	 * The parser will make available the MI console stream output.
	 */
	public InputStream getMIConsoleStream() {
		if (miInConsolePipe == null) {
			try {
				miOutConsolePipe = new PipedOutputStream();
				miInConsolePipe = new PipedInputStream(miOutConsolePipe);
			} catch (IOException e) {
			}
		}
		return miInConsolePipe;
	}

	/**
	 * get MI Console Stream.
	 * The parser will make available the MI console stream output.
	 */
	public InputStream getMILogStream() {
		if (miInLogPipe == null) {
			try {
				miOutLogPipe = new PipedOutputStream();
				miInLogPipe = new PipedInputStream(miOutLogPipe);
			} catch (IOException e) {
			}
		}
		return miInLogPipe;
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

	public boolean useExecConsole() {
		return useInterpreterExecConsole;
	}

	public boolean inPrimaryPrompt() {
		return rxThread.inPrimaryPrompt();
	}

	public boolean inSecondaryPrompt() {
		return rxThread.inSecondaryPrompt();
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
	 * if timeout < 0 the wait will be skipped.
	 * 
	 */
	public void postCommand(Command cmd, long timeout) throws MIException {

		// Test if we are in a sane state.
		if (!txThread.isAlive() || !rxThread.isAlive()) {
			throw new MIException(MIPlugin.getResourceString("src.MISession.Thread_Terminated")); //$NON-NLS-1$
		}

		// Test if we are in the right state?
		if (inferior.isRunning()) {
			// REMINDER: if we support -exec-interrupt
			// Let it throught:
			if (!(cmd instanceof MIExecInterrupt)) {
				throw new MIException(MIPlugin.getResourceString("src.MISession.Target_not_suspended")); //$NON-NLS-1$
			}
		}

		if (inferior.isTerminated()) {
			// the only thing that can call postCommand when the inferior is in a TERMINATED
			// state is MIGDBShowExitCode, for when MIInferior is computing error code.
			if (!(cmd instanceof MIGDBShowExitCode)) {
				throw new MIException(MIPlugin.getResourceString("src.MISession.Inferior_Terminated")); //$NON-NLS-1$
			}
		}

		if (isTerminated()) {
			throw new MIException(MIPlugin.getResourceString("src.MISession.Session_terminated")); //$NON-NLS-1$
		}
		postCommand0(cmd, timeout);
	}
	
	/**
	 * if timeout < 0 the operation will not try to way for
	 * answer from gdb.
	 * 
	 * @param cmd
	 * @param timeout
	 * @throws MIException
	 */
	public synchronized void postCommand0(Command cmd, long timeout) throws MIException {
		// TRACING: print the command;
		if (MIPlugin.getDefault().isDebugging()) {
			MIPlugin.getDefault().debugLog(cmd.toString());
		}

		txQueue.addCommand(cmd);

		// do not wait around the answer.
		if (timeout < 0) {
			return;
		}
		// Wait for the response or timedout
		synchronized (cmd) {
			// RxThread will set the MIOutput on the cmd
			// when the response arrive.
			while (cmd.getMIOutput() == null) {
				try {
					cmd.wait(timeout);
					if (cmd.getMIOutput() == null) {
						throw new MIException(MIPlugin.getResourceString("src.MISession.Target_not_responding")); //$NON-NLS-1$
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
	 * Return the "gdb" Process.
	 */
	public MIProcess getGDBProcess() {
		return gdbProcess;
	}
	
	/**
	 * Return a "fake" Process that will
	 * encapsulate the call input/output of gdb.
	 */
	public Process getSessionProcess() {
		if (sessionProcess == null) {
			sessionProcess = new SessionProcess(this);
		}
		return sessionProcess;
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

		// {in,out}Channel is use as predicate/condition
		// in the {RX,TX,Event}Thread to detect termination
		// and bail out.  So they are set to null.
		InputStream inGDB = inChannel;
		inChannel = null;
		OutputStream outGDB = outChannel;
		outChannel = null;

		// Although we will close the pipe().  It is cleaner
		// to give a chance to gdb to cleanup.
		// send the exit(-gdb-exit).  But we only wait a maximum of 2 sec.
		MIGDBExit exit = getCommandFactory().createMIGDBExit();
		try {
			postCommand0(exit, 2000);
		} catch (MIException e) {
			//ignore any exception at this point.
		}
		
		// Make sure gdb is killed.
		// FIX: the destroy() must be call before closing gdb streams
		// on windows if the order is not follow the close() will hang.
		if (gdbProcess != null) {
			gdbProcess.destroy();
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
			miInConsolePipe = null;
			if (miOutConsolePipe != null) {
				miOutConsolePipe.close();
			}
		} catch (IOException e) {
		}

		// Destroy the MI log stream.
		try {
			miInLogPipe = null;
			if (miOutLogPipe != null) {
				miOutLogPipe.close();
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

		// Kill the event Thread ... if it is not us.
		if (!eventThread.equals(Thread.currentThread())) {			
			// Kill the event Thread.
			try {
				if (eventThread.isAlive()) {
					eventThread.interrupt();
					eventThread.join(cmdTimeout);
				}
			} catch (InterruptedException e) {
			}		
		}

		// Tell the observers that the session is terminated
		notifyObservers(new MIGDBExitEvent(this, 0));

		// Should not be necessary but just to be safe.
		deleteObservers();
	}

	/**
	 * Notify the observers of new MI OOB events.
	 */
	public void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}

	OutputStream getConsolePipe() {
		return miOutConsolePipe;
	}

	OutputStream getLogPipe() {
		if (miOutLogPipe == null) {
			getMILogStream();
		}
		return miOutLogPipe;
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

	public RxThread getRxThread() {
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
