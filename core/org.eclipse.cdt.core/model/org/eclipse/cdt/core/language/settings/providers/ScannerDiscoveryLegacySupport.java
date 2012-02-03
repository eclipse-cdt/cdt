/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.LocalProjectScope;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsExtensionManager;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Collection of utilities for legacy support of older Scanner Discovery functionality.
 * This class is temporary and not intended to be used by clients.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 *
 * @since 5.4
 */
public class ScannerDiscoveryLegacySupport {
	/** ID of User language settings provider (from org.eclipse.cdt.ui) */
	public static final String USER_LANGUAGE_SETTINGS_PROVIDER_ID = "org.eclipse.cdt.ui.UserLanguageSettingsProvider"; //$NON-NLS-1$
	/** ID of MBS language settings provider (from org.eclipse.cdt.managedbuilder.core) */
	public static final String MBS_LANGUAGE_SETTINGS_PROVIDER_ID = "org.eclipse.cdt.managedbuilder.core.MBSLanguageSettingsProvider"; //$NON-NLS-1$

	private static String USE_LANGUAGE_SETTINGS_PROVIDERS_PREFERENCE = "enabled"; //$NON-NLS-1$
//	the default needs to be "false" for legacy projects to be open with old SD enabled for MBS provider
	private static boolean USE_LANGUAGE_SETTINGS_PROVIDERS_DEFAULT = false;
	private static final String PREFERENCES_QUALIFIER = CCorePlugin.PLUGIN_ID;
	private static final String LANGUAGE_SETTINGS_PROVIDERS_NODE = "languageSettingsProviders"; //$NON-NLS-1$

	private static Preferences getPreferences(IProject project) {
		if (project == null)
			return InstanceScope.INSTANCE.getNode(PREFERENCES_QUALIFIER).node(LANGUAGE_SETTINGS_PROVIDERS_NODE);
		else
			return new LocalProjectScope(project).getNode(PREFERENCES_QUALIFIER).node(LANGUAGE_SETTINGS_PROVIDERS_NODE);
	}

	/**
	 * Checks if Language Settings functionality is enabled for given project.
	 *
	 * @param project - project to check the preference
	 * @return {@code true} if functionality is enabled
	 *
	 * @noreference This method is temporary and not intended to be referenced by clients.
	 */
	public static boolean isLanguageSettingsProvidersFunctionalityEnabled(IProject project) {
		Preferences pref = getPreferences(project);
		return pref.getBoolean(USE_LANGUAGE_SETTINGS_PROVIDERS_PREFERENCE, USE_LANGUAGE_SETTINGS_PROVIDERS_DEFAULT);
	}

	/**
	 * Enable/disable Language Settings functionality for the given project.
	 *
	 * @param project
	 * @param value {@code true} to enable or {@code false} to disable the functionality.
	 *
	 * @noreference This method is temporary and not intended to be referenced by clients.
	 */
	public static void setLanguageSettingsProvidersFunctionalityEnabled(IProject project, boolean value) {
		Preferences pref = getPreferences(project);
		pref.putBoolean(USE_LANGUAGE_SETTINGS_PROVIDERS_PREFERENCE, value);
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			CCorePlugin.log(e);
		}
	}

	/**
	 * Return list containing MBS and User provider. Used to initialize for unaware tool-chains (backward compatibility).
	 */
	public static List<ILanguageSettingsProvider> getDefaultProvidersLegacy() {
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(2);
		ILanguageSettingsProvider provider = LanguageSettingsExtensionManager.getExtensionProviderCopy((ScannerDiscoveryLegacySupport.USER_LANGUAGE_SETTINGS_PROVIDER_ID), false);
		if (provider != null) {
			providers.add(provider);
		}
		providers.add(LanguageSettingsProvidersSerializer.getWorkspaceProvider(ScannerDiscoveryLegacySupport.MBS_LANGUAGE_SETTINGS_PROVIDER_ID));
		return providers;
	}

}
