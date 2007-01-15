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
			_cmdShell = _subsystem.runShell(null, _workingDirectory);
			
			
			_subsystem.sendCommandToShell(new NullProgressMonitor(), command, _cmdShell);
			if (exitShell)
			{
				_subsystem.sendCommandToShell(new NullProgressMonitor(), "exit", _cmdShell); //$NON-NLS-1$
			}
		}
		else
		{
			
			Object[] result =_subsystem.runCommand(new NullProgressMonitor(), command, _workingDirectory, false);
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
		_cmdShell = _subsystem.runShell(null, _workingDirectory);
			
		if (exports != null)
		{
			_subsystem.sendCommandToShell(new NullProgressMonitor(), exports, _cmdShell);			
		}
		_subsystem.sendCommandToShell(new NullProgressMonitor(), command, _cmdShell);
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
				_subsystem.sendCommandToShell(new NullProgressMonitor(), "exit", _cmdShell);						 //$NON-NLS-1$
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
			_subsystem.sendCommandToShell(new NullProgressMonitor(), input, _cmdShell);
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
			_cmdShell.getCommandSubSystem().cancelShell(null, _cmdShell);
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
