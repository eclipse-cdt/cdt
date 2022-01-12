/*******************************************************************************
 * Copyright (c) 2002, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * AutoconfEditorMessages
 */
public class AutoconfEditorMessages {

	private static final String RESOURCE_BUNDLE = AutoconfEditorMessages.class.getName();

	private static ResourceBundle fgResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private AutoconfEditorMessages() {
	}

	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 *
	 * @param key	the string used to get the bundle value, must not be null
	 * @since 3.0
	 */
	public static String getFormattedString(String key, Object arg) {
		String format = null;
		try {
			format = fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		if (arg == null)
			arg = ""; //$NON-NLS-1$
		return MessageFormat.format(format, new Object[] { arg });
	}

	/**
	 * Gets a string from the resource bundle and formats it with the arguments
	 *
	 * @param key	the string used to get the bundle value, must not be null
	 * @since 3.0
	 */
	public static String getFormattedString(String key, Object arg1, Object arg2) {
		String format = null;
		try {
			format = fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		if (arg1 == null)
			arg1 = ""; //$NON-NLS-1$
		if (arg2 == null)
			arg2 = ""; //$NON-NLS-1$
		return MessageFormat.format(format, new Object[] { arg1, arg2 });
	}

	/**
	 * Gets a string from the resource bundle and formats it with the arguments
	 *
	 * @param key	the string used to get the bundle value, must not be null
	 * @since 3.0
	 */
	public static String getFormattedString(String key, Object arg1, Object arg2, Object arg3) {
		String format = null;
		try {
			format = fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		if (arg1 == null)
			arg1 = ""; //$NON-NLS-1$
		if (arg2 == null)
			arg2 = ""; //$NON-NLS-1$
		if (arg3 == null)
			arg3 = ""; //$NON-NLS-1$
		return MessageFormat.format(format, new Object[] { arg1, arg2, arg3 });
	}

	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 *
	 * @param key	the string used to get the bundle value, must not be null
	 * @since 3.0
	 */
	public static String getFormattedString(String key, boolean arg) {
		String format = null;
		try {
			format = fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		return MessageFormat.format(format, new Object[] { Boolean.valueOf(arg) });
	}

}
