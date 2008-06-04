/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David McKnight   (IBM)        - [165680] "Show in Remote Shell View" does not work
 * Anna Dushistova  (MontaVista) - Adapted from SystemShowInShellViewAction
 * Martin Oberhuber (Wind River) - [235626] Convert terminals.ui to MessageBundle format
 *******************************************************************************/

package org.eclipse.rse.internal.terminals.ui.actions;

import org.eclipse.rse.internal.terminals.ui.Activator;
import org.eclipse.rse.internal.terminals.ui.TerminalUIResources;
import org.eclipse.rse.internal.terminals.ui.views.TerminalViewer;
import org.eclipse.rse.internal.terminals.ui.views.TerminalsUI;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.swt.widgets.Shell;

public class ShowInTerminalViewAction extends TerminalElementBaseAction {

    public ShowInTerminalViewAction(Shell parent) {
        super(TerminalUIResources.ShowInTerminalViewAction_label, TerminalUIResources.ShowInTerminalViewAction_tooltip,
        		Activator.getDefault().getImageDescriptor(Activator.ICON_ID_LAUNCH_TERMINAL), parent);
        allowOnMultipleSelection(false);
    }

    /**
     * Called when this action is selected from the popup menu.
     */
    public void run() {
        TerminalsUI terminalsUI = TerminalsUI.getInstance();
        TerminalViewer viewPart = terminalsUI.activateTerminalsView();
        for (int i = 0; i < selected.size(); i++) {
            TerminalElement element = (TerminalElement) selected.get(i);
            viewPart.getTabFolder().showPageFor(element.getName());
        }
    }
}
