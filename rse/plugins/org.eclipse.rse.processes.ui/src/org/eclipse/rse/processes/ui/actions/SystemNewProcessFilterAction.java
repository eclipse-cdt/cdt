/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.processes.ui.actions;

import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.processes.ui.SystemProcessFilterStringEditPane;
import org.eclipse.rse.processes.ui.SystemProcessesResources;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.filters.actions.SystemNewFilterAction;
import org.eclipse.rse.ui.filters.dialogs.SystemNewFilterWizard;
import org.eclipse.swt.widgets.Shell;


public class SystemNewProcessFilterAction extends SystemNewFilterAction implements ISystemIconConstants
{
	
	/**
	 * Constructor 
	 */
	public SystemNewProcessFilterAction(Shell shell, ISystemFilterPool parentPool) 

	{
		super(shell, parentPool, SystemProcessesResources.ACTION_NEWPROCESSFILTER_LABEL, 
		      SystemProcessesResources.ACTION_NEWPROCESSFILTER_TOOLTIP, RSEUIPlugin.getDefault().getImageDescriptor(ICON_SYSTEM_NEWFILTER_ID));
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0042");
		setDialogHelp(RSEUIPlugin.HELPPREFIX+"wnfr0000"); 
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
		wizard.setWizardPageTitle(SystemProcessesResources.RESID_NEWPROCESSFILTER_PAGE1_TITLE);
	  	wizard.setWizardImage(RSEUIPlugin.getDefault().getImageDescriptor(ICON_SYSTEM_NEWFILTERWIZARD_ID));
		wizard.setPage1Description(SystemProcessesResources.RESID_NEWPROCESSFILTER_PAGE1_DESCRIPTION);
		wizard.setFilterStringEditPane(new SystemProcessFilterStringEditPane(wizard.getShell()));
	}
}