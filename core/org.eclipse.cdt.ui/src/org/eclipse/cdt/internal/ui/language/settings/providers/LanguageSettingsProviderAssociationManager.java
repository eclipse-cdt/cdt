/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language.settings.providers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * This class manages extensions of extension point org.eclipse.cdt.core.LanguageSettingsProvider
 * which defines appearance and behavior of UI controls for Language Settings Providers.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class LanguageSettingsProviderAssociationManager {
	/** Name of the extension point for contributing language settings provider associations */
	private static final String PROVIDER_ASSOCIATION_EXTENSION_POINT_SIMPLE_ID = "LanguageSettingsProviderAssociation"; //$NON-NLS-1$

	private static final String ELEM_ID_ASSOCIATION = "id-association"; //$NON-NLS-1$
	private static final String ELEM_CLASS_ASSOCIATION = "class-association"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_ICON = "icon"; //$NON-NLS-1$
	private static final String ATTR_PAGE = "page"; //$NON-NLS-1$
	private static final String ATTR_UI_CLEAR_ENTRIES = "ui-clear-entries"; //$NON-NLS-1$
	private static final String ATTR_UI_EDIT_ENTRIES = "ui-edit-entries"; //$NON-NLS-1$

	private static boolean isLoaded = false;
	private static List<URL> loadedIcons = new ArrayList<>();
	private static Map<String, URL> fImagesUrlById = new HashMap<>();
	private static Map<String, URL> fImagesUrlByClass = new HashMap<>();
	private static List<String> fRegirestedIds = new ArrayList<>();
	private static List<String> fRegisteredClasses = new ArrayList<>();

	private static Map<String, Map<String, String>> fAssociationsById = new HashMap<>();
	private static Map<String, Map<String, String>> fAssociationsByClass = new HashMap<>();

	/**
	 * Load extensions into memory maps.
	 */
	private static void loadExtensions() {
		if (isLoaded) {
			return;
		}
		isLoaded = true;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extension = registry.getExtensionPoint(CUIPlugin.PLUGIN_ID,
				PROVIDER_ASSOCIATION_EXTENSION_POINT_SIMPLE_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				@SuppressWarnings("unused")
				String extensionID = ext.getUniqueIdentifier();
				for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
					if (cfgEl.getName().equals(ELEM_ID_ASSOCIATION)) {
						String id = cfgEl.getAttribute(ATTR_ID);
						URL url = getIconUrl(cfgEl);
						fImagesUrlById.put(id, url);
						fRegirestedIds.add(id);

						Map<String, String> properties = new HashMap<>();
						putNotEmpty(properties, ATTR_PAGE, cfgEl.getAttribute(ATTR_PAGE));
						putNotEmpty(properties, ATTR_UI_CLEAR_ENTRIES, cfgEl.getAttribute(ATTR_UI_CLEAR_ENTRIES));
						putNotEmpty(properties, ATTR_UI_EDIT_ENTRIES, cfgEl.getAttribute(ATTR_UI_EDIT_ENTRIES));
						fAssociationsById.put(id, properties);
					} else if (cfgEl.getName().equals(ELEM_CLASS_ASSOCIATION)) {
						String className = cfgEl.getAttribute(ATTR_CLASS);
						URL url = getIconUrl(cfgEl);
						fImagesUrlByClass.put(className, url);
						String pageClass = cfgEl.getAttribute(ATTR_PAGE);
						if (pageClass != null && pageClass.length() > 0) {
							fRegisteredClasses.add(className);
						}

						Map<String, String> properties = new HashMap<>();
						putNotEmpty(properties, ATTR_PAGE, cfgEl.getAttribute(ATTR_PAGE));
						putNotEmpty(properties, ATTR_UI_CLEAR_ENTRIES, cfgEl.getAttribute(ATTR_UI_CLEAR_ENTRIES));
						putNotEmpty(properties, ATTR_UI_EDIT_ENTRIES, cfgEl.getAttribute(ATTR_UI_EDIT_ENTRIES));
						fAssociationsByClass.put(className, properties);
					}
				}
			}
		}

	}

	/**
	 * Put value into properties ignoring nulls.
	 */
	private static void putNotEmpty(Map<String, String> properties, String key, String value) {
		if (value != null)
			properties.put(key, value);
	}

	/**
	 * Find icon URL in its bundle.
	 */
	private static URL getIconUrl(IConfigurationElement config) {
		URL url = null;
		try {
			String iconName = config.getAttribute(ATTR_ICON);
			if (iconName != null) {
				URL pluginInstallUrl = Platform.getBundle(config.getDeclaringExtension().getContributor().getName())
						.getEntry("/"); //$NON-NLS-1$
				url = new URL(pluginInstallUrl, iconName);
				if (loadedIcons.contains(url)) {
					return url;
				}
			}
		} catch (MalformedURLException e) {
			CUIPlugin.log(e);
		}

		loadedIcons.add(url);
		if (url != null) {
			CDTSharedImages.register(url);
		}

		return url;
	}

	/**
	 * Get image URL for language settings provider with the given ID.
	 *
	 * @param providerId - ID of language settings provider.
	 * @return image URL or {@code null}.
	 */
	public static URL getImageUrl(String providerId) {
		loadExtensions();
		return fImagesUrlById.get(providerId);
	}

	/**
	 * Create an Options page for language settings provider with given ID.
	 */
	private static ICOptionPage createOptionsPageById(String providerId) {
		loadExtensions();

		if (fRegirestedIds.contains(providerId)) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extension = registry.getExtensionPoint(CUIPlugin.PLUGIN_ID,
					PROVIDER_ASSOCIATION_EXTENSION_POINT_SIMPLE_ID);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension ext : extensions) {
					try {
						@SuppressWarnings("unused")
						String extensionID = ext.getUniqueIdentifier();
						for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
							if (cfgEl.getName().equals(ELEM_ID_ASSOCIATION)) {
								String id = cfgEl.getAttribute(ATTR_ID);
								if (providerId.equals(id)) {
									String pageClass = cfgEl.getAttribute(ATTR_PAGE);
									if (pageClass != null && pageClass.trim().length() > 0) {
										ICOptionPage page = (ICOptionPage) cfgEl.createExecutableExtension(ATTR_PAGE);
										return page;
									}
								}
							}
						}
					} catch (Exception e) {
						CUIPlugin.log("Cannot load LanguageSettingsProviderAssociation extension " //$NON-NLS-1$
								+ ext.getUniqueIdentifier(), e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Create an Options page for language settings provider class by its name.
	 */
	private static ICOptionPage createOptionsPageByClass(String providerClassName) {
		loadExtensions();

		if (fRegisteredClasses.contains(providerClassName)) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extension = registry.getExtensionPoint(CUIPlugin.PLUGIN_ID,
					PROVIDER_ASSOCIATION_EXTENSION_POINT_SIMPLE_ID);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension ext : extensions) {
					try {
						@SuppressWarnings("unused")
						String extensionID = ext.getUniqueIdentifier();
						for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
							if (cfgEl.getName().equals(ELEM_CLASS_ASSOCIATION)) {
								String className = cfgEl.getAttribute(ATTR_CLASS);
								if (providerClassName.equals(className)) {
									String pageClass = cfgEl.getAttribute(ATTR_PAGE);
									if (pageClass != null && pageClass.trim().length() > 0) {
										ICOptionPage page = (ICOptionPage) cfgEl.createExecutableExtension(ATTR_PAGE);
										return page;
									}
								}
							}
						}
					} catch (Exception e) {
						CUIPlugin.log("Cannot load LanguageSettingsProviderAssociation extension " //$NON-NLS-1$
								+ ext.getUniqueIdentifier(), e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns Language Settings Provider image registered for closest superclass
	 * or interface.
	 *
	 * @param providerClass - class to find Language Settings Provider image.
	 * @return image or {@code null}
	 */
	public static URL getImage(Class<? extends ILanguageSettingsProvider> providerClass) {
		URL url = null;

		outer: for (Class<?> c = providerClass; c != null; c = c.getSuperclass()) {
			url = getImageURL(c);
			if (url != null) {
				break;
			}

			// this does not check for super-interfaces, feel free to implement as needed
			for (Class<?> i : c.getInterfaces()) {
				url = getImageURL(i);
				if (url != null) {
					break outer;
				}
			}
		}
		return url;
	}

	/**
	 * Return image URL registered for the given class.
	 */
	private static URL getImageURL(Class<?> clazz) {
		String className = clazz.getCanonicalName();
		for (Entry<String, URL> entry : fImagesUrlByClass.entrySet()) {
			if (entry.getKey().equals(className)) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Returns language settings provider Options page registered for closest superclass.
	 *
	 * @param provider - instance of provider to create Options page for.
	 * @return image or {@code null}.
	 */
	public static ICOptionPage createOptionsPage(ILanguageSettingsProvider provider) {
		String id = provider.getId();
		ICOptionPage optionsPage = createOptionsPageById(id);
		if (optionsPage != null) {
			return optionsPage;
		}

		Class<? extends ILanguageSettingsProvider> clazz = provider.getClass();
		outer: for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			optionsPage = createOptionsPageByClass(c);
			if (optionsPage != null) {
				break;
			}

			// this does not check for super-interfaces, feel free to implement as needed
			for (Class<?> i : c.getInterfaces()) {
				optionsPage = createOptionsPageByClass(i);
				if (optionsPage != null) {
					break outer;
				}
			}
		}
		return optionsPage;
	}

	/**
	 * Create an Options page for language settings provider class.
	 */
	private static ICOptionPage createOptionsPageByClass(Class<?> clazz) {
		ICOptionPage optionsPage = null;
		String className = clazz.getCanonicalName();
		if (fRegisteredClasses.contains(className)) {
			optionsPage = createOptionsPageByClass(className);
		}
		return optionsPage;
	}

	/**
	 * Returns value of the attribute of the provider by id or closest superclass.
	 */
	private static boolean getBooleanAttribute(ILanguageSettingsProvider provider, String attr) {
		loadExtensions();

		String id = provider.getId();

		Map<String, String> properties = fAssociationsById.get(id);
		if (properties != null) {
			return Boolean.parseBoolean(properties.get(attr));
		}

		for (Class<?> c = provider.getClass(); c != null; c = c.getSuperclass()) {
			String className = c.getCanonicalName();
			properties = fAssociationsByClass.get(className);
			if (properties != null) {
				return Boolean.parseBoolean(properties.get(attr));
			}

			// this does not check for superinterfaces, feel free to implement as needed
			for (Class<?> i : c.getInterfaces()) {
				String interfaceName = i.getCanonicalName();
				properties = fAssociationsByClass.get(interfaceName);
				if (properties != null) {
					return Boolean.parseBoolean(properties.get(attr));
				}
			}
		}
		return false;
	}

	/**
	 * Check if the user is allowed to edit language settings provider entries in UI.
	 * @param provider - language settings provider.
	 * @return {@code true} if editing is allowed or {@code false} if not.
	 */
	public static boolean isAllowedToEditEntries(ILanguageSettingsProvider provider) {
		return getBooleanAttribute(provider, ATTR_UI_EDIT_ENTRIES);
	}

	/**
	 * Check if the user is allowed to clear language settings provider entries in UI.
	 * @param provider - language settings provider.
	 * @return {@code true} if clearing is allowed or {@code false} if not.
	 */
	public static boolean isAllowedToClear(ILanguageSettingsProvider provider) {
		return getBooleanAttribute(provider, ATTR_UI_CLEAR_ENTRIES);
	}

}
