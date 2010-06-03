/*******************************************************************************
 * Copyright (c) 2008, 2009  QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Ericsson - Update for DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import com.ibm.icu.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LaunchMessages {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.dsf.gdb.launching.LaunchMessages";//$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = null;

	static {
        try {
        	RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
        }
        catch (MissingResourceException x) {
        }
	}

	private LaunchMessages() {}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getString(key), new String[]{arg});
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), args);
	}

	public static String getString(String key) {
		if (RESOURCE_BUNDLE == null) return '!' + key + '!';
		return RESOURCE_BUNDLE.getString(key);
	}
}
