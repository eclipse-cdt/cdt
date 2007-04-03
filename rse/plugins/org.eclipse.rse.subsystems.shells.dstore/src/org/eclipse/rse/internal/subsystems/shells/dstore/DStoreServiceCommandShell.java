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

package org.eclipse.rse.internal.subsystems.shells.dstore;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.rse.internal.services.dstore.shells.DStoreHostOutput;
import org.eclipse.rse.internal.services.dstore.shells.DStoreHostShell;
import org.eclipse.rse.internal.services.dstore.shells.DStoreShellOutputReader;
import org.eclipse.rse.internal.subsystems.shells.servicesubsystem.OutputRefreshJob;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.subsystems.shells.core.model.RemoteError;
import org.eclipse.rse.subsystems.shells.core.model.RemoteOutput;
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
		IHostOutput[] lines = event.getLines();
		IRemoteOutput[] outputs = new IRemoteOutput[lines.length];
		for (int i = 0; i < lines.length; i++)
		{
			RemoteOutput output = null;
			Object lineObj = lines[i];
			if (lineObj instanceof DStoreHostOutput)
			{
				DataElement line = ((DStoreHostOutput)lineObj).getElement();
				String type = line.getType();
				String src = line.getSource();
				if (event.isError())
				{
					output = new RemoteError(this, type);		
				
				}
				else
				{
					output = new RemoteOutput(this, type);
				}
				output.setText(line.getName());								
				
				int colonSep = src.indexOf(':');
				// line numbers
				if (colonSep > 0)
				{
					
					String lineNo = src.substring(colonSep + 1);
					String file = src.substring(0, colonSep);
					int linen = 0;
					try
					{
						linen = Integer.parseInt(lineNo);
					}
					catch (Exception e)
					{
						
					}
					if (linen != 0)
					{
						output.setAbsolutePath(file);
						output.setLine(linen);
					}
					else
					{
						output.setAbsolutePath(src);
					}				
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

	public boolean isActive() 
	{
		boolean activeShell = _hostShell.isActive();
		if (!activeShell)
		{
			DataElement status = ((DStoreHostShell)_hostShell).getStatus();
			if (_output.size() < status.getNestedSize())
			{
				return true;
			}				
		}
		return activeShell;
	}
	
}
