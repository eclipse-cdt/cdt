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
import org.eclipse.cdt.core.options.BaseOption;
import org.eclipse.cdt.core.options.OptionMetadata;

/**
 * @author weber
 */
final class ParserPreferencesMetadata implements IParserPreferencesMetadata {

	private final OptionMetadata<Boolean> tryVersionSuffixOption;
	private final OptionMetadata<String> versionSuffixPatternOption;
	private final OptionMetadata<Boolean> allocateConsoleOption;

	public ParserPreferencesMetadata() {
		this.tryVersionSuffixOption = new BaseOption<>(Boolean.class, "versionSuffixPatternEnabled", false, //$NON-NLS-1$
				Messages.ParserPreferencesMetadata_label_try_suffix,
				Messages.ParserPreferencesMetadata_ttip_try_suffix);
		this.versionSuffixPatternOption = new BaseOption<>(String.class, "versionSuffixPattern", "-?\\d+(\\.\\d+)*", //$NON-NLS-1$ //$NON-NLS-2$
				Messages.ParserPreferencesMetadata_label_suffix, Messages.ParserPreferencesMetadata_ttip_suffix);
		this.allocateConsoleOption = new BaseOption<>(Boolean.class, "allocateConsole", false, //$NON-NLS-1$
				Messages.ParserPreferencesMetadata_label_console);
	}

	@Override
	public OptionMetadata<Boolean> tryVersionSuffix() {
		return tryVersionSuffixOption;
	}

	@Override
	public OptionMetadata<String> versionSuffixPattern() {
		return versionSuffixPatternOption;
	}

	@Override
	public OptionMetadata<Boolean> allocateConsole() {
		return allocateConsoleOption;
	}
}
