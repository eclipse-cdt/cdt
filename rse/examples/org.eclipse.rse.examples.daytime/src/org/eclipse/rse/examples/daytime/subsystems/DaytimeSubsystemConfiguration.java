/********************************************************************************
 * Copyright (c) 2006 IBM Corporation and Wind River Systems, Inc.
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
 * Martin Oberhuber (Wind River) - adapted template for daytime example.
 ********************************************************************************/

package org.eclipse.rse.examples.daytime.subsystems;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rse.core.servicesubsystem.ServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.examples.daytime.connectorservice.DaytimeConnectorService;
import org.eclipse.rse.examples.daytime.connectorservice.DaytimeConnectorServiceManager;
import org.eclipse.rse.examples.daytime.service.IDaytimeService;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.IService;

/**
 * The DaytimeSubsystemConfiguration implements the main API for registering
 * a new subsystem type.
 * It gives the RSE framework basic configuration data about enabled
 * or disabled options, and is responsible for instanciating the actual
 * Daytime subsystem as well as the UI-less configuration layer (service).
 */
public class DaytimeSubsystemConfiguration extends ServiceSubSystemConfiguration {

	private Map fServices = new HashMap();
	
	public DaytimeSubsystemConfiguration() {
		super();
	}

	public boolean supportsServerLaunchProperties(IHost host) {
		return false;
	}
	public boolean supportsFilters() {
		return false;
	}
	public boolean supportsSubSystemConnect() {
		//TODO for now, we have to connect in order to pass the hostname to the service
		//This should not be necessary in an ideal world
		return true; 
	}
	public boolean isPortEditable() {
		return false;
	}
	public boolean supportsUserId() {
		return false;
	}
	public boolean isFactoryFor(Class subSystemType) {
		return DaytimeSubSystem.class.equals(subSystemType);
	}

	/**
	 * Instantiate and return an instance of OUR subystem. 
	 * Do not populate it yet though!
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(IHost)
	 */
	public ISubSystem createSubSystemInternal(IHost host) {
		IConnectorService connectorService = getConnectorService(host);
		ISubSystem subsys = new DaytimeSubSystem(host, connectorService, createDaytimeService(host)); // DWD need to provide the subsystem with a name and id too.
		return subsys;
	}

	public IConnectorService getConnectorService(IHost host) {
		return DaytimeConnectorServiceManager.getTheDaytimeConnectorServiceManager()
			.getConnectorService(host, IDaytimeService.class);
	}
	public void setConnectorService(IHost host, IConnectorService connectorService) {
		DaytimeConnectorServiceManager.getTheDaytimeConnectorServiceManager()
			.setConnectorService(host, IDaytimeService.class, connectorService);
	}

	public IDaytimeService createDaytimeService(IHost host) {
		DaytimeConnectorService connectorService = (DaytimeConnectorService)getConnectorService(host);
		return connectorService.getDaytimeService();
	}

	public final IService getService(IHost host) {
		IDaytimeService service = (IDaytimeService)fServices.get(host);
		if (service == null) {
			service = createDaytimeService(host);
			fServices.put(host, service);
		}
		return service;
	}
	
	public final Class getServiceType() {
		return IDaytimeService.class;
	}
	
	public Class getServiceImplType() {
		return IDaytimeService.class;
	}

}
