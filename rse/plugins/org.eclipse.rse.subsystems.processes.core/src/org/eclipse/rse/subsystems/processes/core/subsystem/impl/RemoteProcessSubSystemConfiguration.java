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

package org.eclipse.rse.subsystems.processes.core.subsystem.impl;

import java.util.Vector;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystemConfiguration;
import org.eclipse.rse.subsystems.processes.core.subsystem.SystemProcessesCoreResources;
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
     * We return true.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsUserId()
     */
    public boolean supportsUserId()
    {
    	return true;
    }
    
	/**
     * We return true.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsSubSystemConnect()
	 */
	public boolean supportsSubSystemConnect()
	{
		return true;
	}
    /**
     * We return true.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#isPortEditable()
     */
    public boolean isPortEditable()
    {
    	return true;    
    }	
	
	/**
	 * We return false.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsCommands()
	 */
	public boolean supportsCommands()
	{
		return false;
	}
	/**
	 * We return false.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsProperties()
	 */
	public boolean supportsProperties()
	{
		return false;
	}
    /**
     * We return true.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsFilters()
     */
    public boolean supportsFilters()
    {
    	return true;
    }

    /**
     * We return false
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsNestedFilters()
     */
    public boolean supportsNestedFilters()
    {
    	return false;
    }
	/**
	 * Return true if you support compile actions for the remote system objects returned from expansion of
	 *  subsystems created by this subsystem factory.
	 * <p>
	 * By returning true, user sees a "Work with->Compile Commands..." action item in the popup menu for this
	 *  subsystem. The action is supplied by the framework, but is populated using overridable methods in this subsystem.
	 * <p>We return true.
	 */
	public boolean supportsCompileActions()
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
				    
		      Vector filterStrings = new Vector();	

		      // ----------------------
		      // "All Processes" filter...
		      // ----------------------
		      filterStrings = new Vector();
		      HostProcessFilterImpl allProcessesFilterString = new HostProcessFilterImpl();
		      filterStrings.add(allProcessesFilterString.toString());
		      ISystemFilter filter = mgr.createSystemFilter(pool, SystemProcessesCoreResources.RESID_PROPERTY_PROCESS_DEFAULTFILTER_LABEL,filterStrings);
		      filter.setNonChangable(true);
		      filter.setSingleFilterStringOnly(true);      
		      
		      //------------------------
		      // "My Processes" filter...
		      // ----------------------
		      filterStrings = new Vector();
		      HostProcessFilterImpl myProcessesFilterString = new HostProcessFilterImpl();
		      myProcessesFilterString.setUsername("${user.id}");
		      
		      filterStrings.add(myProcessesFilterString.toString());
		      filter = mgr.createSystemFilter(pool, SystemProcessesCoreResources.RESID_PROPERTY_PROCESS_MYPROCESSESFILTER_LABEL,filterStrings);
		      filter.setNonChangable(true);
		      filter.setSingleFilterStringOnly(true);  
		  }
		} catch (Exception exc)
		{
			SystemBasePlugin.logError("Error creating default filter pool",exc);
		}
		return pool;
	}
	
       

    /**
     * Return the translated string to show in the property sheet for the type property.
     */
    public String getTranslatedFilterTypeProperty(ISystemFilter selectedFilter)
    {
    	return super.getTranslatedFilterTypeProperty(selectedFilter);
    }    

	public ISystemValidator getPortValidator()
	{
		ISystemValidator portValidator = new ValidatorServerPortInput();
		return portValidator;
	}
 
}