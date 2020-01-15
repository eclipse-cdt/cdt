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
package org.eclipse.cdt.internal.core.options;

import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.Preferences;

public final class PreferenceStorage implements OptionStorage {

	private final Preferences preferences;

	public PreferenceStorage(Preferences preferences) {
		this.preferences = preferences;
	}

	@Override
	public <V> V load(OptionMetadata<V> option) {
		Class<V> valueClass = option.valueClass();
		if (Boolean.class.equals(valueClass)) {
			Boolean typed = Boolean.class.cast(option.defaultValue());
			boolean value = preferences.getBoolean(option.identifer(), typed);
			return valueClass.cast(value);
		}
		String message = OptionMessages.PreferenceStorage_e_load_unsupported;
		throw new UnsupportedOperationException(NLS.bind(message, option, valueClass));
	}

	@Override
	public <V> void save(V value, OptionMetadata<V> option) {
		Class<V> valueClass = option.valueClass();
		if (Boolean.class.equals(valueClass)) {
			Boolean typed = Boolean.class.cast(value);
			preferences.putBoolean(option.identifer(), typed);
			return;
		}
		String message = OptionMessages.PreferenceStorage_e_save_unsupported;
		throw new UnsupportedOperationException(NLS.bind(message, option, valueClass));
	}

}
