/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
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
 ********************************************************************************/

package org.eclipse.rse.services.ssh.shell;

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.rse.services.shells.AbstractHostShellOutputReader;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputReader;

/**
 * Listener to shell output. As io streams through, refresh events are sent out
 * for the OutputChangeListener to respond to.
 * VT100 terminal escape sequences are ignored.
 */
public class SshShellOutputReader extends AbstractHostShellOutputReader
		implements IHostShellOutputReader {

	protected BufferedReader fReader;

	public SshShellOutputReader(IHostShell hostShell, BufferedReader reader,
			boolean isErrorReader) {
		super(hostShell, isErrorReader);
		fReader = reader;
	}

	public void dispose() {
		super.dispose();
		//check for active session and notify lost session if necessary
		getHostShell().isActive();
	}

	protected Object internalReadLine() {
		if (fReader == null) {
			//Our workaround sets the stderr reader to null, so we never give any stderr output.
			//TODO Check if ssh supports some method of having separate stdout and stderr streams
			return null;
		}
		StringBuffer theLine = new StringBuffer();
		int ch;
		int lastch = 0;
		boolean done = false;
		while (!done && !isFinished()) {
			try {
				ch = fReader.read();
				switch (ch) {
				case -1:
					if (theLine.length() == 0) // End of Reader
						return null;
					done = true;
					break;
				case 65535:
					if (theLine.length() == 0) // Check why I keep getting this!!!
						return null;
					done = true;
					break;
				case 10:
					done = true; // Newline
					break;
				case 9:
					//TODO Count characters and insert as many as needed to do a real tab
					theLine.append("     "); // Tab //$NON-NLS-1$
					break;
				case 13:
					break; // Carriage Return
				default:
					char tch = (char) ch;
					if (!Character.isISOControl(tch)) {
						theLine.append(tch); // Any other character
					} else if (ch == 27) {
						// Escape: ignore next char too
						int nch = (char)fReader.read();
						if (nch == 91) {
							//vt100 escape sequence: read until end-of-command (skip digits and semicolon)
							//e.g. \x1b;13;m --> ignore the entire command, including the trailing m
							do {
								nch = fReader.read();
							} while (Character.isDigit((char)nch) || nch == ';');
						}
					}
				}

				boolean ready = fReader.ready();
				//TODO Get rid of this to support UNIX and Mac? -- It appears that
				//due to vt100 emulation we get CRLF even for UNIX connections.
				if (ch == 10 && lastch == 13) {
					return theLine.toString();
				}
				lastch = ch;

				// Check to see if the BufferedReader is still ready which means
				// there are more characters
				// in the Buffer...If not, then we assume it is waiting for
				// input.
				if (!ready) {
					// wait to make sure
					try {
						Thread.sleep(_waitIncrement);
					} catch (InterruptedException e) {
					}
					if (!fReader.ready()) {
						if (done) {
							return theLine.toString().trim();
						} else {
							done = true;
						}
					}
				}
			} catch (IOException e) {
				return null;
			}
		}
		return theLine.toString();
	}

}
