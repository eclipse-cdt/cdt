/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 ********************************************************************************/
package org.eclipse.rse.ui.subsystems;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.core.subsystems.AuthenticatingConnectorService;
import org.eclipse.rse.core.subsystems.ICredentials;


/**
 * A standard connector service is an authenticating connector service
 * (see {@link AuthenticatingConnectorService}) that understand and prompts for
 * user ids and passwords. It uses a standard credentials provider 
 * (see {@link StandardCredentialsProvider}) to do so.
 */
public abstract class StandardConnectorService extends AuthenticatingConnectorService {
	
	/**
	 * Construct a standard connector service. This also constructs
	 * and uses a standard credentials provider.
	 * @param name the name of the connector service
	 * @param description the description of the connector service
	 * @param host the host associated with this connector service
	 * @param port the port used by this connector service, if IP based
	 */
	public StandardConnectorService(String name, String description, IHost host, int port) {
		super(name, description, host, port);
		setCredentialsProvider(new StandardCredentialsProvider(this));
	}
	
	/**
	 * Determines if this connector service understand the concept of a password.
	 * This implementation always returns true.
	 * Override if necessary.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsPassword()
	 * @return true
	 */
	public boolean supportsPassword() {
		return true;
	}
	
	/**
	 * Test if this connector service requires a password.
	 * This implementation always returns true.
	 * Override if necessary.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#requiresPassword()
	 * @return true
	 */
	public boolean requiresPassword() {
		return true;
	}
	
	/**
	 * Reports if this connector service can use a user identifier.
	 * This implementation always returns true.
	 * Override if necessary.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsUserId()
	 * @return true
	 */
	public boolean supportsUserId() {
		return true;
	}
	
	/**
	 * Test if this connector service requires a user id.
	 * This implementation always returns true.
	 * Override if necessary.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#requiresUserId()
	 * @return true
	 */
	public boolean requiresUserId() {
		return true;
	}

	/**
	 * @return the SystemSignonInformation constructed from the
	 * credentials provider.
	 */
	protected final SystemSignonInformation getSignonInformation() {
		SystemSignonInformation result = null;
		ICredentials credentials = credentialsProvider.getCredentials();
		result = (SystemSignonInformation) credentials;
		return result;
	}
	
}