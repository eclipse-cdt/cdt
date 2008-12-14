/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.services.ssh.terminal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.rse.internal.services.ssh.ISshService;
import org.eclipse.rse.internal.services.ssh.ISshSessionProvider;
import org.eclipse.rse.internal.services.ssh.SshServiceResources;
import org.eclipse.rse.internal.services.ssh.shell.SshShellService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.terminals.AbstractTerminalService;
import org.eclipse.rse.services.terminals.ITerminalShell;

/**
 * A Terminal Service for ssh.
 */
public class SshTerminalService extends AbstractTerminalService implements ISshService {

	private final ISshSessionProvider fSessionProvider;
	private SshShellService fRelatedShellService;

    public SshTerminalService(ISshSessionProvider sessionProvider) {
    	fSessionProvider = sessionProvider;
    }

    public ISshSessionProvider getSessionProvider() {
		return fSessionProvider;
	}

    public ITerminalShell launchTerminal(String ptyType, String encoding, String[] environment, String initialWorkingDirectory, String commandToRun,
			IProgressMonitor monitor) throws SystemMessageException {
		SshTerminalShell hostShell = new SshTerminalShell(getSessionProvider(), ptyType, encoding, environment, initialWorkingDirectory, commandToRun);
        return hostShell;
    }

    /**
	 * Return an RSE IShellService related to this Terminal Service.
	 */
	protected synchronized SshShellService getRelatedShellService() {
		if (fRelatedShellService == null) {
			fRelatedShellService = new SshShellService(getSessionProvider());
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
    	if (o==null && adapter.isAssignableFrom(SshShellService.class)) {
    		return getRelatedShellService();
		}
		return o;
	}

	public String getName() {
        return SshServiceResources.SshTerminalService_Name;
    }

    public String getDescription() {
        return SshServiceResources.SshTerminalService_Description;
    }

}
