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
package org.eclipse.rse.core.subsystems;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.IRSEUserIdConstants;
import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemSignonInformation;

/**
 * This is a base class to make it easier to create connector service classes.
 * <p>
 * An {@link org.eclipse.rse.core.subsystems.IConnectorService} object
 * is returned from a subsystem object via getConnectorService(), and
 * it is used to represent the live connection to a particular subsystem.
 * <p>
 * You must override/implement
 * <ul>
 * <li>isConnected
 * <li>internalConnect
 * <li>internalDisconnect
 * </ul>
 * You should override:
 * <ul>
 * <li>reset 
 * <li>getVersionReleaseModification
 * <li>getHomeDirectory
 * <li>getTempDirectory
 * </ul>
 * You can override:
 * <ul>
 * <li>supportsUserId
 * <li>requiresUserId
 * <li>supportsPassword
 * <li>requiresPassword
 * </ul>
 * 
 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager
 */
public abstract class AbstractConnectorService extends SuperAbstractConnectorService implements IRSEUserIdConstants {
	public AbstractConnectorService(String name, String description, IHost host, int port) {
		super(name, description, host, port);
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Returns the active userId if we are connected.
	 * If not it returns the userId for the primary subsystem ignoring the 
	 * cached userId.
	 */
	final public String getUserId() {
		return getCredentialsProvider().getUserId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setUserId(java.lang.String)
	 */
	final public void setUserId(String newId) {
		ICredentialsProvider provider = getCredentialsProvider();
		String oldUserId = provider.getUserId();
		if (oldUserId == null || oldUserId.equals(newId)) {
			updateDefaultUserId(getPrimarySubSystem(), getUserId());
			provider.setUserId(newId);
			setDirty(true);
		}
	}

	public final void saveUserId() {
		ICredentialsProvider provider = getCredentialsProvider();
		String userId = provider.getUserId();
		updateDefaultUserId(getPrimarySubSystem(), userId);
	}

	public final void removeUserId() {
		updateDefaultUserId(getPrimarySubSystem(), null);
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Clear internal password cache. Called when user uses the property dialog to 
	 * change his userId.  
	 * 
	 * @param persist if this is true, clear the password from the disk cache as well
	 * @see #clearCredentials()
	 */
	final public void clearPassword(boolean persist, boolean propagate) {
		ICredentialsProvider provider = getCredentialsProvider();
		provider.clearPassword();
		if (persist) {
			removePassword();
		}
		if (sharesCredentials() && propagate) {
			String userId = provider.getUserId();
			clearPasswordForOtherSystemsInConnection(userId, false);
		}
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Return true if password is currently saved either here or in its persisted
	 * form.
	 * @param onDisk true if the check should be made for a persisted form as well, 
	 * false if the check should be made for a password in memory only.
	 * @return true if the password is known, false otherwise.
	 */
	final public boolean hasPassword(boolean onDisk) {
		SystemSignonInformation signonInformation = getSignonInformation();
		boolean cached = (signonInformation != null && signonInformation.getPassword() != null);
		if (!cached && onDisk) {
			String systemType = getHostType();
			String hostName = getHostName();
			String userId = getUserId();
			if (userId != null) {
				return PasswordPersistenceManager.getInstance().passwordExists(systemType, hostName, getUserId());
			}
		}
		return cached;
	}

	/**
	 * <i>Useful utility method. Fully implemented, no need to override.</i><br>
	 * Set the password if you got it from somewhere
	 * @param userId the user for which to set the password
	 * @param password the password to set for this userId
	 * @param persist true if the password is to be persisted, 
	 * false if its persistent form is to be removed.
	 * @param propagate if the password should be propagated to related connector services.
	 */
	public final void setPassword(String userId, String password, boolean persist, boolean propagate) {
		if (getPrimarySubSystem().forceUserIdToUpperCase()) {
			userId = userId.toUpperCase();
		}
		ICredentialsProvider provider = getCredentialsProvider();
		String myUserId = provider.getUserId();
		IHost host = getHost();
		if (host.compareUserIds(userId, myUserId)) {
			provider.setPassword(password);
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

	public final void savePassword() {
		ICredentialsProvider provider = getCredentialsProvider();
		ICredentials credentials = provider.getCredentials();
		if (credentials instanceof SystemSignonInformation) {
			SystemSignonInformation signonInformation = (SystemSignonInformation) credentials;
			PasswordPersistenceManager.getInstance().add(signonInformation, true, true);
		}
	}

	public final void removePassword() {
		ICredentialsProvider provider = getCredentialsProvider();
		String systemType = getHostType();
		String hostName = getHostName();
		String userId = provider.getUserId();
		PasswordPersistenceManager.getInstance().remove(systemType, hostName, userId);
	}

	/**
	 * This connection method wrappers the others (internal connect) so that registered subsystems 
	 * can be notified and initialized after a connect
	 * Previous implementations that overrode this method should now change
	 * their connect() method to internalConnect()
	 */
	public final void connect(IProgressMonitor monitor) throws Exception {
		internalConnect(monitor);
		intializeSubSystems(monitor);
	}

	/**
	 * Disconnects from the remote system.
	 * <p>
	 * You must override
	 * if <code>subsystem.getParentSubSystemConfiguration().supportsServerLaunchProperties</code> 
	 * returns false.
	 * <p>
	 * If the subsystem supports server launch
	 * the default behavior is to use the same remote server
	 * launcher created in <code>connect()</code> and call <code>disconnect()</code>.
	 * <p>
	 * This is called, by default, from the <code>disconnect()</code>
	 * method of the subsystem.
	 * @see IServerLauncher#disconnect()
	 */
	public final void disconnect(IProgressMonitor monitor) throws Exception {
		internalDisconnect(monitor);
		unintializeSubSystems(monitor);
		clearPassword(false, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isSuppressed()
	 */
	public final boolean isSuppressed() {
		return getCredentialsProvider().isSuppressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setSuppressed(boolean)
	 */
	public final void setSuppressed(boolean suppressed) {
		getCredentialsProvider().setSuppressed(suppressed);
	}

	public final void acquireCredentials(boolean reacquire) throws InterruptedException {
		getCredentialsProvider().acquireCredentials(reacquire);
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Clear internal userId. Called when user uses the property dialog to 
	 * change his userId. By default, sets internal userId value to null so that on
	 * the next call to getUserId() it is requeried from subsystem. 
	 * Also clears the password.
	 */
	final public void clearCredentials() {
		getCredentialsProvider().clearCredentials();
		setDirty(true);
	}

	/**
	 * <i>Useful utility method. Fully implemented, no need to override.</i><br>
	 * @return the password information for the primary subsystem of this
	 * connector service. Assumes it has been set by the subsystem at the 
	 * time the subsystem acquires the connector service.
	 */
	final protected SystemSignonInformation getSignonInformation() {
		SystemSignonInformation result = null;
		ICredentialsProvider provider = getCredentialsProvider();
		ICredentials credentials = provider.getCredentials();
		result = (SystemSignonInformation) credentials;
		return result;
	}

	private void updatePasswordForOtherSystemsInConnection(String uid, String password, boolean persist) {
		IHost connection = getPrimarySubSystem().getHost();
		ISystemRegistry registry = RSECorePlugin.getDefault().getSystemRegistry();
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
			ISystemRegistry registry = RSECorePlugin.getDefault().getSystemRegistry();
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
			ISystemRegistry sr = RSECorePlugin.getDefault().getSystemRegistry();
			sr.updateHost(host, host.getSystemType(), host.getAliasName(), host.getHostName(), host.getDescription(), userId, whereToUpdate);
		}
	}
}