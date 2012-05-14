/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
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
 * David McKnight   (IBM)        - [379454] [shells] too many output refresh jobs created when populating shell output
 *******************************************************************************/

package org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem;

import org.eclipse.rse.internal.subsystems.shells.core.ShellStrings;
import org.eclipse.rse.internal.subsystems.shells.servicesubsystem.OutputRefreshJob;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.SimpleHostOutput;
import org.eclipse.rse.subsystems.shells.core.model.RemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.model.RemoteError;
import org.eclipse.rse.subsystems.shells.core.model.RemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.ICandidateCommand;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;


public class ServiceCommandShell extends RemoteCommandShell implements IServiceCommandShell
{
	protected IHostShell _hostShell;
	private OutputRefreshJob _lastRefreshJob;
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
		IHostOutput[] lines = event.getLines();
		IRemoteOutput[] outputs = new IRemoteOutput[lines.length];
		for (int i = 0; i < lines.length; i++)
		{
			RemoteOutput output = null;
			IHostOutput lineObj = lines[i];
			if (lineObj instanceof SimpleHostOutput)
			{
				SimpleHostOutput line = (SimpleHostOutput)lineObj;

				String type = event.isError() ? "stderr" : "stdout"; //$NON-NLS-1$ //$NON-NLS-2$
				if (event.isError())
				{
					output = new RemoteError(this, type);
				}
				else
				{
					output = new RemoteOutput(this, type);
				}
				String str = line.getString();
				output.setText(str);

				addOutput(output);
				outputs[i] = output;
			}
		}
		notifyOutputChanged(outputs, false);
	}

	/**
	 * Notify listeners about new outputs. This will update any shell views with
	 * the new data.
	 *
	 * @param outputs the output objects to notify
	 * @param cwdChanged <code>true</code> if the current directory changed
	 * @since org.eclipse.rse.subsystems.shells.core 3.0
	 */
	protected void notifyOutputChanged(IRemoteOutput[] outputs, boolean cwdChanged) {
		if (_lastRefreshJob == null || _lastRefreshJob.isComplete()){
			_lastRefreshJob = new OutputRefreshJob(this, outputs, cwdChanged);
			_lastRefreshJob.schedule();
		}
		else {
			_lastRefreshJob.addOutputs(outputs);
			_lastRefreshJob.schedule(); 
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
