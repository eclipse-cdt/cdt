/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220123] [api][dstore] Configurable timeout on irresponsiveness
 * David McKnight   (IBM)        - [227406] [dstore] DStoreFileService must listen to buffer size preference changes
 * David McKnight   (IBM)        - [228334][api][breaking][dstore] Default DataStore connection timeout is too short
 * David McKnight   (IBM)        - [233160] [dstore] SSL/non-SSL alert are not appropriate
 * Martin Oberhuber (Wind River) - [245918] Allow customization of DStore Preferences
 *******************************************************************************/

package org.eclipse.rse.internal.connectorservice.dstore;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.rse.connectorservice.dstore.IUniversalDStoreConstants;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends SystemBasePlugin {

	//The shared instance.
	private static Activator plugin;

	public final static String PLUGIN_ID = "org.eclipse.rse.connectorservice.dstore"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		initializeDefaultPreferences();
	}

	/**
	 * A Preference Store that gives direct access to Default Preferences.
	 */
	private static class DefaultPreferenceStore extends ScopedPreferenceStore {
		private IEclipsePreferences[] defaultNodes;
		public DefaultPreferenceStore(String qualifier) {
			super(new DefaultScope(), qualifier);
			defaultNodes = new IEclipsePreferences[] {
				new DefaultScope().getNode(qualifier) };
		}
		public boolean hasDefault(String key) {
			return Platform.getPreferencesService().get(key, null, defaultNodes) != null;
		}
	}

	public void initializeDefaultPreferences() {

		// [245918] Since our Preferences are stored in RSEUIPlugin, we cannot
		// use the core.runtime.preferences extension in order to do
		// initialization in the correct order (allow overriding by
		// plugin_customization.ini). We therefore explicitly check each
		// Preference slot, and only set a there isn't one set already. Note
		// that requires explicit access to the DefaultScope().
		// TODO move Preferences to our own PreferenceStore to simplify this
		DefaultPreferenceStore store = new DefaultPreferenceStore(RSEUIPlugin.getDefault().getBundle().getSymbolicName());
		//IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		//Preferences store = RSECorePlugin.getDefault().getPluginPreferences();

		if (!store.hasDefault(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT))
			store.setDefault(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT, IDStoreDefaultPreferenceConstants.DEFAULT_PREF_SOCKET_TIMEOUT);

		// do keepalive
		if (!store.hasDefault(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE))
			store.setDefault(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE, IDStoreDefaultPreferenceConstants.DEFAULT_PREF_DO_KEEPALIVE);

		// socket read timeout
		if (!store.hasDefault(IUniversalDStoreConstants.RESID_PREF_SOCKET_READ_TIMEOUT))
			store.setDefault(IUniversalDStoreConstants.RESID_PREF_SOCKET_READ_TIMEOUT, IDStoreDefaultPreferenceConstants.DEFAULT_PREF_SOCKET_READ_TIMEOUT);

		// keepalive response timeout
		if (!store.hasDefault(IUniversalDStoreConstants.RESID_PREF_KEEPALIVE_RESPONSE_TIMEOUT))
			store.setDefault(IUniversalDStoreConstants.RESID_PREF_KEEPALIVE_RESPONSE_TIMEOUT,
					IDStoreDefaultPreferenceConstants.DEFAULT_PREF_KEEPALIVE_RESPONSE_TIMEOUT);

		// show mismatched server warning
		if (!store.hasDefault(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER))
			store.setDefault(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER, IDStoreDefaultPreferenceConstants.DEFAULT_ALERT_MISMATCHED_SERVER);

		// cache remote classes
		if (!store.hasDefault(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES))
			store.setDefault(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES, IDStoreDefaultPreferenceConstants.DEFAULT_PREF_CACHE_REMOTE_CLASSES);

		// alert defaults
		if (!store.hasDefault(ISystemPreferencesConstants.ALERT_SSL))
			store.setDefault(ISystemPreferencesConstants.ALERT_SSL, ISystemPreferencesConstants.DEFAULT_ALERT_SSL);
		if (!store.hasDefault(ISystemPreferencesConstants.ALERT_NONSSL))
			store.setDefault(ISystemPreferencesConstants.ALERT_NONSSL, ISystemPreferencesConstants.DEFAULT_ALERT_NON_SSL);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}



	protected void initializeImageRegistry()
	{
		// TODO Auto-generated method stub

	}
}
