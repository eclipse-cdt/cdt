/*******************************************************************************
 * Copyright (c) 2008, 2015  QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Ericsson - Update for DSF
 *     Jonah Graham (Kichwa Coders) - Bug 317173 - cleanup warnings
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

public class LaunchMessages {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.dsf.gdb.launching.LaunchMessages";//$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = null;

	static {
		try {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
		} catch (MissingResourceException x) {
		}
	}

	private LaunchMessages() {
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getString(key), arg);
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), (Object[]) args);
	}

	public static String getString(String key) {
		if (RESOURCE_BUNDLE == null)
			return '!' + key + '!';
		return RESOURCE_BUNDLE.getString(key);
	}
}
