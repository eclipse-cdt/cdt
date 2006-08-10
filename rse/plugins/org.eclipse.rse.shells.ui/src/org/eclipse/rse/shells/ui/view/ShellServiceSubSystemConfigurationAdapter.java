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

package org.eclipse.rse.shells.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.shells.ui.actions.SystemCommandAction;
import org.eclipse.rse.shells.ui.actions.SystemExportShellHistoryAction;
import org.eclipse.rse.shells.ui.actions.SystemExportShellOutputAction;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.view.SubSystemConfigurationAdapter;
import org.eclipse.swt.widgets.Shell;


public class ShellServiceSubSystemConfigurationAdapter extends SubSystemConfigurationAdapter
{
    protected IAction _exportShellHistoryAction;
    protected IAction _exportShellOutputAction;
    protected SystemCommandAction _commandAction;
    protected ImageDescriptor _activeShellImageDescriptor;
    protected ImageDescriptor _inactiveShellImageDescriptor;
    
	public IAction[] getSubSystemActions(ISubSystemConfiguration factory, ISubSystem selectedSubSystem, Shell shell)
	{
		List allActions = new ArrayList();
		IAction[] baseActions = super.getSubSystemActions(factory, selectedSubSystem, shell);
		for (int i = 0; i < baseActions.length; i++)
		{
			allActions.add(baseActions[i]);
		}
		
		//launching shells and finding files
		if (selectedSubSystem instanceof IRemoteFileSubSystem)
		{
			IRemoteFileSubSystem fs = (IRemoteFileSubSystem) selectedSubSystem;
			IRemoteCmdSubSystem cmdSubSystem = RemoteCommandHelpers.getCmdSubSystem(fs.getHost());
			if (cmdSubSystem != null)
			{
			    allActions.add(getCommandShellAction(cmdSubSystem, shell));
			}
		}
		else if (selectedSubSystem instanceof IRemoteCmdSubSystem)
		{
			allActions.add(getCommandShellAction((IRemoteCmdSubSystem)selectedSubSystem, shell));
		}
		
		return (IAction[])allActions.toArray(new IAction[allActions.size()]);
	}

	
    
    public IAction getCommandShellAction(IRemoteCmdSubSystem selectedSubSystem, Shell shell)
    {
    	if (_commandAction == null)
    	{
    		_commandAction = new SystemCommandAction(shell, true, selectedSubSystem);
    	}
    	else
    	{
    		_commandAction.setSubSystem(selectedSubSystem);
    	}
    	return _commandAction;
    }

    /**
     * Return the command shell history export action for the subsystem.  If there is none, return null
     */
    public IAction getCommandShellHistoryExportAction(Shell shell)
    {
        if (_exportShellHistoryAction == null)
        {
            _exportShellHistoryAction = new SystemExportShellHistoryAction(shell);
        }
        return _exportShellHistoryAction;
    }
  
    /**
     * Return the command shell output export action for the subsystem.  If there is none, return null
     */
    public IAction getCommandShellOutputExportAction(Shell shell)
    {
        if (_exportShellOutputAction == null)
        {
            _exportShellOutputAction = new SystemExportShellOutputAction(shell);
        }
        return _exportShellOutputAction;
    }
    
    
    /**
     * Return the active command shell icon for this subsystem
     */
    public ImageDescriptor getActiveCommandShellImageDescriptor()
    {
        if (_activeShellImageDescriptor == null)
        {
            _activeShellImageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SHELLLIVE_ID);
            
        }
        return _activeShellImageDescriptor;
    }
    
    /**
     * Return the inactive command shell icon for this subsystem
     */
    public ImageDescriptor getInactiveCommandShellImageDescriptor()
    {
        if (_inactiveShellImageDescriptor == null)
        {
            _inactiveShellImageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SHELL_ID);
        }
        return _inactiveShellImageDescriptor;
    }
    
}