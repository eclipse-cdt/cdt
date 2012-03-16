/*******************************************************************************
 * Copyright (c) 2002, 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.properties;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * MakefilePreferencesMessages
 */
public class AutotoolsPropertyMessages {

	/**
	 * 
	 */
	private AutotoolsPropertyMessages() {
	}

	private static final String BUNDLE_NAME = AutotoolsPropertyMessages.class.getName();

	public static String getString(String key) {
		try {
			return ResourceBundle.getBundle(BUNDLE_NAME).getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		} catch (NullPointerException e) {
			return '#' + key + '#';
		}
	}	

}
