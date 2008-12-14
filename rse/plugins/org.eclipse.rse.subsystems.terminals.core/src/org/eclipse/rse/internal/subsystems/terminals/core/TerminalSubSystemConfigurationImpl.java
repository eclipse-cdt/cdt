/*******************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo      (MontaVista) - initial API and implementation
 * Anna Dushistova (MontaVista) - adapted from SshTerminalSubsystemConfiguration
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.terminals.core;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.terminals.ITerminalService;
import org.eclipse.rse.subsystems.terminals.core.TerminalServiceSubSystem;
import org.eclipse.rse.subsystems.terminals.core.TerminalServiceSubSystemConfiguration;

public class TerminalSubSystemConfigurationImpl extends
		TerminalServiceSubSystemConfiguration {

	/**
	 * Instantiate and return an instance of OUR subsystem. Do not populate it
	 * yet though!
	 * 
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(IHost)
	 */
	public ISubSystem createSubSystemInternal(IHost host) {
		IConnectorService connectorService = getConnectorService(host);
		ISubSystem subsys = new TerminalServiceSubSystem(host,
				connectorService, getTerminalService(host));
		return subsys;
	}

	public ITerminalService createTerminalService(IHost host) {

		ISubSystem ss = TerminalSubSystemHelper.getSuitableSubSystem(host);
		if (ss != null) {
			return (ITerminalService) (ss.getSubSystemConfiguration()
					.getService(host)).getAdapter(ITerminalService.class);
		} else {
			return new DelegatingTerminalService(host);
		}

	}

	public void setConnectorService(IHost host,
			IConnectorService connectorService) {
		// SshConnectorServiceManager.getInstance().setConnectorService(host,
		// ISshService.class, connectorService);
		// Nothing to do here since we just re-use the existing suitable
		// subsystem
	}

	public IConnectorService getConnectorService(IHost host) {
		ISubSystem ss = TerminalSubSystemHelper.getSuitableSubSystem(host);
		if (ss != null) {
			return ss.getConnectorService();
		} else {
			return new DelegatingTerminalConnectorService(host);
		}
	}
}
