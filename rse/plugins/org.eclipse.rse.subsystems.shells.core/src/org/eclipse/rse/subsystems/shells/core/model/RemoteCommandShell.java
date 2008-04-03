/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 ********************************************************************************/

package org.eclipse.rse.subsystems.shells.core.model;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.ICandidateCommand;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.RemoteCmdSubSystem;

public abstract class RemoteCommandShell implements IAdaptable, IRemoteCommandShell
{
	protected String _id;
	protected String _name;
	protected String _type;
	protected IRemoteFile _cwd;
	protected ArrayList _output;
	protected IRemoteFileSubSystem _fileSubSystem;
	protected IRemoteCmdSubSystem  _cmdSubSystem;
	protected ArrayList _cmdHistory;
	protected IProject _project;

	public RemoteCommandShell(IRemoteCmdSubSystem cmdSubSystem)
	{
		_output = new ArrayList();
		_cmdHistory = new ArrayList();
		_cmdSubSystem = cmdSubSystem;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public String getName()
	{
		return _name;
	}



	public String getId()
	{
	  if (_id == null)
	  {
	   IRemoteCmdSubSystem subSystem = getCommandSubSystem();
	   IHost connection = subSystem.getHost();
	   String name = connection.getAliasName();

	   IRemoteCommandShell[] shells = subSystem.getShells();

	   List currentNames = new ArrayList();
	   for (int i = 0; i < shells.length; i++)
	   {
	    IRemoteCommandShell shell = shells[i];
	    if (shell != this)
	    {
	        // DKM - noticed that this caused a stack overflow in one scenario
	       if (shell instanceof RemoteCommandShell)
	       {
	           currentNames.add(((RemoteCommandShell)shell)._id);
	       }
	    }
	   }
	   if (currentNames.size() > 0)
	   {
	    int number = 2;
	    String newName = name + " " + number; //$NON-NLS-1$
	    if (currentNames.contains(name)) {
	     while (currentNames.contains(newName))
	     {
	         number++;
	         newName = name + " " + number; //$NON-NLS-1$
	     }
	     name = newName;
	    }
	   }
	   _id = name;

	  }
	  return _id;
	}



	public void setType(String type)
	{
		_type = type;
	}

	public String getType()
	{
		return _type;
	}

	public Object getAdapter(Class adapterType)
	{
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	public Object[] listOutput()
	{
		Object[] array = new Object[_output.size()];
		synchronized (_output)
		{
		  _output.toArray(array);
		}
		return array;
	}

	public int getIndexOf(Object output)
	{
		return _output.indexOf(output);
	}

	public Object getOutputAt(int index)
	{
		return _output.get(index);
	}

	public int getSize()
	{
		return _output.size();
	}

	public void addOutput(Object output)
	{
		_output.add(output);
	}

	public void removeOutput()
	{
		_output.clear();
	}

	public void removeOutput(Object output)
	{
		_output.remove(output);
	}

	public IRemoteFileSubSystem getFileSubSystem()
	{
	    if (_fileSubSystem == null)
	    {
	        if (_cmdSubSystem != null)
	        {
	            _fileSubSystem = RemoteFileUtility.getFileSubSystem(_cmdSubSystem.getHost());
	        }
	    }
		return _fileSubSystem;
	}

	public IRemoteCmdSubSystem getCommandSubSystem()
	{
	    if (_cmdSubSystem == null)
	    {
			if (_fileSubSystem != null)
			{
				try
				{
					IHost host = _fileSubSystem.getHost();
					ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
					ISubSystem[] sses = sr.getSubSystems(host);
					for (int i = 0; i < sses.length; i++)
					{
						if (sses[i] instanceof IRemoteCmdSubSystem)
						{
							IRemoteCmdSubSystem cmdSubSystem = (RemoteCmdSubSystem)sses[i];
							_cmdSubSystem = cmdSubSystem;
						}
					}
				}
				catch (Exception e)
				{
				}
			}
	    }

		return _cmdSubSystem;
	}

	public Object[] getRoots()
	{
		RemoteOutput[] results = new RemoteOutput[_output.size()];
		for (int i = 0; i < _output.size(); i++)
		{
			results[i] = (RemoteOutput) _output.get(i);
		}
		return results;
	}

	public abstract String getTitle();

	public abstract boolean isActive();

	public void updateHistory(String cmd)
	{
	    /*
			String text = cmd;
			int tagIndex = text.indexOf("BEGIN-END-TAG");
			if (tagIndex > 0)
			{
			    text=  text.substring(0, tagIndex - 6);
			    cmd = text;
			}
		*/
		_cmdHistory.add(cmd);
	}

	public String[] getHistory()
	{
		String[] cmds = null;
		if (_cmdHistory.size() > 0)
		{
			cmds = new String[_cmdHistory.size()];
			for (int i = 0; i < cmds.length; i++)
				cmds[i] = (String) _cmdHistory.get(i);
		}
		else
		{
			cmds = new String[0];
		}
		return cmds;
	}


	/**
	 * Get the current working directory for this command
	 * @return the current working directory
	 */
	public Object getContext()
	{
		return _cwd;
	}

	/**
	 * Returns the context of this Shell as a String for persistence.
	 * The context is typically the current working directory.
	 * Returns an empty String if no context can be determined.
	 *
	 * @return The current context as String, or an empty String
	 *     if no context exists. Never returns <code>null</code>.
	 */
	public String getContextString()
	{
	    if(_cwd != null) {
	        return _cwd.getAbsolutePath();
	    }
	    return ""; //$NON-NLS-1$
	}

	/**
	 * Get the current working directory for this command
	 * @return the current working directory
	 */
	public IRemoteFile getWorkingDirectory()
	{
		return _cwd;
	}

	/**
	 * Set the current working directory for this command
	 *
	 * @param file the working directory.
	 */
	public void setWorkingDirectory(IRemoteFile file)
	{
		_cwd = file;
	}

	protected boolean isWindows()
	{
		return getCommandSubSystem().getHost().getSystemType().isWindows();
	}



	/**
	 * Override to provide a list of possible commands
	 */
	public abstract ICandidateCommand[] getCandidateCommands();


	public void associateProject(IProject project)
	{
		_project = project;
	}


	public IProject getAssociatedProject()
	{
		return _project;
	}

}