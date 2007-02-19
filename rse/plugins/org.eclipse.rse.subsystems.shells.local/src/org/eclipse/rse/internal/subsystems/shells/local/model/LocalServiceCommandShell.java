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

package org.eclipse.rse.internal.subsystems.shells.local.model;


import java.io.File;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.subsystems.shells.servicesubsystem.OutputRefreshJob;
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
				return _fs.getRemoteFileObject(workingDir);
			}
			catch (Exception e)
			{			
			}
		}
		return null;

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
			}
		
			addOutput(output);
			outputs[i] = output;
		}
		//if (_lastRefreshJob == null || _lastRefreshJob.isComplete())
		{
			_lastRefreshJob = new OutputRefreshJob(this, outputs, false);
			_lastRefreshJob.schedule();
		}
		/*
		else
		{
			_lastRefreshJob.addOutputs(outputs);
			_lastRefreshJob.schedule();
		}
		*/
	}
	
	public void writeToShell(String cmd)
	{
		_patterns.update(cmd);
		super.writeToShell(cmd);

	}
	
}