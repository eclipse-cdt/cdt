/*******************************************************************************
 * Copyright (c) 2006, 2012 Red Hat Inc..
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ConfigureMessages {
	private static final String BUNDLE_NAME = ConfigureMessages.class.getName();

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private ConfigureMessages() {
	}

	public static String getConfigureDescription(String name) {
		return getString("Option.configure." + name); //$NON-NLS-11$
	}

	public static String getConfigureTip(String name) {
		return getString("Option.configure." + name + ".tip"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getParameter(String name) {
		return getString("Option.configure." + name + ".parm"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the string from the resource bundle,
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
	 * Returns the formatted string from the resource bundle,
	 * or 'key' if not found.
	 *
	 * @param key the message key
	 * @param args an array of substituition strings
	 * @return the resource bundle message
	 */
	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), (Object[]) args);
	}

}
