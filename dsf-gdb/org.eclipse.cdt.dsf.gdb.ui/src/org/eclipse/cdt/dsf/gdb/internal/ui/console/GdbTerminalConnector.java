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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;

/**
 * This class will read from the GDB process output and error streams and will write it to any registered
 * ITerminalControl. It must continue reading from the streams, even if there are no ITerminalControl to write
 * to. This is important to prevent GDB's output buffer from getting full and then completely stopping.
 */
final class GdbTerminalConnector implements IGdbTerminalControlConnector {
	private static final int HIST_BUFFER_MAX_SIZE = 1000;  /* lines */
	private static final int HIST_BUFFER_WRITE_SIZE = 100; /* lines */
	private final Process fProcess;
	private final Set<ITerminalControl> fTerminalPageControls = new HashSet<>();
	private final Job fOutputStreamJob;
	private final Job fErrorStreamJob;
	private final ConsoleHistoryLinesBuffer fHistoryBuffer;

	public GdbTerminalConnector(Process process) {
		fProcess = process;

		// Use a history buffer size aligned with the preferences for console buffering 
		// but not exceeding the internal maximum
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		int prefBufferLines = store.getInt(IGdbDebugPreferenceConstants.PREF_CONSOLE_BUFFERLINES);
		int history_buffer_size = prefBufferLines < HIST_BUFFER_MAX_SIZE ? prefBufferLines
				: HIST_BUFFER_MAX_SIZE;

		fHistoryBuffer = new ConsoleHistoryLinesBuffer(history_buffer_size);
		
		// Start the jobs that read the GDB process output streams
		String jobSuffix = ""; //$NON-NLS-1$
		fOutputStreamJob = new OutputReadJob(process.getInputStream(), jobSuffix);
		fOutputStreamJob.schedule();
		
		jobSuffix = "-Error"; //$NON-NLS-1$
		fErrorStreamJob = new OutputReadJob(process.getErrorStream(), jobSuffix);
		fErrorStreamJob.schedule();
	}

	/**
	 * This class will hold a buffer of history lines, it uses a queue to easily pop out the oldest lines once
	 * the maximum is being exceeded.</br>
	 * It also keeps track of partial text at the end of the receiving input i.e. not yet forming a complete
	 * line, once it forms a complete line it gets integrated in the queue
	 * 
	 * In addition the API used in this implementation are synchronized to allow consistent information among
	 * the Jobs using it
	 */
	private class ConsoleHistoryLinesBuffer extends ArrayDeque<String> {

		private static final long serialVersionUID = 1L;
		/**
		 * Holds the last characters received but not yet forming a complete line, The HistoryBuffer contains
		 * complete lines to be able to keep a proper line count that can be then be dimensioned by e.g.
		 * preferences
		 */
		private final StringBuilder fHistoryRemainder = new StringBuilder();

		public ConsoleHistoryLinesBuffer(int size) {
			super(size);
		}

		@Override
		public synchronized int size() {
			return super.size();
		}

		/**
		 * @param text
		 *            Accumulate the text not yet forming a line
		 */
		private synchronized void appendRemainder(String text) {
			fHistoryRemainder.append(text);
		}

		/**
		 * @return The accumulated value 
		 */
		private synchronized String getRemainder() {
			return fHistoryRemainder.toString();
		}
		
		
		/**
		 * @return Returns the accumulated text and clears its internal value
		 */
		private synchronized String popRemainder() {
			String remainder = fHistoryRemainder.toString();
			fHistoryRemainder.setLength(0);
			return remainder;
		}

		/**
		 * Writes complete lines to the history buffer, and accumulates incomplete lines "remainder" until
		 * they form a full line.
		 * 
		 * Adding complete lines to the buffer is needed to respect a specified maximum number of buffered
		 * lines
		 */
		public synchronized void appendHistory(byte[] b, int read) {
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
					lineBuilder.append(popRemainder());
				}

				lineBuilder.append(chunks[i]);
				String line = lineBuilder.toString();

				if (line.endsWith("\n")) { //$NON-NLS-1$
					// We have build a complete line, So lets add it to the history
					// Make sure we don't exceed the maximum buffer size
					while (this.size() >= HIST_BUFFER_MAX_SIZE) {
						this.remove();
					}

					this.offer(line);
				} else {
					// The only line with no separator shall be the last one
					// otherwise it should have been split
					assert i == (chunks.length - 1);
					appendRemainder(line);
				}
			}
		}

		public synchronized String[] toArray(String[] a) {
			return super.toArray(a);
		}
	}

	public void dispose() {
		fOutputStreamJob.cancel();
		fErrorStreamJob.cancel();
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
		// When the user writes to the terminal, it should be sent
		// directly to GDB
		return fProcess.getOutputStream();
	}

	private class OutputReadJob extends Job {
		{
			setSystem(true);
		}

		private InputStream fInputStream;

		private OutputReadJob(InputStream procStream, String nameSuffix) {
			super("GDB CLI output Job" + nameSuffix); //$NON-NLS-1$
			fInputStream = procStream;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				byte[] b = new byte[1024];
				int read = 0;

				do {
					if (monitor.isCanceled()) {
						break;
					}

					read = fInputStream.read(b);
					if (read > 0) {
						// Write fresh output to the existing consoles
						for (ITerminalControl control : fTerminalPageControls) {
							control.getRemoteToTerminalOutputStream().write(b, 0, read);
						}

						// Add this input to the history buffer
						fHistoryBuffer.appendHistory(b, read);
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
			// Append the buffered lines to the terminal control instance
			String[] buffLines = fHistoryBuffer.toArray(new String[fHistoryBuffer.size()]);
			
			// Writing the current buffer in chunks of data
			// Calculate the initial limits
			// The position pointed by 'end' is not written out on the iteration, but used as the limit
			int start = 0;
			int end = buffLines.length <= HIST_BUFFER_WRITE_SIZE ? buffLines.length : HIST_BUFFER_WRITE_SIZE;

			// Write the history in chunks of lines
			StringBuilder sb = new StringBuilder(HIST_BUFFER_WRITE_SIZE);

			OutputStream terminalOutputStream = fTerminalControl.getRemoteToTerminalOutputStream();
			while (start < buffLines.length) {
				// Prepare the data chunk to write
				String[] chunk = Arrays.copyOfRange(buffLines, start, end);

				for (String line : chunk) {
					sb.append(line);
				}

				// Calculate limits for next iteration
				start = end;
				int linesLeft = buffLines.length - end;
				end = start + (linesLeft <= HIST_BUFFER_WRITE_SIZE ? linesLeft : HIST_BUFFER_WRITE_SIZE);
				
				// if this is the last write, 
				if (!(start < buffLines.length)) {
					// Add the accumulated remainder value (i.e. not yet a complete line) as the last line
					sb.append(fHistoryBuffer.getRemainder());
				}

				// Write to Output Stream
				if (sb.length() > 0) {
					if (terminalOutputStream != null) {
						synchronized (terminalOutputStream) {
							try {
								terminalOutputStream.write(sb.toString().getBytes(), 0,
										sb.toString().getBytes().length);
							} catch (IOException e) {
							}
						}
					}
					sb.setLength(0);
				}
			}

			return Status.OK_STATUS;
		}
	}

}