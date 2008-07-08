/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 * Kevin Doyle (IBM) - [203365] Profile should not be saved as a result of file transfer
 * David Dykstal (IBM) - [225911] Exception received after deleting a profile containing a connection
 * David McKnight (IBM)          - [226324] Default user ID from preferences not inherited
 ********************************************************************************/

package org.eclipse.rse.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	
	private static final String ENCODING_PROPERTY_SET = "EncodingPropertySet"; //$NON-NLS-1$
	private static final String ENCODING_REMOTE_PROPERTY_KEY = "EncodingRemotePropertyKey"; //$NON-NLS-1$
	private static final String ENCODING_NON_REMOTE_PROPERTY_KEY = "EncodingNonRemotePropertyKey"; //$NON-NLS-1$

	/**
	 * The system type which is associated to this <code>IHost</code> object.
	 */
	private IRSESystemType systemType = null;
	
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#setHostPool(org.eclipse.rse.core.model.ISystemHostPool)
	 */
	public void setHostPool(ISystemHostPool pool) {
		this.pool = pool;
		previousUserIdKey = getPreferencesKey();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#getHostPool()
	 */
	public ISystemHostPool getHostPool() {
		return pool;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#getConnectorServices()
	 */
	public IConnectorService[] getConnectorServices() {
		return RSECorePlugin.getTheSystemRegistry().getConnectorServices(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#getSubSystems()
	 */
	public ISubSystem[] getSubSystems() {
		return RSECorePlugin.getTheSystemRegistry().getSubSystems(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#deletingHost()
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#renamingSystemProfile(java.lang.String, java.lang.String)
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#getSystemProfile()
	 */
	public ISystemProfile getSystemProfile() {
		return _profile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#getSystemProfileName()
	 */
	public String getSystemProfileName() {
		String result = null;
		if (_profile != null) {
			result = _profile.getName();
		} else if (pool != null) {
			ISystemProfile profile = pool.getSystemProfile();
			if (profile != null) {
				result =  profile.getName();
			}
		}
		return result;
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
	public void setSystemType(IRSESystemType systemType) {
		// defect 43219
		if (systemType != null) {
			//FIXME MOB this should be in IRSESystemType.isForceUC() / IRSESystemType.isUIDCaseSensitive()
			String systemTypeId = systemType.getId();
			boolean forceUC = systemTypeId.equals(IRSESystemType.SYSTEMTYPE_ISERIES_ID);
			boolean caseSensitiveUID = systemTypeId.equals(IRSESystemType.SYSTEMTYPE_UNIX_ID) || systemTypeId.equals(IRSESystemType.SYSTEMTYPE_LINUX_ID)
																	|| (systemType.isLocal() && !systemType.isWindows());
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#getDefaultUserId()
	 */
	public String getDefaultUserId() {
		String uid = getLocalDefaultUserId();
		if ((uid == null) || (uid.length() == 0)) {
			uid = RSEPreferencesManager.getDefaultUserId(getSystemType()); // resolve from preferences	
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#getLocalDefaultUserId()
	 */
	public String getLocalDefaultUserId() {
		return getLocalDefaultUserId(getPreferencesKey());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#clearLocalDefaultUserId()
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapterType) {
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#getSystemType()
	 */
	public IRSESystemType getSystemType() {
		return systemType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEModelObject#getName()
	 */
	public String getName() {
		return getAliasName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#getAliasName()
	 */
	public String getAliasName() {
		return aliasName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#getHostName()
	 */
	public String getHostName() {
		return hostName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.RSEModelObject#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#setDescription(java.lang.String)
	 */
	public void setDescription(String newDescription) {
		setDirty(!compareStrings(description, newDescription));
		description = newDescription;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#isPromptable()
	 */
	public boolean isPromptable() {
		return promptable;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#setPromptable(boolean)
	 */
	public void setPromptable(boolean newPromptable) {
		setDirty(promptable != newPromptable);
		promptable = newPromptable;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#isOffline()
	 */
	public boolean isOffline() {
		return offline;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#setOffline(boolean)
	 */
	public void setOffline(boolean newOffline) {
		setDirty(offline != newOffline);
		offline = newOffline;
	}

	private void setSystemTypeGen(IRSESystemType newSystemType) {
		setDirty( systemType==null ? (newSystemType==null) : !systemType.equals(newSystemType) );
		systemType = newSystemType;
	}

	private void setAliasNameGen(String newAliasName) {
		setDirty(!compareStrings(aliasName, newAliasName));
		aliasName = newAliasName;
	}

	private void setHostNameGen(String newHostName) {
		setDirty(!compareStrings(hostName, newHostName));
		hostName = newHostName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#commit()
	 */
	public boolean commit() {
		ISystemProfile profile = getSystemProfile();
		boolean result = profile.commit();
		return result;
	}
	
	public IRSEPersistableContainer getPersistableParent() {
		return _profile;
	}
	
	public IRSEPersistableContainer[] getPersistableChildren() {
		List children = new ArrayList(10);
		children.addAll(Arrays.asList(getPropertySets()));
		children.addAll(Arrays.asList(getConnectorServices()));
		IRSEPersistableContainer[] result = new IRSEPersistableContainer[children.size()];
		children.toArray(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#getDefaultEncoding(boolean)
	 */
	public String getDefaultEncoding(boolean fromRemote) {
		
		IPropertySet encPropertySet = getPropertySet(ENCODING_PROPERTY_SET);
		
		if (encPropertySet == null) {
			return null;
		}
		else {
			String nonRemoteEncoding = encPropertySet.getPropertyValue(ENCODING_NON_REMOTE_PROPERTY_KEY);
			
			if (nonRemoteEncoding != null) {
				return nonRemoteEncoding;
			}
			else {
				
				if (!fromRemote) {
					return null;
				}
				else {
					String remoteEncoding = encPropertySet.getPropertyValue(ENCODING_REMOTE_PROPERTY_KEY);
					return remoteEncoding;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IHost#setDefaultEncoding(java.lang.String, boolean)
	 */
	public void setDefaultEncoding(String encoding, boolean fromRemote) {
		boolean commit = false;
		IPropertySet encPropertySet = getPropertySet(ENCODING_PROPERTY_SET);
		
		if (encPropertySet == null) {
			encPropertySet = createPropertySet(ENCODING_PROPERTY_SET);
		}
		
		if (encPropertySet != null) {
			String savedNonRemoteEncoding = encPropertySet.getPropertyValue(ENCODING_NON_REMOTE_PROPERTY_KEY);
			String savedRemoteEncoding = encPropertySet.getPropertyValue(ENCODING_REMOTE_PROPERTY_KEY);
			
			if (encoding != null) {
				
				if (!fromRemote && !encoding.equals(savedNonRemoteEncoding)) {
					encPropertySet.addProperty(ENCODING_NON_REMOTE_PROPERTY_KEY, encoding);
					commit = true;
				}
				else if (fromRemote && !encoding.equals(savedRemoteEncoding)) {
					encPropertySet.addProperty(ENCODING_REMOTE_PROPERTY_KEY, encoding);
					commit = true;
				}
			}
			else {
				
				if (!fromRemote && savedNonRemoteEncoding != null) {
					encPropertySet.removeProperty(ENCODING_NON_REMOTE_PROPERTY_KEY);
					commit = true;
				}
				else if (fromRemote && savedRemoteEncoding != null) {
					encPropertySet.removeProperty(ENCODING_REMOTE_PROPERTY_KEY);
					commit = true;
				}
			}
		}
		// Only commit if the encoding has changed
		if (commit) {
			commit();
		}
	}
}