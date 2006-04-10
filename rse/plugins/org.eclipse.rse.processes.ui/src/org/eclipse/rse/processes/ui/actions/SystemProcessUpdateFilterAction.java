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

import org.eclipse.rse.processes.ui.SystemProcessFilterStringEditPane;
import org.eclipse.rse.processes.ui.SystemProcessesResources;
import org.eclipse.rse.ui.filters.actions.SystemChangeFilterAction;
import org.eclipse.rse.ui.filters.dialogs.SystemChangeFilterDialog;
import org.eclipse.swt.widgets.Shell;


public class SystemProcessUpdateFilterAction extends SystemChangeFilterAction
{
	
	/**
	 * Constructor
	 */
	public SystemProcessUpdateFilterAction(Shell parent) 
	{
		super(parent, SystemProcessesResources.ACTION_UPDATEFILTER_LABEL, SystemProcessesResources.ACTION_UPDATEFILTER_TOOLTIP);
		setDialogTitle(SystemProcessesResources.RESID_CHGPROCESSFILTER_TITLE);
	}
	/**
	 * Overridable extension point to configure the filter dialog. Typically you don't need
	 *  to subclass our default dialog.
	 */
	protected void configureFilterDialog(SystemChangeFilterDialog dlg)
	{
		// it is cheaper to do this here, as it defers instantiation of the edit pane until the
		//  user actually runs the action!
		Shell shell = dlg.getShell();
		if (shell == null)
			shell = dlg.getParentShell();
		
		dlg.setFilterStringEditPane(new SystemProcessFilterStringEditPane(shell));		
	}
}