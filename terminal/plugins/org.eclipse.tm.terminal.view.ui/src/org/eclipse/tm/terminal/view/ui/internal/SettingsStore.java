/*******************************************************************************
 * Copyright (c) 2011, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
public class SettingsStore implements ISettingsStore {
	private final Map<String, Object> settings = new HashMap<>();

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

	@Override
	public final String get(String key, String defaultValue) {
		Assert.isNotNull(key);
		String value = settings.get(key) instanceof String ? (String) settings.get(key) : null;
		return value != null ? value : defaultValue;
	}

	@Override
	public final String get(String key) {
		Assert.isNotNull(key);
		return settings.get(key) instanceof String ? (String) settings.get(key) : null;
	}

	@Override
	public final void put(String key, String value) {
		Assert.isNotNull(key);
		if (value == null)
			settings.remove(key);
		else
			settings.put(key, value);
	}
}
