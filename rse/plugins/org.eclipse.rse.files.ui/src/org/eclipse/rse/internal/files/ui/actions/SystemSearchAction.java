/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David McKnight   (IBM)        - [261019] New File/Folder actions available in Work Offline mode
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.files.ui.search.SystemSearchPage;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Shell;


public class SystemSearchAction extends SystemBaseAction {
    

    public SystemSearchAction(Shell parent) {
        super(SystemResources.ACTION_SEARCH_LABEL, 
        RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SEARCH_ID), parent);
        setToolTipText(SystemResources.ACTION_SEARCH_TOOLTIP);
		setHelp(RSEUIPlugin.HELPPREFIX + "rsdi0000"); //$NON-NLS-1$
		
		allowOnMultipleSelection(false);
    }

    public void run() {	
    	NewSearchUI.openSearchDialog(SystemBasePlugin.getActiveWorkbenchWindow(), SystemSearchPage.SYSTEM_SEARCH_PAGE_ID);
    }
    
	public boolean checkObjectType(Object selectedObject)
	{
		if (selectedObject instanceof IAdaptable){
			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)((IAdaptable)selectedObject).getAdapter(ISystemViewElementAdapter.class);
			if (adapter != null){
				ISubSystem ss = adapter.getSubSystem(selectedObject);
				if (ss != null){
					if (ss.isOffline()){
						return false;
					}
				}
			}
		}
		return true;
	}
}