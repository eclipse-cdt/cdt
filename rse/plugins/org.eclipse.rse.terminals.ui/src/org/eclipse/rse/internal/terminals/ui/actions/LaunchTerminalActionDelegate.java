/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * Yu-Fen Kuo       (MontaVista) - Adapted from  LaunchShellActionDelegate
 ********************************************************************************/

package org.eclipse.rse.internal.terminals.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

public class LaunchTerminalActionDelegate extends ActionDelegate implements
        IActionDelegate {

    private LaunchTerminalAction launchTerminalAction;
    private ISelection selection;

    public void run(IAction action) {
        if (launchTerminalAction == null) {
            launchTerminalAction = new LaunchTerminalAction(SystemBasePlugin
                    .getActiveWorkbenchShell(), null);
        }
        launchTerminalAction.updateSelection((IStructuredSelection) selection);
        launchTerminalAction.run();
    }

    public void runWithEvent(IAction action, Event event) {
        super.runWithEvent(action, event);
    }

    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        this.selection = selection;
    }

    public LaunchTerminalActionDelegate() {
    }

}
