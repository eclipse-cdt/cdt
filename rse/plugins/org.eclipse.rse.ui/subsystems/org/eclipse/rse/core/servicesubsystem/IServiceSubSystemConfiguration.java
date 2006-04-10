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

package org.eclipse.rse.core.servicesubsystem;


import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.IService;



/**
 * This is the interface for the class that is for subsystem-providers who desire not to use MOF.
 * 
 * @lastgen interface DefaultSubSystemFactory extends SubSystemFactory {}
 */
public interface IServiceSubSystemConfiguration extends ISubSystemConfiguration
{
	public IConnectorService getConnectorService(IHost host);
	public void setConnectorService(IHost host, IConnectorService connectorService);
	public Class getServiceType();
	public Class getServiceImplType();
	public IService getService(IHost host);
	
} //DefaultSubSystemFactory