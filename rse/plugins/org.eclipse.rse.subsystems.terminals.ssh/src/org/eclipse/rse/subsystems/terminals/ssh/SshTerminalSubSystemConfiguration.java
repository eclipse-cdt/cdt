/*******************************************************************************
 * Copyright (c) 2008, 2009 MontaVista Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo      (MontaVista) - initial API and implementation
 * Anna Dushistova (MontaVista) - [240530][rseterminal][apidoc] Add terminals.rse Javadoc into org.eclipse.rse.doc.isv
 *******************************************************************************/

package org.eclipse.rse.subsystems.terminals.ssh;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.connectorservice.ssh.SshConnectorService;
import org.eclipse.rse.internal.connectorservice.ssh.SshConnectorServiceManager;
import org.eclipse.rse.internal.services.ssh.ISshService;
import org.eclipse.rse.internal.services.ssh.terminal.SshTerminalService;
import org.eclipse.rse.services.terminals.ITerminalService;
import org.eclipse.rse.subsystems.terminals.core.TerminalServiceSubSystem;
import org.eclipse.rse.subsystems.terminals.core.TerminalServiceSubSystemConfiguration;

/**
 * An SSH Terminal Subsystem Factory.
 * 
 */
public class SshTerminalSubSystemConfiguration extends
        TerminalServiceSubSystemConfiguration {

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#isFactoryFor(java.lang.Class)
     */
    public boolean isFactoryFor(Class subSystemType) {
        boolean isFor = TerminalServiceSubSystem.class.equals(subSystemType);
        return isFor;
    }

    /**
     * Instantiate and return an instance of OUR subsystem. Do not populate it
     * yet though!
     *
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(IHost)
     */
    public ISubSystem createSubSystemInternal(IHost host) {
        SshConnectorService connectorService = (SshConnectorService) getConnectorService(host);
        ISubSystem subsys = new TerminalServiceSubSystem(host,
                connectorService, createTerminalService(host));
        return subsys;
    }

	/**
	 * @inheritDoc
	 * @since 1.0
	 */
    public ITerminalService createTerminalService(IHost host) {
		SshConnectorService cserv = (SshConnectorService) getConnectorService(host);
		return new SshTerminalService(cserv);
    }

    public IConnectorService getConnectorService(IHost host) {
        return SshConnectorServiceManager.getInstance().getConnectorService(
                host, ISshService.class);
    }

    public void setConnectorService(IHost host,
            IConnectorService connectorService) {
        SshConnectorServiceManager.getInstance().setConnectorService(host,
                ISshService.class, connectorService);
    }

    public Class getServiceImplType() {
        return ISshService.class;
    }

}
