/********************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - moved from core package in the UI plugin
 *                     - updated to use new RSEPreferencesManager
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [218655][api] Provide SystemType enablement info in non-UI
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [210474] Deny save password function missing
 * David Dykstal (IBM) - [225320] Use equinox secure storage for passwords
 * David Dykstal (IBM) - [379787] Fix secure storage usage in org.eclipse.rse.tests
 ********************************************************************************/

package org.eclipse.rse.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.osgi.framework.Bundle;

/**
 * PasswordPersistenceManager manages the saving and retrieving of user IDs /
 * passwords to Equinox secure storage for registered system types.
 * <p>
 * A PasswordPersistenceManager is sensitive to the "rse.enableSecureStoreAccess" property.
 * If absent it defaults to <code>true</code>.
 * If present then the value must be <code>true</code> to enable access to the secure store.
 * The following code disables access to the secure store.
 * <p>
 * <code>System.setProperty("rse.enableSecureStoreAccess", "false");</code> 
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients. Use
 *                the {@link #getInstance()} method to get the singleton
 *                instance.
 */

/*
 * Passwords are stored in a node that is selected by system type id.
 * Each password has a key that consists of a host name and a user id pair.
 * The key is a string and looks like <hostName>//<userId>.
 * Host names may be symbolic or they be IPv4 or IPv6 addresses.
 * The current design treats these the same.
 * 
 * In addition to the registered system types, there is a "default" system type
 * that can be searched if a password is not found for a registered system type.
 * The API allows for setting this default along with a particular system type.
 * 
 * The current implementation uses Equinox secure storage. The
 * API for this looks like that for a preferences store. This is arranged as a tree
 * of nodes, each node holding a set of keyed values.
 * In the case of this password store value reside only at the lowest
 * nodes in the tree. There is one node for each system type, which is then used to
 * store passwords for a particular host name, user ID pair.
 * 
 * This is similar to the previous implementation which used the Eclipse platform key ring.
 * A particular key ring node was selected using the system type id and a Map object
 * was retrieved or stored at this node. The map was keyed by the same host name, user ID
 * pair that is used in the current implementation.
 * 
 * Migration from the old implementation to the new one is done when accessing a node for
 * the first time. At that point the map entries present in the old implementation are copied
 * to the preferences node in the new implementation.
 * 
 * The old key ring values can be migrated only if they can be accessed using the 
 * compatibility API found in the bundle org.eclipse.core.runtime.compatability.auth.
 * This can be installed by the user, but is not included in the standard
 * packaging for Eclipse 4.2 and subsequent releases.
 * 
 * The nodes in the old implementation were rooted in the URL file://rse<username> where <username>
 * was the name of the java user.name system property. Note that this user.name property may be,
 * and probably is, different that the user ID used on a target system. The new secure preferences are rooted
 * in the default secure preferences node which is kept in a location associated with
 * the current user. Thus, there is no need to additionally qualify the secure
 * storage location with the user.name system property.
 * 
 * Lookup is based on an exact match followed by a fuzzy match on host names. An exact match 
 * uses the host name argument as is and is case sensitive when matching the host name of the 
 * stored keys. If not found, then a fuzzy match is employed that allows for case insensitivity.
 * If one host name is a prefix of the other and they both resolve to the name network entity they
 * are considered to be matching. Network name resolution is very expensive and is employed only if their 
 * is enough extra similar information to justify a match. Thus, for example, hobbiton could 
 * match HOBBITON.EXAMPLE.COM, but could never match an IP address.
 */
public class PasswordPersistenceManager {

	/**
	 * Default System Type
	 */
	private static class DefaultSystemType extends AbstractRSESystemType implements IRSESystemType {
		private static final String DEFAULT_ID = "DEFAULT"; //$NON-NLS-1$

		private DefaultSystemType() {
			super(DEFAULT_ID, DEFAULT_ID, RSECoreMessages.DefaultSystemType_Label, null, null);
		}

		public String getId() {
			return DEFAULT_ID;
		}

		public String[] getSubsystemConfigurationIds() {
			return null;
		}

		public Object getAdapter(Class adapter) {
			return null;
		}

		public boolean isEnabled() {
			return true;
		}
	}

	// Keys used for using the Platform authorization methods
	// The server url is generic so we can lookup all registered user IDs / passwords
	// to display to the user in the password information preference page
	private static final String SERVER_URL = "file://rse"; //$NON-NLS-1$

	private static final String AUTH_SCHEME = ""; // no authorization scheme specified for apis  //$NON-NLS-1$

	// Add return codes
	public static final int RC_OK = 0;
	public static final int RC_ALREADY_EXISTS = 1;
	/** @since org.eclipse.rse.core 3.0 */
	public static final int RC_DENIED = 2;
	public static final int RC_ERROR = -1;

	// Default System Type, on a lookup if the specified system type and host name is not found
	// then the call will automatically lookup the default system type and host name
	public static final IRSESystemType DEFAULT_SYSTEM_TYPE = new DefaultSystemType();

	// Default user name
	public static final String DEFAULT_USER_NAME = "DEFAULT_USER"; //$NON-NLS-1$

	/*
	 * Singleton instance
	 */
	private static PasswordPersistenceManager _instance;

	/**
	 * Retrieve the singleton instance of the PasswordPersistenceManger
	 */
	public static final synchronized PasswordPersistenceManager getInstance() {
		if (_instance == null) {
			_instance = new PasswordPersistenceManager();
			_instance.initializeSystemTypes();
		}
		return _instance;
	}

	/**
	 * Tests the existence of the Eclipse keyring API by looking for installation of the bundle containing
	 * the API.
	 * @return true if the API is installed.
	 */
	private static boolean isAuthorizationCompatibilityInstalled() {
		boolean result = false;
		Bundle authorizationBundle = Platform.getBundle("org.eclipse.core.runtime.compatibility.auth"); //$NON-NLS-1$
		if (authorizationBundle == null) {
			IStatus status = new Status(IStatus.INFO, RSECorePlugin.PLUGIN_ID, "Saved passwords are not available for migration to secure storage. Deprecated authorization classes (org.eclipse.core.runtime.compatibility.auth) are not installed."); //$NON-NLS-1$
			RSECorePlugin.getDefault().getLog().log(status);
		} else {
			result = true;
		}
		return result;
	}

	/**
	 * Examine the preferences to see if password saving is allowed.
	 * @param systemType The system type to check
	 * @param hostName The host name to check
	 * @return true if we are allow to save password for this system type and host
	 */
	private static boolean isSaveAllowed(IRSESystemType systemType, String hostName) {
		boolean allowed = !RSEPreferencesManager.getDenyPasswordSave(systemType, hostName);
		return allowed;
	}

	/**
	 * Build the key to lookup the password for a specific 
	 * user ID and host name
	 * @param hostName the name of the host
	 * @param userId the name of the user
	 * @return a key composed of the host and user name
	 */
	private static String getKey(String hostName, String userId) {
		StringBuffer buffer = new StringBuffer(hostName);
		buffer.append("//"); //$NON-NLS-1$
		buffer.append(userId);
		return buffer.toString();
	}

	/**
	 * Retrieve a host name from a key.
	 * @param passwordKey the key to examine for a host
	 * @return the host name from the key
	 */
	private static String getHostNameFromKey(String passwordKey) {
		int sepIndex = passwordKey.indexOf("//"); //$NON-NLS-1$
		return passwordKey.substring(0, sepIndex);
	}

	/**
	 * Retrieve a user id from a key.
	 * @param passwordKey the key to examine for a host
	 * @return the user id from the key
	 */
	private static String getUserIdFromKey(String passwordKey) {
		int sepIndex = passwordKey.indexOf("//"); //$NON-NLS-1$
		return passwordKey.substring(sepIndex + 2, passwordKey.length());
	}

	/**
	 * Given an array of keys into a password node for a system type return the ones that match the host name and user id criteria.
	 * Fuzzy matching will match hosts if one is a prefix of the other and they both resolve to the same network location.
	 * @param keys the original array of keys
	 * @param hostName the host name to match.
	 * @param userId the userId to match. This may be null. If it is then all users for this host name are matched.
	 * @param respectCase true if case must be respected to match a user id.
	 * @param fuzzy true if a fuzzy host name match is desired.
	 * @return a new array of keys from the original array that match the criteria.
	 */
	private static String[] getMatchingKeys(String[] keys, String hostName, String userId, boolean respectCase, boolean fuzzy) {
		List selectedKeys = new ArrayList();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			String keyHostName = getHostNameFromKey(key);
			String keyUserId = getUserIdFromKey(key);
			boolean match = (userId == null || (respectCase ? userId.equals(keyUserId) : userId.equalsIgnoreCase(keyUserId)));
			if (match) {
				match = hostName.equals(keyHostName);
				if (!match && fuzzy) {
					String phn = hostName.toUpperCase(Locale.US);
					String khn = keyHostName.toUpperCase(Locale.US);
					match = phn.equals(khn);
					if (!match && (phn.startsWith(khn) || khn.startsWith(phn))) {
						khn = RSECorePlugin.getQualifiedHostName(khn);
						phn = RSECorePlugin.getQualifiedHostName(phn);
						match = khn.equalsIgnoreCase(phn);
					}
				}
			}
			if (match) {
				selectedKeys.add(key);
			}
		}
		String[] result = new String[selectedKeys.size()];
		selectedKeys.toArray(result);
		return result;
	}

	/**
	 * Inner class used for storing registered system types.
	 * In particular this class can be used to determine whether a given 
	 * system type supports case-sensitive user id
	 */
	private class RegisteredSystemType {
		private IRSESystemType _systemType;
		private boolean _userIDCaseSensitive;

		protected RegisteredSystemType(IRSESystemType systemType, boolean caseSensitive) {
			_systemType = systemType;
			_userIDCaseSensitive = caseSensitive;
		}

		/**
		 * Returns the system type.
		 * @return the system type.
		 */
		public IRSESystemType getSystemType() {
			return _systemType;
		}

		/**
		 * Returns whether the user ID is case sensitive.
		 * @return <code>true</code> if the user ID is case sensitive, <code>false</code> otherwise.
		 */
		public boolean isUserIDCaseSensitive() {
			return _userIDCaseSensitive;
		}
	}

	private String mapLocation = null;
	private RegisteredSystemType[] systemTypes;

	/**
	 * Singleton so this is a private constructor
	 */
	private PasswordPersistenceManager() {
		String userName = System.getProperty("user.name"); //$NON-NLS-1$
		if (userName == null) {
			userName = DEFAULT_USER_NAME;
		}
		if (isAuthorizationCompatibilityInstalled()) {
			mapLocation = SERVER_URL + userName;
		}
	}
	
	/**
	 * Examine the system type extensions and construct an array of installed system types.
	 */
	private void initializeSystemTypes() {
		IRSESystemType[] sysTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
		systemTypes = new RegisteredSystemType[sysTypes.length];
		for (int i = 0; i < sysTypes.length; i++) {
			systemTypes[i] = new RegisteredSystemType(sysTypes[i], true);
		}
	}

	/**
	 * Retrieve the old password map from the Eclipse keyring for the specified system type.
	 * This uses deprecated APIs only if those APIs are installed.
	 * This should only be used to retrieve maps for migration to the new secure-storage API.
	 * @param systemTypeId the id of the system type to retrieve the map for
	 * @return a Map that maps keys composed of host names and user ids to passwords for this particular
	 * system type. Returns null if the API is not installed or there is no map found.
	 */
	private Map getMap(String systemTypeId) {
		Map passwordMap = null;
		if (mapLocation != null) {
			try {
				URL serverURL = new URL(mapLocation);
				passwordMap = Platform.getAuthorizationInfo(serverURL, systemTypeId, AUTH_SCHEME);
				if (passwordMap == null) {
					URL oldServerURL1 = new URL(SERVER_URL + ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
					passwordMap = Platform.getAuthorizationInfo(oldServerURL1, systemTypeId, AUTH_SCHEME);
					if (passwordMap == null) {
						URL oldServerURL2 = new URL(SERVER_URL);
						passwordMap = Platform.getAuthorizationInfo(oldServerURL2, systemTypeId, AUTH_SCHEME);
					}
				}
			} catch (MalformedURLException e) {
				RSECorePlugin.getDefault().getLogger().logError("PasswordPersistenceManager.getMap", e); //$NON-NLS-1$
			}
		}
		return passwordMap;
	}

	/**
	 * Migrates passwords stored in their old map form into a node of the
	 * secure preferences tree. The old map is left intact.
	 * @param parentNode the parent node of the new node for a system type that will
	 * hold all the passwords for that system type. The new node is created under this 
	 * parent node.
	 * @param systemTypeId the id of this system type to create. The node is named with
	 * this identifier.
	 */
	private void migrateMap(ISecurePreferences parentNode, String systemTypeId) {
		ISecurePreferences systemTypeNode = parentNode.node(systemTypeId);
		Map passwordMap = getMap(systemTypeId);
		if (passwordMap != null) {
			Set entries = passwordMap.entrySet();
			for (Iterator z = entries.iterator(); z.hasNext();) {
				Map.Entry entry = (Map.Entry) z.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				basicPut(systemTypeNode, key, value);
			}
		}
		basicSave(systemTypeNode);
	}

	/**
	 * Returns the preferences node that matches the system type.
	 * It will only return null if secure store access is disallowed.
	 * If secure store access is allowed it will create the node if it does not exist.
	 * If the node does not previous exist then an attempt will be made
	 * to migrate the values from the old map form to this newly created node
	 * of the secure preferences tree.
	 * @param systemType the system type to retrieve
	 * @return the matching secure preferences node. 
	 */
	private ISecurePreferences getNode(IRSESystemType systemType) {
		ISecurePreferences systemTypeNode = null;
		String enableSecureStoreAccess = System.getProperty("rse.enableSecureStoreAccess", "true");  //$NON-NLS-1$//$NON-NLS-2$
		if (enableSecureStoreAccess.equals("true")) { //$NON-NLS-1$
			String id = systemType.getId();
			ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
			ISecurePreferences rseNode = preferences.node("org.eclipse.rse.core.security"); //$NON-NLS-1$
			if (!rseNode.nodeExists(id)) {
				migrateMap(rseNode, id);
			}
			systemTypeNode = rseNode.node(id);
		}
		return systemTypeNode;
	}

	/**
	 * Saves a node of the secure preferences tree.
	 * Logs an error if this cannot be saved for some reason.
	 * @param node the node to save.
	 */
	private void basicSave(ISecurePreferences node) {
		try {
			node.flush();
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Unexpected error saving password.", e); //$NON-NLS-1$
			RSECorePlugin.getDefault().getLog().log(status);
		}
	}
	
	/**
	 * Removes an entry from a node in the secure preferences tree.
	 * This entry is a password for a given host name and user id.
	 * @param node the node from which to remove the password entry.
	 * @param key the key consisting of a host name, user id pair
	 * @see #getKey(String, String)
	 */
	private void basicRemove(ISecurePreferences node, String key) {
		node.remove(key);
	}

	/**
	 * Finds a password in a secure preferences node given its key.
	 * Logs an error if this cannot be found for some malfunction of the storage mechanism.
	 * @param node the node in which to find the password entry.
	 * @param key the key consisting of a host name, user id pair
	 * @see #getKey(String, String)
	 * @return the password associated with this key or null if the 
	 * entry was not found.
	 */
	private String basicGet(ISecurePreferences node, String key) {
		String value = null;
		try {
			value = node.get(key, null);
		} catch (StorageException e) {
			IStatus status = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Unexpected error retrieving password.", e); //$NON-NLS-1$
			RSECorePlugin.getDefault().getLog().log(status);
		}
		return value;
	}

	/**
	 * Writes a password to a secure preferences node.
	 * Logs an error if this cannot be found for some malfunction of the storage mechanism.
	 * @param node the node in which to write the password entry.
	 * @param key the key consisting of a host name, user id pair
	 * @param value the password value to store for this key
	 * @see #getKey(String, String)
	 */
	private void basicPut(ISecurePreferences node, String key, String value) {
		try {
			node.put(key, value, true);
		} catch (StorageException e) {
			IStatus status = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Unexpected error updating password.", e); //$NON-NLS-1$
			RSECorePlugin.getDefault().getLog().log(status);
		}
	}

	/**
	 * Retrieves the password node for the given system type and removes password entries from that node.
	 * Removes only those that match the host name and user id exactly if one such entries exist.
	 * If no such entries exist then it will remove those for the host names that resolve to the same target as the specified host.
	 * @param systemType the IRSESystemType instance to remove passwords for.
	 * @param hostName the name of the host we are removing passwords for.
	 * @param userId the user id to remove passwords for. This may be null. If so then all users for this host name are affected.
	 * @return the number of passwords removed.
	 */
	private int removePassword(IRSESystemType systemType, String hostName, String userId) {
		int result = 0;
		ISecurePreferences passwords = getNode(systemType);
		if (passwords != null) {
			boolean respectCase = isUserIDCaseSensitive(systemType);
			String keys[] = getMatchingKeys(passwords.keys(), hostName, userId, respectCase, false);
			if (keys.length == 0) {
				keys = getMatchingKeys(passwords.keys(), hostName, userId, respectCase, true);
			}
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				basicRemove(passwords, key);
			}
			if (keys.length > 0) {
				basicSave(passwords);
			}
			result = keys.length;
		}
		return result;
	}
	
	/**
	 * Retrieves the password node for the given system type and finds a password in that node.
	 * Initially looks only at those entries that match the host name and user id exactly.
	 * If no such entries exist then it will examine those for the host names that resolve to the same target as the specified host.
	 * @param systemType the IRSESystemType instance to find a password for.
	 * @param hostName the name of the host we are examining for a password.
	 * @param userId the user id to find passwords for.
	 * @return the first such password found that meets these criteria or null if no entry in this 
	 * system type node matches the criteria.
	 */
	private String findPassword(IRSESystemType systemType, String hostName, String userId) {
		String password = null;
		ISecurePreferences passwords = getNode(systemType);
		if (passwords != null) {
			boolean respectCase = isUserIDCaseSensitive(systemType);
			String keys[] = getMatchingKeys(passwords.keys(), hostName, userId, respectCase, false);
			if (keys.length == 0) {
				keys = getMatchingKeys(passwords.keys(), hostName, userId, respectCase, true);
			}
			if (keys.length > 0) {
				String key = keys[0];
				password = basicGet(passwords, key);
			}
		}
		return password;
	}

	/**
	 * Updates the password node for the given system type and finds a password in that node.
	 * Updates only that entry that matches the host name and user id exactly.
	 * If no such entries exist then it will add an entry for that host name and user id.
	 * @param systemType the IRSESystemType instance to find a password for.
	 * @param hostName the name of the host we are examining for a password.
	 * @param userId the user id to find passwords for.
	 * @param password the password to save for this entry.
	 * @return RC_OK if the password was updated, RC_DENIED if the password was not updated.
	 */
	private int updatePassword(IRSESystemType systemType, String hostName, String userId, String password) {
		int result = RC_DENIED;
		ISecurePreferences passwords = getNode(systemType);
		if (passwords != null) {
			String key = getKey(hostName, userId);
			basicPut(passwords, key, password);
			basicSave(passwords);
			result = RC_OK;
		}
		return result;
	}
	
	/**
	 * Resets a given system type. This clears the storage for this system
	 * type and allows it to be re-migrated.
	 * This is not API.
	 * @noreference This method is not intended to be referenced by clients.
	 * @param systemType the system type to reset
	 * @since org.eclipse.rse.core 3.4
	 */
	public void reset(IRSESystemType systemType) {
		ISecurePreferences systemTypeNode = getNode(systemType);
		if (systemTypeNode != null) {
			systemTypeNode.removeNode();
		}
	}
	
	/**
	 * Add a password to the password database.
	 * This will not update the entry for the default system type
	 * @param info The signon information to store
	 * @param overwrite Whether to overwrite any existing entry
	 * @return
	 * RC_OK if the password was successfully stored
	 * RC_ALREADY_EXISTS if the password already exists and overwrite was false
	 * RC_DENIED if passwords may not be saved for this system type and host
	 */
	public int add(SystemSignonInformation info, boolean overwrite) {
		return add(info, overwrite, false);
	}

	/**
	 * Add a password to the password database.
	 * @param info The SystemSignonInformation to store
	 * @param overwrite If true then overwrite the existing entry for this system type, host name, and user id.
	 * @param updateDefault if true then set the entry for the default system type, host name, and user ID, according to the overwrite setting.
	 * @return
	 * RC_OK if the password was successfully stored.
	 * RC_ALREADY_EXISTS if the password already exists and overwrite was false
	 * RC_DENIED if passwords may not be saved for this system type and host
	 */
	public int add(SystemSignonInformation info, boolean overwrite, boolean updateDefault) {
		int result = RC_OK;
		IRSESystemType systemType = info.getSystemType();
		String hostName = info.getHostname();
		String userId = info.getUserId();
		String newPassword = info.getPassword();
		if (isSaveAllowed(systemType, hostName)) {
			if (!isUserIDCaseSensitive(systemType)) {
				userId = userId.toUpperCase();
				info.setUserId(userId);
			}
			if (updateDefault) {
				if (systemType != DEFAULT_SYSTEM_TYPE) {
					SystemSignonInformation newInfo = new SystemSignonInformation(hostName, userId, newPassword, DEFAULT_SYSTEM_TYPE);
					result = add(newInfo, overwrite, false);
				}
			}
			String oldPassword = findPassword(systemType, hostName, userId);
			if (oldPassword == null || (overwrite && !newPassword.equals(oldPassword))) {
				result = updatePassword(systemType, hostName, userId, newPassword);
			} else if (oldPassword != null) {
				result = RC_ALREADY_EXISTS;
			}
		} else {
			result = RC_DENIED;
		}
		return result;
	}

	/**
	 * Determines if password entry exists for a given system type, host name, and user id.
	 * @param systemType the IRSESystemType instance to find a password for.
	 * @param hostName the name of the host we are examining for a password.
	 * @param userId the user id to find passwords for.
	 * @return true if a password exists that meets the search criteria.
	 */
	public boolean passwordExists(IRSESystemType systemType, String hostName, String userId) {
		return passwordExists(systemType, hostName, userId, true);
	}

	/**
	 * Determine if a password entry exists for the specified system type, host name, and user id.
	 * @param systemType the IRSESystemType instance to find a password for.
	 * @param hostName the name of the host we are examining for a password.
	 * @param userId the user id to find passwords for.
	 * @param checkDefault Whether or not to check for a default system type if the specified system type is not found.
	 * @return true if a password exists that meets the search criteria.
	 */
	public boolean passwordExists(IRSESystemType systemType, String hostName, String userId, boolean checkDefault) {
		SystemSignonInformation info = find(systemType, hostName, userId, checkDefault);
		return (info != null);
	}

	/**
	 * Find the password for the specified system type, host name and user id.
	 * If one is not found then the default system type is also searched, thus
	 * the system type in the returned SystemSignonInformation may not be the same as the system type
	 * specified in the argument.
	 * @param systemType the IRSESystemType instance to find a password for.
	 * @param hostName the name of the host we are examining for a password.
	 * @param userId the user id to find passwords for.
	 * @return the {@link SystemSignonInformation} for the specified criteria or null if no such password can be found.
	 */
	public SystemSignonInformation find(IRSESystemType systemType, String hostName, String userId) {
		return find(systemType, hostName, userId, true);
	}

	/**
	 * Find the password for the specified system type, host name and user id.
	 * If one is not found then the default system type is also searched, thus
	 * the system type in the returned SystemSignonInformation may not be the same as the system type
	 * specified in the argument.
	 * @param systemType the IRSESystemType instance to find a password for.
	 * @param hostName the name of the host we are examining for a password.
	 * @param userId the user id to find passwords for.
	 * @param checkDefault true if the default system type should be checked if the specified system type is not found
	 * @return the {@link SystemSignonInformation} for the specified criteria or null if no such password can be found.
	 */
	public SystemSignonInformation find(IRSESystemType systemType, String hostName, String userId, boolean checkDefault) {
		SystemSignonInformation result = null;
		if (!(systemType == null || hostName == null || userId == null)) {
			if (!isUserIDCaseSensitive(systemType)) {
				userId = userId.toUpperCase();
			}
			String password = findPassword(systemType, hostName, userId);
			if (password != null) {
				result = new SystemSignonInformation(hostName, userId, password, systemType);
			}
			if (result == null && checkDefault && !systemType.equals(DEFAULT_SYSTEM_TYPE)) {
				password = findPassword(DEFAULT_SYSTEM_TYPE, hostName, userId);
				if (password != null) {
					result = new SystemSignonInformation(hostName, userId, password, DEFAULT_SYSTEM_TYPE);
				}
			}
		}
		return result;
	}

	/**
	 * Remove the password entry that matches the system type, host name and
	 * user ID from the SystemSignonInfo parameter.
	 */
	public void remove(SystemSignonInformation info) {
		remove(info.getSystemType(), info.getHostname(), info.getUserId());
	}
	
	/**
	 * Removes all passwords for a host name for a given system type.
	 * This does not remove entries for the default system type.
	 * The default system type must be explicitly stated to remove those entries.
	 * @param systemType The system type of the host
	 * @param hostName The IP address or name of the host in canonical format
	 * @return the number of passwords removed
	 * @since org.eclipse.rse.core 3.0
	 */
	public int remove(IRSESystemType systemType, String hostName) {
		int result = removePassword(systemType, hostName, null);
		return result;
	}

	/**
	 * Removes all entries that match the host name, user id, and system type.
	 * This does not remove entries for the default system type.
	 * The default system type must be explicitly stated to remove those entries.
	 * @param systemType the systemType
	 * @param hostName the connection name
	 * @param userId the user id
	 */
	public void remove(IRSESystemType systemType, String hostName, String userId) {
		if (!isUserIDCaseSensitive(systemType)) {
			userId = userId.toUpperCase();
		}
		removePassword(systemType, hostName, userId);
	}

	/**
	 * Retrieve the list of registered system types.
	 * This includes the default system type as well.
	 * @return an array of {@link IRSESystemType}.
	 */
	public IRSESystemType[] getRegisteredSystemTypes() {
		IRSESystemType[] types = new IRSESystemType[systemTypes.length + 1];
		types[0] = DEFAULT_SYSTEM_TYPE;
		for (int i = 0; i < systemTypes.length; i++) {
			types[i + 1] = systemTypes[i].getSystemType();
		}
		return types;
	}

	/**
	 * Retrieves a list of SystemSignonInformation instances that have been saved.
	 * These instances do not contain the saved passwords.
	 * @return List A list of the stored user IDs as SystemSignonInformation instances
	 * without the saved passwords.
	 */
	public List getSavedUserIDs() {
		List savedUserIDs = new ArrayList();
		IRSESystemType[] systemTypes = getRegisteredSystemTypes();
		for (int i = 0; i < systemTypes.length; i++) {
			IRSESystemType systemType = systemTypes[i];
			ISecurePreferences node = getNode(systemType);
			if (node != null) {
				String[] keys = node.keys();
				for (int j = 0; j < keys.length; j++) {
					String key = keys[j];
					String hostName = getHostNameFromKey(key);
					String userId = getUserIdFromKey(key);
					SystemSignonInformation info = new SystemSignonInformation(hostName, userId, systemType);
					savedUserIDs.add(info);
				}
			}
		}
		return savedUserIDs;
	}

	/**
	 * Helper method for determining if system type uses case sensitive user IDs.
	 * @return true if the system type treats user ids as case sensitive.
	 */
	public boolean isUserIDCaseSensitive(IRSESystemType systemType) {
		boolean result = true;
		for (int i = 0; i < systemTypes.length; i++) {
			if (systemTypes[i].getSystemType().equals(systemType)) {
				result = systemTypes[i].isUserIDCaseSensitive();
				break;
			}
		}
		return result;
	}

}