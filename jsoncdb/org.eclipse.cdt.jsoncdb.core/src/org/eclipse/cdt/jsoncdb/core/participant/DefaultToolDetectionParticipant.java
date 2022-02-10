/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.jsoncdb.core.participant;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible to match the first argument (the tool command) of a command-line.
 *
 * @author Martin Weber
 */
public class DefaultToolDetectionParticipant implements IToolDetectionParticipant {
	/**
	 * start of the named capturing group that holds the command name (w/o quotes,
	 * if any)
	 */
	protected static final String REGEX_GROUP_CMD = "cmd"; //$NON-NLS-1$

	/** pattern part that matches file-system paths with forward slashes */
	@SuppressWarnings("nls")
	protected static final String REGEX_CMD_PATH_SLASH = String.format(Locale.ROOT, "\\A(?<%s>(\\S*?%s)?",
			REGEX_GROUP_CMD, "/");
	/** end of pattern part that matches file-system paths with forward slashes */
	@SuppressWarnings("nls")
	protected static final String REGEX_CMD_PATH_SLASH_END = ")\\s";

	/**
	 * pattern part that matches file-system paths with forward slashes and is in
	 * quotes
	 */
	@SuppressWarnings("nls")
	protected static final String REGEX_CMD_PATH_SLASH_QUOTE = String.format(Locale.ROOT,
			"\\A([\"'])(?<%s>((?:(?!\\1).)*?%s)", REGEX_GROUP_CMD, "/");
	/**
	 * end of pattern part that matches file-system paths with forward slashes and
	 * is in quotes
	 */
	@SuppressWarnings("nls")
	protected static final String REGEX_CMD_PATH_SLASH_QUOTE_END = ")\\1\\s";

	/** pattern part that matches win32 file-system paths */
	@SuppressWarnings("nls")
	protected static final String REGEX_CMD_PATH_BSLASH = String.format(Locale.ROOT, "\\A(?<%s>(\\S*?%s)?",
			REGEX_GROUP_CMD, "\\\\");
	/** end of pattern part that matches win32 file-system paths */
	@SuppressWarnings("nls")
	protected static final String REGEX_CMD_PATH_BSLASH_END = ")\\s";

	/**
	 * pattern part that matches file-system paths with back slashes and is in
	 * quotes
	 */
	@SuppressWarnings("nls")
	protected static final String REGEX_CMD_PATH_BSLASH_QUOTE = String.format(Locale.ROOT,
			"\\A([\"'])(?<%s>((?:(?!\\1).)*?%s)?", REGEX_GROUP_CMD, "\\\\");
	/**
	 * end of pattern part that matches file-system paths with back slashes and is
	 * in quotes
	 */
	@SuppressWarnings("nls")
	protected static final String REGEX_CMD_PATH_BSLASH_QUOTE_END = ")\\1\\s";

	/**
	 * the Patterns to match the name of the tool (including its path, BUT WITHOUT
	 * its filename extension) on a given command-line
	 */
	private final Pattern[] toolNamePatterns;
	private final Pattern[] toolNamePatternsBackslash;

	/**
	 * the Matcher that matches the name of the tool (including its path AND its
	 * filename extension) on a given command-line or {@code null}
	 *
	 * @deprecated Use toolNamePatternsExt instead
	 */
	@Deprecated(forRemoval = true)
	protected final Matcher[] toolNameMatchersExt;
	/**
	 * @deprecated Use toolNamePatternsExtBackslash instead
	 */
	@Deprecated(forRemoval = true)
	protected final Matcher[] toolNameMatchersExtBackslash;

	/**
	 * the Pattern that matches the name of the tool (including its path AND its
	 * filename extension) on a given command-line or {@code null}
	 */
	private final Pattern[] toolNamePatternsExt;
	private final Pattern[] toolNamePatternsExtBackslash;

	/**
	 * the corresponding parser for the tool arguments
	 */
	private final IToolCommandlineParser parser;

	protected final String basenameRegex;
	protected final String extensionRegex;

	/**
	 * whether this object can handle NTFS file system paths in the compiler
	 * argument in addition to a Linux path (which has forward slashes to separate
	 * path name components). If {@code true} the detection logic will also try to
	 * match path name with backslashes and will try to expand windows short paths
	 * like <code>C:/pr~1/aa~1.exe</code>.
	 */
	protected final boolean alsoHandleNtfsPaths;

	/**
	 * Creates a {@code DefaultToolDetectionParticipant} that matches linux paths in
	 * the tool name.
	 *
	 * @param basenameRegex a regular expression that matches the base name of the
	 *                      tool to detect. The specified expression should be
	 *                      non-greedy.
	 * @param parser        the corresponding parser for the tool arguments
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public DefaultToolDetectionParticipant(String basenameRegex, IToolCommandlineParser parser) {
		this(basenameRegex, false, parser);
	}

	/**
	 * Creates a {@code DefaultToolDetectionParticipant}.
	 *
	 * @param basenameRegex       a regular expression that matches the base name of
	 *                            the tool to detect. The specified expression
	 *                            should be non-greedy.
	 * @param alsoHandleNtfsPaths whether this object can handle NTFS file system
	 *                            paths in the compiler argument in addition to a
	 *                            Linux path (which has forward slashes to separate
	 *                            path name components). If {@code true} the
	 *                            detection logic will also try to match path name
	 *                            with backslashes and will try to expand windows
	 *                            short paths like <code>C:/pr~1/aa~1.exe</code>.
	 * @param parser              the corresponding parser for the tool arguments
	 */
	public DefaultToolDetectionParticipant(String basenameRegex, boolean alsoHandleNtfsPaths,
			IToolCommandlineParser parser) {
		this(basenameRegex, alsoHandleNtfsPaths, null, parser);
	}

	/**
	 * a {@code ParserDetectorExt} that matches linux paths in the tool name.
	 *
	 * @param basenameRegex  a regular expression that matches the base name of the
	 *                       tool to detect. The specified expression should be
	 *                       non-greedy.
	 * @param extensionRegex a regular expression that matches the filename
	 *                       extension of the tool to detect.
	 * @param parser         the corresponding parser for the tool arguments
	 */
	public DefaultToolDetectionParticipant(String basenameRegex, String extensionRegex, IToolCommandlineParser parser) {
		this(basenameRegex, false, extensionRegex, parser);
	}

	/**
	 * Creates a {@code DefaultToolDetectionParticipant}.
	 *
	 * @param basenameRegex       a regular expression that matches the base name of
	 *                            the tool to detect. The specified expression
	 *                            should be non-greedy.
	 * @param alsoHandleNtfsPaths whether this object can handle NTFS file system
	 *                            paths in the compiler argument in addition to a
	 *                            Linux path (which has forward slashes to separate
	 *                            path name components). If {@code true} the
	 *                            detection logic will also try to match path name
	 *                            with backslashes and will try to expand windows
	 *                            short paths like <code>C:/pr~1/aa~1.exe</code>.
	 * @param extensionRegex      a regular expression that matches the filename
	 *                            extension of the tool to detect.
	 * @param parser              the corresponding parser for the tool arguments
	 */
	@SuppressWarnings("nls")
	public DefaultToolDetectionParticipant(String basenameRegex, boolean alsoHandleNtfsPaths, String extensionRegex,
			IToolCommandlineParser parser) {
		this.basenameRegex = basenameRegex;
		this.parser = parser;
		this.alsoHandleNtfsPaths = alsoHandleNtfsPaths;
		this.extensionRegex = extensionRegex;

		this.toolNamePatterns = new Pattern[] {
				Pattern.compile(String.format(Locale.ROOT, "%s%s%s", REGEX_CMD_PATH_SLASH_QUOTE, basenameRegex,
						REGEX_CMD_PATH_SLASH_QUOTE_END)),
				Pattern.compile(String.format(Locale.ROOT, "%s%s%s", REGEX_CMD_PATH_SLASH, basenameRegex,
						REGEX_CMD_PATH_SLASH_END)) };
		if (alsoHandleNtfsPaths) {
			this.toolNamePatternsBackslash = new Pattern[] {
					Pattern.compile(String.format(Locale.ROOT, "%s%s%s", REGEX_CMD_PATH_BSLASH_QUOTE, basenameRegex,
							REGEX_CMD_PATH_BSLASH_QUOTE_END)),
					Pattern.compile(String.format(Locale.ROOT, "%s%s%s", REGEX_CMD_PATH_BSLASH, basenameRegex,
							REGEX_CMD_PATH_BSLASH_END)) };
		} else {
			this.toolNamePatternsBackslash = new Pattern[] {};
		}

		if (extensionRegex != null) {
			this.toolNamePatternsExt = new Pattern[] {
					Pattern.compile(String.format(Locale.ROOT, "%s%s\\.%s%s", REGEX_CMD_PATH_SLASH_QUOTE, basenameRegex,
							extensionRegex, REGEX_CMD_PATH_SLASH_QUOTE_END)),
					Pattern.compile(String.format(Locale.ROOT, "%s%s\\.%s%s", REGEX_CMD_PATH_SLASH, basenameRegex,
							extensionRegex, REGEX_CMD_PATH_SLASH_END)) };
			if (alsoHandleNtfsPaths) {
				this.toolNamePatternsExtBackslash = new Pattern[] {
						Pattern.compile(String.format(Locale.ROOT, "%s%s\\.%s%s", REGEX_CMD_PATH_BSLASH_QUOTE,
								basenameRegex, extensionRegex, REGEX_CMD_PATH_BSLASH_QUOTE_END)),
						Pattern.compile(String.format(Locale.ROOT, "%s%s\\.%s%s", REGEX_CMD_PATH_BSLASH, basenameRegex,
								extensionRegex, REGEX_CMD_PATH_BSLASH_END)) };
			} else {
				this.toolNamePatternsExtBackslash = new Pattern[] {};
			}
		} else {
			this.toolNamePatternsExt = new Pattern[] {};
			this.toolNamePatternsExtBackslash = new Pattern[] {};
		}

		toolNameMatchersExt = new Matcher[toolNamePatternsExt.length];
		for (int i = 0; i < toolNameMatchersExt.length; i++) {
			toolNameMatchersExt[i] = toolNamePatternsExt[i].matcher("");
		}
		toolNameMatchersExtBackslash = new Matcher[toolNamePatternsExtBackslash.length];
		for (int i = 0; i < toolNameMatchersExtBackslash.length; i++) {
			toolNameMatchersExtBackslash[i] = toolNamePatternsExtBackslash[i].matcher((""));
		}
	}

	@Override
	public IToolCommandlineParser getParser() {
		return parser;
	}

	@Override
	public boolean canHandleNtfsPaths() {
		return alsoHandleNtfsPaths;
	}

	@Override
	public Optional<MatchResult> basenameMatches(String commandLine, boolean matchBackslash) {
		if (matchBackslash && !canHandleNtfsPaths()) {
			return Optional.empty();
		}
		for (Pattern pattern : matchBackslash ? toolNamePatternsBackslash : toolNamePatterns) {
			Optional<MatchResult> result = patternMatches(pattern, commandLine);
			if (result.isPresent()) {
				return result;
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("nls")
	@Override
	public Optional<MatchResult> basenameWithVersionMatches(String commandLine, boolean matchBackslash,
			String versionRegex) {
		if (matchBackslash && !canHandleNtfsPaths()) {
			return Optional.empty();
		}

		Pattern[] toolNamePatterns;
		if (matchBackslash) {
			toolNamePatterns = new Pattern[] {
					Pattern.compile(String.format(Locale.ROOT, "%s%s%s%s", REGEX_CMD_PATH_BSLASH_QUOTE, basenameRegex,
							versionRegex, REGEX_CMD_PATH_BSLASH_QUOTE_END)),
					Pattern.compile(String.format(Locale.ROOT, "%s%s%s%s", REGEX_CMD_PATH_BSLASH, basenameRegex,
							versionRegex, REGEX_CMD_PATH_BSLASH_END)) };
		} else {
			toolNamePatterns = new Pattern[] {
					Pattern.compile(String.format(Locale.ROOT, "%s%s%s%s", REGEX_CMD_PATH_SLASH_QUOTE, basenameRegex,
							versionRegex, REGEX_CMD_PATH_SLASH_QUOTE_END)),
					Pattern.compile(String.format(Locale.ROOT, "%s%s%s%s", REGEX_CMD_PATH_SLASH, basenameRegex,
							versionRegex, REGEX_CMD_PATH_SLASH_END)) };
		}

		for (Pattern pattern : toolNamePatterns) {
			Optional<MatchResult> result = patternMatches(pattern, commandLine);
			if (result.isPresent()) {
				return result;
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<MatchResult> basenameWithExtensionMatches(String commandLine, boolean matchBackslash) {
		if (matchBackslash && !canHandleNtfsPaths()) {
			return Optional.empty();
		}
		for (Pattern pattern : matchBackslash ? toolNamePatternsExtBackslash : toolNamePatternsExt) {
			Optional<MatchResult> result = patternMatches(pattern, commandLine);
			if (result.isPresent()) {
				return result;
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("nls")
	@Override
	public Optional<MatchResult> basenameWithVersionAndExtensionMatches(String commandLine, boolean matchBackslash,
			String versionRegex) {
		if (extensionRegex == null || (matchBackslash && !canHandleNtfsPaths())) {
			return Optional.empty();
		}

		Pattern[] toolNamePatterns;
		if (matchBackslash) {
			toolNamePatterns = new Pattern[] {
					Pattern.compile(String.format(Locale.ROOT, "%s%s%s\\.%s%s", REGEX_CMD_PATH_BSLASH_QUOTE,
							basenameRegex, versionRegex, extensionRegex, REGEX_CMD_PATH_BSLASH_QUOTE_END)),
					Pattern.compile(String.format(Locale.ROOT, "%s%s%s\\.%s%s", REGEX_CMD_PATH_BSLASH, basenameRegex,
							versionRegex, extensionRegex, REGEX_CMD_PATH_BSLASH_END)) };
		} else {
			toolNamePatterns = new Pattern[] {
					Pattern.compile(String.format(Locale.ROOT, "%s%s%s\\.%s%s", REGEX_CMD_PATH_SLASH_QUOTE,
							basenameRegex, versionRegex, extensionRegex, REGEX_CMD_PATH_SLASH_QUOTE_END)),
					Pattern.compile(String.format(Locale.ROOT, "%s%s%s\\.%s%s", REGEX_CMD_PATH_SLASH, basenameRegex,
							versionRegex, extensionRegex, REGEX_CMD_PATH_SLASH_END)) };
		}

		for (Pattern pattern : toolNamePatterns) {
			Optional<MatchResult> result = patternMatches(pattern, commandLine);
			if (result.isPresent()) {
				return result;
			}
		}
		return Optional.empty();
	}

	/**
	 * Gets, whether the specified Pattern for the tool arguments can properly parse
	 * the specified command-line string. If so, the remaining arguments of the
	 * command-line are returned.
	 *
	 * @param pattern     the pattern that performs the match a regular expression
	 *                    that matches the version string in the name of the tool to
	 *                    detect.
	 * @param commandLine the command-line to match
	 * @return An empty {@code Optional} if the matcher did not match the tool name in the
	 *         command-line string. Otherwise, if the tool name matches, a
	 *         MatchResult holding the de-composed command-line is returned.
	 */
	private Optional<DefaultToolDetectionParticipant.MatchResult> patternMatches(Pattern pattern, String commandLine) {
		Matcher matcher = pattern.matcher(commandLine);
		if (matcher.lookingAt()) {
			return Optional.of(new DefaultToolDetectionParticipant.MatchResult(matcher.group(REGEX_GROUP_CMD),
					commandLine.substring(matcher.end())));
		}
		return Optional.empty();
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [basenameRegex=" + basenameRegex + ", extensionRegex=" + extensionRegex
				+ "]";
	}

} // DefaultToolDetectionParticipant