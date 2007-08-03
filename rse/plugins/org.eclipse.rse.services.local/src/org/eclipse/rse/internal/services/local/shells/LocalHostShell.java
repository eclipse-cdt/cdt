/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [161838] local shell reports isActive() wrong
 *******************************************************************************/

package org.eclipse.rse.internal.services.local.shells;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.internal.services.local.shells.LocalShellOutputReader;
import org.eclipse.rse.internal.services.local.shells.LocalShellThread;
import org.eclipse.rse.services.shells.AbstractHostShell;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputReader;

/**
 * @author dmcknigh
 *
 */
public class LocalHostShell extends AbstractHostShell implements IHostShell
{
	private LocalShellThread _shellThread;
	private LocalShellOutputReader _stdoutHandler;
	private LocalShellOutputReader _stderrHandler;
	
	public LocalHostShell(String initialWorkingDirectory, String invocation, String encoding, String[] environment)
	{
		_shellThread = new LocalShellThread(initialWorkingDirectory, invocation, encoding, environment);	
		_stdoutHandler = new LocalShellOutputReader(this, _shellThread.getOutputStream(), false);
		_stderrHandler = new LocalShellOutputReader(this, _shellThread.getErrorStream(),true);
	}
	
	protected void run(IProgressMonitor monitor)
	{
		_shellThread.start();
	}
 

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IHostShell#isActive()
	 */
	public boolean isActive()
	{
		if (_stdoutHandler.isFinished()) {
			if (_shellThread.isAlive()) {
				_shellThread.stopThread();
			}
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IHostShell#writeToStdin(java.lang.String)
	 */
	public void writeToShell(String command)
	{
		_shellThread.sendInput(command);
	}
	
	public IHostShellOutputReader getStandardOutputReader()
	{
		return _stdoutHandler;
	}

	public IHostShellOutputReader getStandardErrorReader()
	{
		return _stderrHandler;
	}

	public void exit()
	{
		writeToShell("exit"); //$NON-NLS-1$
	}


}
