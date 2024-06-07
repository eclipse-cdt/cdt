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

import java.io.IOException;

/**
 * Responsible for loading and persisting {@code ICMakeProperties} objects.
 *
 * @author Martin Weber
 * @since 1.4
 */
public interface ICMakePropertiesController {

	/** Creates a new {@code ICMakeProperties} object, initialized from the persistence store.
	 * If the persistence store does not exist, an object initialized to the default values is returned.
	 *
	 * @throws IOException if the persistence store exists but could not be read
	 */
	ICMakeProperties load() throws IOException;

	/** Saves the specified {@code ICMakeProperties} object to the persistence store.
	 *
	 * @throws IOException if the persistence store could not be written to
	 */
	void save(ICMakeProperties properties) throws IOException;
}
