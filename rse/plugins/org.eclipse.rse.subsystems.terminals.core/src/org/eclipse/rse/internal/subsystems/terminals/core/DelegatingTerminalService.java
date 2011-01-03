/********************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * Anna Dushistova (MontaVista) - [259412][api][rseterminal] Decide whether to extract any API from DelegatingTerminalService.
 ********************************************************************************/
package org.eclipse.rse.internal.subsystems.terminals.core;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.terminals.AbstractDelegatingTerminalService;
import org.eclipse.rse.services.terminals.ITerminalService;

/**
 * Base class that can be used for decorating an existing terminal service with
 * additional functionality. By default, all method calls are passed through to
 * the original service.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 *
 * @since org.eclipse.rse.subsystems.terminals.core 1.0
 */
public class DelegatingTerminalService extends AbstractDelegatingTerminalService {

	private IHost _host;
	private ITerminalService _realService;

	public DelegatingTerminalService(IHost host) {
		_host = host;
	}

	public ITerminalService getRealTerminalService() {
		if (_host != null && _realService == null) {
			ISubSystem[] subSystems = _host.getSubSystems();
			if (subSystems != null) {
				for (int i = 0; i < subSystems.length && _realService == null; i++) {
					ISubSystem subsys = subSystems[i];

					IService svc = subsys.getSubSystemConfiguration()
							.getService(_host);
					if (svc != null) {
						ITerminalService tsvc = (ITerminalService) svc
								.getAdapter(ITerminalService.class);
						if (tsvc != null && tsvc != this) {
							_realService = tsvc;
						}
					}
				}
			}
		}

		return _realService;
	}
}
