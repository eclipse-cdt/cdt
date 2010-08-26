/********************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation from AbstractConnectorService.
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * David Dykstal (IBM) - [210474] Deny save password function missing
 * David Dykstal (IBM) - [225089][ssh][shells][api] Canceling connection leads to exception
 * David McKnight (IBM)  [323648] SSH Terminals subsystem should re-use user id and password for the Files subsystem
 ********************************************************************************/
package org.eclipse.rse.core.subsystems;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.IRSEUserIdConstants;
import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemSignonInformation;

/**
 * An authenticating connector service understands the concept of credentials
 * (see {@link ICredentials})
 * and possibly the concepts of user id and password. It contains a 
 * credentials provider ({@link ICredentialsProvider}) and provides a 
 * framework under which authentication can take place during connections.
 */
public abstract class AuthenticatingConnectorService extends AbstractConnectorService {
	
	protected ICredentialsProvider credentialsProvider = null;

	/**
	 * Constructs an authenticating connector service.
	 * @param name The name of the connector service
	 * @param description The description of the connector service
	 * @param host The host associated with this connector service
	 * @param port The port this connector service will use when connecting if it uses IP.
	 */
	public AuthenticatingConnectorService(String name, String description, IHost host, int port) {
		super(name, description, host, port);
	}

	/**
	 * Obtains the user id, if it understand the concept of user id, from
	 * its credentials provider.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getUserId()
	 * @return the user id or null if not available or not supported.
	 */
	public final String getUserId() {
		return credentialsProvider.getUserId();
	}

	/**
	 * Sets the default user id for use by the credentials provider.
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setUserId(java.lang.String)
	 * @param newId the id to be used by the credentials provider.
	 */
	public final void setUserId(String newId) {
		String oldUserId = credentialsProvider.getUserId();
		if (oldUserId == null || !oldUserId.equals(newId)) {
			credentialsProvider.setUserId(newId);
			saveUserId();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#saveUserId()
	 */
	public final void saveUserId() {
		String userId = credentialsProvider.getUserId();
		updateDefaultUserId(getPrimarySubSystem(), userId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#removeUserId()
	 */
	public final void removeUserId() {
		updateDefaultUserId(getPrimarySubSystem(), null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#clearPassword(boolean, boolean)
	 */
	public final void clearPassword(boolean persist, boolean propagate) {
		credentialsProvider.clearPassword();
		if (persist) {
			removePassword();
		}
		if (sharesCredentials() && propagate) {
			String userId = credentialsProvider.getUserId();
			clearPasswordForOtherSystemsInConnection(userId, false);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#hasPassword(boolean)
	 */
	public final boolean hasPassword(boolean onDisk) {
		ICredentials credentials = credentialsProvider.getCredentials();
		boolean cached = (credentials != null && credentials.getPassword() != null);
		if (!cached && onDisk) {
			IRSESystemType systemType = getHost().getSystemType();
			String hostName = getHostName();
			String userId = getUserId();
			if (userId != null) {
				return PasswordPersistenceManager.getInstance().passwordExists(systemType, hostName, getUserId());
			}
		}
		return cached;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setPassword(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public final void setPassword(String userId, String password, boolean persist, boolean propagate) {
		if (getPrimarySubSystem().forceUserIdToUpperCase()) {
			userId = userId.toUpperCase();
		}
		String myUserId = credentialsProvider.getUserId();
		IHost host = getHost();
		if (host.compareUserIds(userId, myUserId)) {
			credentialsProvider.setPassword(password);
		}
		if (sharesCredentials() && propagate) {
			updatePasswordForOtherSystemsInConnection(userId, password, persist);
		}
		if (persist) {
			savePassword();
		} else {
			removePassword();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#savePassword()
	 */
	public final void savePassword() {
		ICredentials credentials = credentialsProvider.getCredentials();
		if (credentials instanceof SystemSignonInformation) {
			SystemSignonInformation signonInformation = (SystemSignonInformation) credentials;
			PasswordPersistenceManager.getInstance().add(signonInformation, true, false);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#removePassword()
	 */
	public final void removePassword() {
		IRSESystemType systemType = getHost().getSystemType();
		String hostName = getHostName();
		String userId = credentialsProvider.getUserId();
		PasswordPersistenceManager.getInstance().remove(systemType, hostName, userId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#postDisconnect()
	 */
	protected final void postDisconnect() {
		clearPassword(false, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isSuppressed()
	 */
	public final boolean isSuppressed() {
		return credentialsProvider.isSuppressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setSuppressed(boolean)
	 */
	public final void setSuppressed(boolean suppressed) {
		credentialsProvider.setSuppressed(suppressed);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#acquireCredentials(boolean)
	 */
	public final void acquireCredentials(boolean reacquire) throws OperationCanceledException {
		credentialsProvider.acquireCredentials(reacquire);
		ICredentials credentials = credentialsProvider.getCredentials();
		IHost host = getHost();
		String userId = credentials.getUserId();
		String password = credentials.getPassword();		
		boolean persist = PasswordPersistenceManager.getInstance().find(host.getSystemType(), host.getHostName(), userId) != null;		
		setPassword(userId, password, persist, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#clearCredentials()
	 */
	public final void clearCredentials() {
		credentialsProvider.clearCredentials();
		setDirty(true);
	}

	private void updatePasswordForOtherSystemsInConnection(String uid, String password, boolean persist) {
		IHost connection = getPrimarySubSystem().getHost();
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		ISubSystem[] subsystems = registry.getSubSystems(connection);
		List uniqueSystems = new ArrayList();
		for (int i = 0; i < subsystems.length; i++) {
			IConnectorService cs = subsystems[i].getConnectorService();
			if (cs != this && cs.inheritsCredentials()) {
				if (!uniqueSystems.contains(cs)) {
					uniqueSystems.add(cs);
				}
			}
		}
		for (int s = 0; s < uniqueSystems.size(); s++) {
			IConnectorService system = (IConnectorService) uniqueSystems.get(s);
			if (!system.isConnected() && !system.hasPassword(false)) {
				if (system.getPrimarySubSystem().forceUserIdToUpperCase()) {
					uid = uid.toUpperCase();
					password = password.toUpperCase();
				}
				system.setPassword(uid, password, false, false);
			}
		}
	}

	private void clearPasswordForOtherSystemsInConnection(String uid, boolean persist) {
		if (uid != null) {
			IHost connection = getHost();
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			ISubSystem[] subsystems = registry.getSubSystems(connection);
			List uniqueSystems = new ArrayList();
			for (int i = 0; i < subsystems.length; i++) {
				IConnectorService system = subsystems[i].getConnectorService();
				if (system != this && system.inheritsCredentials()) {
					if (!uniqueSystems.contains(system)) {
						uniqueSystems.add(system);
					}
				}
			}
			for (int s = 0; s < uniqueSystems.size(); s++) {
				IConnectorService system = (IConnectorService) uniqueSystems.get(s);
				if (system.hasPassword(persist)) {
					system.clearPassword(persist, false);
				}
			}
		}
	}

	/**
	 * Change the default user Id value in the SubSystem if it is non-null, else
	 * update it in the Connection object
	 */
	private void updateDefaultUserId(ISubSystem subsystem, String userId) {
		String ssLocalUserId = subsystem.getLocalUserId();
		if (ssLocalUserId != null) {
			ISubSystemConfiguration ssc = subsystem.getSubSystemConfiguration();
			ssc.updateSubSystem(subsystem, true, userId, false, 0);
		} else {
			int whereToUpdate = IRSEUserIdConstants.USERID_LOCATION_HOST;
			IHost host = subsystem.getHost();
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			sr.updateHost(host, host.getSystemType(), host.getAliasName(), host.getHostName(), host.getDescription(), userId, whereToUpdate);
		}
	}

	/**
	 * Returns true if this connector service can share it's credentials
	 * with other connector services in this host.
	 * This implementation will always return true.
	 * Override if necessary.
	 * @return true
	 * @see IConnectorService#sharesCredentials()
	 */
	public boolean sharesCredentials() {
	    return true;
	}

	/**
	 * Returns true if this connector service can inherit the credentials of
	 * other connector services in this host.
	 * This implementation always returns true. 
	 * Override if necessary.
	 * @return true
	 * @see IConnectorService#inheritsCredentials()
	 */
	public boolean inheritsCredentials() {
	    return true;
	}

	/**
	 * Sets the credentials provider used by this connector service.
	 * This should be invoked once immediately during the construction of the 
	 * connector service.
	 * @param credentialsProvider the credentials provider to be used
	 * by this connector service.
	 */
	protected final void setCredentialsProvider(ICredentialsProvider credentialsProvider) {
		this.credentialsProvider = credentialsProvider;
	}
	
	/**
	 * @return the credentials provider that is being used by this connector service.
	 */
	protected final ICredentialsProvider getCredentialsProvider() {
		return credentialsProvider;
	}
	
}