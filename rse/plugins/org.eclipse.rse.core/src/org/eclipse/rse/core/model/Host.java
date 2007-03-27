/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - using new API from RSECorePlugin, RSEPreferencesManager
 *                     - moved SystemsPreferencesManager to a new plugin
 * Uwe Stieber (Wind River) - Dynamic system type provider extensions.
 *                          - Moved to package org.eclipse.rse.model for being extendable.
 ********************************************************************************/

package org.eclipse.rse.core.model;

import java.util.Locale;

import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * Default implementation of the <code>IHost</code> interface.
 * <p>
 * Dynamic system type providers may extend this implementation
 * if needed.
 */
public class Host extends RSEModelObject implements IHost {


	private boolean ucId = true;
	private boolean userIdCaseSensitive = true;
	private ISystemHostPool pool;
	protected String previousUserIdKey;

	/**
	 * The system type which is associated to this <code>IHost</code> object.
	 */
	private String systemType = null;
	
	/**
	 * The alias name of this <code>IHost</code> object.
	 */
	private String aliasName = null;

	/**
	 * The host name of the target which is associated to this <code>IHost</code> object.
	 */
	private String hostName = null;
	
	/**
	 * The description of this <code>IHost</code> object.
	 */
	private String description = null;
	
	/**
	 * The default user id to use to login to the target host.
	 */
	private String defaultUserId = null;
	
	/**
	 * Prompt for user id and password.
	 */
	private boolean promptable = false;
	
	/**
	 * Offline mode.
	 */
	private boolean offline = false;

	/**
	 * The system profile associated with this <code>IHost</code> object.
	 */
	private ISystemProfile _profile;

	/**
	 * Constructor
	 */
	public Host(ISystemProfile profile) {
		super();
		_profile = profile;
	}

	/**
	 * Set the parent connection pool this is owned by.
	 * Connection pools are internal management objects, one per profile.
	 */
	public void setHostPool(ISystemHostPool pool) {
		this.pool = pool;
		previousUserIdKey = getPreferencesKey();
	}

	/**
	 * Set the parent connection pool this is owned by.
	 * Connection pools are internal management objects, one per profile.
	 */
	public ISystemHostPool getHostPool() {
		return pool;
	}

	/**
	 * Return all the connector services provided for this host
	 */
	public IConnectorService[] getConnectorServices() {
		return RSECorePlugin.getDefault().getSystemRegistry().getConnectorServices(this);
	}

	/**
	 * Return the subsystem instances under this connection.<br>
	 * Just a shortcut to {@link org.eclipse.rse.core.model.ISystemRegistry#getSubSystems(IHost)} 
	 */
	public ISubSystem[] getSubSystems() {
		return RSECorePlugin.getDefault().getSystemRegistry().getSubSystems(this);
	}

	/**
	 * Private method called when this connection is being deleted, so
	 * we can do any pre-death cleanup we need.
	 * <p>
	 * What we need to do is delete our entry in the preference store for our default userId.
	 */
	public void deletingHost() {
		String oldUserId = null;
		if (previousUserIdKey != null) oldUserId = getLocalDefaultUserId(previousUserIdKey);
		// if the userId attribute held a preference store key of the form profileName.connectionName,
		// we have to delete that key entry from the preference store and re-gen a new keyed entry with the same
		// value (the actual user id) the old keyed entry held.
		if (oldUserId != null) {
			RSEPreferencesManager.clearUserId(previousUserIdKey);
		}
	}

	/**
	 * Private method called when this connection's profile is being rename, so
	 * we can do any pre-death cleanup we need.
	 * <p>
	 * What we need to do is rename our entry in the preference store for our default userId.
	 */
	public void renamingSystemProfile(String oldName, String newName) {
		String userIdValue = null;
		if (previousUserIdKey != null) userIdValue = getLocalDefaultUserId(previousUserIdKey);
		// if the userId attribute held a preference store key of the form profileName.connectionName,
		// we have to delete that key entry from the preference store and re-gen a new keyed entry with the same
		// value (the actual user id) the old keyed entry held.
		String newKey = getPreferencesKey(newName, getAliasName());
		if ((userIdValue != null) && (userIdValue.length() > 0)) {
			RSEPreferencesManager.clearUserId(previousUserIdKey);
			RSEPreferencesManager.setUserId(newKey, userIdValue); // store old value with new preference key
		}
		previousUserIdKey = newKey;
	}

	/**
	 * Return the system profile that owns this connection
	 */
	public ISystemProfile getSystemProfile() {
		return _profile;
	}

	/**
	 * Return the name of system profile that owns this connection
	 */
	public String getSystemProfileName() {
		if (pool == null)
			return null;
		else {
			ISystemProfile profile = pool.getSystemProfile();
			if (profile != null)
				return profile.getName();
			else return null;
		}
	}

	/**
	 * Intercept of setAliasName so we can potentially rename the default-user-id key
	 * for the preferences store. That key is profileName.connectionAliasName so is 
	 * affected when the alias name changes.
	 */
	public void setAliasName(String newName) {
		String userIdValue = null;
		if (previousUserIdKey != null) userIdValue = getLocalDefaultUserId(previousUserIdKey);
		this.setAliasNameGen(newName); // update mof-modelled attribute
		// if the userId attribute held a preference store key of the form profileName.connectionAliasName,
		// we have to delete that key entry from the preference store and re-gen a new keyed entry with the same
		// value (the actual user id) the old keyed entry held.
		String newKey = getPreferencesKey(getSystemProfileName(), newName);
		if ((userIdValue != null) && (userIdValue.length() > 0)) {
			RSEPreferencesManager.clearUserId(previousUserIdKey);
			RSEPreferencesManager.setUserId(newKey, userIdValue); // store old value with new preference key
		}
		previousUserIdKey = newKey;
	}

	/**
	 * Intercept of setSystemType so we can decide if the user ID is case sensitive
	 */
	public void setSystemType(String systemType) {
		// defect 43219
		if (systemType != null) {
			boolean forceUC = systemType.equals(IRSESystemType.SYSTEMTYPE_ISERIES);
			boolean caseSensitiveUID = systemType.equals(IRSESystemType.SYSTEMTYPE_UNIX) || systemType.equals(IRSESystemType.SYSTEMTYPE_LINUX)
																	|| (systemType.equals(IRSESystemType.SYSTEMTYPE_LOCAL) && !System.getProperty("os.name").toLowerCase().startsWith("windows")); //$NON-NLS-1$  //$NON-NLS-2$
			setForceUserIdToUpperCase(forceUC);
			setUserIdCaseSensitive(caseSensitiveUID);
		}
		this.setSystemTypeGen(systemType);
	}

	/**
	 * Intercept of setHostName so we can force it to uppercase.
	 * IPv4 host names are case insensitive. Much data is stored using the host
	 * name as part of the key. Therefore, the host name is capitalized here so that
	 * these comparisons work naturally.
	 * However, this must be done using the US locale since IPv4 host names
	 * use can be compared using this locale. See RFC1035.
	 */
	public void setHostName(String name) {
		if (name != null) {
			name = name.toUpperCase(Locale.US);
		}
		this.setHostNameGen(name);
	}

	/**
	 * Intercept of setDefaultUserId so we can force it to uppercase.
	 * Also, we do not store the user Id per se in the attribute, but rather
	 * we store it in the preference with a key name unique to this connection.
	 * We store that key name in this attribute. However, this is all transparent to
	 * the caller.
	 */
	public void setDefaultUserId(String newId) {
		if ((newId != null) && ucId) newId = newId.toUpperCase();

		if ((newId == null) || (newId.length() == 0)) // a "clear" request?
		{
			clearLocalDefaultUserId();
		}
		else {
			String key = getPreferencesKey();
			if (key != null) {
				RSEPreferencesManager.setUserId(key, newId);
			}
		}
	}

	/**
	 * Returns the default UserId for this Host.
	 * Note that we don't store it directly in
	 * the model, since we don't want the team to share it. Rather,
	 * we store the actual it in the preference store keyed by 
	 * (profileName.connectionName).
	 * <p>
	 * Further, it is possible that there is no default UserId. If so, this 
	 * method will go to the preference store and will try to get the default
	 * UserId for this connection's system type.
	 * <p>
	 * This is all transparent to the caller though.
	 * <p>
	 * @return The value of the DefaultUserId attribute
	 */
	public String getDefaultUserId() {
		String uid = getLocalDefaultUserId();
		if ((uid == null) || (uid.length() == 0)) {
			uid = RSEPreferencesManager.getUserId(getSystemType()); // resolve from preferences	
			if ((uid != null) && ucId) uid = uid.toUpperCase();
		}
		return uid;
	}

	/**
	 * Return the local default user Id without resolving up the food chain.
	 * @see #getDefaultUserId()
	 */
	protected static String getLocalDefaultUserId(String key) {
		String uid = null;
		if ((key != null) && (key.length() > 0)) {
			uid = RSEPreferencesManager.getUserId(key); // resolve from preferences	
		}
		return uid;
	}

	/**
	 * Return the local default user Id without resolving up the food chain.
	 * @see #getDefaultUserId()
	 */
	public String getLocalDefaultUserId() {
		return getLocalDefaultUserId(getPreferencesKey());
	}

	/**
	 * Clear the local default user Id so next query will return the value from
	 * the preference store.
	 * <p>
	 * Same as calling setDefaultUserId(null)
	 * @see #setDefaultUserId(String)
	 */
	public void clearLocalDefaultUserId() {
		if (previousUserIdKey != null) RSEPreferencesManager.clearUserId(previousUserIdKey);
	}

	/**
	 * Helper method to compute a unique name for a given subsystem instance
	 */
	protected String getPreferencesKey() {
		if ((getSystemProfileName() == null) || (getAliasName() == null)) return null;
		return getPreferencesKey(getSystemProfileName());
	}

	/**
	 * Helper method to compute a unique name for a given subsystem instance, given a profile name
	 */
	protected String getPreferencesKey(String profileName) {
		String connectionName = getAliasName();
		if (connectionName == null) return null;
		return getPreferencesKey(profileName, connectionName);
	}

	/**
	 * Helper method to compute a unique name for a given subsystem instance, given a profile name and connection name
	 */
	protected String getPreferencesKey(String profileName, String connectionName) {
		return profileName + "." + connectionName; //$NON-NLS-1$
	}

	/**
	 * Call this with false to turn off the default behaviour of forcing the default userId to uppercase.
	 */
	public void setForceUserIdToUpperCase(boolean force) {
		this.ucId = force;
	}

	/**
	 * Call this to turn off the default behaviour of considering case when comparing userIds
	 */
	public void setUserIdCaseSensitive(boolean caseSensitive) {
		this.userIdCaseSensitive = caseSensitive;
	}

	/**
	 * Call this to query whether the default userId is to be uppercased.
	 */
	public boolean getForceUserIdToUpperCase() {
		return ucId;
	}

	/**
	 * Call this to query whether the default userId is case sensitive
	 */
	public boolean getUserIdCaseSensitive() {
		return userIdCaseSensitive;
	}

	/**
	 * Call this to compare two userIds taking case sensitivity
	 */
	public boolean compareUserIds(String userId1, String userId2) {
		if (userId1 == null) userId1 = ""; //$NON-NLS-1$
		if (userId2 == null) userId2 = ""; //$NON-NLS-1$
		if (userIdCaseSensitive)
			return userId1.equals(userId2);
		else return userId1.equalsIgnoreCase(userId2);
	}

	public String toString() {
		if (getAliasName() == null) {
			StringBuffer result = new StringBuffer(super.toString());
			result.append(" (systemType: "); //$NON-NLS-1$
			result.append(systemType);
			result.append(", aliasName: "); //$NON-NLS-1$
			result.append(aliasName);
			result.append(", hostName: "); //$NON-NLS-1$
			result.append(hostName);
			result.append(", description: "); //$NON-NLS-1$
			result.append(description);
			result.append(", defaultUserId: "); //$NON-NLS-1$
			result.append(defaultUserId);
			result.append(", promptable: "); //$NON-NLS-1$
			result.append(promptable);
			result.append(", offline: "); //$NON-NLS-1$
			result.append(offline);
			result.append(')');
			return result.toString();
		}
		
		return getAliasName();
	}

	/**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
	public Object getAdapter(Class adapterType) {
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getSystemType() {
		return systemType;
	}

	/**
	 * Returns the alias name for this host
	 */
	public String getName() {
		return getAliasName();
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * The unique key for this object. Unique per connection pool
	 */
	public String getAliasName() {
		return aliasName;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setDescription(String newDescription) {
		setDirty(!compareStrings(description, newDescription));
		description = newDescription;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public boolean isPromptable() {
		return promptable;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setPromptable(boolean newPromptable) {
		setDirty(promptable != newPromptable);
		promptable = newPromptable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * Query if this connection is offline or not. It is up to each subsystem to honor this
	 *  flag. 
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isOffline() {
		return offline;
	}

	/**
	 * <!-- begin-user-doc -->
	 * Specify if this connection is offline or not. It is up to each subsystem to honor this
	 *  flag. 
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOffline(boolean newOffline) {
		setDirty(offline != newOffline);
		offline = newOffline;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setSystemTypeGen(String newSystemType) {
		setDirty(!compareStrings(systemType, newSystemType));
		systemType = newSystemType;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setAliasNameGen(String newAliasName) {
		setDirty(!compareStrings(aliasName, newAliasName));
		aliasName = newAliasName;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setHostNameGen(String newHostName) {
		setDirty(!compareStrings(hostName, newHostName));
		hostName = newHostName;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getDefaultUserIdGen() {
		return defaultUserId;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setDefaultUserIdGen(String newDefaultUserId) {
		setDirty(!compareStrings(defaultUserId, newDefaultUserId));
		defaultUserId = newDefaultUserId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.RSEModelObject#setDirty(boolean)
	 */
	public void setDirty(boolean flag) {
		super.setDirty(flag);
		ISystemHostPool myPool = getHostPool();
		if (myPool != null && flag) {
			myPool.setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#commit()
	 */
	public boolean commit() {
		return RSECorePlugin.getThePersistenceManager().commit(this);
	}

}