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

import java.util.List;
import java.util.Map;

/**
 * Receives the indexer relevant information for each source file.
 *
 * @author weber
 */
public interface IIndexerInfoConsumer {
	// TODO Docs
	void acceptSourceFileInfo(String sourceFileName, List<String> systemIncludePaths,
			Map<String, String> definedSymbols, List<String> includePaths);
}
