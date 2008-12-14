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

public interface ITerminalServiceSubSystemConfiguration extends
        ISubSystemConfiguration {
    public ITerminalService getTerminalService(IHost host);

    public ITerminalService createTerminalService(IHost host);

}