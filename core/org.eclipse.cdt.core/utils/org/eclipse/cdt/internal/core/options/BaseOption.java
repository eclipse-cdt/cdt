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
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.options;

public abstract class BaseOption<V> implements OptionMetadata<V> {

	private final Class<V> clazz;
	private final String identifier;
	private final V defaultValue;
	private final String name;
	private final String description;

	public BaseOption(Class<V> clazz, String identifier, V defaultValue, String name) {
		this(clazz, identifier, defaultValue, name, name);
	}

	public BaseOption(Class<V> clazz, String identifier, V defaultValue, String name, String description) {
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
