/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * David Dykstal (IBM) - [197167] adding notification and waiting for RSE model
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [210474] Deny save password function missing
 * David Dykstal (IBM) - [233892] Deny password save is not persistent
 ********************************************************************************/
package org.eclipse.rse.core;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.core.runtime.Preferences;

/**
 * Preferences Manager utility class.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class RSEPreferencesManager {

	/**
	 * Used as a suffix when combined with the system type name to produce the key for the enabled value.
	 */
	private static final String ST_ENABLED = "systemType.enabled"; //$NON-NLS-1$
	/**
	 * Used as a suffix when combined with the system type name to produce the key for the default user id value.
	 */
	private static final String ST_DEFAULT_USERID = "systemType.defaultUserId"; //$NON-NLS-1$
	/**
	 * The default value for using deferred queries. Value is <code>true</code>.
	 */
	private static final boolean DEFAULT_USE_DEFERRED_QUERIES = true;
	/**
	 * The default value for the name of the team profile. Value is "Team".
	 */
	private static final String DEFAULT_TEAMPROFILE = "Team"; //$NON-NLS-1$
	/**
	 * The default value for the list of active user profiles. Value is "Team".
	 */
	private static final String DEFAULT_ACTIVEUSERPROFILES = "Team"; //$NON-NLS-1$

	/**
	 * @return the Hashtable where the key is a string identifying a particular object, and
	 * the value is the user ID for that object.
	 * @see #setUserId(String, String)
	 */
	private static Hashtable getUserIds() {
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		Hashtable userIds = null;
		String value = store.getString(IRSEPreferenceNames.USERIDPERKEY);
		if (value != null) {
			userIds = parseString(value);
		} else {
			userIds = new Hashtable();
		}
		return userIds;
	}

	/**
	 * Store the user ids that are saved keyed by some key.
	 * @param userIds A Hashtable of userids.
	 * @see #setUserId(String, String)
	 */
	private static void setUserIds(Hashtable userIds) {
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		String userIdsString = makeString(userIds);
		store.setValue(IRSEPreferenceNames.USERIDPERKEY, userIdsString);
		savePreferences();
	}

	/**
	 * Retrieves a user id given a key.
	 * @param key the key from which to get the particular user id.
	 * @return user ID for the given key or null if the key was not found.
	 * @see #setUserId(String, String)
	 */
	public static String getUserId(String key) {
		String uid = null;
		Hashtable userIds = getUserIds();
		uid = (String) userIds.get(key);
		return uid;
	}

	/**
	 * Clears the user ID for the given key.
	 * @param key the key for the user id.
	 * @see #setUserId(String, String)
	 */
	public static void clearUserId(String key) {
		Hashtable userIds = getUserIds();
		if (userIds.containsKey(key)) {
			userIds.remove(key);
			setUserIds(userIds);
		}
	}

	/**
	 * Sets the user Id for this key.
	 * The key typically designates a scope for this userid so that a hierarchy
	 * of user ids can be maintained for inheritance.
	 * For example, hosts have greater scope than subsystems.
	 * A key would typically be <profile-name>.<host-name>,
	 * or <profile-name>.<host-type>,
	 * or <profile-name>.<host-name>.<subsystem-name>.
	 * @param key the key used to find the userId
	 * @param userId the userId to retrieve by this key.
	 */
	public static void setUserId(String key, String userId) {
		if ((key != null) && (userId != null)) {
			Hashtable userIds = getUserIds();
			String storedUserId = (String) userIds.get(key);
			if (storedUserId == null || !storedUserId.equals(userId)) { // don't bother updating if its already there
				userIds.put(key, userId);
				setUserIds(userIds);
			}
		}
	}

	/**
	 * Gets the default user id for a given system type.
	 * @param systemType the systemtype for which to retrieve the default user id
	 * @return The default user id
	 */
	public static String getDefaultUserId(IRSESystemType systemType) {
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		String key = getSystemTypePreferencesKey(systemType, RSEPreferencesManager.ST_DEFAULT_USERID);
		if (!store.contains(key)) {
			store.setDefault(key, System.getProperty("user.name")); //$NON-NLS-1$
		}
		String result = store.getString(key);
		return result;
	}

	/**
	 * Sets the default userId for the given system type.
	 * @param systemType the system type for which to set the default
	 * @param userId the default user id for this system type.
	 * This may be null to "unset" the default.
	 */
	public static void setDefaultUserId(IRSESystemType systemType, String userId) {
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		String key = getSystemTypePreferencesKey(systemType, RSEPreferencesManager.ST_DEFAULT_USERID);
		store.setValue(key, userId);
		savePreferences();
	}

	/**
	 * Gets the system type values table for editing. This is a synthesized preference
	 * that is handled as a single value. Rows are separated by semi-colons.
	 * Each row is of the format <systemTypeId>=<enabled>+<defaultUserId>;
	 * @return the table of system types formatted as a single string
	 */
	public static String getSystemTypeValues() {
		IRSESystemType[] systemTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
		StringBuffer buffer = new StringBuffer(100);
		for (int i = 0; i < systemTypes.length; i++) {
			IRSESystemType systemType = systemTypes[i];
			buffer.append(systemType.getId());
			buffer.append('=');
			buffer.append(getIsSystemTypeEnabled(systemType));
			buffer.append('+');
			buffer.append(getDefaultUserId(systemType));
			buffer.append(';');
		}
		String result = buffer.toString();
		return result;
	}

	/**
	 * Sets the default user id and enabled state for all system types.
	 * @param systemTypeValues a tabled encoded as a string that contains
	 * entries for each system type. See {@link #getSystemTypeValues()} for the
	 * table format.
	 */
	public static void setSystemTypeValues(String systemTypeValues) {
		IRSECoreRegistry registry = RSECorePlugin.getTheCoreRegistry();
		Hashtable table = parseString(systemTypeValues);
		Enumeration e = table.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String compoundValue = (String) table.get(key);
			String[] values = compoundValue.split("\\+"); //$NON-NLS-1$
			String isEnabled = values[0];
			String defaultUserId = values[1];
			IRSESystemType systemType = registry.getSystemTypeById(key);
			setIsSystemTypeEnabled(systemType, isEnabled.equals("true")); //$NON-NLS-1$
			setDefaultUserId(systemType, defaultUserId);
		}
	}

	/**
	 * Sets if a system type is enabled.
	 * @param systemType the system type to be enabled on this machine.
	 * @param isEnabled the enabled state
	 */
	public static void setIsSystemTypeEnabled(IRSESystemType systemType, boolean isEnabled) {
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		String key = getSystemTypePreferencesKey(systemType, RSEPreferencesManager.ST_ENABLED);
		if (!store.contains(key)) {
			store.setDefault(key, true);
		}
		store.setValue(key, isEnabled);
		savePreferences();
	}

	/**
	 * Gets the enabled state for a particular system type.
	 * @param systemType the system type
	 * @return the enabled state of that type
	 */
	public static boolean getIsSystemTypeEnabled(IRSESystemType systemType) {
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		String key = getSystemTypePreferencesKey(systemType, RSEPreferencesManager.ST_ENABLED);
		if (!store.contains(key)) {
			store.setDefault(key, true);
		}
		boolean result = store.getBoolean(key);
		return result;
	}

	private static String getSystemTypePreferencesKey(IRSESystemType systemType, String preference) {
		String key = systemType.getId() + "." + preference; //$NON-NLS-1$
		return key;
	}

	/**
	 * @return the names of the profiles the user has elected to make active.
	 */
	public static String[] getActiveProfiles() {
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		String value = store.getString(IRSEPreferenceNames.ACTIVEUSERPROFILES);
		String[] result = parseStrings(value);
		return result;
	}

	/**
	 * Sets the names of the profiles the user has elected to make "active".
	 * The caller must also save the preferences when completing.
	 * @see SystemPreferencesManager#savePreferences()
	 * @param newProfileNames an array of profile names considered to be active.
	 */
	private static void setActiveProfiles(String[] newProfileNames) {
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		store.setValue(IRSEPreferenceNames.ACTIVEUSERPROFILES, makeString(newProfileNames));
		savePreferences();
	}

	/**
	 * Add a name to the active profile list.
	 * A name already in the list is not added again.
	 * The list remains sorted in the natural order.
	 * @param newName a new active profile name
	 */
	public static void addActiveProfile(String newName) {
		SortedSet names = new TreeSet(Arrays.asList(getActiveProfiles()));
		names.add(newName);
		String[] newNames = new String[names.size()];
		names.toArray(newNames);
		RSEPreferencesManager.setActiveProfiles(newNames);
		savePreferences();
	}

	/**
	 * Delete one of the active profile names in the list of names stored in the registry.
	 * @param oldName the name of the profile to remove from the active profiles list.
	 */
	public static void deleteActiveProfile(String oldName) {
		String[] names = getActiveProfiles();
		int matchPos = -1;
		for (int idx = 0; (matchPos == -1) && (idx < names.length); idx++) {
			if (names[idx].equalsIgnoreCase(oldName)) {
				matchPos = idx;
				names[idx] = null;
			}
		}
		if (matchPos >= 0) {
			RSEPreferencesManager.setActiveProfiles(names);
			savePreferences();
		}
	}

	/**
	 * @param profileName the name of the profile to search for in the list of active profiles.
	 * @return the zero-based position of a give profile name in the active list
	 */
	public static int getActiveProfilePosition(String profileName) {
		String[] names = getActiveProfiles();
		int matchPos = -1;
		for (int idx = 0; (matchPos == -1) && (idx < names.length); idx++) {
			if (names[idx].equalsIgnoreCase(profileName)) matchPos = idx;
		}
		return matchPos;
	}

	/**
	 * Renames one of the active profile names in the list of names stored in the registry.
	 * This is usually employed after renaming a profile to ensure that the active names
	 * list stays in synch with the actual profile names. The active state of the profiles
	 * cannot be kept in the profiles themselves since that can vary from workspace to workspace
	 * for profiles that are shared in a team.
	 * @param oldName the old name of the profile
	 * @param newName the new name of the profile
	 */
	public static void renameActiveProfile(String oldName, String newName) {
		// update active profile name list
		String[] names = getActiveProfiles();
		int matchPos = -1;
		for (int idx = 0; (matchPos == -1) && (idx < names.length); idx++) {
			if (names[idx].equalsIgnoreCase(oldName)) {
				matchPos = idx;
				names[idx] = newName;
			}
		}
		if (matchPos >= 0) {
			RSEPreferencesManager.setActiveProfiles(names);
			savePreferences();
		}
	}

	public static void initDefaults() {
		String defaultProfileNames = RSEPreferencesManager.DEFAULT_ACTIVEUSERPROFILES;
		String userProfileName = getDefaultPrivateSystemProfileName();
		defaultProfileNames += ";" + userProfileName; //$NON-NLS-1$
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		store.setDefault(IRSEPreferenceNames.ACTIVEUSERPROFILES, defaultProfileNames);
		store.setDefault(IRSEPreferenceNames.USE_DEFERRED_QUERIES, RSEPreferencesManager.DEFAULT_USE_DEFERRED_QUERIES);
		savePreferences();
	}

	/**
	 * Save the preference store.
	 */
	private static void savePreferences() {
		RSECorePlugin.getDefault().savePluginPreferences();
	}

	/**
	 * @return The name of the default private system profile. This
	 * is typically the short name of the host machine or the name of the
	 * user account.
	 */
	public static String getDefaultPrivateSystemProfileName() {
		String name = RSECorePlugin.getLocalMachineName();
		if (name != null) {
			int i = name.indexOf('.');
			if (i > 0) {
				name = name.substring(0, i);
			}
		}
		if (name == null) {
			name = System.getProperty("user.name"); //$NON-NLS-1$
		}
		return name;
	}

	/**
	 * @return the name of the default team system profile.
	 */
	public static String getDefaultTeamProfileName() {
		String name = RSEPreferencesManager.DEFAULT_TEAMPROFILE;
		return name;
	}

	/**
	 * Convert table of key-value pairs into a single string. Each (name, value) pair is
	 * encoded as "name=value;" thus no keys or values in the string may
	 * contain semi-colons or equal signs.
	 * @param table a Hashtable to convert
	 * @return the string containing the converted table
	 */
	private static String makeString(Hashtable table) {
		Enumeration keys = table.keys();
		StringBuffer sb = new StringBuffer(20 * table.size());
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = (String) table.get(key);
			if ((value != null) && (value.length() > 0)) {
				sb.append(key);
				sb.append('=');
				sb.append(value);
				sb.append(';');
			}
		}
		return sb.toString();
	}

	/**
	 * Make a single string out of an array of strings. A semicolon is used as a
	 * delimiter between the separate values. No value in the array can contain
	 * a semicolon.
	 * 
	 * @param values the array of strings to condense into a single one
	 * @return the condensed string
	 */
	private static String makeString(String[] values) {
		StringBuffer allValues = new StringBuffer(20 * values.length);
		for (int idx = 0; idx < values.length; idx++) {
			if (values[idx] != null) {
				if (idx > 0) {
					allValues = allValues.append(';');
				}
				allValues.append(values[idx]);
			}
		}
		return allValues.toString();
	}

	/**
	 * Parse out list of multiple values into a string array per value.
	 * This is the inverse of the {@link #makeString(String[])} operation.
	 * @param allvalues the string holding the condensed value
	 * @return the reconstituted array of strings.
	 */
	private static String[] parseStrings(String allvalues) {
		if (allvalues == null) return new String[0];
		String[] tokens = allvalues.split(";"); //$NON-NLS-1$
		return tokens;
	}

	/**
	 * Parse out list of key-value pairs into a Hashtable. This is the inverse
	 * of the {@link SystemPreferencesManager#makeString(Hashtable)} operation.
	 * 
	 * @param allValues the string containing the key-value pairs. If empty or
	 *            null returns and empty Hashtable.
	 * @return the reconstituted Hashtable
	 */
	private static Hashtable parseString(String allValues) {
		Hashtable keyValues = new Hashtable(10);
		if (allValues != null) {
			StringTokenizer tokens = new StringTokenizer(allValues, "=;"); //$NON-NLS-1$
			int count = 0;
			String token1 = null;
			String token2 = null;
			while (tokens.hasMoreTokens()) {
				count++;
				if ((count % 2) == 0) // even number
				{
					token2 = tokens.nextToken();
					keyValues.put(token1, token2);
				} else
					token1 = tokens.nextToken();
			}
		}
		return keyValues;
	}

	/**
	 * Get the Preference setting whether the local connection should be created
	 * by default or not.
	 * 
	 * @return the boolean value indicating whether or not to create a local
	 *         connection on a fresh workspace.
	 * @since org.eclipse.rse.core 3.0
	 */
	public static boolean getCreateLocalConnection() {
		Preferences prefs = RSECorePlugin.getDefault().getPluginPreferences();
		boolean result = prefs.getBoolean(IRSEPreferenceNames.CREATE_LOCAL_CONNECTION);
		return result;
	}

	/**
	 * Sets the preference for a particular system type and host address that causes passwords
	 * not to be savable. The default for this attribute is false - that is, passwords are savable for
	 * all system types and hosts.
	 * @param systemType The system type of this preference.
	 * @param hostAddress The host address of this preference
	 * @param deny true if save of passwords is to be denied. false is save is to be allowed.
	 * If true then all passwords that have been saved for this system type and host address are removed.
	 * All passwords saved for the default system type and host address are also removed.
	 * @return the number of passwords removed if deny was set to true
	 * @since org.eclipse.rse.core 3.0
	 */
	public static int setDenyPasswordSave(IRSESystemType systemType, String hostAddress, boolean deny) {
		int result = 0;
		Preferences preferences = RSECorePlugin.getDefault().getPluginPreferences();
		String preferenceName = getPasswordSavePreferenceName(systemType, hostAddress);
		preferences.setValue(preferenceName, deny);
		if (deny) {
			result = PasswordPersistenceManager.getInstance().remove(systemType, hostAddress);
			result += PasswordPersistenceManager.getInstance().remove(PasswordPersistenceManager.DEFAULT_SYSTEM_TYPE, hostAddress);
		}
		savePreferences();
		return result;
	}

	/**
	 * Retrieves the preference for a particular system type and host address that determines if passwords
	 * can be saved. The default for this attribute is false, that is, save is not denied, 
	 * thus passwords are savable.
	 * @param systemType
	 * @param hostAddress
	 * @return true if saving of passwords is denied. false if saving is allowed.
	 * @since org.eclipse.rse.core 3.0
	 */
	public static boolean getDenyPasswordSave(IRSESystemType systemType, String hostAddress) {
		Preferences preferences = RSECorePlugin.getDefault().getPluginPreferences();
		String preferenceName = getPasswordSavePreferenceName(systemType, hostAddress);
		boolean result = preferences.getBoolean(preferenceName);
		return result;
	}

	/**
	 * Retrieves the "denyPasswordSave" preference name of a particular host address. 
	 * @param systemType The system type we are concerned with
	 * @param hostAddress The host address, typically an IP address.
	 * @return the name associated with this preference.
	 * This name is of the form {systemTypeId}___{hostAddress}___DENY_PASSWORD_SAVE.
	 */
	private static String getPasswordSavePreferenceName(IRSESystemType systemType, String hostAddress) {
		StringBuffer b = new StringBuffer(100);
		b.append(systemType.getId());
		b.append("___"); //$NON-NLS-1$
		b.append(hostAddress.toUpperCase(Locale.US)); // should use US locale for IP names and addresses
		b.append("___DENY_PASSWORD_SAVE"); //$NON-NLS-1$
		String preferenceName = b.toString();
		return preferenceName;
	}

	/*
	 * Having this method private disables instance creation.
	 */
	private RSEPreferencesManager() {
	}

}
