/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.internal.discovery;

import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;

/**
 * Configuration for an empty Discovery SubSystemConfiguration
 * to allow listing discovery in the RSE Wizard
 * 
 */
public class ServiceDiscoverySubSystemConfiguration extends SubSystemConfiguration {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createDefaultFilterPool(org.eclipse.rse.core.filters.ISystemFilterPoolManager)
	 */
	protected ISystemFilterPool createDefaultFilterPool(
			ISystemFilterPoolManager mgr) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(org.eclipse.rse.core.model.IHost)
	 */
	public ISubSystem createSubSystemInternal(IHost conn) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchProperties(org.eclipse.rse.core.model.IHost)
	 */
	public boolean supportsServerLaunchProperties(IHost host) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getConnectorService(org.eclipse.rse.core.model.IHost)
	 */
	public IConnectorService getConnectorService(IHost host) {
		return null;
	}

}
