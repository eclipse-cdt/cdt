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
import org.eclipse.rse.internal.model.SystemProfileManager;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemProfileManager;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.widgets.Shell;


/**
 * A selectable profile name action.
 */
public class SystemProfileNameSelectAction extends SystemBaseAction 
                                 
{	
	
	private ISystemProfile profile;
	
	/**
	 * Constructor
	 */
	public SystemProfileNameSelectAction(Shell parent, ISystemProfile profile) 
	{
		super(profile.getName(),parent);
		this.profile = profile;
		ISystemProfileManager mgr = SystemProfileManager.getSystemProfileManager();
		setChecked(mgr.isSystemProfileActive(profile.getName()));
        setSelectionSensitive(false);
        
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0004");
	}


	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		sr.setSystemProfileActive(profile, isChecked());
	}		
}