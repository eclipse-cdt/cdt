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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.SystemStartHere;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.view.team.SystemTeamView;
import org.eclipse.rse.ui.wizards.SystemNewProfileWizard;
import org.eclipse.swt.widgets.Shell;


/**
 * The action that displays the New Profile wizard
 */
public class SystemNewProfileAction extends SystemBaseWizardAction 
                                 
{
	
	/**
	 * Constructor for SystemNewProfileAction for "New -> Profile..."
	 */
	public SystemNewProfileAction(Shell parent) 
	{
		super(SystemResources.ACTION_NEWPROFILE_LABEL, SystemResources.ACTION_NEWPROFILE_TOOLTIP, SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWPROFILE_ID), parent);
        setSelectionSensitive(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);  		  
		setHelp(SystemPlugin.HELPPREFIX+"actn0003");		    
	}
	/**
	 * Constructor for SystemNewProfileAction where you can choose between "New profile..." and "New -> Profile"
	 */
	public SystemNewProfileAction(Shell parent, boolean cascading) 
	{
		super(cascading ? SystemResources.ACTION_NEWPROFILE_LABEL : SystemResources.ACTION_NEW_PROFILE_LABEL, 
				cascading ? SystemResources.ACTION_NEWPROFILE_TOOLTIP : SystemResources.ACTION_NEW_PROFILE_TOOLTIP,
				SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWPROFILE_ID), parent);
		setSelectionSensitive(false);
		if (cascading)
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);
		else  		  
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW_NONCASCADING);
		setHelp(SystemPlugin.HELPPREFIX+"actn0003");		    
	}
	
	/**
	 * Refresh the enabled state
	 */
	public void refreshEnablement()
	{
		setEnabled(isEnabled());
	}

	/**
	 * We disable this action if it is a new workspace and the user has yet to create
	 *  their first connection, and hence rename their default profile.
	 */
	public boolean isEnabled()
	{
		// defect 43428...
	    ISystemProfile defaultProfile = SystemStartHere.getSystemProfileManager().getDefaultPrivateSystemProfile();
	    if (defaultProfile != null)
	      return false;
	    else
	      return true;
	}
	/**
	 * The default processing for the run method calls createDialog, which
	 *  in turn calls this method to return an instance of our wizard.
	 * <p>
	 * Our default implementation is to call SystemNewProfileWizard.
	 */
	protected IWizard createWizard()
	{
		return new SystemNewProfileWizard();		
	}

	/**
	 * Typically, the wizard's performFinish method does the work required by
	 *  a successful finish of the wizard. However, often we also want to be
	 *  able to extract user-entered data from the wizard, by calling getters
	 *  in this action. To enable this, override this method to populate your
	 *  output instance variables from the completed wizard, which is passed
	 *  as a parameter. This is only called after successful completion of the
	 *  wizard.
	 */
	protected void postProcessWizard(IWizard wizard)
	{
		if (getViewer() instanceof SystemTeamView)
		{
			getViewer().refresh();
		}
	}	
}