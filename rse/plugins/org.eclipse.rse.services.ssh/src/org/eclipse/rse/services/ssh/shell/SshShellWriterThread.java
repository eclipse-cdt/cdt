/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.services.ssh.shell;

import java.io.PrintWriter;

/**
 * The SshShellWriterThread is a Thread used to print commands into
 * a running ssh shell channel.
 * 
 * A separate Thread is needed because the PipedInputStream 
 * used by ssh requirs that the writing end of the Pipe be 
 * a Thread that remains alive during the entire lifetime 
 * of the shell.
 */
public class SshShellWriterThread extends Thread
{
	private PrintWriter fOutputWriter;
	private String fNextCommand;
	private boolean fIsCancelled;


	/**
	 * constructor for ssh shell writer thread
	 * @param outputWriter PrintWriter to write to in separate Thread
	 */
	public SshShellWriterThread(PrintWriter outputWriter)
	{
		super();
		fOutputWriter = outputWriter;
		setName("Ssh ShellWriter"+getName()); //$NON-NLS-1$
		start();
	}

	public synchronized boolean isDone()
	{
		return fIsCancelled;
	}

	public synchronized void stopThread()
	{
		fIsCancelled = true;
		notifyAll();
	}

	/**
	 * Write command to remote side. Wait until the
	 * thread takes the command (no queueing).
	 * @param command to send
	 * @return boolean true if command was sent ok
	 */
	public synchronized boolean sendCommand(String command)
	{
		try {
			//In case multiple commands try to send: 
			//wait until it's our turn
			while (!fIsCancelled && fNextCommand!=null) {
				wait();
			}
			if (!fIsCancelled) {
				//Now it's our turn
				fNextCommand = command;
				notifyAll();
				//Wait until our command is processed
				while (!fIsCancelled && fNextCommand!=null) {
					wait();
				}
			}
		} catch(InterruptedException e) {
			stopThread();
		}
		return !fIsCancelled;
	}

	public synchronized void run()
	{
		try {
			while (!fIsCancelled) {
				while (fNextCommand==null && !fIsCancelled) {
					wait();
				}
				if (!fIsCancelled) {
					fOutputWriter.println(fNextCommand);
					fNextCommand=null;
					notifyAll();
					if (fOutputWriter.checkError()) { //flush AND get error
						stopThread();
					}
				}
			}
		} catch(InterruptedException e) {
			/* no special handling -> close stream */
		} finally {
			stopThread();
			fOutputWriter.close();
			fOutputWriter = null;
		}
	}

}