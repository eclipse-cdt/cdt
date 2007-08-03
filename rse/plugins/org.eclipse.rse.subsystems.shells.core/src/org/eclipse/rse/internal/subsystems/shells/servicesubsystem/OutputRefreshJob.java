/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [197848] Fix shell terminated state when remote dies
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.shells.servicesubsystem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.subsystems.shells.core.ShellStrings;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
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
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			if (_outputs != null)
			{
				if ((_outputs.length > 0) && (_outputs[0] != null)) {
					registry.fireEvent(
							new SystemResourceChangeEvent(_outputs, ISystemResourceChangeEvents.EVENT_REFRESH, _command));
				}
				
				if (_cwdChanged)
				{
					registry.fireEvent(
							new SystemResourceChangeEvent(_command, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, _command.getCommandSubSystem()));
				}
				
				//Bug 197848: Artificial event for shell termination
				if (_outputs.length == 0 && !_command.isActive()) {
					ISubSystem subsys = _command.getCommandSubSystem();
					//update action states in commands view
					registry.fireEvent(
							new SystemResourceChangeEvent(_command, ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_FINISHED, subsys));
					//update "connected" overlay in SystemView
					registry.fireEvent(
							new SystemResourceChangeEvent(_command, ISystemResourceChangeEvents.EVENT_REFRESH, subsys));
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