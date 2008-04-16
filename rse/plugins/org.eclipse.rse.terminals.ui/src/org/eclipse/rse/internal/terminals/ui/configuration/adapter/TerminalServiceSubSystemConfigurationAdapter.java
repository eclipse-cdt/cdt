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
 * Yu-Fen Kuo (MontaVista) - Adapted from ShellServiceSubSystemConfigurationAdapter
 ********************************************************************************/

package org.eclipse.rse.internal.terminals.ui.configuration.adapter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.terminals.ui.Activator;
import org.eclipse.rse.internal.terminals.ui.TerminalServiceHelper;
import org.eclipse.rse.internal.terminals.ui.actions.LaunchTerminalAction;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystem;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.SubSystemConfigurationAdapter;
import org.eclipse.swt.widgets.Shell;

public class TerminalServiceSubSystemConfigurationAdapter extends
        SubSystemConfigurationAdapter {
    protected LaunchTerminalAction terminalAction;
    protected ImageDescriptor activeImageDescriptor;
    protected ImageDescriptor inactiveImageDescriptor;

    public IAction[] getSubSystemActions(SystemMenuManager menu,
            IStructuredSelection selection, Shell shell, String menuGroup,
            ISubSystemConfiguration factory, ISubSystem selectedSubSystem) {
        List allActions = new ArrayList();
        IAction[] baseActions = super.getSubSystemActions(menu, selection,
                shell, menuGroup, factory, selectedSubSystem);
        for (int i = 0; i < baseActions.length; i++) {
            allActions.add(baseActions[i]);
        }

        // launching terminals and finding files
        if (selectedSubSystem instanceof IRemoteFileSubSystem) {
            IRemoteFileSubSystem fs = (IRemoteFileSubSystem) selectedSubSystem;
            ITerminalServiceSubSystem cmdSubSystem = TerminalServiceHelper
                    .getTerminalSubSystem(fs.getHost());
            if (cmdSubSystem != null) {
                allActions.add(getTerminalAction(cmdSubSystem, shell));
            }
        } else if (selectedSubSystem instanceof ITerminalServiceSubSystem) {
            allActions.add(getTerminalAction(
                    (ITerminalServiceSubSystem) selectedSubSystem, shell));
        }

        return (IAction[]) allActions.toArray(new IAction[allActions.size()]);
    }

    public IAction getTerminalAction(
            ITerminalServiceSubSystem selectedSubSystem, Shell shell) {
        if (terminalAction == null) {
            terminalAction = new LaunchTerminalAction(shell, selectedSubSystem);
        } else {
            terminalAction.setSubSystem(selectedSubSystem);
        }
        return terminalAction;
    }

    public ImageDescriptor getImage(ISubSystemConfiguration config) {
        if (inactiveImageDescriptor == null) {
            inactiveImageDescriptor = Activator.getDefault()
                    .getImageDescriptor(Activator.ICON_ID_TERMINAL_SUBSYSTEM);
        }
        return inactiveImageDescriptor;
    }

    public ImageDescriptor getLiveImage(ISubSystemConfiguration config) {
        if (activeImageDescriptor == null) {
            activeImageDescriptor = Activator.getDefault().getImageDescriptor(
                    Activator.ICON_ID_TERMINAL_SUBSYSTEM_LIVE);
        }
        return activeImageDescriptor;
    }

}