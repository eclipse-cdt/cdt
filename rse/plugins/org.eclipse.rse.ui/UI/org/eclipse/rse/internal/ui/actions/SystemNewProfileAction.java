/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [cleanup] Avoid using SystemStartHere in production code
 *******************************************************************************/

package org.eclipse.rse.internal.ui.actions;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.view.team.SystemTeamView;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseWizardAction;
import org.eclipse.rse.ui.wizards.SystemNewProfileWizard;
import org.eclipse.swt.widgets.Shell;

/**
 * The action that displays the New Profile wizard
 */
public class SystemNewProfileAction extends SystemBaseWizardAction {

	/**
	 * Constructor for SystemNewProfileAction for "New -> Profile..."
	 * @param parent the parent shell in which this action executes
	 */
	public SystemNewProfileAction(Shell parent) {
		super(SystemResources.ACTION_NEWPROFILE_LABEL, SystemResources.ACTION_NEWPROFILE_TOOLTIP, RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWPROFILE_ID), parent);
		setSelectionSensitive(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0003"); //$NON-NLS-1$		    
	}

	/**
	 * Constructor for SystemNewProfileAction where you can choose between "New profile..." and "New -> Profile"
	 * @param parent the parent shell in which this action executes
	 * @param cascading if true then use the "New -> Profile" style, else use the "New profile..." style.
	 */
	public SystemNewProfileAction(Shell parent, boolean cascading) {
		super(cascading ? SystemResources.ACTION_NEWPROFILE_LABEL : SystemResources.ACTION_NEW_PROFILE_LABEL, cascading ? SystemResources.ACTION_NEWPROFILE_TOOLTIP
				: SystemResources.ACTION_NEW_PROFILE_TOOLTIP, RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWPROFILE_ID), parent);
		setSelectionSensitive(false);
		if (cascading)
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);
		else
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW_NONCASCADING);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0003"); //$NON-NLS-1$		    
	}

	/**
	 * Refresh the enabled state
	 */
	public void refreshEnablement() {
		setEnabled(isEnabled());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.actions.SystemBaseAction#isEnabled()
	 */
	public boolean isEnabled() {
		return true;
		/*
		 * From the old javadoc:
		 * We disable this action if it is a new workspace and the user has yet to create
		 * their first connection, and hence rename their default profile.
		 * @return true if the action is enabled
		 * 
		 * defect 43428 was an early RSE defect whose fix prevented the creation of profiles until the first 
		 * connection was created. However, new default profiles are created when RSE is initially activated
		 * so there is no need to inhibit profile creation. 
		 */
//		ISystemProfile defaultProfile = RSECorePlugin.getTheSystemProfileManager().getDefaultPrivateSystemProfile();
//		if (defaultProfile != null)
//			return false;
//		else
//			return true;
	}

	/**
	 * @return a new Wizard object for creating a profile.
	 */
	protected IWizard createWizard() {
		return new SystemNewProfileWizard();
	}

	/**
	 * Typically, the wizard's performFinish method does the work required by
	 * a successful finish of the wizard. However, often we also want to be
	 * able to extract user-entered data from the wizard, by calling getters
	 * in this action. To enable this, override this method to populate your
	 * output instance variables from the completed wizard, which is passed
	 * as a parameter. This is only called after successful completion of the
	 * wizard.
	 * @param wizard the wizard that was just completed
	 */
	protected void postProcessWizard(IWizard wizard) {
		if (getViewer() instanceof SystemTeamView) {
			getViewer().refresh();
		}
		
	}
}
