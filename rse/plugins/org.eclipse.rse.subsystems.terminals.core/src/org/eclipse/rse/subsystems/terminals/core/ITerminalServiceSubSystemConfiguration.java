/********************************************************************************
 * Copyright (c) 2008, 2009 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo      (MontaVista) - initial API and implementation
 * Anna Dushistova (MontaVista) - [240530][rseterminal][apidoc] Add terminals.rse Javadoc into org.eclipse.rse.doc.isv
 ********************************************************************************/

package org.eclipse.rse.subsystems.terminals.core;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.services.terminals.ITerminalService;

/**
 * An interface representing factories for creating TerminalServiceSubSystem
 * objects.
 *
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