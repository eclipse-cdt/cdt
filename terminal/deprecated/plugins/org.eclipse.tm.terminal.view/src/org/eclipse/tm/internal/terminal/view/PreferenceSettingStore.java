/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

import org.eclipse.core.runtime.Preferences;

/**
 * A preference based settings store.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 */
public class PreferenceSettingStore extends org.eclipse.tm.internal.terminal.provisional.api.Settings {
	private final String fPrefix;
	private final Preferences fPreferences;

	/**
	 * Creates a ISettingStore that uses the preferences as backend.
	 *
	 * @param preferences the backed.
	 * @param prefix a string that is prepended to the key
	 */
	public PreferenceSettingStore(Preferences preferences, String prefix) {
		fPreferences=preferences;
		fPrefix=prefix;
	}
	
	public Object get(String key) {
		return fPreferences.getString(makeKey(key));
	}

	public boolean set(String key, Object value) {
		if (value instanceof String) {
			fPreferences.setValue(makeKey(key), (String)value);
		}
		return true;
	}
	/**
	 * @param key
	 * @return the full path in the preferences
	 */
	private String makeKey(String key) {
		return fPrefix+key;
	}
}
