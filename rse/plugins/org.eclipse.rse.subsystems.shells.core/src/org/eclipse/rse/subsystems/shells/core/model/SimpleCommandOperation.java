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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 ********************************************************************************/

package org.eclipse.rse.subsystems.shells.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;

public class SimpleCommandOperation
{
	protected IRemoteCmdSubSystem  _subsystem;
	protected IRemoteFile         _workingDirectory;
	protected IRemoteCommandShell _cmdShell;
	protected List                _envVars;
	protected int _outputLineIndex = 0;
	protected boolean _runAsShell = false;
	
	public SimpleCommandOperation(IRemoteCmdSubSystem subsystem, IRemoteFile workingDirectory, boolean runAsShell)
	{
		_subsystem = subsystem;
		_workingDirectory = workingDirectory;
		_envVars = new ArrayList();
		_runAsShell = runAsShell;
	}

	public IRemoteCommandShell getCommandShell()
	{
		return _cmdShell;
	}
	
	public void setEnvironmentVariable(String name, String value)
	{
		_envVars.add(name + "=" + value); //$NON-NLS-1$
		
	}
	
	public void setEnvironmentVariables(String[] names, String[] values)
	{
		for (int i = 0; i < names.length; i++)
		{
			setEnvironmentVariable(names[i], values[i]);
		}
	}
	
	public void setEnvironmentVariables(String[] vars)
	{
		for (int i = 0; i < vars.length; i++)
		{
			_envVars.add(vars[i]);
		}
	}
	
	public String[] getEnvironmentVariables()
	{
		String[] vars = new String[_envVars.size()];
		for (int i = 0; i < vars.length; i++)
		{
			vars[i] = (String)_envVars.get(i);
		}
		return vars;
	}
	
	/**
	 * Run a command
	 * @param command the command to run
	 * @param exitShell indicates whether to exit the shell after running the command
	 * @throws Exception
	 */
	public void runCommand(String command, boolean exitShell) throws Exception
	{
		if (_runAsShell)
		{
			_cmdShell = _subsystem.runShell(_workingDirectory, null);
			
			
			_subsystem.sendCommandToShell(command, _cmdShell, new NullProgressMonitor());
			if (exitShell)
			{
				_subsystem.sendCommandToShell("exit", _cmdShell, new NullProgressMonitor()); //$NON-NLS-1$
			}
		}
		else
		{
			
			Object[] result =_subsystem.runCommand(command, _workingDirectory, false, new NullProgressMonitor());
			_cmdShell= (IRemoteCommandShell)result[0];
		}		
	}
	
	/**
	 * Launch a shell with the specified exports and command
	 * @param exports the command to initialize the shell environment
	 * @param command the command to run
	 * @param exitShell indicates whether to exit the shell after running the command
	 * @throws Exception
	 */
	public void runCommandInShell(String exports, String command, boolean exitShell) throws Exception
	{
		_runAsShell = true;
		_cmdShell = _subsystem.runShell(_workingDirectory, null);
			
		if (exports != null)
		{
			_subsystem.sendCommandToShell(exports, _cmdShell, new NullProgressMonitor());			
		}
		_subsystem.sendCommandToShell(command, _cmdShell, new NullProgressMonitor());
		if (exitShell)
		{
			exitShell();
		}
	}
	
	public void removeShell()
	{
		try
		{
			_subsystem.removeShell(_cmdShell);
		}
		catch (Exception e)
		{			
		}
	}
	
	public void exitShell()
	{
		if (_runAsShell)
		{
			try
			{
				_subsystem.sendCommandToShell("exit", _cmdShell, new NullProgressMonitor());						 //$NON-NLS-1$
			}
			catch (Exception e)
			{							
			}
		}
	}
	
	public void putInput(String input) throws Exception
	{
		if (isActive())
		{
			_subsystem.sendCommandToShell(input, _cmdShell, new NullProgressMonitor());
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
			if (_cmdShell.getSize() > _outputLineIndex)
			{
				return true;
			}
			else if (_cmdShell.listOutput().length > _outputLineIndex)
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
			_cmdShell.getCommandSubSystem().cancelShell(_cmdShell, null);
		}
	}

	
	public String readLine(boolean waitForOutput)
	{
		if (_cmdShell != null)
		{
			boolean isActive = true;
			if (!hasMoreOutput() && waitForOutput)
			{				
				while (!hasMoreOutput() && isActive)
				{
								
					try
					{
						isActive = isActive();
						Thread.sleep(100);												
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
				
			{
				Object[] out = _cmdShell.listOutput();
				if (out.length > _outputLineIndex)
				{
					Object output = out[_outputLineIndex];
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
				else if (!isActive)
				{
					return null;
				}
			
			}
		}
		return ""; //$NON-NLS-1$
	}

}
