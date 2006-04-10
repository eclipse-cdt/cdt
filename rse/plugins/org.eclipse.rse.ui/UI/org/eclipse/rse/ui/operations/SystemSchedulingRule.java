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

package org.eclipse.rse.ui.operations;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A simple job scheduling rule for serializing jobs for an ICVSRepositoryLocation
 */
public class SystemSchedulingRule implements ISchedulingRule {
	IAdaptable _location;
	
	public SystemSchedulingRule(IAdaptable location) 
	{		
	    _location = location;
	}		
	
	public boolean isConflicting(ISchedulingRule rule) 
	{
		if(rule instanceof SystemSchedulingRule) 
		{
			return ((SystemSchedulingRule)rule)._location.equals(_location);
		}
		return false;
	}
	
	public boolean contains(ISchedulingRule rule) 
	{		
		return isConflicting(rule);
	}
}