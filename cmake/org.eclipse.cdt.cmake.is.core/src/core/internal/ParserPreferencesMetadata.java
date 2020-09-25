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

package org.eclipse.cdt.cmake.is.core.internal;

import org.eclipse.cdt.cmake.is.core.IParserPreferencesMetadata;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

/**
 * @author weber
 */
final class ParserPreferencesMetadata implements IParserPreferencesMetadata {

	private final PreferenceMetadata<Boolean> tryVersionSuffixOption;
	private final PreferenceMetadata<String> versionSuffixPatternOption;
	private final PreferenceMetadata<Boolean> allocateConsoleOption;

	public ParserPreferencesMetadata() {
		this.tryVersionSuffixOption = new PreferenceMetadata<>(Boolean.class, "versionSuffixPatternEnabled", false, //$NON-NLS-1$
				Messages.ParserPreferencesMetadata_label_try_suffix,
				Messages.ParserPreferencesMetadata_ttip_try_suffix);
		this.versionSuffixPatternOption = new PreferenceMetadata<>(String.class, "versionSuffixPattern", //$NON-NLS-1$
				"-?\\d+(\\.\\d+)*", //$NON-NLS-1$
				Messages.ParserPreferencesMetadata_label_suffix, Messages.ParserPreferencesMetadata_ttip_suffix);
		this.allocateConsoleOption = new PreferenceMetadata<>(Boolean.class, "allocateConsole", false, //$NON-NLS-1$
				Messages.ParserPreferencesMetadata_label_console);
	}

	@Override
	public PreferenceMetadata<Boolean> tryVersionSuffix() {
		return tryVersionSuffixOption;
	}

	@Override
	public PreferenceMetadata<String> versionSuffixPattern() {
		return versionSuffixPatternOption;
	}

	@Override
	public PreferenceMetadata<Boolean> allocateConsole() {
		return allocateConsoleOption;
	}
}
