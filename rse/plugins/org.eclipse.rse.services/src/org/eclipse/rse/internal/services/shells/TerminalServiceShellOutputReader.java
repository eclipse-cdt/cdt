/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - Adapted from LocalShellOutputReader. 
 * Martin Oberhuber (Wind River) - Added vt100 escape sequence ignoring. 
 * Anna Dushistova  (MontaVista) - adapted from SshShellOutputReader
 * Anna Dushistova  (MontaVista) - [240523] [rseterminals] Provide a generic adapter factory that adapts any ITerminalService to an IShellService
 * Rob Stryker (JBoss) - [335059] TerminalServiceShellOutputReader logs error when hostShell.exit() is called
 * Teodor Madan (Freescale)     - [467833] Fix leaking shell writer thread
 *******************************************************************************/

package org.eclipse.rse.internal.services.shells;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

import org.eclipse.rse.internal.services.Activator;
import org.eclipse.rse.services.shells.AbstractHostShellOutputReader;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputListener;
import org.eclipse.rse.services.shells.SimpleHostOutput;

/**
 * @since 3.1
 */
public class TerminalServiceShellOutputReader extends
		AbstractHostShellOutputReader {
	protected BufferedReader fReader;
	private volatile Thread fReaderThread = null;
	private volatile boolean isCanceled = false;
	private String fPromptChars = ">$%#]"; //Characters we accept as the end of a prompt //$NON-NLS-1$;
	private Closeable closable;

	public TerminalServiceShellOutputReader(IHostShell hostShell,
			BufferedReader reader, boolean isErrorReader) {
		this(hostShell, reader, isErrorReader, null);
	}

	public TerminalServiceShellOutputReader(IHostShell hostShell,
			BufferedReader reader, boolean isErrorReader, Closeable closable) {
		super(hostShell, isErrorReader);
		this.closable = closable;
		setName("Terminal Service ShellOutputReader" + getName()); //$NON-NLS-1$
		fReader = reader;
	}

	protected IHostOutput internalReadLine() {
		if (fReader == null) {
			//Our workaround sets the stderr reader to null, so we never give any stderr output.
			//TODO Check if ssh supports some method of having separate stdout and stderr streams
			return null;
		}
		fReaderThread = Thread.currentThread();
		try {
			return interruptableReadLine();
		} finally {
			fReaderThread = null;
		}
	}

	private IHostOutput interruptableReadLine() {
		StringBuffer theLine = new StringBuffer();
		StringBuffer theDebugLine = null;
		theDebugLine = new StringBuffer();
		int ch;
		boolean done = false;
		while (!done && !isFinished() && !isCanceled) {
			try {
				ch = fReader.read();
				switch (ch) {
				case -1:
				case 65535:
					if (theLine.length() == 0) // End of Reader
						return null;
					done = true;
					break;
				case '\b': //backspace
					if(theDebugLine!=null) theDebugLine.append((char)ch);
					int len = theLine.length()-1;
					if (len>=0) theLine.deleteCharAt(len);
					break;
				case 13:
					if(theDebugLine!=null) theDebugLine.append((char)ch);
					break; // Carriage Return: dont append to the buffer
				case 10:
					if(theDebugLine!=null) theDebugLine.append((char)ch);
					done = true; // Newline
					break;
				case 9:
					//Tab: we count tabs at column 8
					//TODO Check: SystemViewRemoteOutputAdapter.translateTabs() also translates
					//Therefore this special handling here might be unnecessary
					if(theDebugLine!=null) theDebugLine.append((char)ch);
					int tabIndex = theLine.length() % 8;
					while (tabIndex < 8) {
						theLine.append(' ');
						tabIndex++;
					}
					break;
				default:
					char tch = (char) ch;
					if(theDebugLine!=null) theDebugLine.append(tch);
					if (!Character.isISOControl(tch)) {
						theLine.append(tch); // Any other character
					} else if (ch == 27) {
						// Escape: ignore next char too
						int nch = fReader.read();
						if (theDebugLine!=null) theDebugLine.append((char)nch);
						if (nch == 91) {
							//vt100 escape sequence: read until end-of-command (skip digits and semicolon)
							//e.g. \x1b;13;m --> ignore the entire command, including the trailing m
							do {
								nch = fReader.read();
								if (theDebugLine!=null) theDebugLine.append((char)nch);
							} while (Character.isDigit((char)nch) || nch == ';');
						}
					}
				}

				// Check to see if the BufferedReader is still ready which means
				// there are more characters
				// in the Buffer...If not, then we assume it is waiting for
				// input.
				if (!done && !fReader.ready()) {
					// wait to make sure -- max. 500 msec to wait for new chars 
					// if we are not at a CRLF seems to be appropriate for the 
					// Pipes and Threads in ssh.
					long waitIncrement = 500;
					// Check if we think we are at a prompt
					int len = theLine.length()-1;
					while (len>0 && Character.isSpaceChar(theLine.charAt(len))) {
						len--;
					}
					if (len>=0 && fPromptChars.indexOf(theLine.charAt(len))>=0) {
						waitIncrement = 5; //wait only 5 msec if we think it's a prompt
					}
					if (!isCanceled) {
						try {
							Thread.sleep(waitIncrement);
						} catch (InterruptedException e) { /*ignore*/ }
					}
					if (!fReader.ready()) {
						done = true;
					}
				}
			} catch (IOException e) {
				//FIXME it's dangerous to return null here since this will end
				//our reader thread completely... the exception could just be
				//temporary, and we should keep running!
				if( !this.isCanceled ) {
					/* 335059: Don't log IOException on close due to cancellation */
					Activator.getDefault().logException(e);
				}
				return null;
			}
		}
		if (theDebugLine!=null) {
			String debugLine = theDebugLine.toString();
			debugLine.compareTo(""); //$NON-NLS-1$
		}
		return new SimpleHostOutput(theLine.toString());
	}
	
	/**
	 * Stop the reader Thread, forcing internalReadLine() to return.
	 * Does not close the Stream.
	 */
	public void stopThread() {
		this.isCanceled = true;
		if (fReaderThread != null) {
			fReaderThread.interrupt();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.AbstractHostShellOutputReader#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (closable != null)
			try {
				closable.close();
			} catch (IOException e) {
				Activator.getDefault().logException(e);
			}
	}

	/**
	 * Remove a registered output listener.
	 * @param listener to be removed.
	 */
	public void removeOutputListener(IHostShellOutputListener listener) {
		_listeners.remove(listener);
	}
}
