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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.rse.ui.view.SystemView;
import org.eclipse.swt.widgets.Shell;



/**
 * Base class for Expand To actions on a container
 */
public abstract class SystemViewExpandToBaseAction extends SystemBaseAction 
{
	

	
	/**
	 * Constructor.
	 */
	public SystemViewExpandToBaseAction(String label, String tooltip, ImageDescriptor image, Shell parent) 
	{
		super(label, tooltip, image, parent);
		allowOnMultipleSelection(false);
		setContextMenuGroup(org.eclipse.rse.ui.ISystemContextMenuConstants.GROUP_EXPANDTO);
		setChecked(false); // will reset once we know the selection.
	}
	
	/**
	 * Second and easiest opportunity to decide if the action should be enabled or not based
	 * on the current selection. Called by default implementation of updateSelection, once for
	 * each item in the selection. If any call to this returns false, the action is disabled.
	 * The default implementation returns true.
	 */
	public boolean checkObjectType(Object selectedObject)
	{
		SystemView sv = getSystemView();
		if (sv == null)
		  return false;
		String currentFilter = sv.getExpandToFilter(selectedObject);
		String thisFilter = getFilterString(selectedObject);
		if (currentFilter != null) 
		{
		  if ((thisFilter!=null) && currentFilter.equals(thisFilter))
		     setChecked(true);
		}
		else if (thisFilter == null) // I assume this is only the case for Expand To->All.
		  setChecked(true); 
		return true;
	}
	
	/**
	 * Actually do the work
	 */	
	public void run() 
	{	
		Object element = getFirstSelection();		
		if (element != null) 
		{
			SystemView view = (SystemView)getCurrentTreeView();
			view.expandTo(getFilterString(element));
		}
	}	
	
	/**
	 * Overridable extension point to get the fully resolved filter string at the time
	 *  action is run.
	 */
	protected abstract String getFilterString(Object selectedObject);
	
	/**
	 * Return the current SystemView or null if the current viewer is not a system view
	 */
	protected SystemView getSystemView()
	{
		ISystemTree tree = getCurrentTreeView();
		if ((tree instanceof SystemView) && (((SystemView)tree).getSystemViewPart() != null))
		  return (SystemView)tree;
		else
		  return null;
	}
}