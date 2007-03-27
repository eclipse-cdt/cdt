/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService
 ********************************************************************************/
package org.eclipse.rse.core.subsystems;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * The {@link BasicCredentialsProvider} provides a simple implementation of the {@link ICredentialsProvider}
 * interface. It is suitable for use with a connector service that needs to provide
 * no special authentication to connect to its target system.
 * <p>
 * This class is not meant to be subclassed.
 */
public class BasicCredentialsProvider implements ICredentialsProvider {
	public void acquireCredentials(boolean reacquire) {
	}

	public void clearCredentials() {
	}

	public void clearPassword() {
		// TODO Auto-generated method stub
	}

	public ICredentials getCredentials() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSuppressed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void repairCredentials(SystemMessage message) throws InterruptedException {
		// TODO Auto-generated method stub
	}

	public boolean requiresPassword() {
		return false;
	}

	public boolean requiresUserId() {
		return false;
	}

	public void setPassword(String password) {
		// TODO Auto-generated method stub
	}

	public void setSuppressed(boolean suppressed) {
		// TODO Auto-generated method stub
	}

	public void setUserId(String userId) {
		// TODO Auto-generated method stub
	}

	public boolean supportsPassword() {
		return false;
	}

	public boolean supportsUserId() {
		return false;
	}

	public IConnectorService getConnectorService() {
		// TODO Auto-generated method stub
		return null;
	}
}
