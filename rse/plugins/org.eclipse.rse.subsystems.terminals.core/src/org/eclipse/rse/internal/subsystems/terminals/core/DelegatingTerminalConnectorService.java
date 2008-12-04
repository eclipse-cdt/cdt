/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * Anna Dushistova (MontaVista) - [239159] The shell process subsystem not working without the shells subsystem present for the systemType
 * David McKnight  (IBM)        - adapted from  DelegatingShellProcessConnectorService
 ********************************************************************************/
package org.eclipse.rse.internal.subsystems.terminals.core;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.AbstractDelegatingConnectorService;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystem;

/**
 * This class delegates the connector service requests for the terminal
 * subsystem to the connector service of any subsystem that has service which
 * can be adopted to ITerminalService.
 */
public class DelegatingTerminalConnectorService extends
		AbstractDelegatingConnectorService {
	private IConnectorService _realService;

	/**
	 * @param host
	 *            the linux host that is the target for this connector service.
	 */
	public DelegatingTerminalConnectorService(IHost host) {
		super(host);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.rse.core.subsystems.AbstractDelegatingConnectorService#
	 * getRealConnectorService()
	 */
	public IConnectorService getRealConnectorService() {
		if (_realService != null) {
			return _realService;
		} else {
			ISubSystem ss = TerminalSubSystemHelper
					.getSuitableSubSystem(getHost());
			if (ss != null) {
				_realService = ss.getConnectorService();

				// register the process subsystem
				ITerminalServiceSubSystem ts = TerminalSubSystemHelper
						.getTerminalServiceSubSystem(getHost());
				_realService.registerSubSystem(ts);
				return _realService;
			} else {
				return null;
			}
		}
	}

}
