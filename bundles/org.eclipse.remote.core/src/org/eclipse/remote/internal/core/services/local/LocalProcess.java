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
package org.eclipse.remote.internal.core.services.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessControlService;
import org.eclipse.remote.core.IRemoteProcessTerminalService;

public class LocalProcess implements IRemoteProcessControlService, IRemoteProcessTerminalService {
	private static int refCount = 0;

	private final IRemoteProcess remoteProcess;
	private final Process localProcess;
	private final PTY pty;
	private int width, height;
	private InputStream procStdout;
	private InputStream procStderr;
	private Thread stdoutReader;
	private Thread stderrReader;
	private final Thread completedChecker;
	private volatile boolean isCompleted;

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
			if (LocalProcess.class.equals(service)) {
				return (T) new LocalProcess(remoteProcess);
			}
			if (IRemoteProcessControlService.class.equals(service)) {
				return (T) remoteProcess.getService(LocalProcess.class);
			}
			if (IRemoteProcessTerminalService.class.equals(service)) {
				return (T) remoteProcess.getService(LocalProcess.class);
			}
			return null;
		}
	}

	/**
	 * Thread to merge stdout and stderr. Keeps refcount so that output stream
	 * is not closed too early.
	 * 
	 */
	private class ProcOutputMerger implements Runnable {
		private final static int BUF_SIZE = 8192;

		private final InputStream input;
		private final OutputStream output;

		public ProcOutputMerger(InputStream input, OutputStream output) {
			this.input = input;
			this.output = output;
			synchronized (this.output) {
				refCount++;
			}
		}

		@Override
		public void run() {
			int len;
			byte b[] = new byte[BUF_SIZE];

			try {
				while ((len = input.read(b)) > 0) {
					output.write(b, 0, len);
				}
			} catch (IOException e) {
				// Ignore
			}
			synchronized (output) {
				if (--refCount == 0) {
					try {
						output.close();
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

	public LocalProcess(IRemoteProcess process) {
		remoteProcess = process;
		LocalProcessBuilder builder = (LocalProcessBuilder) process.getProcessBuilder();
		localProcess = builder.getProcess();
		pty = builder.getPty();

		try {
			if (process.getProcessBuilder().redirectErrorStream()) {
				PipedOutputStream pipedOutput = new PipedOutputStream();

				procStderr = new NullInputStream();
				procStdout = new PipedInputStream(pipedOutput);

				stderrReader = new Thread(new ProcOutputMerger(localProcess.getErrorStream(), pipedOutput));
				stdoutReader = new Thread(new ProcOutputMerger(localProcess.getInputStream(), pipedOutput));

				stderrReader.start();
				stdoutReader.start();
			} else {
				procStderr = localProcess.getErrorStream();
				procStdout = localProcess.getInputStream();
			}
		} catch (IOException e) {
			localProcess.destroy();
		}

		completedChecker = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!isCompleted) {
					try {
						localProcess.waitFor();
					} catch (InterruptedException e) {
						continue;
					}
					isCompleted = true;
				}
			}

		});
		completedChecker.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#destroy()
	 */
	@Override
	public void destroy() {
		localProcess.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#exitValue()
	 */
	@Override
	public int exitValue() {
		return localProcess.exitValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getErrorStream()
	 */
	@Override
	public InputStream getErrorStream() {
		return procStderr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		return procStdout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() {
		return localProcess.getOutputStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#waitFor()
	 */
	@Override
	public int waitFor() throws InterruptedException {
		return localProcess.waitFor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.RemoteProcess#isCompleted()
	 */
	@Override
	public boolean isCompleted() {
		return isCompleted;
	}

	@Override
	public IRemoteProcess getRemoteProcess() {
		return remoteProcess;
	}

	@Override
	public void setTerminalSize(int cols, int rows, int pwidth, int pheight) {
		if (pty != null && (width != cols || height != rows)) {
			width = cols;
			height = rows;
			pty.setTerminalSize(width, height);
		}
	}
}
