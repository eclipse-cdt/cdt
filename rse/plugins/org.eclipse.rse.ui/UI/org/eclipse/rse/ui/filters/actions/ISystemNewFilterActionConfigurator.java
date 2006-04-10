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

package org.eclipse.rse.ui.filters.actions;

import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;

/**
 * Sometimes configuring a filter action can take a reasonably substantial amount of 
 *  processing. For this reason, we would desire to defer that configuration, when simply
 *  filling a popup menu, until such time as the user has select to run the action. This
 *  reduces time and memory in the vast majority of cases.
 * <p>
 * To achieve this, code which populates a context menu can implement this interface, and
 *  pass it to the new filter wizard action. That action will then call back to the caller
 *  via this interface, when the action is run.
 */
public interface ISystemNewFilterActionConfigurator 
{
	
	/**
	 * The user has selected to run this action. Please configure it!
	 * @param newFilterAction - the action to be configured
	 * @param callerData - context data that you supplied when registering this callback
	 */
    public void configureNewFilterAction(ISubSystemConfiguration factory, SystemNewFilterAction newFilterAction, Object callerData);
}