/********************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others. All rights reserved.
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
 * Yu-Fen Kuo       (MontaVista) - Adapted from SystemCommandsUI
 * Anna Dushistova  (MontaVista) - [228577] [rseterminal] Clean up RSE Terminal impl
 * Martin Oberhuber (Wind River) - [235626] Convert terminals.ui to MessageBundle format
 ********************************************************************************/

package org.eclipse.rse.internal.terminals.ui.views;

import org.eclipse.rse.internal.terminals.ui.TerminalUIResources;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * A singleton class which has handle to the terminals view
 */
public class TerminalsUI {

    // singleton instance
    private static TerminalsUI instance;
    private static TerminalViewer viewer;

    private TerminalsUI() {
        super();
    }

    /**
     * Get the singleton instance.
     *
     * @return the singleton object of this type
     */
    public static TerminalsUI getInstance() {
        if (instance == null) {
            instance = new TerminalsUI();
        }

        return instance;
    }

    public TerminalViewer activateTerminalsView() {
        try {
            IWorkbenchPage page = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage();
            viewer = (TerminalViewer) page.showView(TerminalViewer.VIEW_ID);
            page.bringToTop(viewer);
        } catch (PartInitException e) {
            SystemBasePlugin.logError(TerminalUIResources.TerminalsUI_cannotOpenView_error, e);
        }

        return viewer;
    }

    public static TerminalViewer getTerminalsView() {
        return viewer;
    }
}