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
 * Martin Oberhuber (Wind River) - Adapted from LocalConnectorServiceManager.
 ********************************************************************************/

package org.eclipse.rse.connectorservice.ssh;

import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;

/**
 * ConnectorService manager class.
 * 
 * The job of this manager is to manage and return IConnectorService
 * objects. It ensures there is only ever one per unique SystemConnection,
*  so that both the file and cmd subsystems can share the same 
*  ConnectorService object.
 */
public class SshConnectorServiceManager extends AbstractConnectorServiceManager {

	private static SshConnectorServiceManager fInstance;

	private SshConnectorServiceManager() {
		super();
	}

    /**
     * Return singleton instance of this class
     */
    public static SshConnectorServiceManager getInstance()
    {
    	if (fInstance == null)
    	  fInstance = new SshConnectorServiceManager();
    	return fInstance;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#createConnectorService(org.eclipse.rse.model.IHost)
	 */
	public IConnectorService createConnectorService(IHost host) {
    	IConnectorService service =  new SshConnectorService(host);
    	return service;
	}

	public boolean sharesSystem(ISubSystem otherSubSystem) {
		return (otherSubSystem instanceof ISshSubSystem);
	}

	public Class getSubSystemCommonInterface(ISubSystem subsystem) {
		return ISshSubSystem.class;
	}

}
