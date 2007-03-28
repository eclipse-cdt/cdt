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
 * The {@link AbstractCredentialsProvider} provides the base
 * implementation of the {@link ICredentialsProvider}
 * interface. It remembers the connector service and suppression
 * state for the provider.
 * <p>
 * This class is meant to be subclassed.
 */
public abstract class AbstractCredentialsProvider implements ICredentialsProvider {
	
	private IConnectorService connectorService = null;
	private boolean suppressed = false;
	
	public AbstractCredentialsProvider(IConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	public final IConnectorService getConnectorService() {
		return connectorService;
	}

	public final boolean isSuppressed() {
		return suppressed;
	}

	public final void setSuppressed(boolean suppressed) {
		this.suppressed = suppressed;
	}
	
	protected final boolean supportsUserId() {
		return connectorService.supportsUserId();
	}
	
	protected final boolean requiresUserId() {
		return connectorService.requiresUserId();
	}
	
	protected final boolean supportsPassword() {
		return connectorService.supportsPassword();
	}
	
	protected final boolean requiresPassword() {
		return connectorService.requiresPassword();
	}

}
