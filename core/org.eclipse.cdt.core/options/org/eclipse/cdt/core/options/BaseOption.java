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

/**
 * The base implementation for option metadata
 *
 * @param <V> the value type for the option
 *
 * @see OptionMetadata
 * @see OptionStorage
 */
public final class BaseOption<V> implements OptionMetadata<V> {

	private final Class<V> clazz;
	private final String identifier;
	private final V defaultValue;
	private final String name;
	private final String description;

	/**
	 * Created an instance of BaseOption using name as description
	 *
	 * @param clazz the value type of the option, must not be <code>null</code>
	 * @param identifier the identifier of the option, must not be <code>null</code>
	 * @param defaultValue the default value of the option, must not be <code>null</code>
	 * @param name the name of the option, must not be <code>null</code>
	 *
	 * @see BaseOption#BaseOption(Class, String, Object, String, String)
	 */
	public BaseOption(Class<V> clazz, String identifier, V defaultValue, String name) {
		this(clazz, identifier, defaultValue, name, name);
	}

	/**
	 * Created an instance of BaseOption of all the the given parameters
	 *
	 * @param clazz the value type of the option, must not be <code>null</code>
	 * @param identifier the identifier of the option, must not be <code>null</code>
	 * @param defaultValue the default value of the option, must not be <code>null</code>
	 * @param name the name of the option, must not be <code>null</code>
	 * @param description the description of the option, must not be <code>null</code>
	 *
	 * @see BaseOption#BaseOption(Class, String, Object, String)
	 */
	public BaseOption(Class<V> clazz, String identifier, V defaultValue, String name, String description) {
		Objects.requireNonNull(clazz, OptionMessages.BaseOption_e_null_value_type);
		Objects.requireNonNull(identifier, OptionMessages.BaseOption_e_null_identifier);
		Objects.requireNonNull(defaultValue, OptionMessages.BaseOption_e_null_default_value);
		Objects.requireNonNull(name, OptionMessages.BaseOption_e_null_name);
		Objects.requireNonNull(description, OptionMessages.BaseOption_e_null_description);
		this.clazz = clazz;
		this.identifier = identifier;
		this.defaultValue = defaultValue;
		this.name = name;
		this.description = description;
	}

	@Override
	public String identifer() {
		return identifier;
	}

	@Override
	public V defaultValue() {
		return defaultValue;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public Class<V> valueClass() {
		return clazz;
	}

}
