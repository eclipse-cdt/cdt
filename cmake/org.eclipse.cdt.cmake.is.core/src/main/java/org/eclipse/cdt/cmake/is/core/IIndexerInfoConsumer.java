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

import java.util.List;
import java.util.Map;

/**
 * Receives the indexer relevant information for each source file while a {@link
 * CompileCommandsJsonParser#parse() compile_commands.json file is parsed}.
 *
 * @see CompileCommandsJsonParser
 *
 * @author weber
 */
public interface IIndexerInfoConsumer {
	// Cmake writes filenames with forward slashes (/) even if it runs on windows.
	// OTOH, IScannerInfoProvider requests info for IResourceS.
	// Somewhere in	the calling sequence, the filenames	have to	be converted/mapped to IResource. Conversion *could*
	// be done in CompileCommandsJsonParser, but	when I	think of	builds running
	// in a Linux-Docker-Container under windows, it might be better to do the conversion
	// on the IIndexerInfoConsumer side which has more information on the build setup.

	/** Adds indexer relevant information for a single source file.
	 *
	 * @param sourceFileName
	 * 		the name of the source file, in CMake notation. Note that on windows, CMake writes filenames with forward
	 * 		slashes (/) such as {@code H://path//to//source.c}.
	 * @param systemIncludePaths
	 * 		the system include paths ({@code #include <...>}) used to compile the given source file
	 * @param definedSymbols
	 * 		the preprocessor macros used to compile the given source file
	 * @param includePaths
	 * 		the local include paths ({@code #include "..."}) used to compile the given source file
	 * @param macroFiles
	 * 		the names of files that will be pre-processed by the compiler before parsing the source-file in
	 *		order to populate the preprocessor macro-dictionary
	 * @param includeFiles
	 * 		the names of files that will be pre-processed by the compiler as if
	 * 		an {@code #include "file"} directive appeared as the first line of the source file
	 */
	void acceptSourceFileInfo(String sourceFileName, List<String> systemIncludePaths,
			Map<String, String> definedSymbols, List<String> includePaths, List<String> macroFiles,
			List<String> includeFiles);

	/**
	 * Notifies this consumer that no further calls to {link {@link #acceptSourceFileInfo(String, List, Map, List)} will
	 * happen during the current parse operation.
	 */
	void shutdown();
}
