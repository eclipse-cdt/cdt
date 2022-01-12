/*******************************************************************************
 * Copyright (c) 2009, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.language.settings.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsGenericProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Class {@code LanguageSettingsExtensionManager} manages {@link ILanguageSettingsProvider} extensions
 */
public class LanguageSettingsExtensionManager {
	/** Name of the extension point for contributing language settings */
	static final String PROVIDER_EXTENSION_POINT_ID = "org.eclipse.cdt.core.LanguageSettingsProvider"; //$NON-NLS-1$
	static final String PROVIDER_EXTENSION_SIMPLE_ID = "LanguageSettingsProvider"; //$NON-NLS-1$
	static final String ATTR_ID = "id"; //$NON-NLS-1$
	static final String ATTR_NAME = "name"; //$NON-NLS-1$
	static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	static final String ATTR_PREFER_NON_SHARED = "prefer-non-shared"; //$NON-NLS-1$

	static final String ELEM_PROVIDER = "provider"; //$NON-NLS-1$
	static final String ELEM_LANGUAGE_SCOPE = "language-scope"; //$NON-NLS-1$

	static final String ELEM_ENTRY = "entry"; //$NON-NLS-1$
	static final String ATTR_ENTRY_NAME = "name"; //$NON-NLS-1$
	static final String ATTR_ENTRY_KIND = "kind"; //$NON-NLS-1$
	static final String ATTR_ENTRY_VALUE = "value"; //$NON-NLS-1$
	static final String ELEM_ENTRY_FLAG = "flag"; //$NON-NLS-1$

	/**
	 * Extension providers loaded once and used for equality only.
	 * Those who request extension provider will get copy rather than real instance.
	 */
	private static final LinkedHashMap<String, ILanguageSettingsProvider> fExtensionProviders = new LinkedHashMap<>();

	/**
	 * Providers loaded initially via static initializer.
	 */
	static {
		try {
			loadProviderExtensions();
		} catch (Throwable e) {
			CCorePlugin.log("Error loading language settings providers extensions", e); //$NON-NLS-1$
		}
	}

	/**
	 * Load language settings providers contributed via the extension point.
	 */
	synchronized private static void loadProviderExtensions() {
		List<ILanguageSettingsProvider> providers = new ArrayList<>();
		loadProviderExtensions(Platform.getExtensionRegistry(), providers);

		// sort by name - the providers defined via extensions are kept in separate list sorted by name
		Collections.sort(providers, new Comparator<ILanguageSettingsProvider>() {
			@Override
			public int compare(ILanguageSettingsProvider pr1, ILanguageSettingsProvider pr2) {
				return pr1.getName().compareTo(pr2.getName());
			}
		});

		fExtensionProviders.clear();
		for (ILanguageSettingsProvider provider : providers) {
			fExtensionProviders.put(provider.getId(), provider);
		}
	}

	/**
	 * Load contributed extensions from extension registry.
	 *
	 * @param registry - extension registry
	 * @param providers - resulting set of providers
	 */
	private static void loadProviderExtensions(IExtensionRegistry registry,
			Collection<ILanguageSettingsProvider> providers) {
		providers.clear();
		IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID, PROVIDER_EXTENSION_SIMPLE_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
					ILanguageSettingsProvider provider = null;
					String id = null;
					try {
						if (cfgEl.getName().equals(ELEM_PROVIDER)) {
							id = determineAttributeValue(cfgEl, ATTR_ID);
							provider = createExecutableExtension(cfgEl);
							configureExecutableProvider(provider, cfgEl);
							providers.add(provider);
						}
					} catch (Throwable e) {
						CCorePlugin.log("Cannot load LanguageSettingsProvider extension id=" + id, e); //$NON-NLS-1$
					}
				}
			}
		}
	}

	private static String determineAttributeValue(IConfigurationElement ce, String attr) {
		String value = ce.getAttribute(attr);
		return value != null ? value : ""; //$NON-NLS-1$
	}

	/**
	 * Creates empty non-configured provider as executable extension from extension point definition.
	 * If "class" attribute is empty {@link LanguageSettingsBaseProvider} is created.
	 *
	 * @param ce - configuration element with provider definition
	 * @return new non-configured provider
	 * @throws CoreException in case of failure
	 */
	private static ILanguageSettingsProvider createExecutableExtension(IConfigurationElement ce) throws CoreException {
		String ceClass = ce.getAttribute(ATTR_CLASS);
		ILanguageSettingsProvider provider = null;
		if (ceClass == null || ceClass.trim().length() == 0
				|| ceClass.equals(LanguageSettingsBaseProvider.class.getCanonicalName())) {
			provider = new LanguageSettingsBaseProvider();
		} else {
			provider = (ILanguageSettingsProvider) ce.createExecutableExtension(ATTR_CLASS);
		}

		return provider;
	}

	/**
	 * Configure language settings provider with parameters defined in XML metadata.
	 *
	 * @param provider - empty non-configured provider.
	 * @param ce - configuration element from registry representing XML.
	 */
	private static void configureExecutableProvider(ILanguageSettingsProvider provider, IConfigurationElement ce) {
		String ceId = determineAttributeValue(ce, ATTR_ID);
		String ceName = determineAttributeValue(ce, ATTR_NAME);
		Map<String, String> ceAttributes = new HashMap<>();
		List<String> languages = null;
		List<ICLanguageSettingEntry> entries = null;

		for (String attr : ce.getAttributeNames()) {
			if (!attr.equals(ATTR_ID) && !attr.equals(ATTR_NAME) && !attr.equals(ATTR_CLASS)) {
				ceAttributes.put(attr, determineAttributeValue(ce, attr));
			}
		}

		for (IConfigurationElement ceLang : ce.getChildren(ELEM_LANGUAGE_SCOPE)) {
			String langId = determineAttributeValue(ceLang, ATTR_ID);
			if (langId.length() > 0) {
				if (languages == null) {
					languages = new ArrayList<>();
				}
				languages.add(langId);
			}
		}

		for (IConfigurationElement ceEntry : ce.getChildren(ELEM_ENTRY)) {
			try {
				int entryKind = LanguageSettingEntriesSerializer
						.stringToKind(determineAttributeValue(ceEntry, ATTR_ENTRY_KIND));
				String entryName = determineAttributeValue(ceEntry, ATTR_ENTRY_NAME);
				String entryValue = determineAttributeValue(ceEntry, ATTR_ENTRY_VALUE);

				int flags = 0;
				for (IConfigurationElement ceFlags : ceEntry.getChildren(ELEM_ENTRY_FLAG)) {
					int bitFlag = LanguageSettingEntriesSerializer
							.composeFlags(determineAttributeValue(ceFlags, ATTR_ENTRY_VALUE));
					flags |= bitFlag;
				}

				ICLanguageSettingEntry entry = (ICLanguageSettingEntry) CDataUtil.createEntry(entryKind, entryName,
						entryValue, null, flags);

				if (entries == null) {
					entries = new ArrayList<>();
				}
				entries.add(entry);

			} catch (Exception e) {
				CCorePlugin.log("Error creating language settings entry ", e); //$NON-NLS-1$
			}
		}

		if (provider instanceof LanguageSettingsBaseProvider) {
			((LanguageSettingsBaseProvider) provider).configureProvider(ceId, ceName, languages, entries, ceAttributes);
		} else if (provider instanceof AbstractExecutableExtensionBase) {
			((AbstractExecutableExtensionBase) provider).setId(ceId);
			((AbstractExecutableExtensionBase) provider).setName(ceName);
		}
	}

	/**
	 * Creates provider from extension point definition which matches value of the given attribute.
	 * The method will inspect extension registry for extension point "org.eclipse.cdt.core.LanguageSettingsProvider"
	 * to determine bundle and instantiate the class.
	 *
	 * @param attr - attribute to match.
	 * @param attrValue - value of the attribute to match.
	 * @param registry - extension registry.
	 * @param configure - flag which indicates if provider needs to be configured.
	 * @return new instance of the provider
	 */
	private static ILanguageSettingsProvider loadProviderFromRegistry(String attr, String attrValue,
			IExtensionRegistry registry, boolean configure) {
		try {
			IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID, PROVIDER_EXTENSION_SIMPLE_ID);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension ext : extensions) {
					for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
						if (cfgEl.getName().equals(ELEM_PROVIDER) && attrValue.equals(cfgEl.getAttribute(attr))) {
							ILanguageSettingsProvider provider = createExecutableExtension(cfgEl);
							if (configure) {
								configureExecutableProvider(provider, cfgEl);
							}
							return provider;
						}
					}
				}
			}
		} catch (Exception e) {
			CCorePlugin.log("Error creating language settings provider.", e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Create an instance of non-configured language settings provider of given class name.
	 * The class should be known to this method or registered with the extension point.
	 *
	 * @param className - class name to instantiate.
	 * @return new instance of language settings provider.
	 *
	 * @throws CoreException if not able to create a new instance.
	 */
	/*package*/ static ILanguageSettingsProvider instantiateProviderClass(String className) throws CoreException {
		if (className == null || className.equals(LanguageSettingsSerializableProvider.class.getName())) {
			return new LanguageSettingsSerializableProvider();
		}
		if (className.equals(LanguageSettingsGenericProvider.class.getName())) {
			return new LanguageSettingsGenericProvider();
		}

		// Create it as executable extension from the extension registry.
		ILanguageSettingsProvider provider = loadProviderFromRegistry(ATTR_CLASS, className,
				Platform.getExtensionRegistry(), false);
		if (provider == null) {
			String msg = "Not able to load provider class=" + className; //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, msg));
		}
		return provider;
	}

	/**
	 * Load an instance of language settings provider of given id from the extension point.
	 * The class should be registered with the extension point.
	 *
	 * @param id - class name to instantiate.
	 * @return new instance of language settings provider.
	 */
	/*package*/ static ILanguageSettingsProvider loadProvider(String id) {
		if (id == null) {
			return null;
		}

		// Create it as executable extension from the extension registry.
		ILanguageSettingsProvider provider = loadProviderFromRegistry(ATTR_ID, id, Platform.getExtensionRegistry(),
				true);
		if (provider == null) {
			String msg = "Not able to load provider id=" + id; //$NON-NLS-1$
			CCorePlugin.log(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, msg, new Exception(msg)));
		}
		return provider;
	}

	/**
	 * Returns list of provider id-s contributed by all extensions.
	 * @return the provider id-s.
	 */
	public static Set<String> getExtensionProviderIds() {
		return fExtensionProviders.keySet();
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
	 * @return a copy of the provider or {@code null} if copying is not possible.
	 */
	public static ILanguageSettingsEditableProvider getProviderCopy(ILanguageSettingsEditableProvider provider,
			boolean deep) {
		try {
			if (deep) {
				return provider.clone();
			} else {
				return provider.cloneShallow();
			}
		} catch (CloneNotSupportedException e) {
			CCorePlugin.log("Error cloning provider " + provider.getId() + ", class " + provider.getClass(), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
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
		ILanguageSettingsProvider provider = fExtensionProviders.get(id);
		if (provider instanceof ILanguageSettingsEditableProvider) {
			return getProviderCopy((ILanguageSettingsEditableProvider) provider, deep);
		}

		return null;
	}

	/**
	 * Test if the provider is equal to the one defined via extension point
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider}.
	 *
	 * @param provider - the provider to test.
	 * @param deep - {@code true} to check for deep equality testing also settings entries
	 *    or {@code false} to test shallow copy with no settings entries.
	 *    Shallow equality is applicable only for {@link ILanguageSettingsEditableProvider}.
	 * @return - {@code true} if the provider matches the extension or {@code false} otherwise.
	 */
	public static boolean isEqualExtensionProvider(ILanguageSettingsProvider provider, boolean deep) {
		String id = provider.getId();
		if (deep || !(provider instanceof ILanguageSettingsEditableProvider)) {
			ILanguageSettingsProvider extensionProvider = fExtensionProviders.get(id);
			return provider.equals(extensionProvider);
		} else {
			ILanguageSettingsEditableProvider providerShallow = getProviderCopy(
					(ILanguageSettingsEditableProvider) provider, false);
			ILanguageSettingsProvider extensionProviderShallow = getExtensionProviderCopy(id, false);
			return providerShallow == extensionProviderShallow
					|| (providerShallow != null && providerShallow.equals(extensionProviderShallow));
		}
	}

	/**
	 * Tells if the provider is meant to be shared between projects in workspace
	 * or belong to a specific configuration. This attribute is defined in
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
	 * <br>Note that only {@link ILanguageSettingsEditableProvider} can be owned by
	 * a configuration.
	 *
	 * @param id - ID of the provider to inquire.
	 * @return {@code true} if the provider is designed to be shared,
	 *    {@code false} if configuration-owned.
	 */
	public static boolean isPreferShared(String id) {
		ILanguageSettingsProvider provider = fExtensionProviders.get(id);
		if (provider instanceof LanguageSettingsBaseProvider && provider instanceof ILanguageSettingsEditableProvider) {
			return !((LanguageSettingsBaseProvider) provider).getPropertyBool(ATTR_PREFER_NON_SHARED);
		}
		return true;
	}
}
