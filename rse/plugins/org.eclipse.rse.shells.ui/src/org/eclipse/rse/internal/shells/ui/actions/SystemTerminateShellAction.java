/********************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [174945] Remove obsolete icons from rse.shells.ui
 ********************************************************************************/

package org.eclipse.rse.internal.shells.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.internal.shells.ui.ShellResources;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.widgets.Shell;



public class SystemTerminateShellAction extends SystemBaseShellAction
{
    public SystemTerminateShellAction(Shell parent)
	{
		this(ShellResources.ACTION_CANCEL_SHELL_LABEL,			
				ShellResources.ACTION_CANCEL_SHELL_TOOLTIP,
			RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CANCEL_ID),
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
        //ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
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
				cmdSubSystem.cancelShell(command, new NullProgressMonitor());
			}
		}
		catch (Exception e)
		{
			//	RSEUIPlugin.getDefault().logInfo("Exception invoking command " + cmd + " on " + sysConn.getAliasName());
		}
	}

}