/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.launcher;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Wraps the underline process and prevent accessing to its output or error stream.
 * This wrapping is necessary to prevent handling the test module output by Console
 * because we want to handle it here.
 * 
 */
class ProcessWrapper extends Process {
	
	/** The real underlying process. */
	private Process wrappedProcess;
	
	/** The flag shows whether input stream should be replaced with empty dummy. */
	private boolean hideInputStream;

	/** The flag shows whether error stream should be replaced with empty dummy. */
	private boolean hideErrorStream;
	

	/** Buffer for empty dummy stream. */
	private byte buffer[] = new byte[0];

	/** Empty dummy stream. */
	private InputStream emptyInputStream = new ByteArrayInputStream(buffer);
	
	/**
	 * The synchronization event: before it happens <code>waitFor()</code> will
	 * not be called on underlying process object. See also the comments in
	 * <code>waitFor()</code>.
	 */
	private Object waitForSync = new Object();

	/**
	 * Flag that shows that process output was not processed yet so the IO
	 * streams could not be closed yet.
	 */
	private boolean streamsClosingIsAllowed = false;

	
	/**
	 * The constructor
	 * 
	 * @param wrappedProcess underlying process
	 * @param hideInputStream process input stream should be hidden
	 * @param hideErrorStream process error stream should be hidden
	 */
	public ProcessWrapper(Process wrappedProcess, boolean hideInputStream, boolean hideErrorStream) {
		this.wrappedProcess = wrappedProcess;
		this.hideInputStream = hideInputStream;
		this.hideErrorStream = hideErrorStream;
	}
	
	@Override
	public void destroy() {
		wrappedProcess.destroy();
	}

	@Override
	public int exitValue() {
		return wrappedProcess.exitValue();
	}

	@Override
	public InputStream getErrorStream() {
		return hideErrorStream ? emptyInputStream : wrappedProcess.getErrorStream();
	}

	@Override
	public InputStream getInputStream() {
		return hideInputStream ? emptyInputStream : wrappedProcess.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() {
		return wrappedProcess.getOutputStream();
	}

	@Override
	public int waitFor() throws InterruptedException {
		// NOTE: implementation of waitFor() in Spawner will close streams after process is terminated,
		// so we should wait with this operation until we process all stream data
		synchronized (waitForSync) {
			if (!streamsClosingIsAllowed) {
				waitForSync.wait();
			}
		}
		return wrappedProcess.waitFor();
	}
	
	/**
	 * Sets up the flag the allows IO streams closing.
	 */
	public void allowStreamsClosing() {
		synchronized (waitForSync) {
			streamsClosingIsAllowed = true;
			waitForSync.notifyAll();
		}
	}

}