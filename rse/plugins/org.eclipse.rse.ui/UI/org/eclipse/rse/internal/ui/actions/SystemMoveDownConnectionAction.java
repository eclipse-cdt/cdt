/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 ********************************************************************************/

package org.eclipse.rse.internal.ui.actions;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.SystemSortableSelection;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;


/**
 * The action allows users to move the current connection down in the list
 */
public class SystemMoveDownConnectionAction extends SystemBaseAction 
                                 
{
	
	private ISystemProfile prevProfile = null;
	
	/**
	 * Constructor for SystemMoveDownAction
	 */
	public SystemMoveDownConnectionAction(Shell parent) 
	{
		super(SystemResources.ACTION_MOVEDOWN_LABEL, SystemResources.ACTION_MOVEDOWN_TOOLTIP,
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_MOVEDOWN_ID),
		      parent);
        allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORDER);        
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0002"); //$NON-NLS-1$
	}

	/**
	 * We override from parent to do unique checking...
	 * <p>
	 * We intercept to ensure only connections from the same profile are selected.
	 * <p>
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();		
		prevProfile = null;
		Iterator e = selection.iterator();		
		while (enable && e.hasNext())
		{
			Object selectedObject = e.next();
			if (selectedObject instanceof IHost)
			{
			  IHost conn = (IHost)selectedObject;
		      int connCount = sr.getHostCountWithinProfile(conn)-1;
			  if (prevProfile == null)
			    prevProfile = conn.getSystemProfile();
			  else
			    enable = (prevProfile == conn.getSystemProfile());
			  if (enable)
			  {
		        enable = (sr.getHostPosition(conn) < connCount);
		        prevProfile = conn.getSystemProfile();
			  }
			}
			else
			  enable = false;
		}
		return enable;
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();	
		SystemSortableSelection[] sortableArray = SystemSortableSelection.makeSortableArray(getSelection());
		IHost conn = null;
		for (int idx=0; idx<sortableArray.length; idx++)
		{
		   conn = (IHost)sortableArray[idx].getSelectedObject();
           sortableArray[idx].setPosition(sr.getHostPosition(conn));
		}
		SystemSortableSelection.sortArray(sortableArray);
		IHost[] conns = (IHost[])SystemSortableSelection.getSortedObjects(sortableArray, new IHost[sortableArray.length]);
		sr.moveHosts(prevProfile.getName(),conns,1);
	}	
}