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

package org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem;


import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystemConfiguration;


/**
 * @lastgen interface DefaultCmdSubSystemConfiguration extends RemoteCmdSubSystemConfiguration {}
 */
public interface IShellServiceSubSystemConfiguration extends IRemoteCmdSubSystemConfiguration, IServiceSubSystemConfiguration
{
	public IShellService getShellService(IHost host);	
	public IShellService createShellService(IHost host);
	public IServiceCommandShell createRemoteCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell);

} //DefaultCmdSubSystemConfiguration