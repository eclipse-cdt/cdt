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


package org.eclipse.rse.shells.ui.actions;

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.shells.ui.ShellResources;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.swt.widgets.Shell;



public class SystemTerminateRemoveShellAction extends SystemTerminateShellAction
{
    public SystemTerminateRemoveShellAction(Shell parent)
	{
		super(ShellResources.ACTION_CANCEL_REMOVE_SHELL_LABEL,			
				ShellResources.ACTION_CANCEL_REMOVE_SHELL_TOOLTIP,
			SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_REMOVE_SHELL_ID),
			parent);
	}
    
    protected boolean isApplicable(IRemoteCommandShell cmdShell)
	{
	    return true;
	}

    
    public void cancel(IRemoteCommandShell command)
	{
		try
		{
			IRemoteCmdSubSystem cmdSubSystem = command.getCommandSubSystem();
			if (cmdSubSystem != null)
			{
				cmdSubSystem.removeShell(getShell(), command);
			}
		}
		catch (Exception e)
		{
			//	SystemPlugin.getDefault().logInfo("Exception invoking command " + cmd + " on " + sysConn.getAliasName());
		}
	}			  

}