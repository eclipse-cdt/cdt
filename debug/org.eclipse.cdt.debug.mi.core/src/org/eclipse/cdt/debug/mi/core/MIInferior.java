package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIExecAbort;
import org.eclipse.cdt.debug.mi.core.command.MIGDBExit;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowExitCode;
import org.eclipse.cdt.debug.mi.core.event.MIExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowExitCodeInfo;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MIInferior extends Process {

	public final static int SUSPENDED = 1;
	public final static int RUNNING = 2;
	public final static int TERMINATED = 4;

	int state = 0;
	MISession session;
	OutputStream out;

	MIInferior(MISession mi) {
		session = mi;
		out = new OutputStream() {
			StringBuffer buf = new StringBuffer();
			public void write(int b) throws IOException {
				buf.append(b);
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

	/**
	 * @see java.lang.Process#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return out;
	}

	/**
	 * @see java.lang.Process#getInputStream()
	 */
	public InputStream getInputStream() {
		return session.getTargetStream();
	}

	/**
	 * @see java.lang.Process#getErrorStream()
	 */
	public InputStream getErrorStream() {
		// FIXME the same as output??
		return session.getTargetStream();
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
			CommandFactory factory = session.getCommandFactory();
			MIGDBShowExitCode code = factory.createMIGDBShowExitCode();
			try {
				session.postCommand(code);
				MIGDBShowExitCodeInfo info = code.getMIGDBShowExitCodeInfo();
				return info.getCode();
			} catch (MIException e) {
				return 0;
			}
		}
		throw new IllegalThreadStateException();
	}

	/**
	 * @see java.lang.Process#destroy()
	 */
	public void destroy() {
/*
		if (!isTerminated()) {
			CommandFactory factory = session.getCommandFactory();
			MIExecAbort abort = factory.createMIExecAbort();
			try {
				session.postCommand(abort);
				setTerminated();
				session.getRxThread().fireEvent(new MIInferiorExitEvent());
			} catch (MIException e) {
			}
		}
*/
		if (!isTerminated()) {			
			if (!isSuspended())
			{
				// interrupt execution
			}
			CommandFactory factory = session.getCommandFactory();
			MIGDBExit exit = factory.createMIGDBExit();
			try {
				session.postCommand(exit);
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

	public synchronized void setSuspended() {
		state = SUSPENDED;
	}

	public synchronized void setRunning() {
		state = RUNNING;
	}

	public synchronized void setTerminated() {
		state = TERMINATED;
		notifyAll();
	}
}
