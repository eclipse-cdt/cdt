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

package org.eclipse.rse.ui.filters.actions;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.filters.SystemChangeFilterPane;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;



/**
 * The action is used within the Change Filter dialog, in the context menu of the selected filter string.
 * It is used to copy the selected filter string to the clipboard for subsequent paste.
 */
public class SystemChangeFilterActionCopyString extends SystemBaseAction 
                                 
{
	private SystemChangeFilterPane parentDialog;
	
	/**
	 * Constructor 
	 */
	public SystemChangeFilterActionCopyString(SystemChangeFilterPane parentDialog) 
	{
		super(SystemResources.ACTION_COPY_FILTERSTRING_LABEL,SystemResources.ACTION_COPY_FILTERSTRING_TOOLTIP,
			  PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY),
		      null);
        allowOnMultipleSelection(false);
        this.parentDialog = parentDialog;
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);  
		setHelp(RSEUIPlugin.HELPPREFIX+"dufr2000");      
	}

	/**
	 * We override from parent to do unique checking.
	 * We intercept to ensure this is isn't the "new" filter string
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		return parentDialog.canCopy();
	}

	/**
	 * This is the method called when the user selects this action.
	 */
	public void run() 
	{
		parentDialog.doCopy();
	}		
}