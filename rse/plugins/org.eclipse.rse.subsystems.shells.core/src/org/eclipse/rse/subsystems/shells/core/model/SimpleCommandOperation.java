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

package org.eclipse.rse.subsystems.shells.core.model;


import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.swt.widgets.Display;


public class SimpleCommandOperation
{
	private IRemoteCmdSubSystem  _subsystem;
	private IRemoteFile         _workingDirectory;
	private IRemoteCommandShell _cmdShell;
	private int _outputLineIndex = 0;
	
	public SimpleCommandOperation(IRemoteCmdSubSystem subsystem, IRemoteFile workingDirectory)
	{
		_subsystem = subsystem;
		_workingDirectory = workingDirectory;
	}
	
	public void runCommand(String command) throws Exception
	{
		Object[] result =_subsystem.runCommand(command, null, _workingDirectory, false);
		_cmdShell = (IRemoteCommandShell)result[0];
	}

	public IRemoteCommandShell getCommandShell()
	{
		return _cmdShell;
	}
	
	public void putInput(String input) throws Exception
	{
		if (isActive())
		{
			_subsystem.sendCommandToShell(input, null, _cmdShell);
		}
	}
	
	public boolean isActive()
	{
		if (_cmdShell != null)
		{
			return _cmdShell.isActive();
		}
		return false;
	}
	
	protected boolean hasMoreOutput()
	{
		if (_cmdShell != null)
		{			

			if (_cmdShell.listOutput().length > _outputLineIndex)
			{
				return true;
			}
		}
		return false;
	}
	
	public void cancelCommand() throws Exception
	{
		if (_cmdShell != null && _cmdShell.isActive())
		{
			_cmdShell.getCommandSubSystem().cancelShell(null, _cmdShell);
		}
	}

	
	public String readLine(boolean waitForOutput)
	{
		if (_cmdShell != null)
		{
			if (!hasMoreOutput() && waitForOutput)
			{
				while (!hasMoreOutput())
				{
					if (!isActive())
					{
						return null;
					}
					try
					{
						Display d = Display.getCurrent();
						if (d != null)
						{
							while (d.readAndDispatch());
						}
						Thread.sleep(100);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
				
			{
				Object output = _cmdShell.getOutputAt(_outputLineIndex);
				_outputLineIndex++;
				if (output instanceof IRemoteOutput)
				{
					return ((IRemoteOutput)output).getText();
				}
				else if (output instanceof IRemoteFile)
				{
					return ((IRemoteFile)output).getLabel();
				}
			
			}
		}
		return "";
	}

}