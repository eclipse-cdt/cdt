/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.debug.mi.core.command.CLICommand;

/**
 */
public class SessionProcess extends Process {

	MISession session;
	OutputStream out;

	public SessionProcess(MISession s) {
		session = s;
	}

	/**
	 * @see java.lang.Process#destroy()
	 */
	public void destroy() {
		session.terminate();
	}

	/**
	 * @see java.lang.Process#exitValue()
	 */
	public int exitValue() {
		return session.getGDBProcess().exitValue();
	}

	/**
	 * @see java.lang.Process#getErrorStream()
	 */
	public InputStream getErrorStream() {
		return session.getMILogStream();
	}

	/**
	 * @see java.lang.Process#getInputStream()
	 */
	public InputStream getInputStream() {
		return session.getMIConsoleStream();
	}

	/**
	 * @see java.lang.Process#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		if (out == null) {
			out = new OutputStream() {
				StringBuffer buf = new StringBuffer();
				public void write(int b) throws IOException {
					buf.append((char)b);
					if (b == '\n') {
						post();
					}
				}
                                
				// Encapsulate the string sent to gdb in a fake
				// command and post it to the TxThread.
				public void post() throws IOException {
					// Throw away the newline.
					String str = buf.toString().trim();
					CLICommand cmd = new CLICommand(str);
					buf.setLength(0);
					try {
						session.postCommand(cmd);
					} catch (MIException e) {
						//e.printStackTrace();
						throw new IOException(e.getMessage());
					}
				}
			};
		}
		return out;
	}

	/**
	 * @see java.lang.Process#waitFor()
	 */
	public int waitFor() throws InterruptedException {
		return session.getGDBProcess().waitFor();
	}

}
