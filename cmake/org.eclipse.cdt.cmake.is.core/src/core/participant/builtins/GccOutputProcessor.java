/*******************************************************************************
 * Copyright (c) 2018-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.participant.builtins;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.cmake.is.core.participant.IRawIndexerInfoCollector;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;

/**
 * A {link IBuiltinsOutputProcessor} for the GNU C and GNU C++ compiler and derivatives.
 *
 * @author Martin Weber
 */
public final class GccOutputProcessor implements IBuiltinsOutputProcessor {
	@SuppressWarnings("nls")
	private static final OutputLineProcessor[] macros = {
			new OutputLineProcessor("#define\\s+(\\S+)\\s*(.*)", 1, 2, false, 0),
			new OutputLineProcessor("#define\\s+(\\S+)\\(.*?\\)\\s*(.*)", 1, 2, false, 0), };

	@SuppressWarnings("nls")
	private static final OutputLineProcessor[] localIncludes = {
			new OutputLineProcessor(" *(\\S.*)", 1, -1, true, ICSettingEntry.LOCAL) };

	@SuppressWarnings("nls")
	private static final OutputLineProcessor[] systemIncludes = {
			new OutputLineProcessor(" *(\\S.*)", 1, -1, true, 0) };

	@SuppressWarnings("nls")
	private static final OutputLineProcessor[] frameworks = {
			new OutputLineProcessor(" *(\\S.*)", 1, -1, true, ICSettingEntry.FRAMEWORKS_MAC) };

	private State state = State.NONE;

	// private static final String frameworkIndicator = "(framework directory)";

	public GccOutputProcessor() {
	}

	@SuppressWarnings("nls")
	@Override
	public void processLine(String line, IRawIndexerInfoCollector infoCollector) {

		// include paths
		if (line.equals("#include \"...\" search starts here:")) {
			state = State.EXPECTING_LOCAL_INCLUDE;
			return;
		} else if (line.equals("#include <...> search starts here:")) {
			state = State.EXPECTING_SYSTEM_INCLUDE;
			return;
		} else if (line.startsWith("End of search list.")) {
			state = State.NONE;
			return;
		} else if (line.equals("Framework search starts here:")) {
			// NOTE: need sample output of 'gcc -E -P -Wp,-v /tmp/foo.c' to implement this
			state = State.EXPECTING_FRAMEWORK;
			return;
		} else if (line.startsWith("End of framework search list.")) {
			state = State.NONE;
			return;
		}

		if (state == State.EXPECTING_LOCAL_INCLUDE) {
			for (OutputLineProcessor processor : localIncludes) {
				Optional<ICLanguageSettingEntry> result = processor.process(line);
				if (result.isPresent()) {
					infoCollector.addIncludePath(result.get().getName());
					return; // line matched
				}
			}
		} else if (state == State.EXPECTING_SYSTEM_INCLUDE) {
			for (OutputLineProcessor processor : systemIncludes) {
				Optional<ICLanguageSettingEntry> result = processor.process(line);
				if (result.isPresent()) {
					infoCollector.addSystemIncludePath(result.get().getName());
					return; // line matched
				}
			}
		} else if (state == State.EXPECTING_FRAMEWORK) {
			for (OutputLineProcessor processor : frameworks) {
				Optional<ICLanguageSettingEntry> result = processor.process(line);
				if (result.isPresent()) {
					// IExtendedScannerInfo has no mean to specify ICSettingEntry.FRAMEWORKS_MAC
					// infoCollector.addSettingEntry(result.get());
					return; // line matched
				}
			}
		} else {
			// macros
			for (OutputLineProcessor processor : macros) {
				Optional<ICLanguageSettingEntry> result = processor.process(line);
				if (result.isPresent()) {
					ICLanguageSettingEntry settingEntry = result.get();
					infoCollector.addDefine(settingEntry.getName(), settingEntry.getValue());
					return; // line matched
				}
			}
			// System.err.println("NO MATCH ON LINE: '" + line + "'");
		}
	}

	private enum State {
		NONE, EXPECTING_LOCAL_INCLUDE, EXPECTING_SYSTEM_INCLUDE, EXPECTING_FRAMEWORK
	}

	/**
	 * The purpose of this class is to parse a line from the compiler output when
	 * detecting built-in values and to create a language settings entry out of it.
	 *
	 * @author Martin Weber
	 */
	private static class OutputLineProcessor {

		private final Pattern pattern;
		private final int nameGroup;
		private final int valueGroup;
		private final int kind;
		private final int extraFlag;

		/**
		 * Constructor.
		 *
		 * @param pattern       regular expression pattern being parsed by the parser.
		 * @param nameGroup     capturing group number defining the
		 *                      {@link ICSettingEntry#getName() name} of an entry.
		 * @param valueGroup    capturing group number defining the
		 *                      {@link ICSettingEntry#getValue() value} of an entry.
		 *                      Specify {@code -1} if no value is captured.
		 * @param isIncludePath kind of language settings entries to create. Specify
		 *                      {@code true} to create a
		 *                      {@link ICSettingEntry#INCLUDE_PATH} entry, {@code false}
		 *                      to create a {@link ICLanguageSettingEntry#MACRO} entry
		 * @param extraFlag     extra-flags to add to the created language settings
		 *                      entry, e.g. {@link ICSettingEntry#LOCAL} or
		 *                      {@link ICSettingEntry#FRAMEWORKS_MAC}.
		 */
		public OutputLineProcessor(String pattern, int nameGroup, int valueGroup, boolean isIncludePath,
				int extraFlag) {
			this.pattern = Pattern.compile(pattern);
			this.nameGroup = nameGroup;
			this.valueGroup = valueGroup;
			this.kind = isIncludePath ? ICSettingEntry.INCLUDE_PATH : ICSettingEntry.MACRO;
			this.extraFlag = extraFlag;
		}

		/**
		 * Processes specified compiler output line.
		 *
		 * @param line the compiler output line to process
		 * @return the language settings entry constructed from the given output line or
		 *         an empty Optional if the line did not match any settings entry
		 */
		protected Optional<ICLanguageSettingEntry> process(String line) {
			final Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				final String name = matcher.group(nameGroup);
				final String value = valueGroup == -1 ? null : matcher.group(valueGroup);
				return Optional.of((ICLanguageSettingEntry) CDataUtil.createEntry(kind, name, value, null,
						ICSettingEntry.BUILTIN | ICSettingEntry.READONLY | extraFlag));
			}
			return Optional.empty(); // no match
		}
	}
}
