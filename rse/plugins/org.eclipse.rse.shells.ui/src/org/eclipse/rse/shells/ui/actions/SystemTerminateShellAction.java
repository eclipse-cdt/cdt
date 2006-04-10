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

package org.eclipse.rse.shells.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.shells.ui.ShellResources;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.swt.widgets.Shell;



public class SystemTerminateShellAction extends SystemBaseShellAction
{
    public SystemTerminateShellAction(Shell parent)
	{
		this(ShellResources.ACTION_CANCEL_SHELL_LABEL,			
				ShellResources.ACTION_CANCEL_SHELL_TOOLTIP,
			SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CANCEL_ID),
			parent);
	}
    
    public SystemTerminateShellAction(String label,			
										String tooltip,
										ImageDescriptor image, 
										Shell parent)
	{
		super(label,			
			tooltip,
			image,
			parent);
	}

    public void run()
    {
        // DKM - need to deselect in tree 
        //SystemRegistry registry = SystemPlugin.getTheSystemRegistry();
        //registry.fireEvent(new SystemResourceChangeEvent())
        List selected = new ArrayList();
         selected.addAll(_selected);
        for (int i = selected.size() -1; i >= 0; i--)
		{
		    IRemoteCommandShell cmdShell = (IRemoteCommandShell)selected.get(i);
		    terminateShell(cmdShell);
		}
    }
    
	protected boolean isApplicable(IRemoteCommandShell cmdShell)
	{
	    return cmdShell.isActive();
	}
	
    protected void terminateShell(IRemoteCommandShell command)
    {
		cancel(command);
    }
    
	public void cancel(IRemoteCommandShell command)
	{
		try
		{
			IRemoteCmdSubSystem cmdSubSystem = command.getCommandSubSystem();
			if (cmdSubSystem != null)
			{
				cmdSubSystem.cancelShell(getShell(), command);
			}
		}
		catch (Exception e)
		{
			//	SystemPlugin.getDefault().logInfo("Exception invoking command " + cmd + " on " + sysConn.getAliasName());
		}
	}

}