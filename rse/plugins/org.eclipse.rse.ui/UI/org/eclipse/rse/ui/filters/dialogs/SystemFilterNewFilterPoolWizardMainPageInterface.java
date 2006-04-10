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

package org.eclipse.rse.ui.filters.dialogs;

import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.ui.filters.SystemFilterPoolDialogOutputs;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.wizards.ISystemWizardPage;



/**
 * Interface for new Filter wizard main page classes
 */
public interface SystemFilterNewFilterPoolWizardMainPageInterface extends ISystemWizardPage
{
	/**
	 * Call this to specify a validator for the pool name. It will be called per keystroke.
	 * Only call this if you do not call setFilterPoolManagers! 
	 */
	public void setNameValidator(ISystemValidator v);
	/**
	 * Even if you call setFilterPoolManagers and you really want your own validators,
	 * then call this. Otherwise, FolderNameValidator will be called for you.
	 * The input must be an array of validators that is the same length as the array
	 * of filter pool managers. Call this AFTER setFilterPoolManagers!
	 */
	public void setNameValidators(ISystemValidator[] v);
	/**
	 * Call this to specify the list of filter pool managers to allow the user to select from.
	 * Either call this or override getFilterPoolManagerNames, or leave null and this prompt will
	 * not show.
	 */
	public void setFilterPoolManagers(ISystemFilterPoolManager[] mgrs);
	/**
	 * Set the zero-based index of the manager name to preselect.
	 * The default is zero.
	 * Either call this or override getFilterPoolManagerNameSelectionIndex.
	 */
	public void setFilterPoolManagerNameSelectionIndex(int index);
		
	/**
	 * Return user-entered pool name.
	 * Call this after finish ends successfully.
	 */
	public String getPoolName();
	/**
	 * Return user-selected pool manager name.
	 * Call this after finish ends successfully.
	 */
    public String getPoolManagerName();
    /**
     * Return an object containing user-specified information pertinent to filter pool actions
     */
    public SystemFilterPoolDialogOutputs getFilterPoolDialogOutputs();

}