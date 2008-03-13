/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Tobias Schwarz (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.internal.testsubsystem;

import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystem;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemConfiguration;

public class TestSubSystemConfiguration extends SubSystemConfiguration implements ITestSubSystemConfiguration {

	/**
	 * Constructor.
	 */
	public TestSubSystemConfiguration() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(org.eclipse.rse.core.model.IHost)
	 */
	public ISubSystem createSubSystemInternal(IHost conn) {
		return new TestSubSystem(conn, getConnectorService(conn));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getConnectorService(org.eclipse.rse.core.model.IHost)
	 */
	public IConnectorService getConnectorService(IHost host) {
		return TestSubSystemConnectorServiceManager.getInstance().getConnectorService(host, ITestSubSystem.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createDefaultFilterPool(org.eclipse.rse.core.filters.ISystemFilterPoolManager)
	 */
	protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr) {
		ISystemFilterPool defaultPool = null;
		try {
			defaultPool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), true); // true=>is deletable by user

			String[] strings = new String[] { ".*" }; //$NON-NLS-1$

			ISystemFilter filter = mgr.createSystemFilter(defaultPool, "All", strings); //$NON-NLS-1$
			filter.setType("all"); //$NON-NLS-1$
		}
		catch (Exception exc) {
			// ignore exception
		}
		return defaultPool;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#getTranslatedFilterTypeProperty(org.eclipse.rse.core.filters.ISystemFilter)
	 */
	public String getTranslatedFilterTypeProperty(ISystemFilter selectedFilter) {
		String type = selectedFilter.getType();
		if (type.equals("all")) //$NON-NLS-1$
			return "testSubSystemFilter.all"; //$NON-NLS-1$ 
		return "testSubSystemFilter"; //$NON-NLS-1$
	}

}
