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

package org.eclipse.rse.internal.ui.view.team;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.core.model.SystemProfileManager;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;


/**
 * The action allows users to activate all selected profiles
 */
public class SystemTeamViewMakeActiveProfileAction extends SystemBaseAction 
                                 
{
	
	/**
	 * Constructor 
	 */
	public SystemTeamViewMakeActiveProfileAction(Shell parent) 
	{
		super(SystemResources.ACTION_PROFILE_MAKEACTIVE_LABEL,SystemResources.ACTION_PROFILE_MAKEACTIVE_TOOLTIP,
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_MAKEPROFILEACTIVE_ID),
		      parent);
        allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_CHANGE);
		setHelp(RSEUIPlugin.HELPPREFIX+"ActionMakeActive"); //$NON-NLS-1$
	}

	/**
	 * Here we decide whether to enable ths action or not. We enable it
	 * if every selected object is a profile, and if its not the case
	 * that every selected profile is already active.
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		Object currsel = getFirstSelection();
		if (!(currsel instanceof ISystemProfile))
			return false;
		ISystemProfile profile = (ISystemProfile)currsel;
		ISystemProfileManager mgr = SystemProfileManager.getDefault();
		boolean allActive = true;
		while (profile != null)
		{
			if (!mgr.isSystemProfileActive(profile.getName()))
				allActive = false;
			currsel = getNextSelection();
			if ((currsel!=null) && !(currsel instanceof ISystemProfile))
				return false;
			profile = (ISystemProfile)currsel;
		}			
		return !allActive;
	}

	/**
	 * This is the method called when the user selects this action.
	 * It walks through all the selected profiles and make them all active
	 */
	public void run() 
	{
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		ISystemProfile profile = (ISystemProfile)getFirstSelection();
		while (profile != null)
		{
			sr.setSystemProfileActive(profile, true);
			profile = (ISystemProfile)getNextSelection();
		}		
	}		
}