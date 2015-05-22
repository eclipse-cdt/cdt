/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Anna Dushistova  (MontaVista) - adapted from SshShellWriterThread
 * Anna Dushistova  (MontaVista) - [240523] [rseterminals] Provide a generic adapter factory that adapts any ITerminalService to an IShellService
 * Rob Stryker (JBoss) - [335059] TerminalServiceShellOutputReader logs error when hostShell.exit() is called
 * Teodor Madan (Freescale)     - [467833] Fix leaking shell writer thread
 *******************************************************************************/
package org.eclipse.rse.internal.services.shells;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @since 3.1
 */
public class TerminalServiceShellWriterThread extends Thread implements Closeable{
	private PrintWriter fOutputWriter;
	private String fNextCommand;
	private volatile boolean fIsCancelled;

	/**
	 * constructor for terminal service shell writer thread
	 *
	 * @param outputWriter
	 *            PrintWriter to write to in separate Thread
	 */
	public TerminalServiceShellWriterThread(PrintWriter outputWriter) {
		super();
		fOutputWriter = outputWriter;
		setName("Terminal Service ShellWriter" + getName()); //$NON-NLS-1$
		start();
	}

	public synchronized boolean isDone() {
		return fIsCancelled;
	}

	public synchronized void stopThread() {
		fIsCancelled = true;
		notifyAll();
	}

	/**
	 * Write command to remote side. Wait until the thread takes the command (no
	 * queuing).
	 *
	 * @param command
	 *            to send
	 * @return boolean true if command was sent ok
	 */
	public synchronized boolean sendCommand(String command) {
		try {
			// In case multiple commands try to send:
			// wait until it's our turn
			while (!fIsCancelled && fNextCommand != null) {
				wait();
			}
			if (!fIsCancelled) {
				// Now it's our turn
				fNextCommand = command;
				notifyAll();
				// Wait until our command is processed
				while (!fIsCancelled && fNextCommand != null) {
					wait();
				}
			}
		} catch (InterruptedException e) {
			stopThread();
		}
		return !fIsCancelled;
	}

	public synchronized void run() {
		try {
			while (!fIsCancelled) {
				while (fNextCommand == null && !fIsCancelled) {
					wait();
				}
				if (!fIsCancelled) {
					fOutputWriter.println(fNextCommand);
					fNextCommand = null;
					notifyAll();
					if (fOutputWriter.checkError()) { // flush AND get error
						stopThread();
					}
				}
			}
		} catch (InterruptedException e) {
			/* no special handling -> close stream */
		} finally {
			stopThread();
			fOutputWriter.close();
			fOutputWriter = null;
		}
	}

	public void close() throws IOException {
		stopThread();
	}

}
