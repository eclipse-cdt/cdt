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
package org.eclipse.internal.remote.jsch.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.remote.core.AbstractRemoteProcess;

public class JSchProcess extends AbstractRemoteProcess {
	private static int refCount = 0;

	private final Process fRemoteProcess;
	private InputStream fProcStdout;
	private InputStream fProcStderr;
	private Thread fStdoutReader;
	private Thread fStderrReader;

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

	public JSchProcess(Process proc, boolean merge) throws IOException {
		fRemoteProcess = proc;

		if (merge) {
			PipedOutputStream pipedOutput = new PipedOutputStream();

			fProcStdout = new PipedInputStream(pipedOutput);
			fProcStderr = new NullInputStream();

			fStderrReader = new Thread(new ProcReader(proc.getErrorStream(), pipedOutput));
			fStdoutReader = new Thread(new ProcReader(proc.getInputStream(), pipedOutput));

			fStderrReader.start();
			fStdoutReader.start();
		} else {
			fProcStdout = proc.getInputStream();
			fProcStderr = proc.getErrorStream();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#destroy()
	 */
	@Override
	public void destroy() {
		fRemoteProcess.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#exitValue()
	 */
	@Override
	public int exitValue() {
		return fRemoteProcess.exitValue();
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
		return fRemoteProcess.getOutputStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#waitFor()
	 */
	@Override
	public int waitFor() throws InterruptedException {
		return fRemoteProcess.waitFor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.AbstractRemoteProcess#isCompleted()
	 */
	@Override
	public boolean isCompleted() {
		try {
			fRemoteProcess.exitValue();
			return true;
		} catch (IllegalThreadStateException e) {
			return false;
		}
	}
}
