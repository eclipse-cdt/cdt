/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Hewlett-Packard Development Company - fix for bug 109733
 *     ENEA Software AB - CLI command extension - fix for bug 190277
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.CLIExecAbort;
import org.eclipse.cdt.debug.mi.core.command.MIExecInterrupt;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowExitCode;
import org.eclipse.cdt.debug.mi.core.command.CLIInfoProc;
import org.eclipse.cdt.debug.mi.core.command.CLIInfoProgram;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowExitCodeInfo;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoProcInfo;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoProgramInfo;

/**
 */
public class MIInferior extends Process {

	final static int SUSPENDED = 1;
	final static int RUNNING = 2;
	final static int TERMINATED = 4;

	boolean connected = false;

	boolean exitCodeKnown = false;
	int exitCode = 0;
	int state = 0;

	MISession session;

	OutputStream out;
	InputStream in;

	PipedOutputStream inPiped;

	PipedInputStream err;
	PipedOutputStream errPiped;
	IMITTY tty;

	int inferiorPID;
	
	
	/** See {@link #isRemoteInferior()} */
	private boolean fIsRemoteInferior;

	public MIInferior(MISession mi, IMITTY p) {
		session = mi;
		tty = p;
		if (tty != null) {
			out = tty.getOutputStream();
			in = tty.getInputStream();
		}
	}

	/**
	 * @see java.lang.Process#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() {
		if (out == null) {
			out = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					if (!isRunning()) {
						throw new IOException(MIPlugin.getResourceString("src.MIInferior.target_is_suspended")); //$NON-NLS-1$
					}
					OutputStream channel = session.getChannelOutputStream();
					if (channel == null) {
						throw new IOException(MIPlugin.getResourceString("src.MIInferior.No_session")); //$NON-NLS-1$
					}
					channel.write(b);
				}
			};
		}
		return out;
	}

	/**
	 * @see java.lang.Process#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		if (in == null) {
			try {
				inPiped = new PipedOutputStream();
				in = new PipedInputStream(inPiped);
			} catch (IOException e) {
			}
		}
		return in;
	}

	/**
	 * @see java.lang.Process#getErrorStream()
	 */
	@Override
	public InputStream getErrorStream() {
		// FIXME: We do not have any err stream from gdb/mi
		// so this gdb err channel instead.
		if (err == null) {
			try {
				errPiped = new PipedOutputStream();
				err = new PipedInputStream(errPiped);
			} catch (IOException e) {
			}
		}
		return err;
	}

	public synchronized void waitForSync() throws InterruptedException {
		while (state != TERMINATED) {
			wait();
		}		
	}

	/**
	 * @see java.lang.Process#waitFor()
	 */
	@Override
	public int waitFor() throws InterruptedException {
		waitForSync();
		return exitValue();
	}

	/**
	 * @see java.lang.Process#exitValue()
	 */
	@Override
	public int exitValue() {
		if (isTerminated()) {
			if (!session.isTerminated()) {
				if (!exitCodeKnown) {
					CommandFactory factory = session.getCommandFactory();
					MIGDBShowExitCode code = factory.createMIGDBShowExitCode();
					try {
						session.postCommand(code);
						MIGDBShowExitCodeInfo info = code.getMIGDBShowExitCodeInfo();
						exitCode = info.getCode();
					} catch (MIException e) {
						// no rethrown.
					}
					exitCodeKnown = true;
				}
			}
			return exitCode;
		}
		throw new IllegalThreadStateException();
	}

	/**
	 * @see java.lang.Process#destroy()
	 */
	@Override
	public void destroy() {
		try {
			terminate();
		} catch (MIException e) {
			// do nothing.
		}
	}

	public void terminate() throws MIException {
		// An inferior will be destroy():interrupt and kill if
		// - For attach session:
		//   the inferior was not disconnected yet (no need to try
		//   to kill a disconnected program).
		// - For Program session:
		//   if the inferior was not terminated.
		// - For PostMortem(Core): send event
		// else noop
		if ((session.isAttachSession() && isConnected()) || (session.isProgramSession() && !isTerminated())) {
			// Try to interrupt the inferior, first.
			if (isRunning()) {
				interrupt();
			}
			int token = 0;
			if (isSuspended()) {
				try {
					CommandFactory factory = session.getCommandFactory();
					CLIExecAbort abort = factory.createCLIExecAbort();
					session.postCommand0(abort, -1);
					// do not wait for the answer.
					//abort.getMIInfo();
					token = abort.getToken();
				} catch (MIException e) {
					// ignore the error
				}
			}
			setTerminated(token, true);
		} else if (session.isCoreSession() && !isTerminated()){
			setTerminated();
		}
	}

	public void interrupt() throws MIException {
		MIProcess gdb = session.getGDBProcess();
		// Check if they can handle the interrupt
		// Try the exec-interrupt; this will be for "gdb --async"
		CommandFactory factory = session.getCommandFactory();
		MIExecInterrupt interrupt = factory.createMIExecInterrupt();
		if (interrupt != null) {
			try {
				session.postCommand(interrupt);
				// call getMIInfo() even if we discard the value;
				interrupt.getMIInfo();
				// Allow MI command timeout for the interrupt to propagate.
				long maxSec = session.getCommandTimeout()/1000 + 1;
				synchronized(this) {
					for (int i = 0;(state == RUNNING) && i < maxSec; i++) {
						try {
							wait(1000);
						} catch (InterruptedException e) {
						}
					}
				}
			} catch (MIException e) {
			}
		} else if (gdb.canInterrupt(this)) {
			gdb.interrupt(this);
		}

		// If we've failed throw an exception up.
		if (state == RUNNING) {
			throw new MIException(MIPlugin.getResourceString("src.MIInferior.Failed_to_interrupt")); //$NON-NLS-1$
		}
	}

	public boolean isSuspended() {
		return state == SUSPENDED;
	}

	public boolean isRunning() {
		return state == RUNNING;
	}

	public boolean isTerminated() {
		return state == TERMINATED;
	}

	public boolean isConnected() {
		return connected;
	}

	public synchronized void setConnected() {
		connected = true;
	}

	public synchronized void setDisconnected() {
		connected = false;
	}

	public synchronized void setSuspended() {
		state = SUSPENDED;
		notifyAll();
	}

	public synchronized void setRunning() {
		state = RUNNING;
		notifyAll();
	}

	public synchronized void setTerminated() {
		setTerminated(0, true);
	}

	synchronized void setTerminated(int token, boolean fireEvent) {
		state = TERMINATED;
		// Close the streams.
		try {
			if (inPiped != null) {
				inPiped.close();
				inPiped = null;
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
		try {
			if (errPiped != null) {
				errPiped.close();
				errPiped = null;
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}

		// If tty is not null then we are using a master/slave terminal
		// emulation close the master to notify the slave.
		if (tty != null) {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					//e.printStackTrace();
				}
				in = null;
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					//e.printStackTrace();
				}
				out = null;
			}
		}
		if (fireEvent) {
			session.fireEvent(new MIInferiorExitEvent(session, token));
		}
		notifyAll();
	}

	public OutputStream getPipedOutputStream() {
		return inPiped;
	}

	public OutputStream getPipedErrorStream() {
		return errPiped;
	}

	public IMITTY getTTY() {
		return tty;
	}

	public void update() {
		if (getInferiorPID() == 0) {
			int pid = 0;
			// Do not try this on attach session.
			if (!isConnected()) {
				// Try to discover the pid using GDB/CLI Command "info proc"
				CommandFactory factory = session.getCommandFactory();
				CLIInfoProc proc = factory.createCLIInfoProc();
				try {
					RxThread rxThread = session.getRxThread();
					rxThread.setEnableConsole(false);
					session.postCommand(proc); 
					CLIInfoProcInfo infoProc = proc.getMIInfoProcInfo();
					pid = infoProc.getPID();
				} catch (MIException e) {
					// no rethrown.
				}
				
				// Try to discover the pid using GDB/CLI Command "info program" if "info proc" failed
				try {
					if(pid <= 0){ 
					CLIInfoProgram prog = factory.createCLIInfoProgram();
					session.postCommand(prog);
					CLIInfoProgramInfo info = prog.getMIInfoProgramInfo();
					pid = info.getPID();
					}
				} catch (MIException e) {
					// no rethrown.
				} finally {
					RxThread rxThread = session.getRxThread();
					rxThread.setEnableConsole(true);					
				}
			}
			// We fail permantely.
			setInferiorPID((pid == 0) ? -1: pid);
		}
	}

	public int resetInferiorPID() {
		int pid = inferiorPID;
		inferiorPID = 0;
		return pid;
	}
	
	public void setInferiorPID(int pid) {
		inferiorPID = pid;
	}

	public int getInferiorPID() {
		return inferiorPID;
	}

	/**
	 * Called early on in the debug session to mark the inferior process as being
	 * under the control of a gdbserver. 
	 * 
	 * @since 7.0
	 */
	public void setIsRemoteInferior(boolean value) {
		fIsRemoteInferior = value;
	}

	/**
	 * Is the inferior process being debugged remotely through gdbserver?
	 * 
	 * @since 7.0
	 */
	public boolean isRemoteInferior() {
		return fIsRemoteInferior; 
	}

	/**
	 * Was the inferior process attached to by gdb (as opposed to launched by
	 * gdb).
	 * 
	 * @since 7.0
	 */
	public boolean isAttachedInferior() {
		return session.isAttachSession();
	}
}
