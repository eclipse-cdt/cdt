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

package org.eclipse.rse.core;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;


/**
 * Manages remote system property page extenders.
 * @see org.eclipse.rse.core.SystemPropertyPageExtension
 */
public class SystemPropertyPageExtensionManager 
{
    private SystemPropertyPageExtension[] propertyPageSuppliers = null;    
    private static SystemPropertyPageExtensionManager inst = null;
    //private PropertyPageContributorManager test = null;
    /**
     * Constructor. Don't call directly.
     */
    protected SystemPropertyPageExtensionManager()
    {
    }
    /**
     * Get the singleton of this manager
     */
    public static SystemPropertyPageExtensionManager getManager()
    {
    	if (inst == null)
    	  inst = new SystemPropertyPageExtensionManager();
    	return inst;
    }
    
	/**
	 * Get all the extenders of the remote properties page extension point
	 */
	public SystemPropertyPageExtension[] getPropertyPageSuppliers()
	{
		if (propertyPageSuppliers == null)
		{
		  propertyPageSuppliers = RSEUIPlugin.getDefault().getPropertyPageExtensions();
		}
		return propertyPageSuppliers;
	}
	
	/**
	 * Return true if there are any remote property page contributions for the
	 * given selected object
	 */
	public boolean hasContributorsFor(ISystemRemoteElementAdapter adapter, Object element)
	{
		boolean hasContributors = false;
		if (adapter != null)
		{		
			getPropertyPageSuppliers();

			if (propertyPageSuppliers != null)
			{
				for (int idx=0; !hasContributors && (idx<propertyPageSuppliers.length); idx++)
				  hasContributors = propertyPageSuppliers[idx].appliesTo(adapter, element);
			}
		}
		else
		{
			hasContributors = false;
		}
		return hasContributors;
	}
	
	/**
	 * Populate a given property page manager with all the applicable remote property pages
	 */
	public boolean contribute(PropertyPageManager manager, ISystemRemoteElementAdapter adapter, Object object) 
	{
		boolean added = false;
		getPropertyPageSuppliers();
		if (propertyPageSuppliers != null)
		{
			for (int idx=0; idx<propertyPageSuppliers.length; idx++)
			{
			  boolean applies = propertyPageSuppliers[idx].appliesTo(adapter, object);
			  if (applies)
			  {
			  	added = true;
			  	propertyPageSuppliers[idx].contributePropertyPages(manager, (IAdaptable)object);
			  }
			}
		}		
		return added;
	}

}