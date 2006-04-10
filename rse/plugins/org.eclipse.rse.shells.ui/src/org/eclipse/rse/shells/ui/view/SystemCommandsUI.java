/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;


/**
 * A singleton class for dealing with remote commands
 */
public class SystemCommandsUI
{


    // singleton instance
    private static SystemCommandsUI instance;
    private static SystemCommandsViewPart _viewPart;
    private static SystemBuildErrorViewPart _errorPart;

    public static final String COMMANDS_VIEW_ID = SystemCommandsViewPart.ID;
	public static final String BUILD_ERROR_VIEW_ID = SystemBuildErrorViewPart.ID;

    private SystemCommandsUI()
    {
        super();
    }

    /**
     * Get the singleton instance.
     * @return the singleton object of this type
     */
    public static SystemCommandsUI getInstance()
    {
        if (instance == null)
        {
            instance = new SystemCommandsUI();
        }

        return instance;
    }

	public SystemBuildErrorViewPart activateBuildErrorView()
	   {
		   try
		   {
			   IWorkbenchPage page = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
			   _errorPart = (SystemBuildErrorViewPart) page.showView(SystemCommandsUI.BUILD_ERROR_VIEW_ID);
			   page.bringToTop(_errorPart);
		   }
		   catch (PartInitException e)
		   {
			   SystemBasePlugin.logError("Can not open build error view", e);
		   }

		   return _errorPart;
	   }
	   
    public SystemCommandsViewPart activateCommandsView()
    {
        try
        {
            IWorkbenchPage page = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
            _viewPart = (SystemCommandsViewPart) page.showView(SystemCommandsUI.COMMANDS_VIEW_ID);
            page.bringToTop(_viewPart);
        }
        catch (PartInitException e)
        {
        	e.printStackTrace();
           	SystemBasePlugin.logError("Can not open commands view", e);
        }

        return _viewPart;
    }
    
    public static SystemBuildErrorViewPart getBuildErrorView()
    {
        return _errorPart;
    }
    
    public static SystemCommandsViewPart getCommandsView()
    {
        return _viewPart;
    }
}