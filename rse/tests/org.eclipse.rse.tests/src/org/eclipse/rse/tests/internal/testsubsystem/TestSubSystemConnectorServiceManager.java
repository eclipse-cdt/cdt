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

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystem;

/**
 * Test subsystem connector service manager. Singleton!
 */
public class TestSubSystemConnectorServiceManager extends AbstractConnectorServiceManager {

	private static TestSubSystemConnectorServiceManager inst;

	/**
	 * Private Constructor.
	 */
	private TestSubSystemConnectorServiceManager() {
		super();
	}

	/**
	 * Returns the test subsystem connector service manager instance.
	 * 
	 * @return The singleton instance.
	 */
	public static TestSubSystemConnectorServiceManager getInstance() {
		if (inst == null)
			inst = new TestSubSystemConnectorServiceManager();
		return inst;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#createConnectorService(org.eclipse.rse.core.model.IHost)
	 */
	public IConnectorService createConnectorService(IHost host) {
		return new TestSubSystemConnectorService(host);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#sharesSystem(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public boolean sharesSystem(ISubSystem otherSubSystem) {
		return (otherSubSystem instanceof ITestSubSystem);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#getSubSystemCommonInterface(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public Class getSubSystemCommonInterface(ISubSystem subsystem) {
		return ITestSubSystem.class;
	}
}
