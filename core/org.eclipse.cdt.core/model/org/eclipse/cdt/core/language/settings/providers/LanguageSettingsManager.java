/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.internal.core.LocalProjectScope;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsExtensionManager;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * A collection of utility methods to manage language settings providers.
 * See {@link ILanguageSettingsProvider}.
 *
 * @since 6.0
 */
public class LanguageSettingsManager {
	/** @noreference This field is temporary and not intended to be referenced by clients. */
	public static String USE_LANGUAGE_SETTINGS_PROVIDERS_PREFERENCE = "enabled"; //$NON-NLS-1$
	public static boolean USE_LANGUAGE_SETTINGS_PROVIDERS_DEFAULT = true;

	private static final String PREFERENCES_QUALIFIER = CCorePlugin.PLUGIN_ID;
	private static final String LANGUAGE_SETTINGS_PROVIDERS_NODE = "languageSettingsProviders"; //$NON-NLS-1$

	/**
	 * Returns the list of setting entries of the given provider
	 * for the given configuration description, resource and language.
	 * This method reaches to the parent folder of the resource recursively
	 * in case the resource does not define the entries for the given provider.
	 *
	 * @param provider - language settings provider.
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder.
	 * @param languageId - language id.
	 *
	 * @return the list of setting entries. Never returns {@code null}
	 *     although individual providers return {@code null} if no settings defined.
	 */
	public static List<ICLanguageSettingEntry> getSettingEntriesUpResourceTree(ILanguageSettingsProvider provider, ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		return LanguageSettingsExtensionManager.getSettingEntriesUpResourceTree(provider, cfgDescription, rc, languageId);
	}

	/**
	 * Returns the list of setting entries of a certain kind (such as include paths)
	 * for the given configuration description, resource and language. This is a
	 * combined list for all providers taking into account settings of parent folder
	 * if settings for the given resource are not defined.
	 *
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder.
	 * @param languageId - language id.
	 * @param kind - kind of language settings entries, such as
	 *     {@link ICSettingEntry#INCLUDE_PATH} etc. This is a binary flag
	 *     and it is possible to specify composite kind.
	 *     Use {@link ICSettingEntry#ALL} to get all kinds.
	 *
	 * @return the list of setting entries.
	 */
	// FIXME: get rid of callers PathEntryTranslator and DescriptionScannerInfoProvider 
	public static List<ICLanguageSettingEntry> getSettingEntriesByKind(ICConfigurationDescription cfgDescription, IResource rc, String languageId, int kind) {
		return LanguageSettingsExtensionManager.getSettingEntriesByKind(cfgDescription, rc, languageId, kind);
	}

	/**
	 * Get Language Settings Provider defined in the workspace. That includes user-defined
	 * providers and after that providers defined as extensions via
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
	 * That returns actual object, any modifications will affect any configuration
	 * referring to the provider.
	 *
	 * @param id - id of provider to find.
	 * @return the provider or {@code null} if provider is not defined.
	 */
	public static ILanguageSettingsProvider getWorkspaceProvider(String id) {
		return LanguageSettingsProvidersSerializer.getWorkspaceProvider(id);
	}

	/**
	 * @return a list of language settings providers defined on workspace level.
	 * That includes user-defined providers and after that providers defined as
	 * extensions via {@code org.eclipse.cdt.core.LanguageSettingsProvider}
	 * extension point.
	 */
	public static List<ILanguageSettingsProvider> getWorkspaceProviders() {
		return LanguageSettingsProvidersSerializer.getWorkspaceProviders();
	}

	/**
	 * Checks if the provider is defined on the workspace level.
	 *
	 * @param provider - provider to check.
	 * @return {@code true} if the given provider is workspace provider, {@code false} otherwise.
	 */
	public static boolean isWorkspaceProvider(ILanguageSettingsProvider provider) {
		return LanguageSettingsProvidersSerializer.isWorkspaceProvider(provider);
	}

	/**
	 * TODO - helper method for often used chunk of code
	 * @param provider 
	 * @return ILanguageSettingsProvider
	 */
	public static ILanguageSettingsProvider getRawProvider(ILanguageSettingsProvider provider) {
		if (LanguageSettingsManager.isWorkspaceProvider(provider)){
			provider = LanguageSettingsProvidersSerializer.getRawWorkspaceProvider(provider.getId());
		}
		return provider;
	}
	
	
	/**
	 * Set and store in workspace area user defined providers.
	 *
	 * @param providers - array of user defined workspace providers.
	 *    Note that those providers will shadow extension providers with the same ID.
	 *    All not shadowed extension providers will be added to the list to be present
	 *    as workspace providers. {@code null} is equivalent to passing an empty array
	 *    and so will reset workspace providers to match extension providers.
	 * @throws CoreException in case of problems (such as problems with persistence).
	 */
	public static void setWorkspaceProviders(List<ILanguageSettingsProvider> providers) throws CoreException {
		LanguageSettingsProvidersSerializer.setWorkspaceProviders(providers);
	}

	/**
	 * Get Language Settings Provider defined via
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
	 *
	 * @param id - ID of provider to find.
	 * @return the copy of the provider if possible (i.e. for {@link ILanguageSettingsEditableProvider})
	 *    or workspace provider if provider is not copyable.
	 */
	public static ILanguageSettingsProvider getExtensionProviderCopy(String id) {
		ILanguageSettingsProvider provider = null;
		try {
			provider = LanguageSettingsExtensionManager.getExtensionProviderClone(id);
		} catch (CloneNotSupportedException e) {
			// from here falls to get workspace provider
		}
		if (provider==null)
			provider = LanguageSettingsManager.getWorkspaceProvider(id);
		
		return provider;
	}

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
	public static boolean isLanguageSettingsProvidersEnabled(IProject project) {
		Preferences pref = LanguageSettingsManager.getPreferences(project);
		return pref.getBoolean(LanguageSettingsManager.USE_LANGUAGE_SETTINGS_PROVIDERS_PREFERENCE, LanguageSettingsManager.USE_LANGUAGE_SETTINGS_PROVIDERS_DEFAULT);
	}

	/**
	 * Enable/disable Language Settings functionality for the given project.
	 *
	 * @param project
	 * @param value {@code true} to enable or {@code false} to disable the functionality.
	 *
	 * @noreference This method is temporary and not intended to be referenced by clients.
	 */
	public static void setLanguageSettingsProvidersEnabled(IProject project, boolean value) {
		Preferences pref = LanguageSettingsManager.getPreferences(project);
		pref.putBoolean(LanguageSettingsManager.USE_LANGUAGE_SETTINGS_PROVIDERS_PREFERENCE, value);
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			CCorePlugin.log(e);
		}
	}

}
