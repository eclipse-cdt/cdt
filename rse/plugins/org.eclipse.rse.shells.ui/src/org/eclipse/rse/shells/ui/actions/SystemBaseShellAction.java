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


package org.eclipse.rse.shells.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;



public abstract class SystemBaseShellAction extends SystemBaseAction
{
	protected List _selected; 

	public SystemBaseShellAction(String name, String tooltip, ImageDescriptor image, Shell parent)
	{
		super(name,			
			tooltip,
			image,
			parent);
		setAvailableOffline(true);
		allowOnMultipleSelection(true);
		_selected = new ArrayList();
	} 

	/**
	 * Called when the selection changes.  The selection is checked to
	 * make sure this action can be performed on the selected object.
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = false;
		Iterator e = ((IStructuredSelection) selection).iterator();
		_selected.clear();
		while (e.hasNext())
		{
		    Object selected = e.next();
		    if (selected instanceof IRemoteCommandShell)
		    {
		        if (isApplicable((IRemoteCommandShell)selected))
		        {
		            _selected.add(selected);
		        	enable = true;
		        }
		        else
		        {
		            return false;
		        }
		    }
		    else
		    {
		        return false;
		    }
		}
		

		return enable;
	} 
	
	protected boolean isApplicable(IRemoteCommandShell cmdShell)
	{
	    return true;
	}
}