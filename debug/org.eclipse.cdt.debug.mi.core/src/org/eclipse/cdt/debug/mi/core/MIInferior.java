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

import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIExecAbort;
import org.eclipse.cdt.debug.mi.core.command.MIExecInterrupt;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowExitCode;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowExitCodeInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.Spawner;

/**
 */
public class MIInferior extends Process {

	final static int SUSPENDED = 1;
	final static int RUNNING = 2;
	final static int TERMINATED = 4;

	boolean connected = false;

	int state = 0;

	MISession session;

	OutputStream out;
	InputStream in;
	
	PipedOutputStream inPiped;

	PipedInputStream err;
	PipedOutputStream errPiped;
	PTY pty;

	MIInferior(MISession mi, PTY p) {
		session = mi;
		pty = p;
		if (pty != null) {
			out = pty.getOutputStream();
			in = pty.getInputStream();
		}
	}

	/**
	 * @see java.lang.Process#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		if (out == null) {
			out = new OutputStream() {
				StringBuffer buf = new StringBuffer();
				public void write(int b) throws IOException {
					buf.append((char)b);
					if (b == '\n') {
						flush();
					}
				}
				// Encapsulate the string sent to gdb in a fake command.
				// and post it to the TxThread.
				public void flush() throws IOException {
					CLICommand cmd = new CLICommand(buf.toString()) {
						public void setToken(int token) {
							// override to do nothing;
						}
					};
					try {
						session.postCommand(cmd);
					} catch (MIException e) {
						throw new IOException("no mi session");
					}
				}
			};
		}
		return out;
	}

	/**
	 * @see java.lang.Process#getInputStream()
	 */
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

	/**
	 * @see java.lang.Process#waitFor()
	 */
	public int waitFor() throws InterruptedException {
		if (!isTerminated()) {
			synchronized (this) {
				wait();
			}
		}
		return exitValue();
	}

	/**
	 * @see java.lang.Process#exitValue()
	 */
	public int exitValue() {
		if (isTerminated()) {
			if (!session.isTerminated()) {
				CommandFactory factory = session.getCommandFactory();
				MIGDBShowExitCode code = factory.createMIGDBShowExitCode();
				try {
					session.postCommand(code);
					MIGDBShowExitCodeInfo info = code.getMIGDBShowExitCodeInfo();
					return info.getCode();
				} catch (MIException e) {
					// no rethrown.
				}
			}
			return 0;
		}
		throw new IllegalThreadStateException();
	}

	/**
	 * @see java.lang.Process#destroy()
	 */
	public void destroy() {
		if (!isTerminated()) {
			CommandFactory factory = session.getCommandFactory();
			MIExecAbort abort = factory.createMIExecAbort();
			try {
				session.postCommand(abort);
				MIInfo info = abort.getMIInfo();
			} catch (MIException e) {
			}
			setTerminated();
		}
	}

	public void interrupt() {
		Process gdb = session.getMIProcess();
		if (gdb instanceof Spawner) {
			Spawner gdbSpawner = (Spawner)gdb;
			gdbSpawner.interrupt();
		} else {
			// Try the exec-interrupt;
			CommandFactory factory = session.getCommandFactory();
			MIExecInterrupt interrupt = factory.createMIExecInterrupt();
			try {
				session.postCommand(interrupt);
				MIInfo info = interrupt.getMIInfo();
			} catch (MIException e) {
			}
		}
	}

	public synchronized boolean isSuspended() {
		return state == SUSPENDED;
	}

	public synchronized boolean isRunning() {
		return state == RUNNING;
	}

	public synchronized boolean isTerminated() {
		return state == TERMINATED;
	}

	public boolean isConnected() {
		return connected;
	}

	public synchronized void setConnected() {
		connected = true;
	}

	public synchronized void setDisConnected() {
		connected = false;
	}

	public synchronized void setSuspended() {
		state = SUSPENDED;
	}

	public synchronized void setRunning() {
		state = RUNNING;
	}

	public synchronized void setTerminated() {
		state = TERMINATED;
		// Close the streams.
		try {
			if (inPiped != null) {
				inPiped.close();
				inPiped = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (errPiped != null) {
				errPiped.close();
				errPiped = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// If pty is not null then we are using a master/slave terminal
		// emulation close the master to notify the slave.
		if (pty != null) {
			if (in != null) {
				try {
						in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				in = null;
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out = null;
			}
		}
		session.fireEvent(new MIInferiorExitEvent());
		notifyAll();
	}

	public OutputStream getPipedOutputStream() {
		return inPiped;
	}

	public OutputStream getPipedErrorStream() {
		return errPiped;
	}

}
