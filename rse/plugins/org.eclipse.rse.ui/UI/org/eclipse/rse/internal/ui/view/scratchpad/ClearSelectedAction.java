/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Kevin Doyle (IBM) - [189150] setSelection(null) added to clear()
 * Kevin Doyle (IBM) - [193148] Clear Selected Action enabled when not on a root element
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view.scratchpad;
import java.util.Iterator;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

public class ClearSelectedAction extends BrowseAction
{
	public ClearSelectedAction(SystemScratchpadView view)
	{
		super(view, SystemResources.ACTION_CLEAR_SELECTED_LABEL,
		        RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CLEAR_SELECTED_ID));
	
		setToolTipText(SystemResources.ACTION_CLEAR_SELECTED_TOOLTIP);
		// TODO DKM - get help for this!
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.CLEAR_CONSOLE_ACTION);
	}

	public void checkEnabledState()
	{
	    if (_scratchPad.hasChildren())
	    {
	        StructuredSelection selection = (StructuredSelection)_view.getSelection();
	        if (selection != null)
	        {
	            Iterator iterator = selection.iterator();
		        while (iterator.hasNext())
		        {
		            Object obj = iterator.next();
		            if (!_scratchPad.contains(obj))
		            {
		                setEnabled(false);
		                return;
		            }
		            else
		            {
		            	if (selection instanceof TreeSelection)
		            	{
		            		TreeSelection treeSelection = (TreeSelection) selection;
		            		TreePath[] paths = treeSelection.getPathsFor(obj);
		            		// if paths[0].getSegmentCount is not 1 then it's not a root
		            		if (paths.length > 0 && paths[0].getSegmentCount() != 1)
		            		{
		            			setEnabled(false);
		            			return;
		            		}
		            	}
		            }
		        }
		        setEnabled(true);
		        return;
	        }
	    }

	    setEnabled(false);			    
	}

	public void run()
	{
		clear();
	}

	private void clear()
	{
	    StructuredSelection selection = (StructuredSelection)_view.getSelection();
	    if (selection != null)
	    {
	        Iterator iterator = selection.iterator();
	        while (iterator.hasNext())
	        {
	            _scratchPad.removeChild(iterator.next());
	        }
	        RSECorePlugin.getTheSystemRegistry().fireEvent(new SystemResourceChangeEvent(_scratchPad, ISystemResourceChangeEvents.EVENT_REFRESH, _scratchPad));
	        _view.setSelection(null);
	        //_view.updateActionStates();
	    }
	}
}
