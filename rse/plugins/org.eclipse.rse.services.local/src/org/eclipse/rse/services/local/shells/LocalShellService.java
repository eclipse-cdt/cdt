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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.local.shells;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.local.ILocalService;
import org.eclipse.rse.services.local.files.LocalServiceResources;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;

public class LocalShellService implements IShellService, ILocalService
{
	private static final String SHELL_INVOCATION = ">";
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
	
	public IHostShell launchShell(IProgressMonitor monitor, String initialWorkingDirectory, String[] environment)
	{
		String defaultEncoding = System.getProperty("file.encoding");
		return launchShell(monitor, initialWorkingDirectory, defaultEncoding, environment);
	}

	public IHostShell launchShell(IProgressMonitor monitor, String initialWorkingDirectory, String encoding, String[] environment)
	{
		LocalHostShell hostShell = new LocalHostShell(initialWorkingDirectory,SHELL_INVOCATION, encoding, environment);
		hostShell.run(monitor);
		return hostShell;
	}

	public IHostShell runCommand(IProgressMonitor monitor, String initialWorkingDirectory, String command, String[] environment)
	{
		String defaultEncoding = System.getProperty("file.encoding");
		return runCommand(monitor, initialWorkingDirectory, command, defaultEncoding, environment);
	}
	
	public IHostShell runCommand(IProgressMonitor monitor, String initialWorkingDirectory, String command, String encoding, String[] environment)
	{
		LocalHostShell hostShell = new LocalHostShell(initialWorkingDirectory,command, encoding, environment);
		hostShell.run(monitor);
		return hostShell;
	}

	public String[] getHostEnvironment()
	{
		if (_envVars == null)
		{
			List envVars = new ArrayList();

			String[] envCommand = new String[3];
			//If we're on windows, change the envCommand. 
			if (System.getProperty("os.name").toLowerCase().startsWith("win"))
			{
				envCommand[0] = "cmd";
				envCommand[1] = "/c";
				envCommand[2] = "set";
			}
			else
			{
				envCommand[0] = "sh";
				envCommand[1] = "-c";
				envCommand[2] = "env";
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
					if (curLine.indexOf("=") > 0)
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

	public void initService(IProgressMonitor monitor)
	{
		
	}
	
	public void uninitService(IProgressMonitor monitor)
	{
		
	}
	public SystemMessage getMessage(String messageID)
	{
		return null;
	}
}