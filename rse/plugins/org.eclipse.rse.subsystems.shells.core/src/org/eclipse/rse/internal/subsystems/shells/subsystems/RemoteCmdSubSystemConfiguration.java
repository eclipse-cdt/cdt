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

package org.eclipse.rse.internal.subsystems.shells.subsystems;

import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystemConfiguration;



public abstract class RemoteCmdSubSystemConfiguration extends SubSystemConfiguration implements IRemoteCmdSubSystemConfiguration
{
    protected String translatedType;

	
	/**
	 * Default constructor.
	 */
	public RemoteCmdSubSystemConfiguration() 
	{
		super();
	}
	// --------------------------------------------
    // PARENT METHODS RELATED TO WHAT WE SUPPORT...
    // --------------------------------------------
	/**
     * We return true.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsSubSystemConnect()
	 */
	public boolean supportsSubSystemConnect()
	{
		return true;
	}
	
	/**
	 * Return true if the subsystem supports more than one filter string
	 */
	public boolean supportsMultiStringFilters()
	{
		return false;
	}	 
	
	/**
	  * Return true if the subsystem supports the exporting of filter strings from it's filters
	 */
	public boolean supportsFilterStringExport()
	{
		return false;
	}
	
	/**
	 * Return true if subsystems of this configuration support the environment variables property.
	 * For default remote command subsystems, we return <code>true</code>.
	 * @see org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystemConfiguration#supportsEnvironmentVariablesPropertyPage()
	 */
	public boolean supportsEnvironmentVariablesPropertyPage()
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
	 * We return true.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsCommands()
	 */
	public boolean supportsCommands()
	{
		return true;
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
    	return false;
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
	 * Tell us if filter strings are case sensitive. The default is false.
	 */
	public boolean isCaseSensitive()
	{
		return false;
	}
	/**
	 * Tell us if duplicate filter strings are supported. The default is true for command subsystem factories!
	 */
	public boolean supportsDuplicateFilterStrings()
	{
		return true;
	}
    
    // ------------------------------------------------------
    // PARENT METHODS RELATED TO FILTERS...
    // ... ONLY INTERESTING IF supportsFilters() return true!
    // ------------------------------------------------------

	/**
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createDefaultFilterPool(ISystemFilterPoolManager)
	 */
	protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr)
	{
		//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"in createDefaultFilterPool for remote file subsystem factory");
		ISystemFilterPool pool = null;
		//try {
		  // -----------------------------------------------------
		  // create a pool named filters
		  // -----------------------------------------------------      			  
		  // PHIL HERE: SINCE WE DON'T SHOW CMD SUBSYSTEMS BY DEFAULT, WHY BOTHER CREATING A DEFAULT POOL?
		  //pool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName()), true); // true=>is deletable by user
		  //System.out.println("Pool created");
		  // ---------------------------------------------------------------------------------------------
		  // create default filters in that pool iff this is the user's private profile we are creating...
		  // ---------------------------------------------------------------------------------------------
		  //if (isUserPrivateProfile(mgr))
		  //{
		  //}
		//} catch (Exception exc)
		//{
			//RSEUIPlugin.logError("Error creating default filter pool",exc);
		//}
		return pool;
	}

    


    /**
     * Return the translated string to show in the property sheet for the type property.
     */
    public String getTranslatedFilterTypeProperty(ISystemFilter selectedFilter)
    {
    	// do we really need this?
    	//if (translatedType == null)
         // translatedType = SystemResources.RESID_PROPERTY_FILE_FILTER_VALUE;    	  
    	return translatedType;
    }    
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystemConfiguration#getCommandSeparator()
     */
    public String getCommandSeparator()
    {
    	return ";"; //$NON-NLS-1$
    }

  

   


    

}