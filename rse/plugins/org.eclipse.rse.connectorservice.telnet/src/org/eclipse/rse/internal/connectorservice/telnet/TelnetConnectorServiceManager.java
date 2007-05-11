/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - Adapted from LocalConnectorServiceManager.
 * Sheldon D'souza (Celunite) - Adapted from SshConnectorServiceManager
 *******************************************************************************/
package org.eclipse.rse.internal.connectorservice.telnet;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;

public class TelnetConnectorServiceManager extends
		AbstractConnectorServiceManager {

	private static TelnetConnectorServiceManager fInstance;

	private TelnetConnectorServiceManager() {
		super();
	}

    /**
     * Return singleton instance of this class
     * @return the singleton instance
     */
    public static TelnetConnectorServiceManager getInstance()
    {
    	if (fInstance == null)
    	  fInstance = new TelnetConnectorServiceManager();
    	return fInstance;
    }
	

	public Class getSubSystemCommonInterface(ISubSystem subsystem) {
		return ITelnetSubSystem.class;
	}

	public boolean sharesSystem(ISubSystem otherSubSystem) {
		return (otherSubSystem instanceof ITelnetSubSystem);
	}

	public IConnectorService createConnectorService(IHost host) {
		IConnectorService service =  new TelnetConnectorService(host);
    	return service;
	}

}
