/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.services.shells;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.AbstractService;

/**
 * Abstract base class for RSE Shell Service implementations.
 */
public abstract class AbstractShellService extends AbstractService implements IShellService {

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IShellService#getHostEnvironment()
	 */
	public String[] getHostEnvironment() {
		// not implemented by default
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IShellService#launchShell(java.lang.String, java.lang.String[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IHostShell launchShell(String initialWorkingDirectory, String[] environment, IProgressMonitor monitor) {
		return launchShell(initialWorkingDirectory, null, environment, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IShellService#runCommand(java.lang.String, java.lang.String, java.lang.String[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IHostShell runCommand(String initialWorkingDirectory, String command, String[] environment, IProgressMonitor monitor) {
		return runCommand(initialWorkingDirectory, command, null, environment, monitor);
	}

}
