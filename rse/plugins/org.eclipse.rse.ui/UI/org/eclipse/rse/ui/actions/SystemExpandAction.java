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

package org.eclipse.rse.ui.actions;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.widgets.Shell;


/**
 * The action allows users to expand the selected nodes in the Remote Systems Explorer tree view
 */
public class SystemExpandAction extends SystemBaseAction 
                                 
{
	
	// see defect 41203
	
	/**
	 * Constructor
	 */
	public SystemExpandAction(Shell parent) 
	{
		super(SystemResources.ACTION_EXPAND_SELECTED_LABEL,SystemResources.ACTION_EXPAND_SELECTED_TOOLTIP,
		      parent);
        allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_EXPAND);
		setAccelerator('+');
		setHelp(SystemPlugin.HELPPREFIX+"actn0025");
		setAvailableOffline(true);
}

	/**
	 * <p>
	 * We intercept to ensure at least one selected item is expandable
     *
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = false;
		if ((viewer != null) && (viewer instanceof ISystemTree))
		{
			return ((ISystemTree)viewer).areAnySelectedItemsExpandable();
		}
		Iterator e= ((IStructuredSelection) selection).iterator();		
		ISystemViewElementAdapter adapter = null;
		while (!enable && e.hasNext())
		{
			Object selectedObject = e.next();
			adapter = getAdapter(selectedObject);
			if (adapter != null)
			{
				if (adapter.hasChildren(selectedObject))
				  enable = true;
			}
		}
		return enable;
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		//System.out.println("Inside run of SystemRefreshAction");
		ISystemRegistry sr = SystemPlugin.getTheSystemRegistry();
		if ((viewer != null) && (viewer instanceof ISystemResourceChangeListener))
		{			
		  sr.fireEvent((ISystemResourceChangeListener)viewer,
		               new SystemResourceChangeEvent("dummy", 
		                    ISystemResourceChangeEvents.EVENT_EXPAND_SELECTED, null));
		}
		else
		  sr.fireEvent(new SystemResourceChangeEvent("dummy", ISystemResourceChangeEvents.EVENT_EXPAND_SELECTED, null));
	}		
}