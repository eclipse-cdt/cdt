/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

/**
 * @since 2.0
 */
public class ManagedMakeMessages {
	// Bundle ID
	private static final String BUNDLE_ID = "org.eclipse.cdt.managedbuilder.internal.core.PluginResources"; //$NON-NLS-1$
	//Resource bundle.
	private static ResourceBundle resourceBundle;

	static {
		try {
			resourceBundle = ResourceBundle.getBundle(BUNDLE_ID);
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	public static String getString(String key) {
		return getResourceString(key);
	}

	public static String getResourceString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException e) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new Object[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), (Object[]) args);
	}

	/**
	 *
	 */
	private ManagedMakeMessages() {
		// No constructor
	}
}
