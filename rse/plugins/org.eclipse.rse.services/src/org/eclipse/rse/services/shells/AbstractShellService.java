/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [226301][api] IShellService should throw SystemMessageException on error
 *******************************************************************************/

package org.eclipse.rse.services.shells;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.AbstractService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * Abstract base class for RSE Shell Service implementations.
 * 
 * @since org.eclipse.rse.services 3.0
 */
public abstract class AbstractShellService extends AbstractService implements IShellService {

	private static final String[] EMPTY_ARRAY = new String[0];

	/**
	 * Return an empty host environment. Extenders should override this method
	 * if they are able to return environment on the remote side. If they do not
	 * implement this feature, they must not override this method.
	 *
	 * @see IShellService#getHostEnvironment()
	 */
	public String[] getHostEnvironment() throws SystemMessageException {
		// not implemented by default
		// TODO SSH https://bugs.eclipse.org/bugs/show_bug.cgi?id=162018
		return EMPTY_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IShellService#launchShell(java.lang.String, java.lang.String[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IHostShell launchShell(String initialWorkingDirectory, String[] environment, IProgressMonitor monitor) throws SystemMessageException {
		return launchShell(initialWorkingDirectory, null, environment, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IShellService#runCommand(java.lang.String, java.lang.String, java.lang.String[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IHostShell runCommand(String initialWorkingDirectory, String command, String[] environment, IProgressMonitor monitor) throws SystemMessageException {
		return runCommand(initialWorkingDirectory, command, null, environment, monitor);
	}

}
