/********************************************************************************
 * Copyright (c) 2009 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.examples.dstore.subsystems;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorServiceManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.examples.dstore.services.SampleService;
import org.eclipse.rse.services.dstore.IDStoreService;

public class SampleSubSystemConfiguration extends SubSystemConfiguration {


	protected Map _services = new HashMap();
	
	public ISubSystem createSubSystemInternal(IHost conn) {
		
		return new SampleSubSystem(conn, getConnectorService(conn), getSampleService(conn));
	}
	
	public IConnectorService getConnectorService(IHost host) {
		return DStoreConnectorServiceManager.getInstance().getConnectorService(host, getServiceImplType());
	}
	
	public SampleService getSampleService(IHost host) {
		SampleService service = (SampleService)_services.get(host);
		if (service == null)
		{
			DStoreConnectorService connectorService = (DStoreConnectorService)getConnectorService(host);
			service = new SampleService(connectorService);
			_services.put(host, service);
		}
		return service;		
	}

	public boolean supportsFilters() {
		return false;
	}
	
	public Class getServiceImplType(){
		return IDStoreService.class;
	}
	


}
