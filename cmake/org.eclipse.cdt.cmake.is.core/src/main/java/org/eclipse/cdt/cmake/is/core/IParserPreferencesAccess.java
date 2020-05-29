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

package org.eclipse.cdt.cmake.is.core;

/**
 * Provides access to the {@code compile_commands.json} parser preferences and its preference metadata.<br>
 *
 * A concrete implementation object of this interface can be retrieved through the OSGI service feature.
 *
 * @author weber
 */
public interface IParserPreferencesAccess {
	/**
	 * Gets the workspace {@code compile_commands.json} parser preferences.
	 */
	IParserPreferences getWorkspacePreferences();

	/**
	 * Gets the metadata for the parser preferences to be used in UI.
	 *
	 * @return the parser option metadata
	 */
	IParserPreferencesMetadata metadata();
}
