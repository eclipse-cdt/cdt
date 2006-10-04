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

package org.eclipse.rse.subsystems.shells.dstore.model;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.rse.internal.services.dstore.shell.DStoreShellOutputReader;
import org.eclipse.rse.internal.subsystems.shells.servicesubsystem.OutputRefreshJob;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteError;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteOutput;
import org.eclipse.rse.services.dstore.shells.DStoreHostShell;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ServiceCommandShell;

public class DStoreServiceCommandShell extends ServiceCommandShell
{

	public DStoreServiceCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell)
	{
		super(cmdSS, hostShell);
	}

	public Object getContext()
	{
		DStoreHostShell shell = (DStoreHostShell)getHostShell();
		DStoreShellOutputReader reader = (DStoreShellOutputReader)shell.getStandardOutputReader();
		String workingDir = reader.getWorkingDirectory();
		if (workingDir != null && workingDir.length() > 0)
		{
			try
			{
				return getFileSubSystem().getRemoteFileObject(workingDir);
			}
			catch (Exception e)
			{			
			}
		}
		return null;

	}

	public void shellOutputChanged(IHostShellChangeEvent event)
	{
		Object[] lines = event.getLines();
		IRemoteOutput[] outputs = new IRemoteOutput[lines.length];
		for (int i = 0; i < lines.length; i++)
		{
			RemoteOutput output = null;
			Object lineObj = lines[i];
			if (lineObj instanceof DataElement)
			{
				DataElement line = (DataElement)lineObj;
				String type = line.getType();
				if (event.isError())
				{
					output = new RemoteError(this, type);									
				}
				else
				{
					output = new RemoteOutput(this, type);
				}
				output.setText(line.getName());
				
				String src = line.getSource();
				int colonSep = src.indexOf(':');
				// line numbers
				if (colonSep > 0)
				{
					String lineNo = src.substring(colonSep + 1);
					String file = src.substring(0, colonSep);
					output.setAbsolutePath(file);
					output.setLine(Integer.parseInt(lineNo));
				}
				else
				{
					output.setAbsolutePath(src);	
				}
	
				addOutput(output);
				outputs[i] = output;
			}
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
	
}
