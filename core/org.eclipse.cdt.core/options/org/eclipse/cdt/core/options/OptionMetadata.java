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
 * The option metadata provides the information needed to configure
 * everything about the option except the option value itself.
 *
 * @param <V> the value type for the option
 */
public interface OptionMetadata<V> {

	/**
	 * The option identifier to use as a key to access the option value.
	 * Must not be <code>null</code>.
	 *
	 * @return the identifier
	 */
	String identifer();

	/**
	 * The default value for the option. Must not be <code>null</code>.
	 *
	 * @return the option's default value
	 */
	V defaultValue();

	/**
	 * Briefly describes the option purpose, intended to be used in UI.
	 * Must not be <code>null</code> and should be localized. Should not be blank.
	 *
	 * @return the option's name
	 */
	String name();

	/**
	 * Widely describes the option purpose, intended to be used in UI.
	 * Must not be <code>null</code> and should be localized. May be blank.
	 *
	 * @return the option's description
	 */
	String description();

	/**
	 * The type of option value needed to perform type checks. Must not be <code>null</code>.
	 *
	 * @return the value class
	 */
	Class<V> valueClass();
}
