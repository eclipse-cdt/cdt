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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 ********************************************************************************/

package org.eclipse.rse.ui.actions;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.widgets.Shell;


/**
 * The action allows users to refresh the entire Remote System Explorer tree view
 */
public class SystemRefreshAllAction extends SystemBaseAction 
                                 
{
	
	//private SystemProfile prevProfile = null;
	private IStructuredSelection _selection = null;
	private Object _rootObject = null;
	
	/**
	 * Constructor for SystemRefreshAllAction
	 */
	public SystemRefreshAllAction(Shell parent) 
	{
		super(SystemResources.ACTION_REFRESH_ALL_LABEL,SystemResources.ACTION_REFRESH_ALL_TOOLTIP,
		      parent);
        allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_BUILD);        
        //setSelectionSensitive(false);
        setSelectionSensitive(true);// use selection to decide what to invalidate
        
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0009"); //$NON-NLS-1$
	}

	public void setRootObject(Object object)
	{
		_rootObject = object;
	}
	
	/**
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		_selection = selection;
		return enable;
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		if (_selection != null)
		{
			// mark all selected objects as stale if applicable
			Iterator iter = _selection.iterator();
			while(iter.hasNext())
			{
				Object obj = iter.next();
				
				if (obj instanceof ISystemContainer)
				{
					((ISystemContainer)obj).markStale(true);
				}
			}
		}
		if (_rootObject != null)
		{
			if (_rootObject instanceof ISystemContainer)
			{
				((ISystemContainer)_rootObject).markStale(true);
			}
		}
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		sr.fireEvent(new SystemResourceChangeEvent(sr, ISystemResourceChangeEvents.EVENT_REFRESH, null));
	}		
}