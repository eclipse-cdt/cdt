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

/**
 * Provides metadata-based access to an enclosed storage of options.
 *
 */
public interface OptionStorage {

	/**
	 * Checks if the value type can be consumed by an enclosed storage.
	 *
	 * @param <V> the value type for the option
	 * @param valueType the value type to be checked
	 *
	 * @return the option value or default value if option is unknown
	 */
	<V> boolean consumable(Class<V> valueType);

	/**
	 * Loads the value of specified option from an enclosed storage.
	 * If the value is not found returns the option default value.
	 *
	 * @param <V> the value type for the option
	 * @param option the option metadata, must not be <code>null</code>.
	 *
	 * @return the option value or default value if option is unknown
	 * @throws UnsupportedOperationException for unsupported option value types
	 *
	 * @see #consumable(Class)
	 * @see OptionMetadata
	 */
	<V> V load(OptionMetadata<V> option);

	/**
	 * Saves the value of specified option to the enclosed storage.
	 *
	 * @param <V> the value type for the option
	 * @param value to be saved, must not be <code>null</code>.
	 * @param option the option metadata, must not be <code>null</code>.
	 *
	 * @return the option value or default value if option is unknown
	 * @throws UnsupportedOperationException for unsupported option value types
	 *
	 * @see #consumable(Class)
	 * @see OptionMetadata
	 */
	<V> void save(V value, OptionMetadata<V> option);

}
