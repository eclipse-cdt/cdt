/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService
 ********************************************************************************/

package org.eclipse.rse.ui.subsystems;

import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ICredentialsProvider;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SuperAbstractConnectorService;

/**
 * The {@link StandardCredentialsProvider} is an implementation of
 * {@link ICredentialsProvider} that provides for the prompting of a userid
 * and password.
 * <p>
 * It uses a {@link PasswordPersistenceManager} to store the passwords in the
 * keychain keyed by {@link IHost} and possibly by {@link ISubSystemConfiguration}.
 * <p>
 * This is suitable for use by subclasses of {@link SuperAbstractConnectorService}
 * that wish to provide prompting and persistence for userids and passwords when
 * connecting.
 * <p>
 * This class is may be subclassed. Typically to provide connector service
 * specific prompting.
 */
public class StandardCredentialsProvider implements ICredentialsProvider {

	private IConnectorService connectorService = null;
	
	public StandardCredentialsProvider(IConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	public boolean requiresPassword() {
		return true;
	}

	public boolean requiresUserId() {
		return true;
	}

	public boolean supportsPassword() {
		return true;
	}

	public boolean supportsUserId() {
		return true;
	}

	protected final IHost getHost() {
		return connectorService.getHost();
	}
	
	public void acquireCredentials(boolean reacquire) {
		// TODO Auto-generated method stub
	}
	
	public void clearCredentials() {
		// TODO Auto-generated method stub
	}
	
}
