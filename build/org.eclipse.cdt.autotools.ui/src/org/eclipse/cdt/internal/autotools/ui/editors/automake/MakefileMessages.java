/*******************************************************************************
 * Copyright (c) 2007, 2012 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MakefileMessages {

	private static final String BUNDLE_NAME = MakefileMessages.class.getName();

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private MakefileMessages() {
	}

	public static ResourceBundle getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 *
	 * @param key the message key
	 * @return the resource bundle message
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 *
	 * @param key the message key
	 * @param args an array of substitution strings
	 * @return the resource bundle message
	 */
	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), (Object[]) args);
	}

}
