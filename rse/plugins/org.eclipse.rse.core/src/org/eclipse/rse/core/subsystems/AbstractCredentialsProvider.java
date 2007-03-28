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
	
	/**
	 * Create a credentials provider for a particular connector service.
	 * Subclasses should implement their own constuctors but invoke this constructor
	 * in them.
	 * @param connectorService the associatated connector service.
	 */
	public AbstractCredentialsProvider(IConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICredentialsProvider#getConnectorService()
	 */
	public final IConnectorService getConnectorService() {
		return connectorService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICredentialsProvider#isSuppressed()
	 */
	public final boolean isSuppressed() {
		return suppressed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICredentialsProvider#setSuppressed(boolean)
	 */
	public final void setSuppressed(boolean suppressed) {
		this.suppressed = suppressed;
	}
	
	/**
	 * @return true if the associated connector service supports user ids.
	 */
	protected final boolean supportsUserId() {
		return connectorService.supportsUserId();
	}
	
	/**
	 * @return true if the associated connector service requires a user id.
	 */
	protected final boolean requiresUserId() {
		return connectorService.requiresUserId();
	}
	
	/**
	 * @return true if the associated connector service supports a password.
	 */
	protected final boolean supportsPassword() {
		return connectorService.supportsPassword();
	}
	
	/**
	 * @return true if the associated connector service requires a password.
	 */
	protected final boolean requiresPassword() {
		return connectorService.requiresPassword();
	}

}
