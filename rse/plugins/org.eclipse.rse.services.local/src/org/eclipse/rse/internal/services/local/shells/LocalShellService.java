/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable
 * Martin Oberhuber (Wind River) - [226301][api] IShellService should throw SystemMessageException on error
 ********************************************************************************/

package org.eclipse.rse.internal.services.local.shells;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.internal.services.local.ILocalService;
import org.eclipse.rse.internal.services.local.LocalServiceResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.AbstractShellService;
import org.eclipse.rse.services.shells.IHostShell;

public class LocalShellService extends AbstractShellService implements ILocalService
{
	private static final String SHELL_INVOCATION = ">"; //$NON-NLS-1$
	private String[] _envVars;

	public LocalShellService()
	{
	}

	public String getName()
	{
		return LocalServiceResources.Local_Shell_Service_Name;
	}

	public String getDescription()
	{
		return LocalServiceResources.Local_Shell_Service_Description;
	}

	public IHostShell launchShell(String initialWorkingDirectory, String encoding, String[] environment, IProgressMonitor monitor) throws SystemMessageException
	{
		LocalHostShell hostShell = new LocalHostShell(initialWorkingDirectory,SHELL_INVOCATION, encoding, environment);
		hostShell.run(monitor);
		return hostShell;
	}

	public IHostShell runCommand(String initialWorkingDirectory, String command, String encoding, String[] environment, IProgressMonitor monitor) throws SystemMessageException
	{
		LocalHostShell hostShell = new LocalHostShell(initialWorkingDirectory,command, encoding, environment);
		hostShell.run(monitor);
		return hostShell;
	}

	public String[] getHostEnvironment() throws SystemMessageException
	{
		if (_envVars == null)
		{
			List envVars = new ArrayList();

			String[] envCommand = new String[3];
			//If we're on windows, change the envCommand.
			if (System.getProperty("os.name").toLowerCase().startsWith("win")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				envCommand[0] = "cmd"; //$NON-NLS-1$
				envCommand[1] = "/c"; //$NON-NLS-1$
				envCommand[2] = "set"; //$NON-NLS-1$
			}
			else
			{
				envCommand[0] = "sh"; //$NON-NLS-1$
				envCommand[1] = "-c"; //$NON-NLS-1$
				envCommand[2] = "env"; //$NON-NLS-1$
			}

			BufferedReader reader = null;
			try
			{
				Process process = Runtime.getRuntime().exec(envCommand);
				InputStream stdout = process.getInputStream();
				InputStreamReader ireader = new InputStreamReader(stdout);

				reader = new BufferedReader(ireader);
				String curLine = null;
				while ((curLine = reader.readLine()) != null)
				{
					if (curLine.indexOf("=") > 0) //$NON-NLS-1$
					{
						envVars.add(curLine);
					}
				}
				reader.close();
				process.exitValue();
			}
			catch (Exception e)
			{
				if (reader != null)
				{
					try
					{
						reader.close();
					}
					catch (Exception ex)
					{
					}
				}
			}
			_envVars = (String[])envVars.toArray(new String[envVars.size()]);
		}
		return _envVars;
	}

}