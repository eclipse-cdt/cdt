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

package org.eclipse.rse.ui.filters.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.filters.SystemChangeFilterPane;


/**
 * The action is used within the Change Filter dialog, in the context menu of the selected filter string.
 * It is used to move the selected filter string up by one in the list
 */
public class SystemChangeFilterActionMoveStringDown extends SystemBaseAction                                  
{

	private SystemChangeFilterPane parentDialog;
	
	/**
	 * Constructor
	 */
	public SystemChangeFilterActionMoveStringDown(SystemChangeFilterPane parentDialog) 
	{
		super(SystemResources.ACTION_MOVEDOWN_LABEL,SystemResources.ACTION_MOVEDOWN_TOOLTIP,
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_MOVEDOWN_ID),
		      null);
        allowOnMultipleSelection(false);
        this.parentDialog = parentDialog;
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORDER);  
		setHelp(RSEUIPlugin.HELPPREFIX+"dufr5000");      
	}

	/**
	 * We override from parent to do unique checking.
	 * We intercept to ensure this is isn't the last filter string in the list
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		return parentDialog.canMoveDown();
	}

	/**
	 * This is the method called when the user selects this action.
	 */
	public void run() 
	{
		parentDialog.doMoveDown();
	}		
		
}