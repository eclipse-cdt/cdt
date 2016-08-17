/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.utils.pty;

import java.io.IOException;

/**
 * A type of PTY that is persistent.  That is that once the input
 * stream loses the connection to its process, it will not close
 * itself; instead, it will remain open to be used again.
 * {@link PersistentPTY#closeStream()} must be called to properly
 * cleanup the stream once it is known not be needed anymore.
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
	}
	
	final PersistentPTYInputStream in2;

	public PersistentPTY() throws IOException {
		this(Mode.CONSOLE);
	}

	public PersistentPTY(Mode mode) throws IOException {
		super(mode);
		in2 = new PersistentPTYInputStream(new MasterFD());
	}
	
	@Override
	public PTYInputStream getInputStream() {
		return in2;
	}
	
	/**
	 * This method must be called once the PersistentPTY is
	 * no longer needed, so that its stream can be closed. 
	 */
	public void closeStream() throws IOException {
		in2.realClose();
	}
}
