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

package org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem;

import org.eclipse.rse.internal.subsystems.shells.servicesubsystem.OutputRefreshJob;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteCommandShell;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteError;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.subsystems.shells.core.ShellStrings;
import org.eclipse.rse.subsystems.shells.core.subsystems.ICandidateCommand;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;


public class ServiceCommandShell extends RemoteCommandShell implements IServiceCommandShell
{
	protected IHostShell _hostShell;
	protected OutputRefreshJob _lastRefreshJob;
	public ServiceCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell)
	{
		super(cmdSS);
		_hostShell = hostShell;
		setType(ShellStrings.RESID_SHELLS_COMMAND_SHELL_LABEL);
	}
	
	public String getTitle()
	{
		return getId();
	}

	public boolean isActive()
	{
		return _hostShell.isActive();
	}


	public IHostShell getHostShell()
	{
		return _hostShell;
	}
	


	public void shellOutputChanged(IHostShellChangeEvent event)
	{
		Object[] lines = event.getLines();
		IRemoteOutput[] outputs = new IRemoteOutput[lines.length];
		for (int i = 0; i < lines.length; i++)
		{
			RemoteOutput output = null;
			Object lineObj = lines[i];
			if (lineObj instanceof String)
			{
				String line = (String)lineObj;
			
				if (line != null)
				{
					String type = event.isError() ? "stderr" : "stdout";
					if (event.isError())
					{
						output = new RemoteError(this, type);
						
					}
					else
					{
						output = new RemoteOutput(this, type);
					}
					output.setText(line);
					
					addOutput(output);
					outputs[i] = output;
				}
			}
		}
		if (_lastRefreshJob == null || _lastRefreshJob.isComplete())
		{
			_lastRefreshJob = new OutputRefreshJob(this, outputs, false);
			_lastRefreshJob.schedule();
		}
		else
		{
			_lastRefreshJob.addOutputs(outputs);
		}
	}
	

	public ICandidateCommand[] getCandidateCommands()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void writeToShell(String cmd)
	{
		_hostShell.writeToShell(cmd);
	}
}