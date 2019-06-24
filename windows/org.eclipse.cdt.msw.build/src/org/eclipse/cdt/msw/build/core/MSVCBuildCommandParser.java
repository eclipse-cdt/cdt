/*******************************************************************************
 * Copyright (c) 2019 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.msw.build.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;

/**
 * Build command parser capable to parse cl.exe command in build output and generate
 * language settings per file being compiled.
 */
public class MSVCBuildCommandParser extends AbstractBuildCommandParser implements ILanguageSettingsEditableProvider {

	private static String unescapeString(String value) {
		// There are probably many other things to unescape but these are the most
		// common.
		value = value.replaceAll("\\\\\\\\", "\\\\"); //$NON-NLS-1$//$NON-NLS-2$
		value = value.replaceAll("\\\\\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		return value;
	}

	private static class MSVCMacroOptionParser extends MacroOptionParser {

		public MSVCMacroOptionParser(String pattern, String nameExpression, String valueExpression) {
			super(pattern, nameExpression, valueExpression);
		}

		@Override
		public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
			return super.createEntry(name, unescapeString(value), flag);
		}
	}

	private static class MSVCIncludePathOptionParser extends IncludePathOptionParser {

		public MSVCIncludePathOptionParser(String pattern, String nameExpression) {
			super(pattern, nameExpression);
		}

		@Override
		public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
			return super.createEntry(name, unescapeString(value), flag);
		}
	}

	private static class MSVCForceIncludePathOptionParser extends IncludeFileOptionParser {

		public MSVCForceIncludePathOptionParser(String pattern, String nameExpression) {
			super(pattern, nameExpression);
		}

		@Override
		public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
			return super.createEntry(name, unescapeString(value), flag);
		}
	}

	// TODO: Should these be considered "built-in" entries (ICSettingEntry.BUILTIN)?
	private static class ClangCLMSVCSystemPathOptionParser extends IncludePathOptionParser {

		public ClangCLMSVCSystemPathOptionParser(String pattern, String nameExpression) {
			super(pattern, nameExpression);
		}

		@Override
		public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
			return super.createEntry(name, unescapeString(value), flag);
		}
	}

	@SuppressWarnings("nls")
	static final AbstractOptionParser[] optionParsers = { new MSVCIncludePathOptionParser("(-|/)I\\s*\"(.*)\"", "$2"),
			new MSVCIncludePathOptionParser("(-|/)I\\s*([^\\s\"]*)", "$2"),
			new MSVCForceIncludePathOptionParser("(-|/)FI\\s*\"(.*)\"", "$2"),
			new MSVCForceIncludePathOptionParser("(-|/)FI\\s*([^\\s\"]*)", "$2"),
			new ClangCLMSVCSystemPathOptionParser("(-|/)imsvc\\s*\"(.*)\"", "$2"),
			new ClangCLMSVCSystemPathOptionParser("(-|/)imsvc\\s*([^\\s\"]*)", "$2"),
			// /D "FOO=bar"
			new MSVCMacroOptionParser("(-|/)D\\s*\"([^=]+)=(.*)\"", "$2", "$3"),
			// /D FOO="bar"
			new MSVCMacroOptionParser("(-|/)D\\s*([^\\s=\"]+)=\"(.*?)(?<!\\\\)\"", "$2", "$3"),
			// /D FOO=bar
			new MSVCMacroOptionParser("(-|/)D\\s*([^\\s=\"]+)=([^\\s\"][^\\s]*)?", "$2", "$3"),
			// /D FOO
			new MSVCMacroOptionParser("(-|/)D\\s*([^\\s=\"]+)", "$2", "1"),
			// /D"FOO"
			new MSVCMacroOptionParser("(-|/)D\\s*\"([^\\s=\"]+)\"", "$2", "1"),
			// /U FOO
			new MacroOptionParser("(-|/)U\\s*([^\\s=\"]+)", "$2", ICSettingEntry.UNDEFINED),
			// /U "FOO"
			new MacroOptionParser("(-|/)U\\s*\"(.*?)\"", "$2", ICSettingEntry.UNDEFINED) };

	/**
	 * "foo" or "C:\foo\\"
	 *
	 * Using look-behind to resolve ambiguity with \" e.g. "\"in quotes\""
	 */
	private static final String QUOTE = "(\".*?(?<!\\\\)(\\\\\\\\)?\")"; //$NON-NLS-1$

	private static final Pattern OPTIONS_PATTERN = Pattern.compile("(-|/)[^\\s\"\\\\]*(\\s*[^-/\\s\"\\\\]*(" + QUOTE //$NON-NLS-1$
			+ "|" + "([^-/\\s][^\\s]+)))?"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final int OPTION_GROUP = 0;

	@Override
	protected List<String> parseOptions(String line) {
		if (line == null || currentResource == null) {
			return null;
		}

		List<String> options = new ArrayList<>();
		Matcher optionMatcher = OPTIONS_PATTERN.matcher(line);
		while (optionMatcher.find()) {
			String option = optionMatcher.group(OPTION_GROUP);
			if (option != null) {
				options.add(option);
			}
		}
		return options;
	}

	@Override
	protected AbstractOptionParser[] getOptionParsers() {
		return optionParsers;
	}

	@Override
	public MSVCBuildCommandParser cloneShallow() throws CloneNotSupportedException {
		return (MSVCBuildCommandParser) super.cloneShallow();
	}

	@Override
	public MSVCBuildCommandParser clone() throws CloneNotSupportedException {
		return (MSVCBuildCommandParser) super.clone();
	}

}
