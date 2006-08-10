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

import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;

/**
 * This class manages our DeveloperConnectorService objects, so that if we 
 * ever have multiple subsystem factories, different subsystems can share 
 * the same system object if they share the communication layer.
 */
public class DeveloperConnectorServiceManager extends
		AbstractConnectorServiceManager {

	private static DeveloperConnectorServiceManager inst;

	/**
	 * Constructor for DeveloperConnectorServiceManager.
	 */
	public DeveloperConnectorServiceManager()
	{
		super();
	}

	/**
	 * Return singleton instance
	 * @return the singleton instance
	 */
	public static DeveloperConnectorServiceManager getTheDeveloperConnectorServiceManager()
	{
		if (inst == null)
		  inst = new DeveloperConnectorServiceManager();
		return inst;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#createConnectorService(org.eclipse.rse.model.IHost)
	 */
	public IConnectorService createConnectorService(IHost host)
	{
		return new DeveloperConnectorService(host);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#sharesSystem(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public boolean sharesSystem(ISubSystem otherSubSystem)
	{
		return (otherSubSystem instanceof IDeveloperSubSystem);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#getSubSystemCommonInterface(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public Class getSubSystemCommonInterface(ISubSystem subsystem)
	{
		return IDeveloperSubSystem.class;
	}

}
