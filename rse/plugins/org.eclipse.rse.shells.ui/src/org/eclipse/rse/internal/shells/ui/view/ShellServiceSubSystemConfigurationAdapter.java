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
 * Martin Oberhuber (Wind River) - [174945] Remove obsolete icons from rse.shells.ui
 * Anna Dushistova  (MontaVista) - [252058] Actions for shells subsystem should be contributed declaratively
 ********************************************************************************/

package org.eclipse.rse.internal.shells.ui.view;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.internal.shells.ui.ShellsUIPlugin;
import org.eclipse.rse.internal.shells.ui.actions.SystemExportShellHistoryAction;
import org.eclipse.rse.internal.shells.ui.actions.SystemExportShellOutputAction;
import org.eclipse.rse.ui.view.SubSystemConfigurationAdapter;
import org.eclipse.swt.widgets.Shell;


public class ShellServiceSubSystemConfigurationAdapter extends SubSystemConfigurationAdapter
{
    protected IAction _exportShellHistoryAction;
    protected IAction _exportShellOutputAction;
    protected ImageDescriptor _activeShellImageDescriptor;
    protected ImageDescriptor _inactiveShellImageDescriptor;

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
            _activeShellImageDescriptor = ShellsUIPlugin.getDefault().getImageDescriptor(ShellsUIPlugin.ICON_SYSTEM_SHELLLIVE_ID);
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
            _inactiveShellImageDescriptor = ShellsUIPlugin.getDefault().getImageDescriptor(ShellsUIPlugin.ICON_SYSTEM_SHELL_ID);
        }
        return _inactiveShellImageDescriptor;
    }
    
}