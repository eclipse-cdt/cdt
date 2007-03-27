/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 ********************************************************************************/
package org.eclipse.rse.core.subsystems;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;

public interface ICredentialsProvider {
	
	void acquireCredentials(boolean reacquire) throws InterruptedException;
	
	void clearCredentials();
	
	void clearPassword();
	
	ICredentials getCredentials();
	
	String getUserId();
	
	boolean isSuppressed();
	
	void repairCredentials(SystemMessage message)throws InterruptedException;
	
	boolean requiresPassword();
	
	boolean requiresUserId();
	
	void setPassword(String password);
	
	void setSuppressed(boolean suppressed);
	
	void setUserId(String userId);
	
	boolean supportsPassword();
	
	boolean supportsUserId();
	
	IConnectorService getConnectorService();
	
}
