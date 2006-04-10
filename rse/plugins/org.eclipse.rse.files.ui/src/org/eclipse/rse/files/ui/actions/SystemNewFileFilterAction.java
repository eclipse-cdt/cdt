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

package org.eclipse.rse.files.ui.actions;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.files.ui.SystemFileFilterStringEditPane;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.filters.actions.SystemNewFilterAction;
import org.eclipse.rse.ui.filters.dialogs.SystemNewFilterWizard;
import org.eclipse.swt.widgets.Shell;



/**
 * The action that displays the New File Filter wizard.
 * File Filters are typed filters that allow users to get a list of files meeting the filtering criteria.
 */
public class SystemNewFileFilterAction 
       extends SystemNewFilterAction
{
	//private RemoteFileSubSystemFactory inputSubsystemFactory;
		
	/**
	 * Constructor 
	 */
	public SystemNewFileFilterAction(IRemoteFileSubSystemConfiguration subsystemFactory, ISystemFilterPool parentPool, Shell shell) 

	{
		super(shell, parentPool, SystemFileResources.ACTION_NEWFILTER_LABEL, SystemFileResources.ACTION_NEWFILTER_TOOLTIP,
		      SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFILTER_ID));

        //setHelp(SystemPlugin.HELPPREFIX+"anff0000");
        //setDialogHelp(SystemPlugin.HELPPREFIX+"wnff0000");
		setHelp(SystemPlugin.HELPPREFIX+"actn0042");
		setDialogHelp(SystemPlugin.HELPPREFIX+"wnfr0000");       
	}		

	/**
	 * Set the parent filter pool that the new-filter actions need.
	 */
	public void setParentFilterPool(ISystemFilterPool parentPool)
	{
		this.parentPool = parentPool;
	}
	/**
	 * Parent intercept.
	 * <p>
	 * Overridable extension. For those cases when you don't want to create your
	 * own wizard subclass, but prefer to simply configure the default wizard.
	 * <p>
	 * Note, at the point this is called, all the base configuration, based on the 
	 * setters for this action, have been called. 
	 * <p>
	 * We do it here versus via setters as it defers some work until the user actually 
	 * selects this action.
	 */
	protected void configureNewFilterWizard(SystemNewFilterWizard wizard)
	{		
		// configuration that used to only be possible via subclasses...
		wizard.setWizardPageTitle(SystemFileResources.RESID_NEWFILEFILTER_PAGE1_TITLE);
	  	wizard.setWizardImage(SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFILTERWIZARD_ID));
		wizard.setPage1Description(SystemFileResources.RESID_NEWFILEFILTER_PAGE1_DESCRIPTION);
		wizard.setFilterStringEditPane(new SystemFileFilterStringEditPane(wizard.getShell()));		
	}
}