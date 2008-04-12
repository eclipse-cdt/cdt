/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * David Dykstal (IBM) - [225089][ssh][shells][api] Canceling connection leads to exception
 ********************************************************************************/
package org.eclipse.rse.core.subsystems;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * A credentials provider provides credentials to a connector service.
 * Every authenticating connector service has its own credentials provider, 
 * usually created when that connector service is created.
 * <p>
 * Credentials will usually consist of a user id and password
 * but may be something else in which case implementers are free 
 * change the user id and password methods to something that makes
 * sense for their case.
 * <p>
 * A provider may be in a suppressed state, in which case it will
 * prohibit the acquisition of credentials.
 * @see ICredentials
 * @see IConnectorService
 * @see AuthenticatingConnectorService
 */
public interface ICredentialsProvider {
	
	/**
	 * Causes the provider to create a set of credentials for use by a 
	 * connector service in order to connect to a target host system.
	 * This may be done by presenting a dialog or by retrieving remembered
	 * values.
	 * @param reacquire true if the provider should refresh any remembered
	 * credentials. Typically used to force the showing of a dialog containing
	 * remembered credentials.
	 * @throws OperationCanceledException if the acquisition of credentials is 
	 * cancelled by the user, if the provider is in suppressed state,
	 * a resource (such as the workbench) not being available, 
	 * or interrupted by some other means.
	 * @since 3.0 throws OperationCanceledException instead of InterruptedException
	 */
	void acquireCredentials(boolean reacquire) throws OperationCanceledException;
	
	/**
	 * The connector service using this provider may find the credentials provided
	 * are incorrector or expired. This method asks the provider to repair those
	 * credentials. This would typically be used to present a dialog asking for the reentry of 
	 * an expired password.
	 * @param message the message indicating the nature of the damage that must
	 * be repaired. For example, indicating expiration of a password.
	 * @throws OperationCanceledException if the repair is cancelled for some reason. This could
	 * include the inability of a credentials provider to open a dialog or a dialog being
	 * cancelled.
	 * @since 3.0 throws OperationCanceledException instead of InterruptedException
	 */
	void repairCredentials(SystemMessage message)throws OperationCanceledException;

	/**
	 * Clears the credentials known by this credentials provider. This will 
	 * cause a reacquistion of all compoenents of the credentials at the next
	 * acquire. If credentials consist of a user id and password then both of those
	 * are cleared.
	 * Does not clear any persistently remembered values.
	 */
	void clearCredentials();
	
	/**
	 * Retrieves the credentials known to this credentials provider. Does not
	 * cause the credentials to be acquired. May be used after {@link #acquireCredentials(boolean)}
	 * to retrieve credentials.
	 * @return the credentials that have previously been acquired or repaired.
	 * May be null if no credentials have yet been acquired.
	 */
	ICredentials getCredentials();
	
	/**
	 * If the credentials include a password or equivalent then clears only that
	 * that portion of the credentials. If the credentials do not include a password
	 * then the implementation may somehow invalidate the credentials so that 
	 * they will be reacquired at the next acquisition.
	 */
	void clearPassword();

	/**
	 * If the credentials include a password or equivalent then set that password
	 * to the new value.
	 * If the credentials do not include a password then
	 * the implementation may ignore this.
	 * @param password the new value of the password in the credentials held by this provider
	 */
	void setPassword(String password);

	/**
	 * If the credentials include a user id then set that user id to the new value.
	 * If the credentials do not include a user id then this is implementation 
	 * defined.
	 * @param userId the user id to place into the credentials held by this provider
	 */
	void setUserId(String userId);

	/**
	 * If the credentials include a user id then retrieve that user id.
	 * If the credentials do not currently contain a user id then a default user id
	 * related to the connector service may be obtained.
	 * If the credentials do not support a user id then this should return null.
	 * @return the userid of the credentials held by this provider
	 */
	String getUserId();
	
	/**
	 * Retrieves the suppression state of the provider.
	 * Acquisition may be suppressed for a period of time after a previous attempt.
	 * This is to provide client control of the acquisition policy.
	 * If true then {@link #acquireCredentials(boolean)} will immediately cancel when invoked.
	 * @return true if the provider is suppressed.
	 */
	boolean isSuppressed();
	
	/**
	 * Sets the suppressed state of the provider.
	 * Acquisition may be suppressed for a period of time after a previous attempt.
	 * This is to provide client control of the acquisition policy.
	 * If true then {@link #acquireCredentials(boolean)} will immediately cancel when invoked.
	 * @param suppressed true if the provider is to be suppressed.
	 */
	void setSuppressed(boolean suppressed);
	
	/**
	 * Retrieves the connector service associated with this provider. Each provider
	 * has its own connector service. All authenticating connector services have their 
	 * own provider.
	 * @return the connector service associated with this provider
	 */
	IConnectorService getConnectorService();
	
}
