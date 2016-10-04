/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;

final class GdbTerminalConnector implements IGDBTerminalControlManager {

	private static final int MAX_HIST_BUFFER_SIZE = 10000; /* lines */
	private final Process fProcess;
	private final Set<ITerminalControl> fTerminalPageControls = new HashSet<>();
	private final ArrayDeque<String> fHistoryBuffer = new ArrayDeque<>(MAX_HIST_BUFFER_SIZE);

	public GdbTerminalConnector(Process process) {
		fProcess = process;

		// Start the jobs that read the GDB process output streams
		new OutputReadJob(process.getInputStream()).schedule(); // $NON-NLS-1$
		new OutputReadJob(process.getErrorStream()).schedule(); // $NON-NLS-1$
	}

	@Override
	public void addPageTerminalControl(ITerminalControl terminalControl) {
		fTerminalPageControls.add(terminalControl);

		// write the currently available buffered history to this new terminal
		if (fHistoryBuffer.size() > 0) {
			new WriteHistoryJob(terminalControl).schedule();
		}
	}

	@Override
	public void removePageTerminalControl(ITerminalControl terminalControl) {
		if (terminalControl != null) {
			fTerminalPageControls.remove(terminalControl);
		}
	}

	@Override
	public OutputStream getTerminalToRemoteStream() {
		return fProcess.getOutputStream();
	}

	/**
	 * Writes complete lines to the history buffer, and accumulates incomplete lines "remainder" until they
	 * form a full line. The remainder is simply updated and belongs to the calling thread
	 * 
	 * Adding complete lines to the buffer is needed respect a specified maximum number of buffered lines
	 */
	private void appendHistory(byte[] b, int read, StringBuilder remainder) {
		// Read this new input
		StringBuilder info = new StringBuilder(new String(b, StandardCharsets.UTF_8));
		info.setLength(read);

		// Separate by lines but keep the separator character
		String regEx = "(?<=\\n)"; //$NON-NLS-1$
		String[] chunks = info.toString().split(regEx);

		for (int i = 0; i < chunks.length; i++) {
			StringBuilder lineBuilder = new StringBuilder();
			if (i == 0) {
				// Add the previous incomplete line info ("remainder") first
				lineBuilder.append(remainder.toString());
				// Clear out the remainder as it has now been consumed
				remainder.setLength(0);
			}

			lineBuilder.append(chunks[i]);
			String line = lineBuilder.toString();

			if (line.endsWith("\n")) { //$NON-NLS-1$
				// We have build a complete line, So lets add it to the history
				synchronized (fHistoryBuffer) {
					// Make sure we don't exceed the maximum buffer size
					while (fHistoryBuffer.size() >= MAX_HIST_BUFFER_SIZE) {
						fHistoryBuffer.remove();
					}

					fHistoryBuffer.offer(line);
				}
			} else {
				// The only line with no separator shall be the last one
				// otherwise it should have been split
				assert i == (chunks.length - 1);
				remainder.append(line);
			}
		}
	}

	private class OutputReadJob extends Job {
		{
			setSystem(true);
		}

		private InputStream fInputStream;

		private OutputReadJob(InputStream procStream) {
			super("GDB CLI output Job"); //$NON-NLS-1$
			fInputStream = procStream;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				byte[] b = new byte[1024];
				int read = 0;
				StringBuilder remainder = new StringBuilder();

				do {
					read = fInputStream.read(b);

					if (read > 0) {
						// Write fresh output to the existing consoles
						for (ITerminalControl control : fTerminalPageControls) {
							control.getRemoteToTerminalOutputStream().write(b, 0, read);
						}

						// Add this input to the history buffer
						appendHistory(b, read, remainder);
					}
				} while (read >= 0);
			} catch (IOException e) {
			}

			return Status.OK_STATUS;
		}
	}

	private class WriteHistoryJob extends Job {
		{
			setSystem(true);
		}

		private final ITerminalControl fTerminalControl;

		public WriteHistoryJob(ITerminalControl terminalControl) {
			super("GDB CLI write history job"); //$NON-NLS-1$
			fTerminalControl = terminalControl;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			// Append the buffered lines to this new terminal control instance
			String[] buffLines;

			synchronized (fHistoryBuffer) {
				buffLines = fHistoryBuffer.toArray(new String[fHistoryBuffer.size()]);
			}

			StringBuilder sb = new StringBuilder(buffLines.length);
			for (String line : buffLines) {
				sb.append(line);
			}

			if (sb.length() > 0) {
				OutputStream terminalOutputStream = fTerminalControl.getRemoteToTerminalOutputStream();
				if (terminalOutputStream != null) {
					synchronized (terminalOutputStream) {
						try {
							terminalOutputStream.write(sb.toString().getBytes(), 0,
									sb.toString().getBytes().length);
						} catch (IOException e) {
						}
					}
				}
			}

			return Status.OK_STATUS;
		}
	}

}