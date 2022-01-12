/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.utils.pty;

import java.io.IOException;

import org.eclipse.core.runtime.Platform;

/**
 * A type of PTY that is persistent.  This means that closing
 * its streams (e.g., once the connection to the process is lost)
 * will not close the PTY or the streams; instead, they will
 * remain open to be used again by reconnecting to the streams.
 * {@link PersistentPTY#closeStreams()} must be called to properly
 * cleanup the streams once the PersistentPTY is known not be needed
 * anymore.
 *
 * @since 5.10
 */
public class PersistentPTY extends PTY {

	private class PersistentPTYInputStream extends PTYInputStream {
		public PersistentPTYInputStream(MasterFD fd) {
			super(fd);
		}

		@Override
		public void close() throws IOException {
			// This is the change to bring persistence.
			// Don't actually close the stream.
		}

		public void realClose() throws IOException {
			// This method should be called to actually close
			// the stream once we know it won't be needed anymore
			super.close();
		}

		@Override
		protected void finalize() throws IOException {
			realClose();
		}
	}

	private class PersistentPTYOutputStream extends PTYOutputStream {
		public PersistentPTYOutputStream(MasterFD fd, boolean sendEotBeforeClose) {
			super(fd, sendEotBeforeClose);
		}

		@Override
		public void close() throws IOException {
			// This is the change to bring persistence.
			// Don't actually close the stream.
		}

		public void realClose() throws IOException {
			// This method should be called to actually close
			// the stream once we know it won't be needed anymore
			super.close();
		}

		@Override
		protected void finalize() throws IOException {
			realClose();
		}
	}

	final PersistentPTYInputStream in2;
	final PersistentPTYOutputStream out2;

	public PersistentPTY() throws IOException {
		this(Mode.CONSOLE);
	}

	public PersistentPTY(Mode mode) throws IOException {
		super(mode);
		in2 = new PersistentPTYInputStream(new MasterFD());
		out2 = new PersistentPTYOutputStream(new MasterFD(), !Platform.OS_WIN32.equals(Platform.getOS()));
	}

	@Override
	public PTYInputStream getInputStream() {
		return in2;
	}

	@Override
	public PTYOutputStream getOutputStream() {
		return out2;
	}

	/**
	 * This method must be called once the PersistentPTY is
	 * no longer needed, so that its streams can be closed.
	 */
	public void closeStreams() throws IOException {
		in2.realClose();
		out2.realClose();
	}
}
