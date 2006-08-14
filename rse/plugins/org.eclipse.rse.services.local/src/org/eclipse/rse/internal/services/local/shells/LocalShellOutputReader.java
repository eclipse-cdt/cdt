/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.internal.services.local.shells;

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.rse.services.local.Activator;
import org.eclipse.rse.services.shells.AbstractHostShellOutputReader;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputReader;

/**
 * Listener to shell output. As io streams through, refresh events are sent out
 * for the OutputChangeListener to respond to.
 */
public class LocalShellOutputReader extends AbstractHostShellOutputReader implements IHostShellOutputReader
{
	protected BufferedReader _reader;
	private String fPromptChars = ">$%#]"; //Characters we accept as the end of a prompt //$NON-NLS-1$;

	
	public LocalShellOutputReader(IHostShell hostShell, BufferedReader reader, boolean isErrorReader)
	{
		super(hostShell, isErrorReader);
		_reader = reader;
	}
	/*
	protected Object internalReadLine()
	{
		StringBuffer theLine = new StringBuffer();
		int ch;
		int lastch = 0;
		boolean done = false;
		while (!done && !isFinished())
		{
			try
			{

				ch = _reader.read();
				switch (ch)
				{
				case -1:
					if (theLine.length() == 0) // End of Reader
						return null;
					done = true;
					break;
				case 65535:
					if (theLine.length() == 0) // Check why I keep getting
						// this!!!
						return null;
					done = true;
					break;
				case 10:
					done = true; // Newline

					break;
				case 9:
					theLine.append("     "); // Tab
					break;
				case 13:
					break; // Carriage Return
				default:
					char tch = (char) ch;
					if (!Character.isISOControl(tch))
					{
						theLine.append(tch); // Any other character
					}
					else
					{
						// ignore next char too
						_reader.read();
					}
				}

				boolean ready = _reader.ready();
				if (ch == 10 && lastch == 13)
				{
					return theLine.toString();
				}
				lastch = ch;

				// Check to see if the BufferedReader is still ready which means
				// there are more characters
				// in the Buffer...If not, then we assume it is waiting for
				// input.
				if (!ready)
				{
					// wait to make sure
					try
					{
						//_reader.wait(_waitIncrement);
						Thread.sleep(_waitIncrement);
					}
					catch (InterruptedException e)
					{
					}
					if (!_reader.ready())
					{
						if (done)
						{
							return theLine.toString().trim();
						}
						else
						{
							done = true;
						}

					}
				}

			}
			catch (IOException e)
			{
				return null;
			}
		}

		return theLine.toString();
	}
*/
	protected Object internalReadLine() {
		if (_reader == null) {
			//Our workaround sets the stderr reader to null, so we never give any stderr output.
			//TODO Check if ssh supports some method of having separate stdout and stderr streams
			return null;
		}
		StringBuffer theLine = new StringBuffer();
		StringBuffer theDebugLine = null;
		theDebugLine = new StringBuffer();
		int ch;
		boolean done = false;
		while (!done && !isFinished()) {
			try {
				ch = _reader.read();
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
				case 13:
					if(theDebugLine!=null) theDebugLine.append((char)ch);
					break; // Carriage Return: dont append to the buffer
				case 10:
					if(theDebugLine!=null) theDebugLine.append((char)ch);
					done = true; // Newline
					break;
				case 9:
					//Tab: we count tabs at column 8
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
						int nch = _reader.read();
						if (theDebugLine!=null) theDebugLine.append((char)nch);
						if (nch == 91) {
							//vt100 escape sequence: read until end-of-command (skip digits and semicolon)
							//e.g. \x1b;13;m --> ignore the entire command, including the trailing m
							do {
								nch = _reader.read();
								if (theDebugLine!=null) theDebugLine.append((char)nch);
							} while (Character.isDigit((char)nch) || nch == ';');
						}
					}
				}

				// Check to see if the BufferedReader is still ready which means
				// there are more characters
				// in the Buffer...If not, then we assume it is waiting for
				// input.
				if (!done && !_reader.ready()) {
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
					try {
						Thread.sleep(waitIncrement);
					} catch (InterruptedException e) {
					}
					if (!_reader.ready()) {
						done = true;
					}
				}
			} catch (IOException e) {
				//FIXME it's dangerous to return null here since this will end
				//our reader thread completely... the exception could just be
				//temporary, and we should keep running!
				Activator.getDefault().logException(e);
				return null;
			}
		}
		if (theDebugLine!=null) {
			String debugLine = theDebugLine.toString();
			debugLine.compareTo(""); //$NON-NLS-1$
		}
		return theLine.toString();
	}


}