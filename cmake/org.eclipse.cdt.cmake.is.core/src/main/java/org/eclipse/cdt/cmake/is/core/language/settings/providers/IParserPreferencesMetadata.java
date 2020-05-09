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

package org.eclipse.cdt.cmake.is.core.language.settings.providers;

import org.eclipse.cdt.core.options.OptionMetadata;

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
	OptionMetadata<Boolean> tryVersionSuffix();

	/**
	 * Returns the metadata for the {@link IParserPreferences#getVersionSuffixPattern()} preference.
	 *
	 * @return the metadata for the preference option, never {@null}
	 */
	OptionMetadata<String> versionSuffixPattern();

	/**
	 * Returns the metadata for the {@link IParserPreferences#getAllocateConsole()} preference.
	 *
	 * @return the metadata for the preference option, never {@null}
	 */
	OptionMetadata<Boolean> allocateConsole();
}
