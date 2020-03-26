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
	// Yes.  Docs are currently intentionally missing here, since ATM it is not clear how to properly handle the sourceFileName argument.
	//
	// Cmake writes filenames with forward slashes (/) even if it runs on windows.
	// OTOH, IScannerInfoProvider requests info for IResourceS.
	// Somewhere in	the calling sequence, the filenames	have to	be converted/mapped to IResource.Conversion*could*
	// be done in CompileCommandsJsonParser, but	when I	think of	builds running
	// in a Linux-Docker-Container under windows, it might be better to do the conversion
	//on the IIndexerInfoConsumer side which has more information on the build setup.
	void acceptSourceFileInfo(String sourceFileName, List<String> systemIncludePaths,
			Map<String, String> definedSymbols, List<String> includePaths);
}
