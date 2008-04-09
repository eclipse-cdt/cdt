/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - [225089][ssh][shells][api] Canceling connection leads to exception
 ********************************************************************************/
package org.eclipse.rse.core.subsystems;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rse.core.model.IHost;

/**
 * A basic connector service is one that does not require any type of
 * authentication to connect to its target system.
 * Since this is the case, many of the methods of
 * {@link IConnectorService} are implemented only in skeletal form.
 */
public abstract class BasicConnectorService extends AbstractConnectorService {

	/**
	 * Constructs a basic connector service.
	 * @param name The name of the connector service
	 * @param description The description of the connector service
	 * @param host the host associated with this connector service
	 * @param port the port used by this connector service, if IP based
	 */
	public BasicConnectorService(String name, String description, IHost host, int port) {
		super(name, description, host, port);
	}

	/**
	 * Indicates if this connector service understands passwords.
	 * This implementation always returns false.
	 * Override if necessary.
	 * @return false
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsPassword()
	 */
	public boolean supportsPassword() {
		return false;
	}

	/**
	 * Indicates if this connector service requires passwords.
	 * This implementation always returns false.
	 * Override if necessary.
	 * @return false
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#requiresPassword()
	 */
	public boolean requiresPassword() {
		return false;
	}

	/**
	 * Indicates if this connector service understands user ids.
	 * This implementation always returns false.
	 * Override if necessary.
	 * @return false
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsUserId()
	 */
	public boolean supportsUserId() {
		return false;
	}

	/**
	 * Indicates if this connector service requires a user id.
	 * This implementation always returns false.
	 * Override if necessary.
	 * @return false
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#requiresUserId()
	 */
	public boolean requiresUserId() {
		return false;
	}

	/**
	 * Acquires credentials. This implementation does nothing.
	 * 
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#acquireCredentials(boolean)
	 */
	public void acquireCredentials(boolean refresh) throws OperationCanceledException {
	}

	/**
	 * Clears credentials.
	 * This implementation does nothing.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#clearCredentials()
	 */
	public void clearCredentials() {
	}

	/**
	 * Clears a password.
	 * This implementation does nothing.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#clearPassword(boolean, boolean)
	 */
	public void clearPassword(boolean persist, boolean propagate) {
	}

	/**
	 * Gets the user id.
	 * This implementation returns null.
	 * @return null
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getUserId()
	 */
	public String getUserId() {
		return null;
	}

	/**
	 * Indicates the presence of a password.
	 * This implementation returns false.
	 * @param onDisk true if checking for a persistent form of a password
	 * @return false
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#hasPassword(boolean)
	 */
	public boolean hasPassword(boolean onDisk) {
		return false;
	}

	/**
	 * Indicates if this connector service can inherit its credentials from others.
	 * This implementation returns false.
	 * @return false
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#inheritsCredentials()
	 */
	public boolean inheritsCredentials() {
		return false;
	}

	/**
	 * Indicates if this connector service is currently being suppressed.
	 * This implementation returns false.
	 * @return false
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isSuppressed()
	 */
	public boolean isSuppressed() {
		return false;
	}

	/**
	 * Removes the persistent form of a password.
	 * This implementation does nothing.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#removePassword()
	 */
	public void removePassword() {
	}

	/**
	 * Removes the persistent form of the default user id.
	 * This implementation does nothing.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#removeUserId()
	 */
	public void removeUserId() {
	}

	/**
	 * Saves the remembered password persistently.
	 * This implementation does nothing.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#savePassword()
	 */
	public void savePassword() {
	}

	/**
	 * Saves the remembered user id persistently.
	 * This implementation does nothing.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#saveUserId()
	 */
	public void saveUserId() {
	}

	/**
	 * Sets the password for a particular user id and optionally persists it.
	 * This implementation does nothing.
	 * 
	 * @param matchingUserId the user id to set the password for
	 * @param password the password to set.
	 * @param persist true if this is to be persisted.
	 * @param propagate true if this password should be propagated to other
	 *            connector services.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setPassword(java.lang.String,
	 *      java.lang.String, boolean, boolean)
	 */
	public void setPassword(String matchingUserId, String password, boolean persist, boolean propagate) {
	}

	/**
	 * Indicates if credentials are shared with other connector services.
	 * This implementation returns false.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#sharesCredentials()
	 */
	public boolean sharesCredentials() {
		return false;
	}

	/**
	 * Sets the suppressed state of this connector service.
	 * This implementation does nothing.
	 * @param suppress true if this connector service should be suppressed.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setSuppressed(boolean)
	 */
	public void setSuppressed(boolean suppress) {
	}

	/**
	 * Sets the user id for this connector service.
	 * This implementation does nothing.
	 * @param userId the user id to set for this service.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setUserId(java.lang.String)
	 */
	public void setUserId(String userId) {
	}

}