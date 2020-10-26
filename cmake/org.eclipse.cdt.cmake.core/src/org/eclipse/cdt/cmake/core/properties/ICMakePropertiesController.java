/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.properties;

/**
 * Responsible for loading and persting @code ICMakeProperties} objects.
 *
 * @author Martin Weber
 */
public interface ICMakePropertiesController {

	/** Creates a new {@code ICMakeProperties} object, initialized from the persistence store.
	 */
	ICMakeProperties load();

	/** Saves the specified {@code ICMakeProperties} object to the persistence store.
	 */
	void save(ICMakeProperties properties);
}
