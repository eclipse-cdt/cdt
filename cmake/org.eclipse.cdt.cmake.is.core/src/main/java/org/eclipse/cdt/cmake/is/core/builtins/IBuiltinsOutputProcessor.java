/*******************************************************************************
 * Copyright (c) 2018-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.builtins;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;

/**
 * Responsible for parsing the output that is produced when a compiler is
 * invoked to detect its-built-in preprocessor macros and include paths.
 */
public interface IBuiltinsOutputProcessor {

	/**
	 * Parsers the given line from the compiler output and places each
	 * ICLanguageSettingEntry found in the given {@code IProcessingContext}.
	 *
	 * @param line              a line from the compiler output to parse
	 * @param processingContext the buffer that receives the new
	 *                          {@code LanguageSetting} entries
	 */
	void processLine(String line, IProcessingContext processingContext);

	/**
	 * Gathers the results of argument parsing.
	 *
	 * @author Martin Weber
	 */
	public interface IProcessingContext {
		/**
		 * Adds a ICLanguageSettingEntry to the result list.
		 *
		 * @param entry the entry to add to the result list
		 */
		void addSettingEntry(ICLanguageSettingEntry entry);
	} // IProcessingContext

	/**
	 * The result of processing the complete compiler output.
	 *
	 * @author Martin Weber
	 *
	 * @see IBuiltinsOutputProcessor#processLine(String, IProcessingContext)
	 */
	public interface IResult {
		/**
		 * Gets the language setting entries produced during processing.
		 *
		 * @return the language setting entries
		 */
		List<ICLanguageSettingEntry> getSettingEntries();
	} // IResult
}
