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
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsExtensionManager;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * A collection of utility methods to manage language settings providers.
 * See {@link ILanguageSettingsProvider}.
 */
public class LanguageSettingsManager {
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
		return LanguageSettingsProvidersSerializer.getSettingEntriesUpResourceTree(provider, cfgDescription, rc, languageId);
	}

	/**
	 * Builds for the provider a nice looking resource tree to present hierarchical view to the user.
	 *
	 * TODO - Note that after using this method for a while for BOP parsers it appears that disadvantages
	 * outweigh benefits. In particular, it doesn't result in saving memory as the language settings
	 * (and the lists itself) are not duplicated in memory anyway but optimized with using WeakHashSet
	 * and SafeStringInterner.
	 *
	 * @param provider - language settings provider to build the tree for.
	 * @param cfgDescription - configuration description.
	 * @param languageId - language ID.
	 * @param project - the project which is considered the root of the resource tree.
	 */
	public static void buildResourceTree(LanguageSettingsSerializableProvider provider,
			ICConfigurationDescription cfgDescription, String languageId, IProject project) {
		LanguageSettingsProvidersSerializer.buildResourceTree(provider, cfgDescription, languageId, project);
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
		return LanguageSettingsProvidersSerializer.getSettingEntriesByKind(cfgDescription, rc, languageId, kind);
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
	 * Checks if the provider is a workspace level provider.
	 * This method is intended to check providers retrieved from a configuration.
	 * Raw providers from {@link #getRawProvider(ILanguageSettingsProvider)}
	 * are not considered as workspace providers.
	 *
	 * @param provider - provider to check.
	 * @return {@code true} if the given provider is workspace provider, {@code false} otherwise.
	 */
	public static boolean isWorkspaceProvider(ILanguageSettingsProvider provider) {
		return LanguageSettingsProvidersSerializer.isWorkspaceProvider(provider);
	}

	/**
	 * Helper method to get to real underlying provider collecting entries as opposed to wrapper
	 * which is normally used for workspace provider.
	 * @see LanguageSettingsProvidersSerializer#isWorkspaceProvider(ILanguageSettingsProvider)
	 * 
	 * @param provider - the provider to get raw provider for. Can be either workspace provider
	 *    or regular one.
	 * @return raw underlying provider for workspace provider or provider itself if no wrapper is used.
	 */
	public static ILanguageSettingsProvider getRawProvider(ILanguageSettingsProvider provider) {
		if (LanguageSettingsManager.isWorkspaceProvider(provider)) {
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
	 * Copy language settings provider. It is different from clone() methods in that
	 * it does not throw {@code CloneNotSupportedException} but returns {@code null}
	 * instead.
	 * 
	 * @param provider - language settings provider to copy.
	 * @param deep - {@code true} to request deep copy including copying settings entries
	 *    or {@code false} to return shallow copy with no settings entries.
	 *
	 * @return a copy of the provider or null if copying is not possible.
	 */
	public static ILanguageSettingsEditableProvider getProviderCopy(ILanguageSettingsEditableProvider provider, boolean deep) {
		return LanguageSettingsExtensionManager.getProviderCopy(provider, deep);
	}

	/**
	 * Get language settings provider defined via extension point
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider}.
	 * A new copy of the extension provider is returned.
	 *
	 * @param id - ID of the extension provider.
	 * @param deep - {@code true} to request deep copy including copying settings entries
	 *    or {@code false} to return shallow copy with no settings entries.
	 * @return the copy of the extension provider if possible (i.e. for {@link ILanguageSettingsEditableProvider})
	 *    or {@code null} if provider is not copyable.
	 */
	public static ILanguageSettingsProvider getExtensionProviderCopy(String id, boolean deep) {
		return LanguageSettingsExtensionManager.getExtensionProviderCopy(id, deep);
	}

	/**
	 * Test if the provider is equal to the one defined via extension point
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider}.
	 * 
	 * @param provider - the provider to test.
	 * @param deep - {@code true} to check for deep equality testing also settings entries
	 *    or {@code false} to test shallow copy with no settings entries.
	 * @return - {@code true} if the provider matches the extension or {@code false} otherwise.
	 */
	public static boolean isEqualExtensionProvider(ILanguageSettingsProvider provider, boolean deep) {
		return LanguageSettingsExtensionManager.isEqualsExtensionProvider(provider, deep);
	}

	/**
	 * Find language IDs for the resource represented by resource description.
	 * Under the hood build component is inquired and the language IDs would
	 * commonly come from the input type(s).
	 *
	 * @param rcDescription - resource description
	 * @return list of language IDs for the resource.
	 *    Never returns {@code null} but empty list if no languages can be found.
	 *
	 */
	public static List<String> getLanguages(ICResourceDescription rcDescription) {
		ICLanguageSetting[] languageSettings = null;
		if (rcDescription instanceof ICFileDescription) {
			ICLanguageSetting languageSetting = ((ICFileDescription)rcDescription).getLanguageSetting();
			if (languageSetting != null) {
				languageSettings = new ICLanguageSetting[] {languageSetting};
			}
		} else if (rcDescription instanceof ICFolderDescription) {
			languageSettings = ((ICFolderDescription)rcDescription).getLanguageSettings();
		}

		List<String> languageIds = new ArrayList<String>();
		if (languageSettings != null) {
			for (ICLanguageSetting languageSetting : languageSettings) {
				if (languageSetting!=null) {
					String languageId = languageSetting.getLanguageId();
					if (languageId != null && !languageId.isEmpty()) {
						languageIds.add(languageId);
					}
				}
			}
		}

		return languageIds;
	}

	/**
	 * Find language IDs for the resource in given build configuration.
	 * Under the hood build component is inquired and the language IDs would
	 * commonly come from the input type(s).
	 *
	 * @param resource - the resource to find languages for.
	 * @param cfgDescription
	 * @return list of language IDs for the resource.
	 *    Never returns {@code null} but empty list if no languages can be found.
	 */
	public static List<String> getLanguages(IResource resource, ICConfigurationDescription cfgDescription) {
		List<String> languageIds = new ArrayList<String>();
		IPath prjRelPath = resource.getProjectRelativePath();
		if (resource instanceof IFile) {
			String langId = null;
			if (cfgDescription != null) {
				ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(prjRelPath, true);
				if (ls != null) {
					langId = ls.getLanguageId();
				}
			} else {
				try {
					ILanguage lang = LanguageManager.getInstance().getLanguageForFile((IFile) resource, null);
					langId = lang.getId();
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
			if (langId != null) {
				languageIds.add(langId);
			}
		} else {
			ICResourceDescription rcDes = cfgDescription.getResourceDescription(prjRelPath, false);
			if (rcDes == null) {
				rcDes = cfgDescription.getRootFolderDescription();
			}
			languageIds = getLanguages(rcDes);
		}

		return languageIds;
	}

	/**
	 * Adds a listener that will be notified of changes in language settings.
	 *
	 * @param listener the listener to add
	 */
	public static void registerLanguageSettingsChangeListener(ILanguageSettingsChangeListener listener) {
		LanguageSettingsProvidersSerializer.registerLanguageSettingsChangeListener(listener);
	}

	/**
	 * Removes a language settings change listener.
	 *
	 * @param listener the listener to remove.
	 */
	public static void unregisterLanguageSettingsChangeListener(ILanguageSettingsChangeListener listener) {
		LanguageSettingsProvidersSerializer.unregisterLanguageSettingsChangeListener(listener);
	}

	/**
	 * Tells if language settings entries of the provider are persisted with the project
	 * (under .settings/ folder) or in workspace area. Persistence in the project area lets
	 * the entries migrate with the project.
	 *
	 * @param provider - provider to check the persistence mode.
	 * @return {@code true} if LSE persisted with the project or {@code false} if in the workspace.
	 */
	public static boolean isStoringEntriesInProjectArea(LanguageSettingsSerializableProvider provider) {
		return LanguageSettingsProvidersSerializer.isStoringEntriesInProjectArea(provider);
	}

	/**
	 * Define where language settings are persisted for the provider.
	 *
	 * @param provider - provider to set the persistence mode.
	 * @param storeEntriesWithProject - {@code true} if with the project,
	 *    {@code false} if in workspace area.
	 */
	public static void setStoringEntriesInProjectArea(LanguageSettingsSerializableProvider provider, boolean storeEntriesWithProject) {
		LanguageSettingsProvidersSerializer.setStoringEntriesInProjectArea(provider, storeEntriesWithProject);
	}

	/**
	 * Save language settings providers of a project to persistent storage.
	 *
	 * @param prjDescription - project description of the project.
	 * @throws CoreException if something goes wrong.
	 */
	public static void serializeLanguageSettings(ICProjectDescription prjDescription) throws CoreException {
		LanguageSettingsProvidersSerializer.serializeLanguageSettings(prjDescription);
	}

	/**
	 * Save language settings providers of the workspace (global providers) to persistent storage.
	 *
	 * @throws CoreException
	 */
	public static void serializeLanguageSettingsWorkspace() throws CoreException {
		LanguageSettingsProvidersSerializer.serializeLanguageSettingsWorkspace();
	}

}
