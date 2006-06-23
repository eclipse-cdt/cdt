/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.preferences;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * MakefilePreferencesMessages
 */
public class MakefilePreferencesMessages {

	/**
	 * 
	 */
	private MakefilePreferencesMessages() {
	}

	private static final String BUNDLE_NAME = "org.eclipse.cdt.make.internal.ui.preferences.MakefilePreferencesMessages"; //$NON-NLS-1$

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
