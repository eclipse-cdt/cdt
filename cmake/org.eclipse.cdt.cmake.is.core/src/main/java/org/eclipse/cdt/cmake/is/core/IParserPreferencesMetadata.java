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

import org.eclipse.core.runtime.preferences.PreferenceMetadata;

/**
 * The metadata for options to configure the {@code compile_commands.json} parser.
 *
 * @author weber
 */
public interface IParserPreferencesMetadata {
	/**
	 * Returns the metadata for the {@link IParserPreferences#getTryVersionSuffix()} preference.
	 *
	 * @return the metadata for the preference option, never {@null}
	 */
	PreferenceMetadata<Boolean> tryVersionSuffix();

	/**
	 * Returns the metadata for the {@link IParserPreferences#getVersionSuffixPattern()} preference.
	 *
	 * @return the metadata for the preference option, never {@null}
	 */
	PreferenceMetadata<String> versionSuffixPattern();

	/**
	 * Returns the metadata for the {@link IParserPreferences#getAllocateConsole()} preference.
	 *
	 * @return the metadata for the preference option, never {@null}
	 */
	PreferenceMetadata<Boolean> allocateConsole();
}
