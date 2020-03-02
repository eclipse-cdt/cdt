/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

/**
 * @noreference This class is not intended to be referenced by clients.
 */
public class CoreModelMessages {
	private static final String RESOURCE_BUNDLE = "org.eclipse.cdt.internal.core.model.CoreModelMessages"; //$NON-NLS-1$
	private static ResourceBundle fgResourceBundle;
	static {
		try {
			fgResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
		} catch (MissingResourceException x) {
			fgResourceBundle = null;
		}
	}

	private CoreModelMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		} catch (NullPointerException e) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Returns a string from the resource bundle and formats it with the argument
	 *
	 * @param key the string used to get the bundle value, must not be {@code null}
	 */
	public static String getFormattedString(String key) {
		return getString(key);
	}

	/**
	 * Returns a string from the resource bundle and formats it with the argument
	 *
	 * @param key the string used to get the bundle value, must not be {@code null}
	 */
	public static String getFormattedString(String key, Object arg) {
		return MessageFormat.format(getString(key), new Object[] { arg });
	}

	/**
	 * Returns a string from the resource bundle and formats it with arguments
	 */
	public static String getFormattedString(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}
}
