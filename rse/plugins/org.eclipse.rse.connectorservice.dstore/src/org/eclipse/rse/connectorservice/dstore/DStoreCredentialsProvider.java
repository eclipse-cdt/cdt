/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService
 ********************************************************************************/

package org.eclipse.rse.connectorservice.dstore;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.ui.subsystems.StandardCredentialsProvider;

public class DStoreCredentialsProvider extends StandardCredentialsProvider {
	
	public DStoreCredentialsProvider(DStoreConnectorService connectorService) {
		super(connectorService);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsPassword()
	 */
	public boolean supportsPassword() {
		boolean result = super.supportsPassword();
		IHost host = getConnectorService().getHost();
		String systemType = host.getSystemType();
		if (systemType.equals(IRSESystemType.SYSTEMTYPE_WINDOWS)) {
			result = false;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsUserid()
	 */
	public boolean supportsUserId() {
		boolean result = super.supportsUserId();
		IHost host = getConnectorService().getHost();
		String systemType = host.getSystemType();
		if (systemType.equals(IRSESystemType.SYSTEMTYPE_WINDOWS)) {
			result = false;
		}
		return result;
	}
}
