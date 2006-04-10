/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;


/**
 * class for managing sets of remote command shell operations.
 */
public class RemoteCommandShellOperationManager 
{	
	protected List _commandShellOperations;
	protected static RemoteCommandShellOperationManager _instance= new RemoteCommandShellOperationManager();
	
	protected RemoteCommandShellOperationManager()
	{		
		_commandShellOperations = new ArrayList();
	}
	
	public static RemoteCommandShellOperationManager getInstance()
	{
		return _instance;
	}
	
	public RemoteCommandShellOperation findRemoteCommandShellOperation(IRemoteCmdSubSystem ss, IProject project, Class type)	
	{
		for (int i = 0; i < _commandShellOperations.size();i++)
		{
			RemoteCommandShellOperation op = (RemoteCommandShellOperation)_commandShellOperations.get(i);
			if (op.isActive())
			{
				if (op.getClass() == type)
				{
					IRemoteCommandShell rmtShell = op.getRemoteCommandShell();
					if (rmtShell.getCommandSubSystem() == ss && rmtShell.getAssociatedProject() == project)
					{
						return op;
					}
				}
			}
			else
			{
				_commandShellOperations.remove(op);
			}
		}
		
		return null;
	}
	
	public void registerRemoteCommandShellOperation(RemoteCommandShellOperation newOp)
	{
		_commandShellOperations.add(newOp);
	}

}