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

package org.eclipse.rse.internal.subsystems.shells.servicesubsystem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.subsystems.shells.core.ShellStrings;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.ui.progress.UIJob;


public class OutputRefreshJob extends UIJob
	{
		private IRemoteCommandShell _command;
		private IRemoteOutput[] _outputs;
		private boolean _cwdChanged = false;
		private boolean _isComplete = false;


		public OutputRefreshJob(IRemoteCommandShell command, IRemoteOutput[] outputs, boolean cwdChanged)
		{
			super(ShellStrings.RSESubSystemOperation_Refresh_Output);
		    _command = command;
		    _cwdChanged = cwdChanged;
		    _outputs = outputs;
		}
		
		public void addOutputs(IRemoteOutput[] outputs)
		{		
			IRemoteOutput[] oldOutputs = _outputs;
			int total = oldOutputs.length + outputs.length;
			IRemoteOutput[] newOutputs = new IRemoteOutput[total];
			int k = 0;
			for (int i = 0; i < oldOutputs.length; i++)
			{
				newOutputs[k] = oldOutputs[i];
				k++;
			}
			for (int j = 0; j < outputs.length; j++)
			{
				newOutputs[k] = outputs[j];
				k++;
			}
			_outputs = newOutputs;
		}
		
		public boolean isComplete()
		{
			return _isComplete;
		}

		public IStatus runInUIThread(IProgressMonitor monitor)
		{
			try
			{
			ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			if (_outputs != null)
			{
				registry.fireEvent(
					new SystemResourceChangeEvent(_outputs, ISystemResourceChangeEvents.EVENT_REFRESH, _command));
				
				if (_cwdChanged)
				{
					registry.fireEvent(
							new SystemResourceChangeEvent(_command, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, _command.getCommandSubSystem()));
				}
			}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			_isComplete= true;
			return Status.OK_STATUS;
		}
	}