/********************************************************************************
 * Copyright (c) 2008, 2009 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo       (MontaVista) - initial API and implementation
 * Anna Dushistova  (MontaVista) - [227569] [rseterminal][api] Provide a "generic" Terminal subsystem
 * Anna Dushistova  (MontaVista) - [240530] [rseterminal][apidoc] Add terminals.rse Javadoc into org.eclipse.rse.doc.isv
 ********************************************************************************/

package org.eclipse.rse.subsystems.terminals.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.terminals.ITerminalService;

/**
 * The implementation of the ITerminalServiceSubSystemConfiguration interface
 * contains information about what features the subsystem supports, and creates
 * TerminalServiceSubSystem instances.
 * 
 */
public abstract class TerminalServiceSubSystemConfiguration extends
		SubSystemConfiguration implements
		ITerminalServiceSubSystemConfiguration {

	private Map _services;

	protected TerminalServiceSubSystemConfiguration() {
		super();
		_services = new HashMap();
	}

	public boolean supportsFilters() {
		return false;
	}

	/**
	 * @inheritDoc
	 * @since 1.0
	 */
	public final ITerminalService getTerminalService(IHost host) {
		ITerminalService service = (ITerminalService) _services.get(host);
		if (service == null) {
			service = createTerminalService(host);
			_services.put(host, service);
		}
		return service;
	}

	public final IService getService(IHost host) {
		return getTerminalService(host);
	}

	public Class getServiceType() {
		return ITerminalService.class;
	}

	public boolean isFactoryFor(Class subSystemType) {
		boolean isFor = TerminalServiceSubSystem.class.equals(subSystemType);
		return isFor;
	}

}
