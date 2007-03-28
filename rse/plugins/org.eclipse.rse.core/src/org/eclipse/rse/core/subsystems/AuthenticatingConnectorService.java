package org.eclipse.rse.core.subsystems;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.IRSEUserIdConstants;
import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemSignonInformation;

public abstract class AuthenticatingConnectorService extends AbstractConnectorService {
	
	protected ICredentialsProvider credentialsProvider = null;

	public AuthenticatingConnectorService(String name, String description, IHost host, int port) {
		super(name, description, host, port);
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Returns the active userId if we are connected.
	 * If not it returns the userId for the primary subsystem ignoring the 
	 * cached userId.
	 */
	public final String getUserId() {
		return credentialsProvider.getUserId();
	}

	public final void setUserId(String newId) {
		String oldUserId = credentialsProvider.getUserId();
		if (oldUserId == null || oldUserId.equals(newId)) {
			updateDefaultUserId(getPrimarySubSystem(), getUserId());
			credentialsProvider.setUserId(newId);
			setDirty(true);
		}
	}

	public final void saveUserId() {
		String userId = credentialsProvider.getUserId();
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

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Return true if password is currently saved either here or in its persisted
	 * form.
	 * @param onDisk true if the check should be made for a persisted form as well, 
	 * false if the check should be made for a password in memory only.
	 * @return true if the password is known, false otherwise.
	 */
	public final boolean hasPassword(boolean onDisk) {
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

	public final void savePassword() {
		ICredentials credentials = credentialsProvider.getCredentials();
		if (credentials instanceof SystemSignonInformation) {
			SystemSignonInformation signonInformation = (SystemSignonInformation) credentials;
			PasswordPersistenceManager.getInstance().add(signonInformation, true, true);
		}
	}

	public final void removePassword() {
		String systemType = getHostType();
		String hostName = getHostName();
		String userId = credentialsProvider.getUserId();
		PasswordPersistenceManager.getInstance().remove(systemType, hostName, userId);
	}

	protected final void postDisconnect() {
		clearPassword(false, true);
	}

	public final boolean isSuppressed() {
		return credentialsProvider.isSuppressed();
	}

	public final void setSuppressed(boolean suppressed) {
		credentialsProvider.setSuppressed(suppressed);
	}

	public final void acquireCredentials(boolean reacquire) throws InterruptedException {
		credentialsProvider.acquireCredentials(reacquire);
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Clear internal userId. Called when user uses the property dialog to 
	 * change his userId. By default, sets internal userId value to null so that on
	 * the next call to getUserId() it is requeried from subsystem. 
	 * Also clears the password.
	 */
	public final void clearCredentials() {
		credentialsProvider.clearCredentials();
		setDirty(true);
	}

	/**
	 * <i>Useful utility method. Fully implemented, no need to override.</i><br>
	 * @return the password information for the primary subsystem of this
	 * connector service. Assumes it has been set by the subsystem at the 
	 * time the subsystem acquires the connector service.
	 */
	protected final SystemSignonInformation getSignonInformation() {
		SystemSignonInformation result = null;
		ICredentials credentials = credentialsProvider.getCredentials();
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

	/**
	 * Returns true if this connector service can share it's credentials
	 * with other connector services in this host.
	 * This default implementation will always return true.
	 * Override if necessary.
	 * @return true
	 */
	public boolean sharesCredentials() {
	    return true;
	}

	/**
	 * Returns true if this connector service can inherit the credentials of
	 * other connector services in this host.
	 * This default implementation always returns true. 
	 * Override if necessary.
	 * @return true
	 */
	public boolean inheritsCredentials() {
	    return true;
	}

	protected final void setCredentialsProvider(ICredentialsProvider credentialsProvider) {
		this.credentialsProvider = credentialsProvider;
	}
	
	protected final ICredentialsProvider getCredentialsProvider() {
		return credentialsProvider;
	}
	
}