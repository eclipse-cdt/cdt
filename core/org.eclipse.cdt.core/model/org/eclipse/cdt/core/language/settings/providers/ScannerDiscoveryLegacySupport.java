/*******************************************************************************
 * Copyright (c) 2009, 2013 Andrew Gvozdev and others.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.LocalProjectScope;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsExtensionManager;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.cdt.internal.core.language.settings.providers.ScannerInfoExtensionLanguageSettingsProvider;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
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
	/** ID of ScannerInfo language settings provider wrapping ScannerInfoProvider defined by org.eclipse.cdt.core.ScannerInfoProvider extension point */
	private static final String SI_LANGUAGE_SETTINGS_PROVIDER_ID = "org.eclipse.cdt.core.LegacyScannerInfoLanguageSettingsProvider"; //$NON-NLS-1$

	private static String DISABLE_LSP_PREFERENCE = "language.settings.providers.disabled"; //$NON-NLS-1$
	//	the default for project needs to be "disabled" - for legacy projects to be open with old SD enabled for MBS provider
	private static boolean DISABLE_LSP_DEFAULT_PROJECT = true;
	private static boolean DISABLE_LSP_DEFAULT_WORKSPACE = false;
	private static final String PREFERENCES_QUALIFIER_CCORE = CCorePlugin.PLUGIN_ID;

	private static Map<String, String> legacyProfiles = null;

	/**
	 * Get preferences node for org.eclipse.cdt.core.
	 */
	private static Preferences getPreferences(IProject project) {
		if (project == null) {
			return InstanceScope.INSTANCE.getNode(PREFERENCES_QUALIFIER_CCORE);
		} else {
			return new LocalProjectScope(project).getNode(PREFERENCES_QUALIFIER_CCORE);
		}
	}

	/**
	 * Checks if Language Settings functionality is defined for given project in preferences.
	 *
	 * @param project - project to check the preference
	 * @return {@code true} if functionality is defined
	 *
	 * @noreference This method is temporary and not intended to be referenced by clients.
	 * 
	 * @since 5.5
	 */
	public static boolean isLanguageSettingsProvidersFunctionalityDefined(IProject project) {
		Preferences pref = getPreferences(project);
		String value = pref.get(DISABLE_LSP_PREFERENCE, null);
		return value != null;
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
		boolean defaultValue = project != null ? DISABLE_LSP_DEFAULT_PROJECT : DISABLE_LSP_DEFAULT_WORKSPACE;
		return !pref.getBoolean(DISABLE_LSP_PREFERENCE, defaultValue);
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
		pref.putBoolean(DISABLE_LSP_PREFERENCE, !value);
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			CCorePlugin.log(e);
		}
	}

	/**
	 * Check if legacy Scanner Discovery in MBS should be active.
	 * @noreference This is internal helper method to support compatibility with previous versions
	 * which is not intended to be referenced by clients.
	 */
	public static boolean isMbsLanguageSettingsProviderOn(ICConfigurationDescription cfgDescription) {
		if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
			List<ILanguageSettingsProvider> lsProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			for (ILanguageSettingsProvider lsp : lsProviders) {
				if (MBS_LANGUAGE_SETTINGS_PROVIDER_ID.equals(lsp.getId())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @noreference This is internal helper method to support compatibility with previous versions
	 * which is not intended to be referenced by clients.
	 */
	public static boolean isLegacyScannerDiscoveryOn(ICConfigurationDescription cfgDescription) {
		IProject project = null;
		if (cfgDescription != null) {
			ICProjectDescription prjDescription = cfgDescription.getProjectDescription();
			if (prjDescription != null) {
				project = prjDescription.getProject();
			}
		}
		return !isLanguageSettingsProvidersFunctionalityEnabled(project) || isMbsLanguageSettingsProviderOn(cfgDescription);
	}

	/**
	 * @noreference This is internal helper method to support compatibility with previous versions
	 * which is not intended to be referenced by clients.
	 */
	public static boolean isLegacyScannerDiscoveryOn(IProject project) {
		ICConfigurationDescription cfgDescription = null;
		ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project);
		if (prjDescription != null) {
			cfgDescription = prjDescription.getActiveConfiguration();
		}
		return !isLanguageSettingsProvidersFunctionalityEnabled(project) || isMbsLanguageSettingsProviderOn(cfgDescription);
	}

	/**
	 * Return list containing User provider and one of wrapper providers to support legacy projects (backward compatibility).
	 * 
	 * @noreference This is internal helper method to support compatibility with previous versions
	 * which is not intended to be referenced by clients.
	 * @since 5.5
	 */
	public static String[] getDefaultProviderIdsLegacy(ICConfigurationDescription cfgDescription) {
		boolean useScannerInfoProviderExtension = new ScannerInfoExtensionLanguageSettingsProvider().getScannerInfoProvider(cfgDescription) != null;
		if (useScannerInfoProviderExtension) {
			return new String[] {USER_LANGUAGE_SETTINGS_PROVIDER_ID, SI_LANGUAGE_SETTINGS_PROVIDER_ID};
		}
		if (CProjectDescriptionManager.getInstance().isNewStyleCfg(cfgDescription)) {
			return new String[] {USER_LANGUAGE_SETTINGS_PROVIDER_ID, MBS_LANGUAGE_SETTINGS_PROVIDER_ID};
		}
		return null;

	}
	
	/**
	 * Checks if the provider is applicable for configuration from backward compatibility point of view
	 * 
	 * @noreference This is internal helper method to support compatibility with previous versions
	 * which is not intended to be referenced by clients.
	 * @since 5.5
	 */
	public static boolean isProviderCompatible(String providerId, ICConfigurationDescription cfgDescription) {
		if (cfgDescription != null) {
			boolean useScannerInfoProviderExtension = new ScannerInfoExtensionLanguageSettingsProvider().getScannerInfoProvider(cfgDescription) != null;
			if (SI_LANGUAGE_SETTINGS_PROVIDER_ID.equals(providerId)) {
				return useScannerInfoProviderExtension;
			}

			if (MBS_LANGUAGE_SETTINGS_PROVIDER_ID.equals(providerId)) {
				boolean isNewStyleCfg = CProjectDescriptionManager.getInstance().isNewStyleCfg(cfgDescription);
				return !useScannerInfoProviderExtension && isNewStyleCfg;
			}
		}

		return true;
	}

	/**
	 * Return list containing User and MBS providers. Used to initialize older MBS tool-chains (backward compatibility).
	 * 
	 * @noreference This is internal helper method to support compatibility with previous versions
	 * which is not intended to be referenced by clients.
	 */
	public static List<ILanguageSettingsProvider> getDefaultProvidersLegacy() {
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(2);
		ILanguageSettingsProvider provider = LanguageSettingsExtensionManager.getExtensionProviderCopy((USER_LANGUAGE_SETTINGS_PROVIDER_ID), false);
		if (provider != null) {
			providers.add(provider);
		}
		providers.add(LanguageSettingsProvidersSerializer.getWorkspaceProvider(MBS_LANGUAGE_SETTINGS_PROVIDER_ID));
		return providers;
	}

	/**
	 * Returns the values of scanner discovery profiles (scannerConfigDiscoveryProfileId) which were deprecated
	 * and replaced with language settings providers in plugin.xml.
	 * This (temporary) function serves as fail-safe switch during the transition.
	 *
	 * @param id - can be id of either org.eclipse.cdt.managedbuilder.internal.core.InputType
	 * or org.eclipse.cdt.managedbuilder.internal.core.ToolChain.
	 * @return legacy scannerConfigDiscoveryProfileId.
	 */
	@SuppressWarnings("nls")
	public static String getDeprecatedLegacyProfiles(String id) {
		if (legacyProfiles == null) {
			legacyProfiles = new HashMap<String, String>();

			// InputTypes
			legacyProfiles.put("cdt.managedbuild.tool.gnu.c.compiler.input", "org.eclipse.cdt.managedbuilder.core.GCCManagedMakePerProjectProfileC|org.eclipse.cdt.make.core.GCCStandardMakePerFileProfile");
			legacyProfiles.put("cdt.managedbuild.tool.gnu.cpp.compiler.input", "org.eclipse.cdt.managedbuilder.core.GCCManagedMakePerProjectProfileCPP|org.eclipse.cdt.make.core.GCCStandardMakePerFileProfile");
			legacyProfiles.put("cdt.managedbuild.tool.gnu.c.compiler.input.cygwin", "org.eclipse.cdt.managedbuilder.core.GCCWinManagedMakePerProjectProfileC");
			legacyProfiles.put("cdt.managedbuild.tool.gnu.cpp.compiler.input.cygwin", "org.eclipse.cdt.managedbuilder.core.GCCWinManagedMakePerProjectProfileCPP");
			legacyProfiles.put("cdt.managedbuild.tool.xlc.c.compiler.input", "org.eclipse.cdt.managedbuilder.xlc.core.XLCManagedMakePerProjectProfile");
			legacyProfiles.put("cdt.managedbuild.tool.xlc.cpp.c.compiler.input", "org.eclipse.cdt.managedbuilder.xlc.core.XLCManagedMakePerProjectProfile");
			legacyProfiles.put("cdt.managedbuild.tool.xlc.cpp.compiler.input", "org.eclipse.cdt.managedbuilder.xlc.core.XLCManagedMakePerProjectProfileCPP");

			// Toolchains
		}

		return legacyProfiles.get(id);
	}

}
