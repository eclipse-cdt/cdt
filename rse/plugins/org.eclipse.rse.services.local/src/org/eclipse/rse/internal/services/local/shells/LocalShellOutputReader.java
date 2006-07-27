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

	public LocalShellOutputReader(IHostShell hostShell, BufferedReader reader, boolean isErrorReader)
	{
		super(hostShell, isErrorReader);
		_reader = reader;
	}
	
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



}