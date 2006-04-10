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

package org.eclipse.rse.ui.filters;
import org.eclipse.rse.ui.filters.actions.SystemFilterAbstractFilterPoolAction;


/**
 * Common interface for dialogs or wizards that work with filter pools.
 */
public interface SystemFilterPoolDialogInterface 
{


	/**
	 * Allow base action to pass instance of itself for callback to get info
	 */
    public void setFilterPoolDialogActionCaller(SystemFilterAbstractFilterPoolAction caller);
    /**
     * Return an object containing user-specified information pertinent to filter pool actions
     */
    public SystemFilterPoolDialogOutputs getFilterPoolDialogOutputs();    
    /**
     * Set the help context id for this wizard
     */
    public void setHelpContextId(String id);
}