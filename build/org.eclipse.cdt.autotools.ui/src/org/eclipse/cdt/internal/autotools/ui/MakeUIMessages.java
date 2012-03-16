/*******************************************************************************
 * Copyright (c) 2002, 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MakeUIMessages {

	public static final String BUNDLE_NAME = MakeUIMessages.class.getName();

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private MakeUIMessages() {

	}
	public static String getResourceString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/**
	 * Returns the plugin's resource bundle,
	 */
	public static ResourceBundle getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

}
