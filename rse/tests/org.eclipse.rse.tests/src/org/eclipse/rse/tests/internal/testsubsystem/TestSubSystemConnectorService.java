/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Tobias Schwarz (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.internal.testsubsystem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.AbstractConnectorService;

public class TestSubSystemConnectorService extends AbstractConnectorService {

	private boolean connected = false;

	/**
	 * Constructor.
	 * 
	 * @param host The RSE connection object.
	 */
	public TestSubSystemConnectorService(IHost host) {
		super("TestSubSystemConnectorService", //$NON-NLS-1$
		"The connector Service for the TestSubSystem", //$NON-NLS-1$
		host, 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isConnected()
	 */
	public boolean isConnected() {
		return connected;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#internalConnect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void internalConnect(IProgressMonitor monitor) throws Exception {
		super.internalConnect(monitor);
		connected = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#internalDisconnect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void internalDisconnect(IProgressMonitor monitor) throws Exception {
		super.internalDisconnect(monitor);
		connected = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsRemoteServerLaunching()
	 */
	public boolean supportsRemoteServerLaunching() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#hasRemoteServerLauncherProperties()
	 */
	public boolean hasRemoteServerLauncherProperties() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsServerLaunchProperties()
	 */
	public boolean supportsServerLaunchProperties() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsPassword()
	 */
	public boolean supportsPassword() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsUserId()
	 */
	public boolean supportsUserId() {
		return false;
	}
}
