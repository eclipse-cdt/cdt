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

import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIExecAbort;
import org.eclipse.cdt.debug.mi.core.command.MIExecInterrupt;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowExitCode;
import org.eclipse.cdt.debug.mi.core.command.MIInfoProgram;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowExitCodeInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoProgramInfo;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.Spawner;

/**
 */
public class MIInferior extends Process {

	final static int SUSPENDED = 1;
	final static int RUNNING = 2;
	final static int TERMINATED = 4;

	boolean connected = false;

	int exitCode = 0;
	int state = 0;

	MISession session;

	OutputStream out;
	InputStream in;

	PipedOutputStream inPiped;

	PipedInputStream err;
	PipedOutputStream errPiped;
	PTY pty;

	int inferiorPid;

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
					if (!isRunning()) {
						throw new IOException("target is suspended");
					}
					OutputStream channel = session.getChannelOutputStream();
					if (channel == null) {
						throw new IOException("No MI Session");
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
	public synchronized int waitFor() throws InterruptedException {
		while (state != TERMINATED) {
			wait();
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
					exitCode = info.getCode();
				} catch (MIException e) {
					// no rethrown.
				}
			}
			return exitCode;
		}
		throw new IllegalThreadStateException();
	}

	/**
	 * @see java.lang.Process#destroy()
	 */
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
		// - For PostMortem(Core): noop
		if ((session.isAttachSession() && isConnected()) || (session.isProgramSession() && !isTerminated())) {

			CommandFactory factory = session.getCommandFactory();
			MIExecAbort abort = factory.createMIExecAbort();
			// Try to interrupt the inferior, first.
			interrupt();
			session.postCommand(abort);
			abort.getMIInfo();
			setTerminated(abort.getToken(), true);
		}
	}

	public synchronized void interrupt() throws MIException {
		Process gdb = session.getGDBProcess();
		// Check if they can handle the interrupt
		// Try the exec-interrupt; this will be for "gdb --async"
		CommandFactory factory = session.getCommandFactory();
		MIExecInterrupt interrupt = factory.createMIExecInterrupt();
		if (interrupt != null) {
			try {
				session.postCommand(interrupt);
				MIInfo info = interrupt.getMIInfo();
				// Allow (5 secs) for the interrupt to propagate.
				for (int i = 0;(state == RUNNING) && i < 5; i++) {
					try {
						wait(1000);
					} catch (InterruptedException e) {
					}
				}
			} catch (MIException e) {
			}
		} else if (gdb instanceof Spawner) {
			Spawner gdbSpawner = (Spawner) gdb;
			gdbSpawner.interrupt();
			// Allow (5 secs) for the interrupt to propagate.
			for (int i = 0;(state == RUNNING) && i < 5; i++) {
				try {
					wait(1000);
				} catch (InterruptedException e) {
				}
			}
			if ((state == RUNNING) && inferiorPid > 0) {
				// lets try something else.
				gdbSpawner.raise(inferiorPid, gdbSpawner.INT);
				for (int i = 0;(state == RUNNING) && i < 5; i++) {
					try {
						wait(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}

		// If we've failed throw an exception up.
		if (state == RUNNING) {
			throw new MIException("Failed to interrupt");
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
		setTerminated(0, false);
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

		// If pty is not null then we are using a master/slave terminal
		// emulation close the master to notify the slave.
		if (pty != null) {
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
			session.fireEvent(new MIInferiorExitEvent(token));
		}
		notifyAll();
	}

	public OutputStream getPipedOutputStream() {
		return inPiped;
	}

	public OutputStream getPipedErrorStream() {
		return errPiped;
	}

	public PTY getPTY() {
		return pty;
	}

	public void update() {
		if (inferiorPid == 0) {
			// Do not try this on attach session.
			if (!isConnected()) {
				// Try to discover the pid
				CommandFactory factory = session.getCommandFactory();
				MIInfoProgram prog = factory.createMIInfoProgram();
				try {
					session.postCommand(prog);
					MIInfoProgramInfo info = prog.getMIInfoProgramInfo();
					inferiorPid = info.getPID();
				} catch (MIException e) {
					// no rethrown.
				}
			}
			// We fail permantely.
			if (inferiorPid == 0) {
				inferiorPid = -1;
			}
		}
	}
}
