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
import org.eclipse.rse.files.ui.SystemFileFilterStringEditPane;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.filters.actions.SystemChangeFilterAction;
import org.eclipse.rse.ui.filters.dialogs.SystemChangeFilterDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * The action that displays the Change File Filter dialog.
 * Note that the input filter is deduced from the current selection, which must be a filter reference.
 */
public class SystemFileUpdateFilterAction 
       extends SystemChangeFilterAction 
{

	/**
	 * Constructor
	 */
	public SystemFileUpdateFilterAction(Shell parent) 
	{ 
		super(parent, SystemResources.ACTION_UPDATEFILTER_LABEL, SystemResources.ACTION_UPDATEFILTER_TOOLTIP);
		setDialogTitle(SystemFileResources.RESID_CHGFILEFILTER_TITLE);
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
		
		dlg.setFilterStringEditPane(new SystemFileFilterStringEditPane(shell));		
	}
}