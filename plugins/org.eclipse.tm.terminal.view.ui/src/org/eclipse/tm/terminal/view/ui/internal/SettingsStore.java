/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

/**
 * Simple default Terminal settings store implementation keeping the settings
 * within memory.
 */
@SuppressWarnings("restriction")
public class SettingsStore implements ISettingsStore {
	private final Map<String, Object> settings = new HashMap<String, Object>();

	/**
	 * Constructor.
	 */
	public SettingsStore() {
	}

	/**
	 * Returns the map containing the settings.
	 *
	 * @return The map containing the settings.
	 */
	public final Map<String, Object> getSettings() {
		return settings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#get(java.lang.String, java.lang.String)
	 */
	@Override
	public final String get(String key, String defaultValue) {
		Assert.isNotNull(key);
		String value = settings.get(key) instanceof String ? (String) settings.get(key) : null;
		return value != null ? value : defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#get(java.lang.String)
	 */
	@Override
	public final String get(String key) {
		Assert.isNotNull(key);
		return settings.get(key) instanceof String ? (String) settings.get(key) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#put(java.lang.String, java.lang.String)
	 */
	@Override
	public final void put(String key, String value) {
		Assert.isNotNull(key);
		if (value == null) settings.remove(key);
		else settings.put(key, value);
	}
}
