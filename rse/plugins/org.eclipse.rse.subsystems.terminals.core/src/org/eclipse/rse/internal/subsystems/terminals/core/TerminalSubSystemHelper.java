/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.internal.subsystems.terminals.core;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.services.terminals.ITerminalService;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystem;

/**
 * Helper class that helps to get subsystem with service that can be adapted to
 * ITerminalService most of the code
 * 
 */
public class TerminalSubSystemHelper {
	/**
	 * Find the first ITerminalServiceSubSystem service associated with the
	 * host.
	 * 
	 * @param host
	 *            the connection
	 * @return shell service subsystem, or <code>null</code> if not found.
	 */
	public static ISubSystem getSuitableSubSystem(IHost host) {
		if (host == null)
			return null;
		ISubSystem[] subSystems = host.getSubSystems();
		ITerminalService ssvc = null;
		for (int i = 0; subSystems != null && i < subSystems.length; i++) {
			IService svc = subSystems[i].getSubSystemConfiguration()
					.getService(host);
			if (svc != null) {
				ssvc = (ITerminalService) svc
						.getAdapter(ITerminalService.class);
				if (ssvc != null) {
					return subSystems[i];
				}
			}
		}
		return null;
	}

	/**
	 * Returns ITerminalServiceSubSystem associated with the host.
	 * 
	 * @param host
	 *            the connection
	 * @return shell service subsystem, or <code>null</code> if not found.
	 */
	public static ITerminalServiceSubSystem getTerminalServiceSubSystem(
			IHost host) {
		if (host == null)
			return null;
		ISubSystem[] subSystems = host.getSubSystems();
		for (int i = 0; subSystems != null && i < subSystems.length; i++) {
			if (subSystems[i] instanceof ITerminalServiceSubSystem) {
				return (ITerminalServiceSubSystem) subSystems[i];
			}
		}
		return null;
	}

}
