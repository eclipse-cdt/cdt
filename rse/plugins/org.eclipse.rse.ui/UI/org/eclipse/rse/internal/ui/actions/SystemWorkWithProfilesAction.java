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
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.view.SystemPerspectiveHelpers;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewPart;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;


/**
 * The action shows in the local toolbar of the Remote Systems View, and 
 *  users can select it to give the Team view focus. 
 */
public class SystemWorkWithProfilesAction extends SystemBaseAction 
                                 
{
    //private ISystemRegistry sr = null;

	/**
	 * Constructor
	 */
	public SystemWorkWithProfilesAction(Shell parent) 
	{
		super(SystemResources.ACTION_WORKWITH_PROFILES_LABEL, SystemResources.ACTION_WORKWITH_PROFILES_TOOLTIP, parent);
        setSelectionSensitive(false);
        allowOnMultipleSelection(true);
        //sr = RSECorePlugin.getTheSystemRegistry();	        
		setHelp(RSEUIPlugin.HELPPREFIX+"actnwwpr"); //$NON-NLS-1$
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		SystemPerspectiveHelpers.showView(SystemTeamViewPart.ID);
	}		
	
}