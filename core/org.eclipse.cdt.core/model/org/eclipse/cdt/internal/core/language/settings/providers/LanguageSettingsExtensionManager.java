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

package org.eclipse.cdt.internal.core.language.settings.providers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
	static final String PROVIDER_EXTENSION_FULL_ID = "org.eclipse.cdt.core.LanguageSettingsProvider"; //$NON-NLS-1$
	static final String PROVIDER_EXTENSION_SIMPLE_ID = "LanguageSettingsProvider"; //$NON-NLS-1$

	static final String ELEM_PROVIDER = "provider"; //$NON-NLS-1$
	static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	static final String ATTR_ID = "id"; //$NON-NLS-1$
	static final String ATTR_NAME = "name"; //$NON-NLS-1$

	static final String ELEM_LANGUAGE_SCOPE = "language-scope"; //$NON-NLS-1$

	static final String ELEM_ENTRY = "entry"; //$NON-NLS-1$
	static final String ELEM_FLAG = "flag"; //$NON-NLS-1$
	static final String ATTR_KIND = "kind"; //$NON-NLS-1$
	static final String ATTR_VALUE = "value"; //$NON-NLS-1$

	/**
	 * Extension providers loaded once. If the provider is editable (read cloneable)
	 * external callers get copy rather than real instance.
	 */
	private static final LinkedHashMap<String, ILanguageSettingsProvider> fExtensionProviders = new LinkedHashMap<String, ILanguageSettingsProvider>();

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
		// sort by name - the providers defined via extensions are kept in separate list sorted
		Set<ILanguageSettingsProvider> sortedProviders = new TreeSet<ILanguageSettingsProvider>(
				new Comparator<ILanguageSettingsProvider>() {
					@Override
					public int compare(ILanguageSettingsProvider pr1, ILanguageSettingsProvider pr2) {
						return pr1.getName().compareTo(pr2.getName());
					}
				}
		);

		loadProviderExtensions(Platform.getExtensionRegistry(), sortedProviders);

		fExtensionProviders.clear();
		for (ILanguageSettingsProvider provider : sortedProviders) {
			fExtensionProviders.put(provider.getId(), provider);
		}
	}

	/**
	 * Load contributed extensions from extension registry.
	 *
	 * @param registry - extension registry
	 * @param providers - resulting set of providers
	 */
	private static void loadProviderExtensions(IExtensionRegistry registry, Set<ILanguageSettingsProvider> providers) {
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
		return value!=null ? value : ""; //$NON-NLS-1$
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
		if (ceClass==null || ceClass.trim().length()==0 || ceClass.equals(LanguageSettingsBaseProvider.class.getCanonicalName())) {
			provider = new LanguageSettingsBaseProvider();
		} else {
			provider = (ILanguageSettingsProvider)ce.createExecutableExtension(ATTR_CLASS);
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
		Map<String, String> ceAttributes = new HashMap<String, String>();
		List<String> languages = null;
		List<ICLanguageSettingEntry> entries = null;

		for (String attr : ce.getAttributeNames()) {
			ceAttributes.put(attr, determineAttributeValue(ce, attr));
		}

		for (IConfigurationElement ceLang : ce.getChildren(ELEM_LANGUAGE_SCOPE)) {
			String langId = determineAttributeValue(ceLang, ATTR_ID);
			if (langId.length() > 0) {
				if (languages == null) {
					languages = new ArrayList<String>();
				}
				languages.add(langId);
			}
		}

		for (IConfigurationElement ceEntry : ce.getChildren(ELEM_ENTRY)) {
			try {
				int entryKind = LanguageSettingEntriesSerializer.stringToKind(determineAttributeValue(ceEntry, ATTR_KIND));
				String entryName = determineAttributeValue(ceEntry, ATTR_NAME);
				String entryValue = determineAttributeValue(ceEntry, ATTR_VALUE);

				int flags = 0;
				for (IConfigurationElement ceFlags : ceEntry.getChildren(ELEM_FLAG)) {
					int bitFlag = LanguageSettingEntriesSerializer.composeFlags(determineAttributeValue(ceFlags, ATTR_VALUE));
					flags |= bitFlag;
				}

				ICLanguageSettingEntry entry = (ICLanguageSettingEntry) CDataUtil.createEntry(
						entryKind, entryName, entryValue, null, flags);

				if (entries == null) {
					entries = new ArrayList<ICLanguageSettingEntry>();
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
	 * Creates empty non-configured provider from extension point definition. The method will
	 * inspect extension registry for extension point "org.eclipse.cdt.core.LanguageSettingsProvider"
	 * to determine bundle and instantiate the class.
	 * ID and name of provider are assigned from the first encountered extension point specifying the class.
	 *
	 * @param className - full qualified class name of provider.
	 * @param registry - extension registry
	 * @return new non-configured provider
	 */
	private static ILanguageSettingsProvider createProviderCarcass(String className, IExtensionRegistry registry) {
		if (className==null || className.length()==0) {
			return new LanguageSettingsBaseProvider();
		}

		try {
			IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID, PROVIDER_EXTENSION_SIMPLE_ID);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension ext : extensions) {
					for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
						if (cfgEl.getName().equals(ELEM_PROVIDER) && className.equals(cfgEl.getAttribute(ATTR_CLASS))) {
							ILanguageSettingsProvider provider = createExecutableExtension(cfgEl);
							if (provider instanceof AbstractExecutableExtensionBase) {
								String ceId = determineAttributeValue(cfgEl, ATTR_ID);
								String ceName = determineAttributeValue(cfgEl, ATTR_NAME);
								((AbstractExecutableExtensionBase) provider).setId(ceId);
								((AbstractExecutableExtensionBase) provider).setName(ceName);
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
	 * Create an instance of language settings provider of given class name.
	 *
	 * @param className - class name to instantiate.
	 * @return new instance of language settings provider.
	 */
	/*package*/ static ILanguageSettingsProvider getProviderInstance(String className) {
		if (className==null || className.equals(LanguageSettingsSerializableProvider.class.getName())) {
			return new LanguageSettingsSerializableProvider();
		}

		// TODO unit test case for this
		if (className.equals(LanguageSettingsGenericProvider.class.getName())) {
			return new LanguageSettingsGenericProvider();
		}
		// FIXME - older usage, will fade it out
		if (className.equals("org.eclipse.cdt.internal.ui.language.settings.providers.UserLanguageSettingsProvider")) {
			return new LanguageSettingsGenericProvider();
		}

		ILanguageSettingsProvider provider = createProviderCarcass(className, Platform.getExtensionRegistry());
		if (provider==null) {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Not able to load provider class=" + className);
			CCorePlugin.log(new CoreException(status));
		}
		return provider;
	}

	/**
	 * Get Language Settings Provider defined via
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
	 *
	 * @param id - ID of provider to find.
	 * @return the clone of the provider or {@code null} if provider is not defined.
	 * @throws CloneNotSupportedException if the provider is not cloneable
	 */
	public static ILanguageSettingsProvider getExtensionProviderClone(String id) throws CloneNotSupportedException {
		ILanguageSettingsProvider provider = fExtensionProviders.get(id);
		if (provider!=null) {
			if (!(provider instanceof ILanguageSettingsEditableProvider))
				throw new CloneNotSupportedException("Not able to clone provider " + provider.getClass());

			provider = ((ILanguageSettingsEditableProvider) provider).clone();
		}
		return provider;
	}

	/**
	 * TODO
	 *
	 * @param id
	 * @return
	 */
	public static ILanguageSettingsProvider getExtensionProviderShallow(String id) {
		ILanguageSettingsProvider provider = fExtensionProviders.get(id);
		if (provider instanceof ILanguageSettingsEditableProvider) {
			try {
				return ((ILanguageSettingsEditableProvider) provider).cloneShallow();
			} catch (CloneNotSupportedException e) {
				IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Not able to clone provider " + provider.getClass());
				CCorePlugin.log(new CoreException(status));
				return null;
			}
		}
		return provider;
	}

	/**
	 * @return list of providers contributed by all extensions. Preferable copy but if not possible
	 *   will return raw provider.
	 */
	/*package*/ static List<ILanguageSettingsProvider> getExtensionProvidersInternal() {
		ArrayList<ILanguageSettingsProvider> list = new ArrayList<ILanguageSettingsProvider>(fExtensionProviders.size());
		for (String id : fExtensionProviders.keySet()) {
			ILanguageSettingsProvider extensionProvider = null;
			try {
				extensionProvider = getExtensionProviderClone(id);
			} catch (CloneNotSupportedException e) {
				// from here falls to get raw extension provider
			}
			if (extensionProvider==null)
				extensionProvider = fExtensionProviders.get(id);

			if (extensionProvider!=null)
				list.add(extensionProvider);
		}
		return list;
	}

	public static boolean equalsExtensionProviderShallow(ILanguageSettingsEditableProvider provider) throws CloneNotSupportedException {
		String id = provider.getId();
		ILanguageSettingsProvider extensionProviderShallow = getExtensionProviderShallow(id);
		return provider.cloneShallow().equals(extensionProviderShallow);
	}

	public static boolean equalsExtensionProvider(ILanguageSettingsProvider provider) {
		String id = provider.getId();
		ILanguageSettingsProvider extensionProvider = fExtensionProviders.get(id);
		return provider.equals(extensionProvider);
	}

}
