package org.eclipse.cdt.internal.ui.language.settings.providers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;

public class LanguageSettingsProviderAssociationManager {
	public static final String LANGUAGE_SETTINGS_PROVIDER_UI = "LanguageSettingsProviderAssociation"; //$NON-NLS-1$

	private static final String ELEM_ID_ASSOCIATION = "id-association"; //$NON-NLS-1$
	private static final String ELEM_CLASS_ASSOCIATION = "class-association"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_ICON = "icon"; //$NON-NLS-1$
	private static final String ATTR_PAGE = "page"; //$NON-NLS-1$
	private static final String ATTR_SHARED = "shared"; //$NON-NLS-1$
	private static final String ATTR_UI_CLEAR_ENTRIES = "ui-clear-entries"; //$NON-NLS-1$
	private static final String ATTR_UI_EDIT_ENTRIES = "ui-edit-entries"; //$NON-NLS-1$

	private static List<URL> loadedIcons = null;
	private static Map<String, URL> fImagesUrlById = null;
	private static Map<String, URL> fImagesUrlByClass = null;
	private static List<String> fRegirestedIds = null;
	private static List<String> fRegisteredClasses = null;

	private static Map<String, Map<String, String>> fAssociationsById = null;
	private static Map<String, Map<String, String>> fAssociationsByClass = null;

	private static void loadExtensions() {
		if (loadedIcons!=null) {
			return;
		}
		if (loadedIcons==null) loadedIcons = new ArrayList<URL>();
		if (fImagesUrlById==null) fImagesUrlById = new HashMap<String, URL>();
		if (fImagesUrlByClass==null) fImagesUrlByClass = new HashMap<String, URL>();
		if (fRegirestedIds==null) fRegirestedIds = new ArrayList<String>();
		if (fRegisteredClasses==null) fRegisteredClasses = new ArrayList<String>();

		if (fAssociationsById==null) fAssociationsById = new HashMap<String, Map<String, String>>();
		if (fAssociationsByClass==null) fAssociationsByClass = new HashMap<String, Map<String, String>>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extension = registry.getExtensionPoint(CUIPlugin.PLUGIN_ID, LANGUAGE_SETTINGS_PROVIDER_UI);
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

						Map<String, String> properties = new HashMap<String, String>();
						sensiblePut(properties, ATTR_PAGE, cfgEl.getAttribute(ATTR_PAGE));
						sensiblePut(properties, ATTR_SHARED, cfgEl.getAttribute(ATTR_SHARED));
						sensiblePut(properties, ATTR_UI_CLEAR_ENTRIES, cfgEl.getAttribute(ATTR_UI_CLEAR_ENTRIES));
						sensiblePut(properties, ATTR_UI_EDIT_ENTRIES, cfgEl.getAttribute(ATTR_UI_EDIT_ENTRIES));
						fAssociationsById.put(id, properties);
					} else if (cfgEl.getName().equals(ELEM_CLASS_ASSOCIATION)) {
						String className = cfgEl.getAttribute(ATTR_CLASS);
						URL url = getIconUrl(cfgEl);
						fImagesUrlByClass.put(className, url);
						String pageClass = cfgEl.getAttribute(ATTR_PAGE);
						if (pageClass!=null && pageClass.length()>0) {
							fRegisteredClasses.add(className);
						}

						Map<String, String> properties = new HashMap<String, String>();
						sensiblePut(properties, ATTR_PAGE, cfgEl.getAttribute(ATTR_PAGE));
						sensiblePut(properties, ATTR_SHARED, cfgEl.getAttribute(ATTR_SHARED));
						sensiblePut(properties, ATTR_UI_CLEAR_ENTRIES, cfgEl.getAttribute(ATTR_UI_CLEAR_ENTRIES));
						sensiblePut(properties, ATTR_UI_EDIT_ENTRIES, cfgEl.getAttribute(ATTR_UI_EDIT_ENTRIES));
						fAssociationsByClass.put(className, properties);
					}
				}
			}
		}

	}

	private static void sensiblePut(Map<String, String> properties, String key, String value) {
		if (value != null)
			properties.put(key, value);
	}

	private static URL getIconUrl(IConfigurationElement config) {
		URL url = null;
		try {
			String iconName = config.getAttribute(ATTR_ICON);
			if (iconName != null) {
				URL pluginInstallUrl = Platform.getBundle(config.getDeclaringExtension().getContributor().getName()).getEntry("/"); //$NON-NLS-1$
				url = new URL(pluginInstallUrl, iconName);
				if (loadedIcons.contains(url))
					return url;
			}
		} catch (MalformedURLException exception) {}

		loadedIcons.add(url);
		if (url!=null) {
			CDTSharedImages.register(url);
		}

		return url;
	}

	public static URL getImageUrl(String id) {
		if (fImagesUrlById==null) {
			loadExtensions();
		}
		return fImagesUrlById.get(id);
	}

	private static ICOptionPage createOptionsPageById(String providerId) {
		if (fRegirestedIds==null) {
			loadExtensions();
		}
		if (fRegirestedIds.contains(providerId)) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extension = registry.getExtensionPoint(CUIPlugin.PLUGIN_ID, LANGUAGE_SETTINGS_PROVIDER_UI);
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
									if (pageClass!=null && pageClass.trim().length()>0) {
										ICOptionPage page = (ICOptionPage) cfgEl.createExecutableExtension(ATTR_PAGE);
										return page;
									}
								}
							}
						}
					} catch (Exception e) {
						CUIPlugin.log("Cannot load LanguageSettingsProviderAssociation extension " + ext.getUniqueIdentifier(), e); //$NON-NLS-1$
					}
				}
			}
		}
		return null;
	}

	private static ICOptionPage createOptionsPageByClass(String providerClassName) {
		if (fRegisteredClasses==null) {
			loadExtensions();
		}
		if (fRegisteredClasses.contains(providerClassName)) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extension = registry.getExtensionPoint(CUIPlugin.PLUGIN_ID, LANGUAGE_SETTINGS_PROVIDER_UI);
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
									if (pageClass!=null && pageClass.trim().length()>0) {
										ICOptionPage page = (ICOptionPage) cfgEl.createExecutableExtension(ATTR_PAGE);
										return page;
									}
								}
							}
						}
					} catch (Exception e) {
						CUIPlugin.log("Cannot load LanguageSettingsProviderAssociation extension " + ext.getUniqueIdentifier(), e); //$NON-NLS-1$
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
	 * @param clazz - class to find Language Settings Provider image.
	 * @return image or {@code null}
	 */
	public static URL getImage(Class<? extends ILanguageSettingsProvider> clazz) {
		URL url = null;

		outer: for (Class<?> cl=clazz;cl!=null;cl=cl.getSuperclass()) {
			url = getImageURL(cl);
			if (url!=null)
				break;

			// this does not check for superinterfaces, feel free to implement as needed
			for (Class<?> in : cl.getInterfaces()) {
				url = getImageURL(in);
				if (url!=null)
					break outer;
			}
		}
		return url;
	}

	private static URL getImageURL(Class<?> clazz) {
		String className = clazz.getCanonicalName();
		Set<Entry<String, URL>> entrySet = fImagesUrlByClass.entrySet();
		for (Entry<String, URL> entry : entrySet) {
			if (entry.getKey().equals(className)) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Returns Language Settings Provider image registered for closest superclass.
	 * @param provider TODO
	 * @return image or {@code null}
	 */
	public static ICOptionPage createOptionsPage(ILanguageSettingsProvider provider) {
		String id = provider.getId();
		ICOptionPage optionsPage = createOptionsPageById(id);
		if (optionsPage!=null) {
			return optionsPage;
		}

		Class<? extends ILanguageSettingsProvider> clazz = provider.getClass();
		outer: for (Class<?> cl=clazz;cl!=null;cl=cl.getSuperclass()) {
			optionsPage = createOptionsPageByClass(cl);
			if (optionsPage!=null)
				break;

			// this does not check for superinterfaces, feel free to implement as needed
			for (Class<?> in : cl.getInterfaces()) {
				optionsPage = createOptionsPageByClass(in);
				if (optionsPage!=null)
					break outer;
			}
		}
		return optionsPage;
	}

	private static ICOptionPage createOptionsPageByClass(Class<?> c) {
		ICOptionPage optionsPage = null;
		String className = c.getCanonicalName();
		if (fRegisteredClasses.contains(className)) {
			optionsPage = createOptionsPageByClass(className);
		}
		return optionsPage;
	}

	/**
	 * Returns TODO for id or closest superclass.
	 * @param provider TODO
	 * @return TODO
	 */
	private static boolean getBooleanAttribute(ILanguageSettingsProvider provider, String attr) {
		loadExtensions();

		String id = provider.getId();

		Map<String, String> properties = fAssociationsById.get(id);
		if (properties != null) {
			return Boolean.parseBoolean(properties.get(attr));
		}

		for (Class<?> clazz=provider.getClass();clazz!=null;clazz=clazz.getSuperclass()) {
			String className = clazz.getCanonicalName();
			properties = fAssociationsByClass.get(className);
			if (properties != null) {
				return Boolean.parseBoolean(properties.get(attr));
			}

			// this does not check for superinterfaces, feel free to implement as needed
			for (Class<?> iface : clazz.getInterfaces()) {
				String interfaceName = iface.getCanonicalName();
				properties = fAssociationsByClass.get(interfaceName);
				if (properties != null) {
					return Boolean.parseBoolean(properties.get(attr));
				}
			}
		}
		return false;
	}


	/**
	 * Returns TODO for id or closest superclass.
	 * @param provider TODO
	 * @return TODO
	 */
	public static boolean shouldBeShared(ILanguageSettingsProvider provider) {
		return getBooleanAttribute(provider, ATTR_SHARED);
	}

	/**
	 * Returns TODO for id or closest superclass.
	 * @param provider TODO
	 * @return TODO
	 */
	public static boolean isToClear(ILanguageSettingsProvider provider) {
		return getBooleanAttribute(provider, ATTR_UI_CLEAR_ENTRIES);
	}

	/**
	 * Returns TODO for id or closest superclass.
	 * @param provider TODO
	 * @return TODO
	 */
	public static boolean isToEditEntries(ILanguageSettingsProvider provider) {
		return getBooleanAttribute(provider, ATTR_UI_EDIT_ENTRIES);
	}

}
