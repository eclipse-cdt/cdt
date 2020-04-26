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
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.language.settings.providers;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.core.runtime.content.IContentType;

public final class LanguageFileExtension implements Function<String, Optional<String>> {

	private final Function<String, ILanguageDescriptor> languages;
	private final Function<List<String>, Optional<String>> best;

	public LanguageFileExtension(Function<String, ILanguageDescriptor> languages) {
		Objects.requireNonNull(languages);
		this.languages = languages;
		this.best = new BestFileExtension();
	}

	@Override
	public Optional<String> apply(String language) {
		return Optional.ofNullable(languages.apply(language))//
				.map(ILanguageDescriptor::getContentTypes)//
				.filter(types -> types.length > 0)//
				.map(types -> types[0].getFileSpecs(IContentType.FILE_EXTENSION_SPEC))//
				.flatMap(specs -> best.apply(Arrays.asList(specs)));
	}

}
