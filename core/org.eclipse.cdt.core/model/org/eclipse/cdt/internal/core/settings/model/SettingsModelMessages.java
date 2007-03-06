package org.eclipse.cdt.internal.core.settings.model;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class SettingsModelMessages {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.core.settings.model.SettingsModelMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private SettingsModelMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
