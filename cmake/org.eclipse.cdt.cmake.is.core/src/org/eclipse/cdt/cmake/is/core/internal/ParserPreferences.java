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

import java.util.Objects;

import org.eclipse.cdt.cmake.is.core.IParserPreferences;
import org.eclipse.cdt.cmake.is.core.IParserPreferencesMetadata;
import org.eclipse.core.runtime.preferences.IPreferenceMetadataStore;

/**
 * @author weber
 */
final class ParserPreferences implements IParserPreferences {

	private final IPreferenceMetadataStore optionStorage;
	private final IParserPreferencesMetadata metadata;

	public ParserPreferences(IPreferenceMetadataStore optionStorage, IParserPreferencesMetadata metadata) {
		this.optionStorage = Objects.requireNonNull(optionStorage, "optionStorage"); //$NON-NLS-1$
		this.metadata = Objects.requireNonNull(metadata, "metadata"); //$NON-NLS-1$
	}

	@Override
	public boolean getTryVersionSuffix() {
		return optionStorage.load(metadata.tryVersionSuffix());
	}

	@Override
	public void setTryVersionSuffix(boolean tryVersionSuffix) {
		optionStorage.save(tryVersionSuffix, metadata.tryVersionSuffix());
	}

	@Override
	public String getVersionSuffixPattern() {
		return optionStorage.load(metadata.versionSuffixPattern());
	}

	@Override
	public void setVersionSuffixPattern(String versionSuffixPattern) {
		Objects.requireNonNull(versionSuffixPattern);
		optionStorage.save(versionSuffixPattern, metadata.versionSuffixPattern());
	}

	@Override
	public boolean getAllocateConsole() {
		return optionStorage.load(metadata.allocateConsole());
	}

	@Override
	public void setAllocateConsole(boolean allocateConsole) {
		optionStorage.save(allocateConsole, metadata.allocateConsole());
	}
}
