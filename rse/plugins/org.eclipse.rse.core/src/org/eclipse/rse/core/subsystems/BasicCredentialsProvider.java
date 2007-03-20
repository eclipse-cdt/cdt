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


/**
 * The {@link BasicCredentialsProvider} provides a simple implementation of the {@link ICredentialsProvider}
 * interface. It is suitable for use with a connector service that needs to provide
 * no special authentication to connect to its target system.
 * <p>
 * This class is not meant to be subclassed.
 */
public class BasicCredentialsProvider implements ICredentialsProvider {
	
	public boolean requiresPassword() {
		return false;
	}
	
	public boolean requiresUserId() {
		return false;
	}
	
	public boolean supportsPassword() {
		return false;
	}
	
	public boolean supportsUserId() {
		return false;
	}
	
	public void acquireCredentials(boolean reacquire) {
	}
	
	public void clearCredentials() {
	}

}
