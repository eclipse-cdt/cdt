/********************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - moved SystemPreferencesManager to a this package, was in
 *                       the org.eclipse.rse.core package of the UI plugin.
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [215820] Move SystemRegistry implementation to Core
 * David McKnight   (IBM)        - [237300] Problem with setDefaultHistory for SystemHistoryCombo.
 * David McKnight   (IBM)        - [240991] RSE startup creates display on worker thread before workbench.
 * David McKnight   (IBM)        - [240991] Avoiding calling SystemBasePluging.getWorkbench()
 * Martin Oberhuber (Wind River) - [246406] Timeout waiting when loading RSE
 ********************************************************************************/
package org.eclipse.rse.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.rse.core.IRSEPreferenceNames;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemModelChangeEvent;
import org.eclipse.rse.core.events.ISystemModelChangeEvents;
import org.eclipse.rse.core.events.ISystemModelChangeListener;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

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
	private static boolean showProfilePage; // This is not a persistent preference
	private static boolean showNewConnectionPrompt; // This is not a persistent preference

	/*
	 * Singleton instance to support listening to model change events
	 */
	private static SystemPreferencesManager fInstance = new SystemPreferencesManager();
	private int fModelChangeListeners = 0;
	private ISystemModelChangeListener fModelChangeListener = null;

	/*
	 * Private Constructor to discourage instance creation other than by ourselves.
	 */
	private SystemPreferencesManager() {
	}

	/**
	 * Migrate Preferences from UI Preference Store into Core Preference store
	 */
	private static void migrateCorePreferences() {
		String[] keys = {
				IRSEPreferenceNames.ACTIVEUSERPROFILES,
				IRSEPreferenceNames.USE_DEFERRED_QUERIES,
				IRSEPreferenceNames.USERIDPERKEY
		};
		for (int i = 0; i < keys.length; i++) {
			migrateCorePreference(keys[i]);
		}
	}

	private static void migrateCorePreference(String preferenceName) {
		String name = ISystemPreferencesConstants.ROOT + preferenceName;
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		if (store.contains(name) && !store.isDefault(name)) {
			String value = store.getString(name);
			store.setToDefault(name);
			store.setDefault(name, "*migrated*"); //$NON-NLS-1$
			store = RSECorePlugin.getDefault().getPluginPreferences();
			store.setValue(preferenceName, value);
		}
	}

	/**
	 * Initialize our preference store with our defaults.
	 * This is called once at plugin startup.
	 */
	public static void initDefaults() {
		migrateCorePreferences();
		initDefaultsUI();
		savePreferences();
		fInstance.startModelChangeListening();
	}

	private static void initDefaultsUI() {

		//String showProp = System.getProperty("rse.showNewConnectionPrompt");
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
	    store.setDefault(Mnemonics.POLICY_PREFERENCE, Mnemonics.POLICY_DEFAULT);
	    store.setDefault(Mnemonics.APPEND_MNEMONICS_PATTERN_PREFERENCE, Mnemonics.APPEND_MNEMONICS_PATTERN_DEFAULT);
	    store.setDefault(ISystemPreferencesConstants.SHOW_EMPTY_LISTS, ISystemPreferencesConstants.DEFAULT_SHOW_EMPTY_LISTS);
	    savePreferences();
	}

	private static boolean getBooleanProperty(String propertyName, boolean defaultValue) {
		String property = System.getProperty(propertyName);
		boolean value = (property == null) ? defaultValue : property.equals(Boolean.toString(true));
		return value;
	}

	public static boolean getShowLocalConnection() {
		return showLocalConnection;
	}

	public static boolean getShowProfilePage() {
		return showProfilePage;
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
		List l = new ArrayList();
		for (int idx = 0; idx < allConnectionNamesOrder.length; idx++)
			if (allConnectionNamesOrder[idx].startsWith(profileName)) {
				l.add(allConnectionNamesOrder[idx].substring(profileNameLength));
			}
		String[] names = new String[l.size()];
		l.toArray(names);
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
	 * This resets any user-specified ordering of profiles since the SystemRegistry
	 * has no concept of ordered profiles. The hosts inside a profile, though,
	 * will be ordered according to user preference.
	 */
	public static void setConnectionNamesOrder() {
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
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
	 * Resolves differences between two ordered name lists.
	 * Used when there are differences between the actual list of names and
	 * a restored ordered list of names.
	 */
	private static String[] resolveOrderPreferenceVersusReality(String[] reality, String[] ordered) {
		List finalList = new ArrayList();
		// step 1: include all names from preferences list which do exist in reality...
		for (int idx = 0; idx < ordered.length; idx++) {
			if (SystemPreferencesManager.find(reality, ordered[idx])) finalList.add(ordered[idx]);
		}
		// step 2: add all names in reality which do not exist in preferences list...
		for (int idx = 0; idx < reality.length; idx++) {
			if (!SystemPreferencesManager.find(ordered, reality[idx])) finalList.add(reality[idx]);
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
			RSEUIPlugin.getTheSystemRegistryUI().setShowFilterPools(show);
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
		String result = store.getString(key);

		// bug 237300
		// don't parse strings if we have ""
		if (result == null || result.length() == 0){
			return null;
		}
		else {
			return parseStrings(result);
		}
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
	 * Save the preference stores.
	 */
	public static void savePreferences() {
		RSEUIPlugin.getDefault().savePluginPreferences();
		RSECorePlugin.getDefault().savePluginPreferences();
	}

	/*
	 * Start listening to SystemRegistry model change events
	 */
	private void startModelChangeListening() {
		//TODO Register a listener for shutdown, to stop model change listening
		boolean alreadyListening;
		synchronized(this) {
			alreadyListening = (fModelChangeListeners>0);
			fModelChangeListeners++;
		}

		if (!alreadyListening) {
			// FIXME bug 240991: With the current workaround, we might miss events
			// Instead of adding the listener deferred, the SystemRegistry
			// should send events via the IRSEInteractionProvider
			Bundle bnd = RSEUIPlugin.getDefault().getBundle();
			if (bnd.getState() == Bundle.ACTIVE) {
				// addListenerJob.schedule();
				fModelChangeListener = new ModelChangeListener();
				RSECorePlugin.getTheSystemRegistry().addSystemModelChangeListener(fModelChangeListener);
			} else {
				final BundleContext ctx = bnd.getBundleContext();
				ctx.addBundleListener(new BundleListener() {
					public void bundleChanged(BundleEvent event) {
						if (event.getType() == BundleEvent.STARTED) {
							// addListenerJob.schedule();
							fModelChangeListener = new ModelChangeListener();
							RSECorePlugin.getTheSystemRegistry().addSystemModelChangeListener(fModelChangeListener);
							ctx.removeBundleListener(this);
						}
					}
				});
			}
		}
	}

	/*
	 * A listener for SystemRegistry Model Change events
	 */
	private static class ModelChangeListener implements ISystemModelChangeListener, ISystemResourceChangeListener {

		public void systemModelResourceChanged(ISystemModelChangeEvent event) {
			int rt = event.getResourceType();
			if (rt==ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_CONNECTION) {
				switch(event.getEventType()) {
				case ISystemModelChangeEvents.SYSTEM_RESOURCE_RENAMED:
				case ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED:
				case ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED:
					//TODO Change order of hosts from affected profile only?
					SystemPreferencesManager.setConnectionNamesOrder();
					break;
				}
			} else if (rt==ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_PROFILE) {
				switch (event.getEventType()) {
				case ISystemModelChangeEvents.SYSTEM_RESOURCE_RENAMED:
				case ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED:
				case ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED:
					//TODO Change order of hosts from affected profile only?
					SystemPreferencesManager.setConnectionNamesOrder();
					break;
				}
				if (event.getEventType()==ISystemModelChangeEvents.SYSTEM_RESOURCE_RENAMED) {
					boolean namesQualified = SystemPreferencesManager.getQualifyConnectionNames();
					RSEUIPlugin.getTheSystemRegistryUI().setQualifiedHostNames(namesQualified); // causes refresh events to be fired
				}
			}
		}

		public void systemResourceChanged(ISystemResourceChangeEvent event) {
			if (event.getType()==ISystemResourceChangeEvents.EVENT_MOVE_MANY
			 && (event.getSource() instanceof IHost[])
			) {
				//TODO Change order of hosts from affected profile only?
				SystemPreferencesManager.setConnectionNamesOrder();
			}
		}

	}

}