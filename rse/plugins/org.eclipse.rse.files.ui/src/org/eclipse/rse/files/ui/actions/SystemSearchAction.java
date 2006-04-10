/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.actions;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.files.ui.search.SystemSearchPage;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Shell;


public class SystemSearchAction extends SystemBaseAction {
    

    public SystemSearchAction(Shell parent) {
        super(SystemResources.ACTION_SEARCH_LABEL, 
        SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SEARCH_ID), parent);
        setToolTipText(SystemResources.ACTION_SEARCH_TOOLTIP);
		setHelp(SystemPlugin.HELPPREFIX + "rsdi0000");
		
		allowOnMultipleSelection(false);
    }

    public void run() {	
    	NewSearchUI.openSearchDialog(SystemBasePlugin.getActiveWorkbenchWindow(), SystemSearchPage.SYSTEM_SEARCH_PAGE_ID);
    }
}