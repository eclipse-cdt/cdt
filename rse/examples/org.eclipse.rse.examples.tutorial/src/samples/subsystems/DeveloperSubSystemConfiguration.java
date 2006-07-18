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
 * Martin Oberhuber (Wind River) - Adapted original tutorial code to Open RSE.
 ********************************************************************************/

package samples.subsystems;

import java.util.Vector;

import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.internal.filters.SystemFilter;
import org.eclipse.rse.model.IHost;

import samples.RSESamplesPlugin;

/**
 * This is our subsystem factory, which creates instances of our subsystems,
 *  and supplies the subsystem and filter actions to their popup menus.
 */
public class DeveloperSubSystemConfiguration extends SubSystemConfiguration {

	/**
	 * Constructor for DeveloperSubSystemConfiguration.
	 */
	public DeveloperSubSystemConfiguration() {
		super();
	}

	/**
	 * Create an instance of our subsystem.
	 */
	public ISubSystem createSubSystemInternal(IHost conn) {
	   	return new DeveloperSubSystem(conn, getConnectorService(conn));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getConnectorService(org.eclipse.rse.model.IHost)
	 */
	public IConnectorService getConnectorService(IHost host) {
		return DeveloperConnectorServiceManager.getTheDeveloperConnectorServiceManager()
			.getConnectorService(host, IDeveloperSubSystem.class);
	}

	/**
	 * Intercept of parent method that creates an initial default filter pool.
	 * We intercept so that we can create an initial filter in that pool, which will
	 *  list all teams.
	 */
	protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr)
	{
		ISystemFilterPool defaultPool = null;
		try {
			defaultPool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), true); // true=>is deletable by user
			Vector strings = new Vector();
			strings.add("*"); //$NON-NLS-1$
			//--tutorial part 1
			//mgr.createSystemFilter(defaultPool, "All teams", strings);
			//--tutorial part 2
			ISystemFilter filter = mgr.createSystemFilter(defaultPool, 
					RSESamplesPlugin.getResourceString("filter.default.name"), //$NON-NLS-1$
					strings);
			filter.setType("team"); //$NON-NLS-1$
		} catch (Exception exc) {}
		return defaultPool;
	}
	
	/**
	 * Intercept of parent method so we can supply our own value shown in the property
	 *  sheet for the "type" property when a filter is selected within our subsystem.
	 *
	 * Requires this line in rseSamplesResources.properties: property.type.teamfilter=Team filter
	 */
	public String getTranslatedFilterTypeProperty(SystemFilter selectedFilter)
	{
		//--tutorial part 1
	   	//return RSESamplesPlugin.getResourceString("property.type.teamfilter"); //$NON-NLS-1$
		//--tutorial part 2
	   	String type = selectedFilter.getType();
	   	if (type == null)
	   	  type = "team"; //$NON-NLS-1$
	   	if (type.equals("team")) //$NON-NLS-1$
	   	  return RSESamplesPlugin.getResourceString("property.type.teamfilter"); //$NON-NLS-1$ 
	   	else
	   	  return RSESamplesPlugin.getResourceString("property.type.devrfilter"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsUserId()
	 */
	public boolean supportsUserId() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchProperties(org.eclipse.rse.model.IHost)
	 */
	public boolean supportsServerLaunchProperties(IHost host) {
		return false;
	}

}
