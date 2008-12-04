/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 ********************************************************************************/

package org.eclipse.rse.subsystems.shells.core.model;

import java.util.Random;
import java.util.Stack;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.model.ISystemShellProvider;
import org.eclipse.swt.widgets.Shell;

/**
 * Base class for command shell wrappers that use echo markers to parse command finish.
 * This abstract class can be extended to provide a command shell wrapper. When
 * running a shell, commands can be piped to the shell via the sendCommand()
 * method. Echo commands are used to determine when each command is complete.
 * Whenever a command completes, the handleCommandFinished() method is called.
 *
 */
public abstract class RemoteCommandShellOperation
	implements ISystemResourceChangeListener, ISystemShellProvider
{

	protected class CommandAlias
	{
		private String _alias;
		private String _command;
		public CommandAlias(String alias, String command)
		{
			_alias = alias;
			_command = command;
		}

		public String getAlias()
		{
			return _alias;
		}

		public String getCommand()
		{
			return _command;
		}
	}


	protected IRemoteCmdSubSystem _cmdSubSystem;
	protected IRemoteFile _pwd;
	protected Shell _shell;

	protected IRemoteCommandShell _remoteCmdShell;

	private Stack _commandStack;
	private int _outputOffset = 0;
	private String _cmdSeparator = ";"; //$NON-NLS-1$

	private Random _random;

	/**
	 * Constructor
	 */
	public RemoteCommandShellOperation(Shell shell, IRemoteCmdSubSystem cmdSubSystem, IRemoteFile pwd)
	{
		_random = new Random(System.currentTimeMillis());
		_pwd = pwd;
		_cmdSubSystem = cmdSubSystem;
		_shell = shell;
		_commandStack = new Stack();
		_cmdSeparator = _cmdSubSystem.getParentRemoteCmdSubSystemConfiguration().getCommandSeparator();
	}

	public void setWorkingDirectory(IRemoteFile pwd)
	{
	    _pwd = pwd;
	}

	/**
	 * Launches a new remote shell
	 */
	public IRemoteCommandShell run()
	{
		try
		{
			RSECorePlugin.getTheSystemRegistry().addSystemResourceChangeListener(this);
			_remoteCmdShell = _cmdSubSystem.runShell(_pwd, new NullProgressMonitor());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return _remoteCmdShell;
	}

	public IRemoteCommandShell getRemoteCommandShell()
	{
		return _remoteCmdShell;
	}

	public void associateProject(IProject project)
	{
		if (_remoteCmdShell != null)
		{
			_remoteCmdShell.associateProject(project);
		}
	}

	/**
	 * Called when a shell is complete.
	 */
	public void finish()
	{
		RSECorePlugin.getTheSystemRegistry().removeSystemResourceChangeListener(this);
		if (_remoteCmdShell != null && _remoteCmdShell.isActive())
		{
			try
			{
				_cmdSubSystem.cancelShell(_remoteCmdShell, new NullProgressMonitor());
			}
			catch (Exception e)
			{
			}
		}

	}

	private String getEchoCmd(CommandAlias alias)
	{
		return "echo " + getEchoResult(alias); //$NON-NLS-1$
	}

	private String getEchoResult(CommandAlias alias)
	{
		return "BEGIN-END-TAG:" + alias.getAlias() + " done"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getCurrentCommand()
	{
	    if (_commandStack != null && _commandStack.size() > 0)
	    {
	        CommandAlias firstCommand = (CommandAlias)_commandStack.firstElement();
	        return firstCommand.getCommand();
	    }
	    return null;
	}

	/**
	 * Send a command to the running command shell.
	 * @param cmd the command to run in the shell
	 */
	public void sendCommand(String cmd)
	{
		if (_remoteCmdShell != null)
		{
			try
			{
				String newId = String.valueOf(_random.nextInt());
				CommandAlias alias = new CommandAlias(newId, cmd);
				_commandStack.push(alias);
				String echoCmd = getEchoCmd(alias);

				// echo command appended after ; so that
				// it isn't treated like stdin for the intial command
				_cmdSubSystem.sendCommandToShell(cmd + _cmdSeparator + echoCmd, _remoteCmdShell,  new NullProgressMonitor());

			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * Send input to the running command shell.  The input is treated as input to
	 * a running program - rather than a new command.  As such, no echos are used
	 * and it's not logged for completion.
	 * @param input the input to send to the running program
	 */
	public void sendInput(String input)
	{
		if (_remoteCmdShell != null)
		{
			try
			{
				_cmdSubSystem.sendCommandToShell(input, _remoteCmdShell,  new NullProgressMonitor());

			}
			catch (Exception e)
			{
			}
		}
	}


	public Shell getShell()
	{
		if (_shell.isDisposed())
		{
			_shell = SystemBasePlugin.getActiveWorkbenchShell();
		}
		return _shell;
	}



	/**
	 * Indicates whether the command shell is active or not
	 * @return true if the command shell is running
	 */
	public boolean isActive()
	{
		if (_remoteCmdShell != null)
		{
			return _remoteCmdShell.isActive() && _cmdSubSystem.isConnected();
		}
		return false;
	}

	/*
	 * Check for remote changes
	 */
	public void systemResourceChanged(ISystemResourceChangeEvent event)
	{
		if (event.getType() == ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_FINISHED)
		{
			Object source = event.getSource();
			if (source == _cmdSubSystem)
			{
				handleShellFinished();
			}
			else if (source == _remoteCmdShell)
			{
				handleShellFinished();
			}
		}
		else if (event.getType() == ISystemResourceChangeEvents.EVENT_REFRESH)
		{
			Object parent = event.getParent();
			if (parent == _remoteCmdShell)
			{
				outputUpdated();
			}
		}
	}

	/**
	 * Called (on the main Thread) whenever output is retrieved from the host
	 */
	public void outputUpdated()
	{
		// check for command completion
		synchronized(_commandStack)
		{
		if (!_commandStack.empty())
		{
			Object[] outputs = _remoteCmdShell.listOutput();
			synchronized(outputs)
			{
			for (int i = _outputOffset; i < outputs.length && !_commandStack.empty(); i++)
			{
				boolean handledOutput = false;

				CommandAlias firstCommand = (CommandAlias)_commandStack.firstElement();

				Object output = outputs[_outputOffset];
				if (output instanceof IRemoteOutput)
				{
					IRemoteOutput rmtOutput = (IRemoteOutput)output;
					String text = rmtOutput.getText();
					if (commandMatches(text, firstCommand))
					{
						_commandStack.remove(0);
						handleCommandFinished(firstCommand.getCommand());
						handledOutput = true;
					}
				}
				if (!handledOutput)
				{
					handleOutputChanged(firstCommand.getCommand(), output);
				}
				_outputOffset++;
			}
			}

		}
		}
	}

	protected boolean commandMatches(String outputEcho, CommandAlias firstCommand)
	{
	    String expected = getEchoResult(firstCommand);
	    if (outputEcho.equals(expected))
	    {
	        return true;
	    }
	    else
	    {
	        int index = outputEcho.indexOf(expected);
	        if (index > 0)
	        {
	            if (outputEcho.charAt(index - 1) != ';' && (outputEcho.indexOf("echo") == -1)) //$NON-NLS-1$
	            {
	                return true;
	            }
	        }
	    }

	    return false;
	}

	/**
	 * Called (on the main Thread) when the shell is complete
	 */
	public void handleShellFinished()
	{
		finish();
	}

	/**
	 * Called (on the main Thread) when the specified command is complete
	 * 
	 * @param cmd the completed command
	 */
	public abstract void handleCommandFinished(String cmd);

	/**
	 * Called (on the main Thread) whenever output has changed
	 * 
	 * @param command the current command
	 * @param output the new output object
	 */
	public abstract void handleOutputChanged(String command, Object output);

}