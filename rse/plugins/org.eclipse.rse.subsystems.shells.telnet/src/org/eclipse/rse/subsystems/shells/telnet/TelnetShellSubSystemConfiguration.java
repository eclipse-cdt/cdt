/*******************************************************************************
 *  Copyright (c) 2006, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Initial Contributors:
 *  The following IBM employees contributed to the Remote System Explorer
 *  component that contains this file: David McKnight, Kushal Munir, 
 *  Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 *  Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 *  Contributors:
 *  Martin Oberhuber (Wind River) - Adapted template for ssh service.
 *  Sheldon D'souza  (Celunite)   - Adapted template for telnet service
 *  Anna Dushistova  (MontaVista) - [240523] [rseterminals] Provide a generic adapter factory that adapts any ITerminalService to an IShellService
 *******************************************************************************/
package org.eclipse.rse.subsystems.shells.telnet;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.connectorservice.telnet.TelnetConnectorService;
import org.eclipse.rse.internal.connectorservice.telnet.TelnetConnectorServiceManager;
import org.eclipse.rse.internal.services.telnet.ITelnetService;
import org.eclipse.rse.internal.services.telnet.terminal.TelnetTerminalService;
import org.eclipse.rse.internal.subsystems.shells.telnet.TelnetServiceCommandShell;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IServiceCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystemConfiguration;

public class TelnetShellSubSystemConfiguration extends
		ShellServiceSubSystemConfiguration {

	public TelnetShellSubSystemConfiguration() {
		super();
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
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(IHost)
	 */
	public ISubSystem createSubSystemInternal(IHost host) 
	{
		TelnetConnectorService connectorService = (TelnetConnectorService)getConnectorService(host);
		ISubSystem subsys = new ShellServiceSubSystem(host, connectorService, createShellService(host));
		return subsys;
	}

	public IShellService createShellService(IHost host) {
		TelnetConnectorService cserv = (TelnetConnectorService)getConnectorService(host);
		return (IShellService) (new TelnetTerminalService(cserv)).getAdapter(IShellService.class);
	}

	public IConnectorService getConnectorService(IHost host) {
		return TelnetConnectorServiceManager.getInstance().getConnectorService(host, ITelnetService.class);
	}

	public void setConnectorService(IHost host,
			IConnectorService connectorService) {
		TelnetConnectorServiceManager.getInstance().setConnectorService(host, ITelnetService.class, connectorService);
	}

	public Class getServiceImplType() {
		return ITelnetService.class;
	}

	public IServiceCommandShell createRemoteCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell) {		
		return new TelnetServiceCommandShell(cmdSS, hostShell);
	}

}
