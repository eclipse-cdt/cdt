/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo (MontaVista) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.subsystems.terminals.core;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.services.terminals.ITerminalService;

/**
 * An interface representing factories for creating TerminalServiceSubSystem
 * objects.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/dsdp/tm/">Target Management</a> team.
 * </p>
 */
public interface ITerminalServiceSubSystemConfiguration extends
        ISubSystemConfiguration {
	/**
	 * Return the terminal service.
	 *
	 * @param host connection
	 * @return the internal terminal service interface.
	 * @since 1.0
	 */
    public ITerminalService getTerminalService(IHost host);

	/**
	 * Create the terminal service.
	 * 
	 * @param host connection
	 * @return the new terminal service interface.
	 * @since 1.0
	 */
    public ITerminalService createTerminalService(IHost host);

}