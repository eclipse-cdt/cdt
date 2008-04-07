/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David Dykstal (IBM) - [222270] clean up interfaces in org.eclipse.rse.core.filters
 *******************************************************************************/

package org.eclipse.rse.subsystems.processes.core.subsystem.impl;

import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.internal.subsystems.processes.core.subsystem.SystemProcessesCoreResources;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystemConfiguration;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorServerPortInput;

/**
 * The implementation of the RemoteProcessSubSystemConfiguration interface
 * Contains information about what features the subsystem supports 
 * @author mjberger
 *
 */
public abstract class RemoteProcessSubSystemConfiguration extends
		SubSystemConfiguration implements IRemoteProcessSubSystemConfiguration
{
	
	public RemoteProcessSubSystemConfiguration()
	{
		super();
	}
	
    // --------------------------------------------
    // PARENT METHODS RELATED TO WHAT WE SUPPORT...
    // --------------------------------------------
    
    /**
     * We return false
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsNestedFilters()
     */
    public boolean supportsNestedFilters()
    {
    	return false;
    }

	/**
	 * Tell us if this subsystem factory supports server launch properties, which allow the user
	 *  to configure how the server-side code for these subsystems are started. There is a Server
	 *  Launch Setting property page, with a pluggable composite, where users can configure these 
	 *  properties. 
	 * <br> We return true.
	 */
	public boolean supportsServerLaunchProperties()
	{
		return true;
	}
	
    // ------------------------------------------------------
    // PARENT METHODS RELATED TO FILTERS...
    // ... ONLY INTERESTING IF supportsFilters() return true!
    // ------------------------------------------------------

	/**
	 * Override from parent.
	 * <p>
	 * Here we create the default filter pool for this subsystem factory, and populate it
	 *  with default filters. 
	 * <p>
	 */
	protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr)
	{
		ISystemFilterPool pool = null;
		try {
		  // -----------------------------------------------------
		  // create a pool named filters
		  // -----------------------------------------------------      			  
		  pool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), true); // true=>is deletable by user

		  // ---------------------------------------------------------------------------------------------
		  // create default filters in that pool iff this is the user's private profile we are creating...
		  // ---------------------------------------------------------------------------------------------
		  if (isUserPrivateProfile(mgr))
		  {
				    

		      // ----------------------
		      // "All Processes" filter...
		      // ----------------------
		      HostProcessFilterImpl allProcessesFilterString = new HostProcessFilterImpl();
		      String[] filterStrings = new String[] {allProcessesFilterString.toString()};	
		      ISystemFilter filter = mgr.createSystemFilter(pool, SystemProcessesCoreResources.RESID_PROPERTY_PROCESS_DEFAULTFILTER_LABEL, filterStrings);
		      filter.setNonChangable(true);
		      filter.setSingleFilterStringOnly(true);      
		      
		      //------------------------
		      // "My Processes" filter...
		      // ----------------------
		      HostProcessFilterImpl myProcessesFilterString = new HostProcessFilterImpl();
		      myProcessesFilterString.setUsername("${user.id}"); //$NON-NLS-1$
		      
		      filterStrings = new String[] {myProcessesFilterString.toString()};
		      filter = mgr.createSystemFilter(pool, SystemProcessesCoreResources.RESID_PROPERTY_PROCESS_MYPROCESSESFILTER_LABEL, filterStrings);
		      filter.setNonChangable(true);
		      filter.setSingleFilterStringOnly(true);  
		}
		else
		{

		      // ----------------------
		      // "All Processes" filter...
		      // ----------------------
		      HostProcessFilterImpl allProcessesFilterString = new HostProcessFilterImpl();
				 String[] filterStrings = new String[] {allProcessesFilterString.toString()};	
		      ISystemFilter filter = mgr.createSystemFilter(pool, SystemProcessesCoreResources.RESID_PROPERTY_PROCESS_DEFAULTFILTER_LABEL, filterStrings);
		      filter.setNonChangable(true);
		      filter.setSingleFilterStringOnly(true); 
		}
		}
		catch (Exception exc)
		{
			SystemBasePlugin.logError("Error creating default filter pool",exc); //$NON-NLS-1$
		}
		return pool;
	}
	
	public ISystemValidator getPortValidator()
	{
		ISystemValidator portValidator = new ValidatorServerPortInput();
		return portValidator;
	}
  
}
