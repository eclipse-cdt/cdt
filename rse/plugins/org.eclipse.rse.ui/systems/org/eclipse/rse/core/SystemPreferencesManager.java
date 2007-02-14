/********************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.core;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.model.SystemProfileManager;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * A utility class that encapsulates all global preferences for the remote system framework
 * for both core and UI preferences.
 * <p>
 * These include:
 * <ul>
 *   <li>The list of profile names that are active
 *   <li>The default user Id per system type
 *   <li>The global setting about whether to show filter pools
 *   <li>The global setting about whether to show filter strings
 *   <li>
 * </ul>
 * This class should not be subclassed.
 */
public class SystemPreferencesManager {
	
	/*
	 * The following are preferences that may be set from the 
	 * system properties.
	 */
	private static boolean showLocalConnection;
	private static boolean showProfilePage;
	private static boolean showNewConnectionPrompt;

	private static void migrateCorePreferences() {
		String[] keys = {
				IRSEPreferenceNames.ACTIVEUSERPROFILES, 
				IRSEPreferenceNames.SYSTEMTYPE, 
				IRSEPreferenceNames.USE_DEFERRED_QUERIES, 
				IRSEPreferenceNames.USERIDKEYS,
				IRSEPreferenceNames.USERIDPERKEY
		};
		for (int i = 0; i < keys.length; i++) {
			migrateCorePreference(keys[i]);
		}
	}

	private static void migrateCorePreference(String preferenceName) {
		String name = ISystemPreferencesConstants.ROOT + preferenceName;
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		if (store.contains(name)) {
			String value = store.getString(name);
			String defaultValue = store.getDefaultString(name);
			store.setToDefault(name);
			store.setDefault(name, "*migrated*"); //$NON-NLS-1$
			store = RSECorePlugin.getDefault().getPluginPreferences();
			store.setDefault(preferenceName, defaultValue);
			store.setValue(preferenceName, value);
		}
	}

	/**
	 * Determines if a string (the needle) is present in an array of strings (the haystack)
	 * @param haystack an array of strings to search
	 * @param needle the string for which to search
	 * @return true if the needle was found
	 */
	private static boolean find(String[] haystack, String needle) {
		for (int idx = 0; idx < haystack.length; idx++) {
			if (haystack[idx].equals(needle)) return true;
		}
		return false;
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
	 * Parse out list of key-value pairs into a hashtable. This is the inverse of the
	 * {@link #makeString(Hashtable)} operation.
	 * @param allValues the string containing the key-value pairs. If empty or null returns
	 * and empty Hashtable.
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
	 * Make a single string out of an array of strings. A semi-colon is
	 * used as a delimiter between the separate values. No value in the
	 * array can contain a semi-colon.
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
	 * Initialize our preference store with our defaults.
	 * This is called once at plugin startup.
	 */
	public static void initDefaults() {
		migrateCorePreferences();
		initDefaultsCore();
		initDefaultsUI();
		initDefaultsComm();
		savePreferences();
	}

	private static void initDefaultsUI() {
		RSEUIPlugin ui = RSEUIPlugin.getDefault();
		Preferences store = ui.getPluginPreferences();
		showNewConnectionPrompt= getBooleanProperty("rse.showNewConnectionPrompt", ISystemPreferencesConstants.DEFAULT_SHOWNEWCONNECTIONPROMPT); //$NON-NLS-1$
	    showLocalConnection = getBooleanProperty("rse.showLocalConnection", true); //$NON-NLS-1$
	    showProfilePage = getBooleanProperty("rse.showProfilePage", false); //$NON-NLS-1$
		store.setDefault(ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE, ISystemPreferencesConstants.DEFAULT_RESTORE_STATE_FROM_CACHE);
		store.setDefault(ISystemPreferencesConstants.QUALIFY_CONNECTION_NAMES, ISystemPreferencesConstants.DEFAULT_QUALIFY_CONNECTION_NAMES);
		store.setDefault(ISystemPreferencesConstants.SHOWFILTERPOOLS, ISystemPreferencesConstants.DEFAULT_SHOWFILTERPOOLS);
		store.setDefault(ISystemPreferencesConstants.ORDER_CONNECTIONS, ISystemPreferencesConstants.DEFAULT_ORDER_CONNECTIONS);
		store.setDefault(ISystemPreferencesConstants.HISTORY_FOLDER, ISystemPreferencesConstants.DEFAULT_HISTORY_FOLDER);
		store.setDefault(ISystemPreferencesConstants.REMEMBER_STATE, ISystemPreferencesConstants.DEFAULT_REMEMBER_STATE);
		store.setDefault(ISystemPreferencesConstants.CASCADE_UDAS_BYPROFILE, ISystemPreferencesConstants.DEFAULT_CASCADE_UDAS_BYPROFILE);
	    store.setDefault(ISystemPreferencesConstants.SHOWNEWCONNECTIONPROMPT, showNewConnectionPrompt);
	    savePreferences();
	}
	
	private static boolean getBooleanProperty(String propertyName, boolean defaultValue) {
		String property = System.getProperty(propertyName);
		boolean value = (property == null) ? defaultValue : property.equals(Boolean.toString(true));
		return value;
	}
	
	private static void initDefaultsCore() {
		String defaultProfileNames = IRSEPreferenceNames.DEFAULT_ACTIVEUSERPROFILES;
		String userProfileName = SystemProfileManager.getDefaultPrivateSystemProfileName();
		defaultProfileNames += ";" + userProfileName; //$NON-NLS-1$
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		store.setDefault(IRSEPreferenceNames.SYSTEMTYPE, IRSEPreferenceNames.DEFAULT_SYSTEMTYPE);
		store.setDefault(IRSEPreferenceNames.ACTIVEUSERPROFILES, defaultProfileNames);
		store.setDefault(IRSEPreferenceNames.USE_DEFERRED_QUERIES, IRSEPreferenceNames.DEFAULT_USE_DEFERRED_QUERIES);
		savePreferences();
	}

	/**
	 * Set default communications preferences
	 */
	private static void initDefaultsComm() {
		RSEUIPlugin ui = RSEUIPlugin.getDefault();
		Preferences store = ui.getPluginPreferences();
		store.setDefault(ISystemPreferencesConstants.DAEMON_AUTOSTART, ISystemPreferencesConstants.DEFAULT_DAEMON_AUTOSTART);
		store.setDefault(ISystemPreferencesConstants.DAEMON_PORT, ISystemPreferencesConstants.DEFAULT_DAEMON_PORT);
		ui.savePluginPreferences();
	}

	public static boolean getShowLocalConnection() {
		return showLocalConnection;
	}

	public static boolean getShowProfilePage() {
		return showProfilePage;
	}

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
		String key = getSystemTypePreferencesKey(systemType, IRSEPreferenceNames.ST_DEFAULT_USERID);
		if (!store.contains(key)) {
			store.setDefault(key, System.getProperty("user.name")); //$NON-NLS-1$
		}
		String result = store.getString(key);
		return result;
	}

	/**
	 * Sets the default userId for the given system type.
	 * @param systemTypeName the name of the system type
	 * @param userId the default user id for this system type.
	 * This may be null to "unset" the default.
	 */
	public static void setDefaultUserId(String systemTypeName, String userId) {
		IRSESystemType systemType = RSECorePlugin.getDefault().getRegistry().getSystemType(systemTypeName);
		if (systemType != null) {
			setDefaultUserId(systemType, userId);
		}
	}

	/**
	 * Sets the default userId for the given system type.
	 * @param systemType the system type for which to set the default
	 * @param userId the default user id for this system type.
	 * This may be null to "unset" the default.
	 */
	public static void setDefaultUserId(IRSESystemType systemType, String userId) {
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		String key = getSystemTypePreferencesKey(systemType, IRSEPreferenceNames.ST_DEFAULT_USERID);
		store.setValue(key, userId);
		savePreferences();
	}

	/**
	 * Gets the system type values table for editing. This is a synthesized preference
	 * that is handled as a single value. Rows are separated by semi-colons.
	 * Each row is of the format <systemTypeName>=<enabled>+<defaultUserId>;
	 * @return the table of system types formatted as a single string
	 */
	public static String getSystemTypeValues() {
		IRSESystemType[] systemTypes = RSECorePlugin.getDefault().getRegistry().getSystemTypes();
		StringBuffer buffer = new StringBuffer(100);
		for (int i = 0; i < systemTypes.length; i++) {
			IRSESystemType systemType = systemTypes[i];
			buffer.append(systemType.getName());
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
	 * @return the system type to default when no explicit system type is available.
	 */
	public static String getSystemType() {
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		String result = store.getString(IRSEPreferenceNames.SYSTEMTYPE);
		return result;
	}

	/**
	 * Sets the system type to default when no explicit system type is available.
	 * @param systemType the string giving the system type name.
	 */
	public static void setSystemType(String systemType) {
		Preferences store = RSECorePlugin.getDefault().getPluginPreferences();
		store.setValue(IRSEPreferenceNames.SYSTEMTYPE, systemType);
		savePreferences();
	}

	/**
	 * Sets the default user id and enabled state for all system types.
	 * @param systemTypeValues a tabled encoded as a string that contains
	 * entries for each system type. See {@link #getSystemTypeValues()} for the
	 * table format.
	 */
	public static void setSystemTypeValues(String systemTypeValues) {
		IRSECoreRegistry registry = RSECorePlugin.getDefault().getRegistry();
		Hashtable table = parseString(systemTypeValues);
		Enumeration e = table.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String compoundValue = (String) table.get(key);
			String[] values = compoundValue.split("\\+"); //$NON-NLS-1$
			String isEnabled = values[0];
			String defaultUserId = values[1];
			IRSESystemType systemType = registry.getSystemType(key);
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
		String key = getSystemTypePreferencesKey(systemType, IRSEPreferenceNames.ST_ENABLED);
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
		String key = getSystemTypePreferencesKey(systemType, IRSEPreferenceNames.ST_ENABLED);
		if (!store.contains(key)) {
			store.setDefault(key, true);
		}
		boolean result = store.getBoolean(key);
		return result;
	}

	private static String getSystemTypePreferencesKey(IRSESystemType systemType, String preference) {
		String key = systemType.getName() + "." + preference; //$NON-NLS-1$
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
	 * @see #savePreferences()
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
		SystemPreferencesManager.setActiveProfiles(newNames);
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
			SystemPreferencesManager.setActiveProfiles(names);
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
			SystemPreferencesManager.setActiveProfiles(names);
			savePreferences();
		}
	}

	/**
	 * @return true if the user has elected to show user defined actions cascaded by profile
	 */
	public static boolean getCascadeUserActions() {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		return store.getBoolean(ISystemPreferencesConstants.CASCADE_UDAS_BYPROFILE);
	}

	/**
	 * Sets if the user has elected to show user defined actions cascaded by profile.
	 * Does not save the preferences. This must be done by the caller.
	 * @see #savePreferences()
	 * @param cascade whether or not to cascade user action menus
	 */
	public static void setCascadeUserActions(boolean cascade) {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		store.setValue(ISystemPreferencesConstants.CASCADE_UDAS_BYPROFILE, cascade);
	}

	/**
	 * @return the ordered list of connection names.
	 * This is how user arranged his connections in the system view.
	 */
	public static String[] getConnectionNamesOrder() {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		return parseStrings(store.getString(ISystemPreferencesConstants.ORDER_CONNECTIONS));
	}

	/**
	 * Gets the user's preference for the order of the connection names within a given profile
	 * @param profileName the name of the profile to return the connection names for.
	 * @return an array of connection names for this profile in the order preferred by the user.
	 */
	public static String[] getConnectionNamesOrder(String profileName) {
		String[] allConnectionNamesOrder = SystemPreferencesManager.getConnectionNamesOrder();
		profileName = profileName + "."; //$NON-NLS-1$
		int profileNameLength = profileName.length();
		Vector v = new Vector();
		for (int idx = 0; idx < allConnectionNamesOrder.length; idx++)
			if (allConnectionNamesOrder[idx].startsWith(profileName)) {
				v.addElement(allConnectionNamesOrder[idx].substring(profileNameLength));
			}
		String[] names = new String[v.size()];
		for (int idx = 0; idx < names.length; idx++) {
			names[idx] = (String) v.elementAt(idx);
		}
		return names;
	}

	/**
	 * Gets the user's preference for the order of a given list of connections,
	 * after resolving it against the actual list of connection names contained within
	 * a specified profile.
	 * Connections not in the given profile will be ignored.
	 * @param realityConnectionList The list of connections that will be reordered according to
	 * the user's preferred ordering.
	 * @param profileName the name of the profile that we will search for these connections.
	 * @return the list of connection names from the given list and profile in the order
	 * preferred by the user.
	 */
	public static String[] getConnectionNamesOrder(IHost[] realityConnectionList, String profileName) {
		if (realityConnectionList == null) return new String[0];
		String[] realityNames = new String[realityConnectionList.length];
		for (int idx = 0; idx < realityConnectionList.length; idx++) {
			realityNames[idx] = realityConnectionList[idx].getAliasName();
		}
		String[] names = resolveOrderPreferenceVersusReality(realityNames, getConnectionNamesOrder(profileName));
		return names;
	}

	/**
	 * Sets user's preference for the order of the connection names according to the 
	 * list kept in the system registry.
	 */
	public static void setConnectionNamesOrder() {
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		IHost[] conns = sr.getHosts();
		String[] names = new String[conns.length];
		for (int idx = 0; idx < names.length; idx++)
			names[idx] = conns[idx].getSystemProfileName() + "." + conns[idx].getAliasName(); //$NON-NLS-1$
		setConnectionNamesOrder(names);
	}

	/**
	 * Sets the ordered list of connection names.
	 * This is how user arranged connections in the system view.
	 * @param connectionNames an array of connection names in the order they are to be presented.
	 */
	public static void setConnectionNamesOrder(String[] connectionNames) {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		store.setValue(ISystemPreferencesConstants.ORDER_CONNECTIONS, makeString(connectionNames));
		savePreferences();
	}

	/**
	 * Resolves differences between two ordered name lists.
	 * Used when there are differences between the actual list of names and
	 * a restored ordered list of names.
	 */
	private static String[] resolveOrderPreferenceVersusReality(String[] reality, String[] ordered) {
		Vector finalList = new Vector();
		// step 1: include all names from preferences list which do exist in reality...
		for (int idx = 0; idx < ordered.length; idx++) {
			if (find(reality, ordered[idx])) finalList.addElement(ordered[idx]);
		}
		// step 2: add all names in reality which do not exist in preferences list...
		for (int idx = 0; idx < reality.length; idx++) {
			if (!find(ordered, reality[idx])) finalList.addElement(reality[idx]);
		}
		String[] resolved = new String[finalList.size()];
		finalList.toArray(resolved);
		return resolved;
	}

	/**
	 * @return the history for the folder combo box widget
	 */
	public static String[] getFolderHistory() {
		return getWidgetHistory(ISystemPreferencesConstants.HISTORY_FOLDER);
	}

	/**
	 * Sets the history for the folder combo box widget.
	 * Does not save the preferences. This must be done by the caller.
	 * @see #savePreferences()
	 * @param newHistory the names of the folders to be saved in the folder history
	 */
	public static void setFolderHistory(String[] newHistory) {
		setWidgetHistory(ISystemPreferencesConstants.HISTORY_FOLDER, newHistory);
	}

	/**
	 * @return true if the user has elected to show connection names qualified by profile
	 */
	public static boolean getQualifyConnectionNames() {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		return store.getBoolean(ISystemPreferencesConstants.QUALIFY_CONNECTION_NAMES);
	}

	/**
	 * Sets if the user has elected to show connection names qualified by profile.
	 * @param qualify whether or not to qualify the connection names in the UI.
	 */
	public static void setQualifyConnectionNames(boolean qualify) {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		store.setValue(ISystemPreferencesConstants.QUALIFY_CONNECTION_NAMES, qualify);
		savePreferences();
	}

	/**
	 * @return true if the user has elected to remember the state of the Remote Systems View
	 */
	public static boolean getRememberState() {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		return store.getBoolean(ISystemPreferencesConstants.REMEMBER_STATE);
	}

	/**
	 * Sets if the user has elected to remember the state of RSE.
	 * @param remember true if the state should be remembered.
	 */
	public static void setRememberState(boolean remember) {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		store.setValue(ISystemPreferencesConstants.REMEMBER_STATE, remember);
		savePreferences();
	}

	/**
	 * Return true if the user has elected to restore the state of the Remote Systems view from cached information
	 */
	public static boolean getRestoreStateFromCache() {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		return store.getBoolean(ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE);
	}

	/**
	 * Set if the user has elected to restore the state of the 
	 * Remote Systems View from cached information
	 * @param restore whether or not to restore the state of RSE from cached information.
	 */
	public static void setRestoreStateFromCache(boolean restore) {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		store.setValue(ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE, restore);
		savePreferences();
	}

	/**
	 * Return true if the user has elected to show filter pools in the Remote System Explorer view
	 */
	public static boolean getShowFilterPools() {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		return store.getBoolean(ISystemPreferencesConstants.SHOWFILTERPOOLS);
	}

	/**
	 * Sets whether or not to show filter pools.
	 * @param show true if we want to show the filter pools
	 */
	public static void setShowFilterPools(boolean show) {
		boolean prevValue = getShowFilterPools();
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		store.setValue(ISystemPreferencesConstants.SHOWFILTERPOOLS, show);
		savePreferences();
		if (show != prevValue) {
			RSEUIPlugin.getTheSystemRegistry().setShowFilterPools(show);
		}
	}

	/**
	 * @return true if the user has elected to show the "New Connection..." prompt
	 * in the Remote Systems View
	 */
	public static boolean getShowNewConnectionPrompt() {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		boolean value = store.getBoolean(ISystemPreferencesConstants.SHOWNEWCONNECTIONPROMPT);
		return value;
	}

	/**
	 * Sets whether to show the new connection... prompt in the Remote System Explorer view.
	 * @param show true if we want to show the filter pools
	 */
	public static void setShowNewConnectionPrompt(boolean show) {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		store.setValue(ISystemPreferencesConstants.SHOWNEWCONNECTIONPROMPT, show);
		savePreferences();
	}

	/**
	 * @return whether to turn on "Verify connection" checkbox on the New Connection wizard
	 */
	public static boolean getVerifyConnection() {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		store.setDefault(ISystemPreferencesConstants.VERIFY_CONNECTION, ISystemPreferencesConstants.DEFAULT_VERIFY_CONNECTION);
		return store.getBoolean(ISystemPreferencesConstants.VERIFY_CONNECTION);
	}

	/**
	 * Sets whether connections should be verified by the New Connection wizard.
	 * @param verify true if the connection should be verified
	 */
	public static void setVerifyConnection(boolean verify) {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		store.setValue(ISystemPreferencesConstants.VERIFY_CONNECTION, verify);
		savePreferences();
	}

	/**
	 * Return the history for a widget given an arbitrary key uniquely identifying it
	 */
	public static String[] getWidgetHistory(String key) {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		return parseStrings(store.getString(key));
	}

	/**
	 * Sets the history for a widget given an arbitrary key uniquely identifying it.
	 * Does not save the preferences. This must be done by the caller.
	 * @see #savePreferences()
	 */
	public static void setWidgetHistory(String key, String[] newHistory) {
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		store.setValue(key, makeString(newHistory));
	}

	/**
	 * Save the preference stores.
	 */
	public static void savePreferences() {
		RSEUIPlugin.getDefault().savePluginPreferences();
		RSECorePlugin.getDefault().savePluginPreferences();
	}

	/*
	 * Private to discourage instance creation. 
	 */
	private SystemPreferencesManager() {
	}
}