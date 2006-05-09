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
 * Martin Oberhuber (Wind River) - Adapted template for ssh service.
 ********************************************************************************/

package org.eclipse.rse.subsystems.shells.ssh;

import org.eclipse.rse.connectorservice.ssh.SshConnectorService;
import org.eclipse.rse.connectorservice.ssh.SshConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.model.Host;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.services.ssh.ISshService;
import org.eclipse.rse.services.ssh.ISshSessionProvider;
import org.eclipse.rse.services.ssh.shell.SshShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystemConfiguration;

public class SshShellSubSystemConfiguration extends
		ShellServiceSubSystemConfiguration {

	public SshShellSubSystemConfiguration() {
		super();
	}

	public boolean supportsCommands() {
		//TODO support commands in SshShellService.runCommand()
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#isFactoryFor(java.lang.Class)
	 */
	public boolean isFactoryFor(Class subSystemType) {
		boolean isFor = ShellServiceSubSystem.class.equals(subSystemType);
		return isFor;
	}

	/**
	 * Instantiate and return an instance of OUR subystem. 
	 * Do not populate it yet though!
	 * @see org.eclipse.rse.core.subsystems.impl.SubSystemFactoryImpl#createSubSystemInternal(Host)
	 */
	public ISubSystem createSubSystemInternal(IHost host) 
	{
		SshConnectorService connectorService = (SshConnectorService)getConnectorService(host);
		ISubSystem subsys = new ShellServiceSubSystem(host, connectorService, createShellService(host));
		return subsys;
	}

	public IShellService createShellService(IHost host) {
		SshConnectorService cserv = (SshConnectorService)getConnectorService(host);
		return new SshShellService((ISshSessionProvider)cserv);
	}

	public IConnectorService getConnectorService(IHost host) {
		return SshConnectorServiceManager.getInstance().getConnectorService(host, ISshService.class);
	}

	public void setConnectorService(IHost host,
			IConnectorService connectorService) {
		SshConnectorServiceManager.getInstance().setConnectorService(host, ISshService.class, connectorService);
	}

	public Class getServiceImplType() {
		return ISshService.class;
	}

}
