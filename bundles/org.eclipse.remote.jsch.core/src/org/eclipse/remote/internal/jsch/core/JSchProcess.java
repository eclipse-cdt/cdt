/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.jsch.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessControlService;
import org.eclipse.remote.core.IRemoteProcessSignalService;
import org.eclipse.remote.core.IRemoteProcessTerminalService;
import org.eclipse.remote.core.exception.RemoteConnectionException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;

public class JSchProcess implements IRemoteProcessControlService, IRemoteProcessSignalService, IRemoteProcessTerminalService {
	@SuppressWarnings("nls")
	private final String signals[] = new String[] { "", "HUP", "INT", "QUIT", "ILL", "", "ABRT", "", "FPE", "KILL", "", "SEGV", "",
			"PIPE", "ALRM", "TERM", "", "STOP", "TSTP", "CONT", "", "", "", "", "", "", "", "", "", "", "USR1", "USR2" };

	private static int WAIT_TIMEOUT = 1000;
	private static int refCount = 0;

	private final Channel fChannel;
	private final IRemoteProcess fProcess;

	private InputStream fProcStdout;
	private InputStream fProcStderr;
	private Thread fStdoutReader;
	private Thread fStderrReader;

	public static class Factory implements IRemoteProcess.Service.Factory {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.remote.core.IRemoteProcess.Service.Factory#getService(org.eclipse.remote.core.IRemoteProcess,
		 * java.lang.Class)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T extends IRemoteProcess.Service> T getService(IRemoteProcess remoteProcess, Class<T> service) {
			// This little trick creates an instance of this class
			// then for each interface it implements, it returns the same object.
			// This works because the connection caches the service so only one gets created.
			// As a side effect, it makes this class a service too which can be used
			// by the this plug-in
			if (JSchProcess.class.equals(service)) {
				return (T) new JSchProcess(remoteProcess);
			}
			if (IRemoteProcessControlService.class.equals(service) || IRemoteProcessSignalService.class.equals(service)
					|| IRemoteProcessTerminalService.class.equals(service)) {
				return (T) remoteProcess.getService(JSchProcess.class);
			}
			return null;
		}
	}

	private class ProcReader implements Runnable {
		private final static int BUF_SIZE = 8192;

		private final InputStream fInput;
		private final OutputStream fOutput;

		public ProcReader(InputStream input, OutputStream output) {
			fInput = input;
			fOutput = output;
			synchronized (this.fOutput) {
				refCount++;
			}
		}

		@Override
		public void run() {
			int len;
			byte b[] = new byte[BUF_SIZE];

			try {
				while ((len = fInput.read(b)) > 0) {
					fOutput.write(b, 0, len);
				}
			} catch (IOException e) {
				// Ignore
			}
			synchronized (fOutput) {
				if (--refCount == 0) {
					try {
						fOutput.close();
					} catch (IOException e) {
						// Ignore
					}
				}
			}
		}
	}

	public class NullInputStream extends InputStream {
		@Override
		public int read() throws IOException {
			return -1;
		}

		@Override
		public int available() {
			return 0;
		}
	}

	public JSchProcess(IRemoteProcess process) {
		fProcess = process;
		fChannel = ((JSchProcessBuilder) process.getProcessBuilder()).getChannel();

		try {
			if (process.getProcessBuilder().redirectErrorStream()) {
				PipedOutputStream pipedOutput = new PipedOutputStream();

				fProcStdout = new PipedInputStream(pipedOutput);
				fProcStderr = new NullInputStream();

				fStderrReader = new Thread(new ProcReader(fChannel.getExtInputStream(), pipedOutput));
				fStdoutReader = new Thread(new ProcReader(fChannel.getInputStream(), pipedOutput));

				fStderrReader.start();
				fStdoutReader.start();
			} else {
				fProcStdout = fChannel.getInputStream();
				fProcStderr = fChannel.getExtInputStream();
			}
		} catch (IOException e) {
			Activator.log(e);
			destroy();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#destroy()
	 */
	@Override
	public void destroy() {
		fChannel.disconnect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#exitValue()
	 */
	@Override
	public int exitValue() {
		return fChannel.getExitStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getErrorStream()
	 */
	@Override
	public InputStream getErrorStream() {
		return fProcStderr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		return fProcStdout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() {
		try {
			return fChannel.getOutputStream();
		} catch (IOException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#waitFor()
	 */
	@Override
	public int waitFor() throws InterruptedException {
		while (!isCompleted()) {
			Thread.sleep(WAIT_TIMEOUT);
		}
		return exitValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.RemoteProcess#isCompleted()
	 */
	@Override
	public boolean isCompleted() {
		return fChannel.isClosed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcess.Service#getRemoteProcess()
	 */
	@Override
	public IRemoteProcess getRemoteProcess() {
		return fProcess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcessTerminalService#setTerminalSize(int, int, int, int)
	 */
	@Override
	public void setTerminalSize(int cols, int rows, int width, int height) {
		if (fChannel instanceof ChannelExec) {
			((ChannelExec) fChannel).setPtySize(cols, rows, width, height);
		} else if (fChannel instanceof ChannelShell) {
			((ChannelShell) fChannel).setPtySize(cols, rows, width, height);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcessSignalService#sendSignal(int)
	 */
	@Override
	public void sendSignal(int signal) throws RemoteConnectionException {
		if (signal >= 0 && signal <= USR2) {
			try {
				fChannel.sendSignal(signals[signal]);
			} catch (Exception e) {
				throw new RemoteConnectionException(e.getMessage());
			}
		}
	}
}
