/*******************************************************************************
 * Copyright (c) 2009, 2016 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.language.settings.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.errorparsers.RegexErrorPattern;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

/**
 * Build command parser capable to parse gcc command in build output and generate
 * language settings per file being compiled.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class interface is not stable yet as
 * it is not currently (CDT 8.1, Juno) clear how it may need to be used in future.
 * There is no guarantee that this API will work or that it will remain the same.
 * Please do not use this API without consulting with the CDT team.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 *
 * @since 8.1
 */
public class GCCBuildCommandParser extends AbstractBuildCommandParser implements ILanguageSettingsEditableProvider {
	@SuppressWarnings("nls")
	static final AbstractOptionParser[] includeOptionParsers = {
			new IncludePathOptionParser("-I\\s*(?<quote>[\"'])(.*)\\k<quote>", "$2"),
			new IncludePathOptionParser("-I\\s*([^\\s\"']*)", "$1"), };

	@SuppressWarnings("nls")
	static final AbstractOptionParser[] systemIncludeOptionParsers = {
			new IncludePathOptionParser("-isystem\\s*(?<quote>[\"'])(.*)\\k<quote>", "$2"),
			new IncludePathOptionParser("-isystem\\s*([^\\s\"']*)", "$1"), };

	@SuppressWarnings("nls")
	static final AbstractOptionParser[] frameworkOptionParsers = {
			new IncludePathOptionParser("-(F|(iframework))\\s*(?<quote>[\"'])(.*)\\k<quote>", "$4",
					ICSettingEntry.FRAMEWORKS_MAC),
			new IncludePathOptionParser("-(F|(iframework))\\s*([^\\s\"']*)", "$3", ICSettingEntry.FRAMEWORKS_MAC), };

	@SuppressWarnings("nls")
	static final AbstractOptionParser[] forceIncludeOptionParsers = {
			new IncludeFileOptionParser("-include\\s*(?<quote>[\"'])(.*)\\k<quote>", "$2"),
			new IncludeFileOptionParser("-include\\s*([^\\s\"']*)", "$1"), };

	@SuppressWarnings("nls")
	static final AbstractOptionParser[] defineOptionParsers = {
			new MacroOptionParser("-D\\s*(?<quote>[\"'])([^=]*)(=(.*))?\\k<quote>", "$2", "$4"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=(\"\\\\(\")(.*?)\\\\\"\")", "$1", "$3$4$3"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=(?<quote>\\\\([\"']))(.*?)\\k<quote>", "$1", "$3$4$3"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=(?<quote>[\"'])(.*?)\\k<quote>", "$1", "$3"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=([^\\s\"']*)?", "$1", "$2"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)", "$1", "1"), };

	@SuppressWarnings("nls")
	static final AbstractOptionParser[] undefineOptionParsers = {
			new MacroOptionParser("-U\\s*([^\\s=\"']*)", "$1", ICSettingEntry.UNDEFINED), };

	@SuppressWarnings("nls")
	static final AbstractOptionParser[] macrosOptionParsers = {
			new MacroFileOptionParser("-imacros\\s*(?<quote>[\"'])(.*)\\k<quote>", "$2"),
			new MacroFileOptionParser("-imacros\\s*([^\\s\"']*)", "$1"), };

	@SuppressWarnings("nls")
	static final AbstractOptionParser[] libraryOptionParsers = {
			new LibraryPathOptionParser("-L\\s*(?<quote>[\"'])(.*)\\k<quote>", "$2"),
			new LibraryPathOptionParser("-L\\s*([^\\s\"']*)", "$1"),
			new LibraryFileOptionParser("-l\\s*([^\\s\"']*)", "lib$1.a"), };

	static final AbstractOptionParser[] emptyParsers = new AbstractOptionParser[0];

	static final AbstractOptionParser[] optionParsers;
	static {
		List<AbstractOptionParser> parsers = new ArrayList<>(Arrays.asList(includeOptionParsers));
		Collections.addAll(parsers, systemIncludeOptionParsers);
		Collections.addAll(parsers, frameworkOptionParsers);
		Collections.addAll(parsers, forceIncludeOptionParsers);
		Collections.addAll(parsers, defineOptionParsers);
		Collections.addAll(parsers, undefineOptionParsers);
		Collections.addAll(parsers, macrosOptionParsers);
		Collections.addAll(parsers, libraryOptionParsers);

		optionParsers = parsers.toArray(new AbstractOptionParser[0]);
	}

	@Override
	protected AbstractOptionParser[] getOptionParsers() {
		return optionParsers;
	}

	@SuppressWarnings("nls")
	@Override
	protected AbstractOptionParser[] getOptionParsers(String optionToParse) {
		if (optionToParse.length() <= 1) {
			return emptyParsers;
		}

		// Skip -, we know it's there with the OPTIONS_PATTERN
		String optionName = optionToParse.substring(1);

		if (optionName.startsWith("I")) {
			return includeOptionParsers;
		}

		if (optionName.startsWith("D")) {
			return defineOptionParsers;
		}

		if (optionName.startsWith("l") || optionName.startsWith("L")) {
			return libraryOptionParsers;
		}

		if (optionName.startsWith("i")) {
			if (optionName.startsWith("include")) {
				return forceIncludeOptionParsers;
			}

			if (optionName.startsWith("isystem")) {
				return systemIncludeOptionParsers;
			}

			if (optionName.startsWith("imacros")) {
				return macrosOptionParsers;
			}

			if (optionName.startsWith("iframework")) {
				return frameworkOptionParsers;
			}

			return emptyParsers;
		}

		if (optionName.startsWith("F")) {
			return frameworkOptionParsers;
		}

		if (optionName.startsWith("U")) {
			return undefineOptionParsers;
		}

		return emptyParsers;
	}

	@Override
	public GCCBuildCommandParser cloneShallow() throws CloneNotSupportedException {
		return (GCCBuildCommandParser) super.cloneShallow();
	}

	@Override
	public GCCBuildCommandParser clone() throws CloneNotSupportedException {
		return (GCCBuildCommandParser) super.clone();
	}

	/**
	 * Error Parser which allows highlighting of output lines matching the patterns of this parser.
	 * Intended for better troubleshooting experience.
	 */
	public static class GCCBuildCommandPatternHighlighter
			extends AbstractBuildCommandParser.AbstractBuildCommandPatternHighlighter {
		// ID of the parser taken from the existing extension point
		private static final String GCC_BUILD_COMMAND_PARSER_EXT = "org.eclipse.cdt.managedbuilder.core.GCCBuildCommandParser"; //$NON-NLS-1$

		/**
		 * Default constructor.
		 */
		public GCCBuildCommandPatternHighlighter() {
			super(GCC_BUILD_COMMAND_PARSER_EXT);
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			GCCBuildCommandPatternHighlighter that = new GCCBuildCommandPatternHighlighter();
			that.setId(getId());
			that.setName(getName());
			for (RegexErrorPattern pattern : getPatterns()) {
				that.addPattern((RegexErrorPattern) pattern.clone());
			}
			return that;
		}
	}

}
