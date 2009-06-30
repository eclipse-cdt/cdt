/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
 * David McKnight         (IBM)   - [281371] Check box Only create filter in this connection doesn't work
 *******************************************************************************/

package org.eclipse.rse.internal.processes.ui.actions;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolWrapperInformation;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.internal.processes.ui.SystemProcessesResources;
import org.eclipse.rse.processes.ui.SystemProcessFilterStringEditPane;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.filters.actions.SystemNewFilterAction;
import org.eclipse.rse.ui.filters.dialogs.SystemNewFilterWizard;
import org.eclipse.swt.widgets.Shell;

public class SystemNewProcessFilterAction extends SystemNewFilterAction 
{
	private SubSystem _selectedSubSystem;
	/**
	 * Constructor 
	 */
	public SystemNewProcessFilterAction(Shell shell, ISystemFilterPool parentPool) 

	{
		super(shell, parentPool, SystemProcessesResources.ACTION_NEWPROCESSFILTER_LABEL, 
		      SystemProcessesResources.ACTION_NEWPROCESSFILTER_TOOLTIP, RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFILTER_ID));
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0042"); //$NON-NLS-1$
		setDialogHelp(RSEUIPlugin.HELPPREFIX+"wnfr0000");  //$NON-NLS-1$
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
	  	wizard.setWizardImage(RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFILTERWIZARD_ID));
		wizard.setPage1Description(SystemProcessesResources.RESID_NEWPROCESSFILTER_PAGE1_DESCRIPTION);
		wizard.setFilterStringEditPane(new SystemProcessFilterStringEditPane(wizard.getShell()));
	}
	
	public void run()
	{
		if (_selectedSubSystem != null){
			setAllowFilterPoolSelection(_selectedSubSystem.getFilterPoolReferenceManager().getReferencedSystemFilterPools());			
		}
		else {
			// disallow filter pool select (because this is from a filter pool)
			setAllowFilterPoolSelection((ISystemFilterPool[])null);
			setAllowFilterPoolSelection((ISystemFilterPoolWrapperInformation)null);
			
			callbackConfigurator = null;
			callbackConfiguratorCalled = false;
		}
		super.run();
	}
	
	/**
	 * Called when the selection changes in the systems view.  This determines
	 * the input object for the command and whether to enable or disable
	 * the action.
	 * 
	 * @param selection the current seleciton
	 * @return whether to enable or disable the action
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		_selectedSubSystem = null;
		Iterator e = selection.iterator();
		Object selected = e.next();

		if (selected != null && selected instanceof SubSystem)
		{
			_selectedSubSystem = (SubSystem) selected;
		}

		return super.updateSelection(selection);
	}
}
