/********************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others. All rights reserved.
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
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view.monitor;

import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;


/**
 * A singleton class for dealing with remote commands
 */
public class SystemMonitorUI
{


    // singleton instance
    private static SystemMonitorUI instance;
    private static SystemMonitorViewPart _viewPart;

    public static final String MONITOR_VIEW_ID = "org.eclipse.rse.ui.view.monitorView"; //$NON-NLS-1$

    private SystemMonitorUI()
    {
        super();
    }

    /**
     * Get the singleton instance.
     * @return the singleton object of this type
     */
    public static SystemMonitorUI getInstance()
    {
        if (instance == null)
        {
            instance = new SystemMonitorUI();
        }

        return instance;
    }

	   
    public SystemMonitorViewPart activateCommandsView()
    {
        try
        {
            IWorkbenchPage page = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
            _viewPart = (SystemMonitorViewPart) page.showView(SystemMonitorUI.MONITOR_VIEW_ID);
            page.bringToTop(_viewPart);
        }
        catch (PartInitException e)
        {
           	SystemBasePlugin.logError("Can not open remote shell view", e); //$NON-NLS-1$
        }

        return _viewPart;
    }
   
    
    public static SystemMonitorViewPart getMonitorView()
    {
        return _viewPart;
    }
}