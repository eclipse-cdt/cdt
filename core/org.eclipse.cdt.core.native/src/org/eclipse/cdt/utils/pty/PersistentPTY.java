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
 * A type of PTY that is persistent.  That is that once a the input
 * stream loses the connection to its process, it will not close
 * itself; instead, it will remain to be used again.
 * 
 * @since 5.10
 */
public class PersistentPTY extends PTY {

	class PersistentPTYInputStream extends PTYInputStream {
		public PersistentPTYInputStream(MasterFD fd) {
			super(fd);
		}

		@Override
		public void close() throws IOException {
			// This is the change to bring persistence.
			// Don't actually close the stream.
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
}
