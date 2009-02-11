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
 * Anna Dushistova (MontaVista) - extracted from DelegatingTerminalService
 ********************************************************************************/
package org.eclipse.rse.services.terminals;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.internal.services.RSEServicesMessages;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * Base class that can be used for decorating an existing terminal service with
 * additional functionality. By default, all method calls are passed through to
 * the original service.
 * 
 * @since 3.1
 */
public abstract class AbstractDelegatingTerminalService extends AbstractTerminalService {

	public abstract ITerminalService getRealTerminalService(); 

	public ITerminalShell launchTerminal(String ptyType, String encoding,
			String[] environment, String initialWorkingDirectory,
			String commandToRun, IProgressMonitor monitor)
			throws SystemMessageException {
		return getRealTerminalService().launchTerminal(ptyType, encoding, environment,
				initialWorkingDirectory, commandToRun, monitor);
	}

	public String getDescription() {
		return RSEServicesMessages.AbstractDelegatingTerminalService_description;
	}

	public String getName() {
		return RSEServicesMessages.AbstractDelegatingTerminalService_name;
	}

	public void initService(IProgressMonitor monitor) {
		getRealTerminalService().initService(monitor);
	}

	public void uninitService(IProgressMonitor monitor) {
		getRealTerminalService().uninitService(monitor);
	}

	public Object getAdapter(Class adapter) {
		return getRealTerminalService().getAdapter(adapter);
	}

}
