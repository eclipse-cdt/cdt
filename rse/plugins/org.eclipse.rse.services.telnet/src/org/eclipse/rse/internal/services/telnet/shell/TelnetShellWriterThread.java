/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 * Sheldon D'souza (Celunite) - Adapted from SshShellWriterThread
 * Martin Oberhuber (Wind River) - [187218] Fix error reporting for connect() 
 *******************************************************************************/
package org.eclipse.rse.internal.services.telnet.shell;

import java.io.PrintWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.internal.services.telnet.Activator;

public class TelnetShellWriterThread extends Thread {
	
	private PrintWriter fOutputWriter;
	private String fNextCommand;
	private boolean fIsCancelled;


	/**
	 * constructor for ssh shell writer thread
	 * @param outputWriter PrintWriter to write to in separate Thread
	 */
	public TelnetShellWriterThread(PrintWriter outputWriter)
	{
		super();
		fOutputWriter = outputWriter;
		setName("Telnet ShellWriter"+getName()); //$NON-NLS-1$
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
		} catch(Exception e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING,
					Activator.PLUGIN_ID,
					e.getLocalizedMessage()!=null ? e.getLocalizedMessage() : e.getClass().getName(),
					e));
		} finally {
			stopThread();
//			if( fOutputWriter != null )
//				fOutputWriter.close();
			fOutputWriter = null;
		}
	}

}
