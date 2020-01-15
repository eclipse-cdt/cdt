/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.options;

import java.util.Objects;

import org.eclipse.cdt.internal.core.options.OptionMessages;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.Preferences;

/**
 * The option storage implementation that uses OSGi preference node as an enclosed storage.
 *
 * @see Preferences
 *
 */
public final class OsgiPreferenceStorage implements OptionStorage {

	private final Preferences preferences;

	/**
	 *
	 * @param preferences the OSGi preference node, must not be <code>null</code>
	 */
	public OsgiPreferenceStorage(Preferences preferences) {
		Objects.requireNonNull(preferences, OptionMessages.OsgiPreferenceStorage_e_null_preference_node);
		this.preferences = preferences;
	}

	@Override
	public <V> V load(OptionMetadata<V> option) {
		Class<V> valueClass = option.valueClass();
		if (Boolean.class.equals(valueClass)) {
			return valueClass
					.cast(preferences.getBoolean(option.identifer(), Boolean.class.cast(option.defaultValue())));
		}
		String message = OptionMessages.PreferenceStorage_e_load_unsupported;
		throw new UnsupportedOperationException(NLS.bind(message, option, valueClass));
	}

	@Override
	public <V> void save(V value, OptionMetadata<V> option) {
		Class<V> valueClass = option.valueClass();
		if (Boolean.class.equals(valueClass)) {
			preferences.putBoolean(option.identifer(), Boolean.class.cast(value));
			return;
		}
		String message = OptionMessages.PreferenceStorage_e_save_unsupported;
		throw new UnsupportedOperationException(NLS.bind(message, option, valueClass));
	}

}
