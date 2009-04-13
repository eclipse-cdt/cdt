/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [225510][api] Fix OutputRefreshJob API leakage
 * David McKnight   (IBM)        - [272032][ssh][telnet][local] shell output not setting line numbers when available
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.shells.local.model;


import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.ParsedOutput;
import org.eclipse.rse.services.shells.Patterns;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.model.ISystemOutputRemoteTypes;
import org.eclipse.rse.subsystems.shells.core.model.RemoteError;
import org.eclipse.rse.subsystems.shells.core.model.RemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ServiceCommandShell;

public class LocalServiceCommandShell extends ServiceCommandShell
{
	private Patterns _patterns;
	private String _workingDir;
	private IRemoteFileSubSystem _fs;

	public LocalServiceCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell)
	{
		super(cmdSS, hostShell);
		_patterns = new Patterns();
		_patterns.update("cmd"); //$NON-NLS-1$
		ISubSystem[] sses = cmdSS.getHost().getSubSystems();
		for (int i = 0; i < sses.length; i++)
		{
			if (sses[i] instanceof IRemoteFileSubSystem)
			{
				_fs = (IRemoteFileSubSystem)sses[i];
			}
		}
	}

	public Object getContext()
	{
		String workingDir = _workingDir;
		if (workingDir != null && workingDir.length() > 0)
		{
			try
			{
				return _fs.getRemoteFileObject(workingDir, new NullProgressMonitor());
			}
			catch (Exception e)
			{
			}
		}
		return null;
	}

	public String getContextString()
	{
		return _workingDir;
	}



	public void shellOutputChanged(IHostShellChangeEvent event)
	{
		IHostOutput[] lines = event.getLines();
		IRemoteOutput[] outputs = new IRemoteOutput[lines.length];
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].getString();
			ParsedOutput parsedMsg = null;

			try
			{
				parsedMsg = _patterns.matchLine(line);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}

			RemoteOutput output = null;
			String type = "stdout"; //$NON-NLS-1$
			if (parsedMsg != null)
			{
				type = parsedMsg.type;
			}
			else
			{
				//System.out.println("parsedMsg = null");
			}
			if (event.isError())
			{
				output = new RemoteError(this, type);
			}
			else
			{
				output = new RemoteOutput(this, type);
			}

			output.setText(line);
			if (parsedMsg != null)
			{
				String file = parsedMsg.file;
				if (type.equals(ISystemOutputRemoteTypes.TYPE_PROMPT))
				{
					_workingDir = file;
					output.setAbsolutePath(_workingDir);
				}
				/*
				else if (type.equals(ISystemOutputRemoteTypes.TYPE_FILE) || type.equals(ISystemOutputRemoteTypes.TYPE_DIRECTORY))
				{
					output.setAbsolutePath(parsedMsg.file);
				}
				*/
				else
				{
					output.setAbsolutePath(_workingDir + File.separatorChar + file);
				}
				if (parsedMsg.line > 0){
					output.setLine(parsedMsg.line);
				}
			}

			addOutput(output);
			outputs[i] = output;
		}
		notifyOutputChanged(outputs, false);
	}

	public void writeToShell(String cmd)
	{
		_patterns.update(cmd);
		super.writeToShell(cmd);

	}

}
