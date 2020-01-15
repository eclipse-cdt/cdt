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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
	private final Set<Class<?>> classes;

	/**
	 *
	 * @param preferences the OSGi preference node, must not be <code>null</code>
	 */
	public OsgiPreferenceStorage(Preferences preferences) {
		Objects.requireNonNull(preferences, OptionMessages.OsgiPreferenceStorage_e_null_preference_node);
		this.preferences = preferences;
		this.classes = new HashSet<>();
		classes.add(String.class);
		classes.add(Boolean.class);
		classes.add(byte[].class);
		classes.add(Double.class);
		classes.add(Float.class);
		classes.add(Integer.class);
		classes.add(Long.class);
	}

	@Override
	public <V> boolean consumable(Class<V> valueType) {
		return classes.contains(valueType);
	}

	@Override
	public <V> V load(OptionMetadata<V> option) {
		Class<V> valueClass = option.valueClass();
		String identifer = option.identifer();
		V defaultValue = option.defaultValue();
		if (String.class.equals(valueClass)) {
			return valueClass.cast(preferences.get(identifer, String.class.cast(defaultValue)));
		} else if (Boolean.class.equals(valueClass)) {
			return valueClass.cast(preferences.getBoolean(identifer, Boolean.class.cast(defaultValue)));
		} else if (byte[].class.equals(valueClass)) {
			return valueClass.cast(preferences.getByteArray(identifer, byte[].class.cast(defaultValue)));
		} else if (Double.class.equals(valueClass)) {
			return valueClass.cast(preferences.getDouble(identifer, Double.class.cast(defaultValue)));
		} else if (Float.class.equals(valueClass)) {
			return valueClass.cast(preferences.getFloat(identifer, Float.class.cast(defaultValue)));
		} else if (Integer.class.equals(valueClass)) {
			return valueClass.cast(preferences.getInt(identifer, Integer.class.cast(defaultValue)));
		} else if (Long.class.equals(valueClass)) {
			return valueClass.cast(preferences.getLong(identifer, Long.class.cast(defaultValue)));
		}
		String message = OptionMessages.PreferenceStorage_e_load_unsupported;
		throw new UnsupportedOperationException(NLS.bind(message, option, valueClass));
	}

	@Override
	public <V> void save(V value, OptionMetadata<V> option) {
		Class<V> valueClass = option.valueClass();
		String identifer = option.identifer();
		if (String.class.equals(valueClass)) {
			preferences.put(identifer, String.class.cast(value));
		} else if (Boolean.class.equals(valueClass)) {
			preferences.putBoolean(identifer, Boolean.class.cast(value));
		} else if (byte[].class.equals(valueClass)) {
			preferences.putByteArray(identifer, byte[].class.cast(value));
		} else if (Double.class.equals(valueClass)) {
			preferences.putDouble(identifer, Double.class.cast(value));
		} else if (Float.class.equals(valueClass)) {
			preferences.putFloat(identifer, Float.class.cast(value));
		} else if (Integer.class.equals(valueClass)) {
			preferences.putInt(identifer, Integer.class.cast(value));
		} else if (Long.class.equals(valueClass)) {
			preferences.putLong(identifer, Long.class.cast(value));
		} else {
			String message = OptionMessages.PreferenceStorage_e_save_unsupported;
			throw new UnsupportedOperationException(NLS.bind(message, option, valueClass));
		}
	}

}
