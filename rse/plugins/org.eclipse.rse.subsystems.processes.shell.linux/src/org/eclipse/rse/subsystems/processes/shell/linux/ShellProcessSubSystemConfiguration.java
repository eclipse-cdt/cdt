/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Yu-Fen Kuo (MontaVista) - adapted from RSE ProcessServiceSubSystemConfiguration
 * Martin Oberhuber (Wind River) - [refactor] "shell" instead of "ssh" everywhere
 * Martin Oberhuber (Wind River) - [186523] Move subsystemConfigurations from UI to core
 * Anna Dushistova  (MontaVista) - [239159] The shell process subsystem not working without the shells subsystem present for the systemType
 *******************************************************************************/

package org.eclipse.rse.subsystems.processes.shell.linux;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.subsystems.processes.shell.linux.Activator;
import org.eclipse.rse.internal.subsystems.processes.shell.linux.DelegatingShellProcessConnectorService;
import org.eclipse.rse.internal.subsystems.processes.shell.linux.LinuxShellProcessService;
import org.eclipse.rse.internal.subsystems.processes.shell.linux.ShellProcessAdapter;
import org.eclipse.rse.services.processes.IProcessService;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter;
import org.eclipse.rse.subsystems.processes.servicesubsystem.ProcessServiceSubSystem;
import org.eclipse.rse.subsystems.processes.servicesubsystem.ProcessServiceSubSystemConfiguration;

/**
 * This class is used by org.eclipse.rse.core.subsystemConfigurations extension
 * that defines the process subsystem using ssh protocol on linux remote
 * targets.
 * 
 */
public class ShellProcessSubSystemConfiguration extends
        ProcessServiceSubSystemConfiguration {
    protected IHostProcessToRemoteProcessAdapter hostProcessAdapter;

    public ISubSystem createSubSystemInternal(IHost conn) {
    	IConnectorService connectorService = getConnectorService(conn);
        ISubSystem subsys = new ProcessServiceSubSystem(conn, connectorService,
                getProcessService(conn), getHostProcessAdapter());
        return subsys;
    }

    public IHostProcessToRemoteProcessAdapter getHostProcessAdapter() {
        if (hostProcessAdapter == null) {
            hostProcessAdapter = new ShellProcessAdapter();
        }
        return hostProcessAdapter;
    }

    public IConnectorService getConnectorService(IHost host) 
    {
    	ISubSystem ss = Activator.getSuitableSubSystem(host);
    	if (ss!=null) 
    	{
    		return ss.getConnectorService();
    	}
    	else
    	{
    		return new DelegatingShellProcessConnectorService(host);
    	} 
    }
    
    public Class getServiceImplType() {
        return IShellService.class;
    }

    public void setConnectorService(IHost host, IConnectorService connectorService) {
    	//Nothing to do here since we just re-use the existing IShellServiceSubSystem
    }

    public IProcessService createProcessService(IHost host) {
        return new LinuxShellProcessService(host);
    }

}
