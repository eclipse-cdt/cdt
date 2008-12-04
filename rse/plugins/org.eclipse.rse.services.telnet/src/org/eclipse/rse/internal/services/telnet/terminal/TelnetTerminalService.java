/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Anna Dushistova  (MontaVista) - adapted from SshTerminalService
 * Anna Dushistova  (MontaVista) - [240523] [rseterminals] Provide a generic adapter factory that adapts any ITerminalService to an IShellService
 *******************************************************************************/

package org.eclipse.rse.internal.services.telnet.terminal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.rse.internal.services.shells.TerminalShellService;
import org.eclipse.rse.internal.services.telnet.ITelnetService;
import org.eclipse.rse.internal.services.telnet.ITelnetSessionProvider;
import org.eclipse.rse.internal.services.telnet.TelnetServiceResources;
import org.eclipse.rse.internal.services.terminals.AbstractTerminalService;
import org.eclipse.rse.internal.services.terminals.ITerminalShell;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IShellService;

/**
 * A Terminal Service for telnet.
 * @since 1.2
 */
public class TelnetTerminalService extends AbstractTerminalService implements ITelnetService {

	private final ITelnetSessionProvider fSessionProvider;
	private IShellService fRelatedShellService;

    public TelnetTerminalService(ITelnetSessionProvider sessionProvider) {
    	fSessionProvider = sessionProvider;
    }

    public ITelnetSessionProvider getSessionProvider() {
		return fSessionProvider;
	}

    public ITerminalShell launchTerminal(String ptyType, String encoding, String[] environment, String initialWorkingDirectory, String commandToRun,
			IProgressMonitor monitor) throws SystemMessageException {
		TelnetTerminalShell hostShell = new TelnetTerminalShell(fSessionProvider, ptyType, encoding, environment, initialWorkingDirectory, commandToRun);
        return hostShell;
    }

    /**
	 * Return an RSE IShellService related to this Terminal Service.
	 */
	protected synchronized IShellService getRelatedShellService() {
		if (fRelatedShellService == null) {
			fRelatedShellService = new TerminalShellService(this);
		}
		return fRelatedShellService;
	}

	/**
	 * Adapt this terminal service to different (potentially contributed)
	 * interfaces, in order to provide additional functionality.
	 *
	 * Asks the adapter manager first whether it got any contributed adapter; if
	 * none is found contributed externally, try to adapt to an SshShellService.
	 * That way, clients can easily convert this ITerminalService into an
	 * IShellService:
	 *
	 * <pre>
	 * IShellService ss = (IShellService) myTerminalService.getAdapter(IShellService.class);
	 * </pre>
	 *
	 * @see IAdaptable
	 * @see PlatformObject#getAdapter(Class)
	 */
    public Object getAdapter(Class adapter) {
		// TODO I'm not sure if this is the right way doing things. First of
		// all, we're holding on to the created terminal service forever if
		// we're asked for it, thus needing extra memory.
		// Second, by asking the adapter manager first, we might get no chance
		// returning what we think is right.
    	Object o = super.getAdapter(adapter);
    	if (o==null && adapter.isAssignableFrom(IShellService.class)) {
    		return getRelatedShellService();
		}
		return o;
	}

	public String getName() {
        return TelnetServiceResources.TelnetShellService_Name;
    }

    public String getDescription() {
        return TelnetServiceResources.TelnetShellService_Description;
    }

}
