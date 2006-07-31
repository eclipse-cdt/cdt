/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.processes.ui.view;

import java.util.Vector;

import org.eclipse.jface.action.IAction;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.processes.ui.actions.SystemNewProcessFilterAction;
import org.eclipse.rse.processes.ui.actions.SystemProcessUpdateFilterAction;
import org.eclipse.rse.ui.view.SubsystemConfigurationAdapter;
import org.eclipse.swt.widgets.Shell;


public class RemoteProcessSubSystemConfigurationAdapter extends SubsystemConfigurationAdapter
{
	
	SystemNewProcessFilterAction _newProcessFilterAction;
	SystemProcessUpdateFilterAction _changeProcessFilterAction;
	
	Vector _additionalActions;
	
	/**
	 * Overridable parent method to return the action for creating a new filter.
	 * Returns new SystemNewFileFilterAction.
	 */
	protected IAction getNewFilterPoolFilterAction(ISubSystemConfiguration factory, ISystemFilterPool selectedPool, Shell shell) 
	{
		if (_newProcessFilterAction == null)
		{
			_newProcessFilterAction = new SystemNewProcessFilterAction(shell, selectedPool);
		}
		return _newProcessFilterAction;
	}

    
    /**
     * Overridable method to return the action for changing an existing filter.
     * Returns new SystemProcessUpdateFilterAction.
     */
    protected IAction getChangeFilterAction(ISubSystemConfiguration factory, ISystemFilter selectedFilter, Shell shell)
    {
    	if (_changeProcessFilterAction == null)
    	{    		
    		_changeProcessFilterAction = new SystemProcessUpdateFilterAction(shell);
    	}
    	return _changeProcessFilterAction;
    }      

 
	
}