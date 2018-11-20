/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.externaltool;

import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor;
import org.eclipse.cdt.codan.core.param.MapProblemPreference;

/**
 * Single external tool configuration setting.
 * @param <T> the type of the value this setting stores.
 *
 * @noextend This class is not intended to be extended by clients.
 * @since 2.1
 */
public class SingleConfigurationSetting<T> {
	private final IProblemPreferenceDescriptor descriptor;
	private final T defaultValue;
	private final Class<T> valueType;

	private T value;

	/**
	 * Constructor.
	 * @param descriptor meta-data that tells the UI how to display this setting.
	 * @param defaultValue the setting's default value.
	 * @param valueType the type of the value to store (used for safe casting.)
	 */
	public SingleConfigurationSetting(IProblemPreferenceDescriptor descriptor, T defaultValue, Class<T> valueType) {
		this.descriptor = descriptor;
		this.defaultValue = defaultValue;
		this.valueType = valueType;
	}

	/**
	 * Returns this setting's value.
	 * @return this setting's value.
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Returns the meta-data that tells the UI how to display this setting.
	 * @return the meta-data that tells the UI how to display this setting.
	 */
	public IProblemPreferenceDescriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * Returns this setting's default value.
	 * @return this setting's default value.
	 */
	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Updates this setting's value with the one stored in the given preference map.
	 * @param preferences the given preference map that may contain the value to set.
	 * @throws ClassCastException if the value to set is not of the same type as the one supported
	 * by this setting.
	 */
	public void updateValue(MapProblemPreference preferences) {
		IProblemPreference childDescriptor = preferences.getChildDescriptor(descriptor.getKey());
		if (childDescriptor != null) {
			value = valueType.cast(childDescriptor.getValue());
		}
	}
}
