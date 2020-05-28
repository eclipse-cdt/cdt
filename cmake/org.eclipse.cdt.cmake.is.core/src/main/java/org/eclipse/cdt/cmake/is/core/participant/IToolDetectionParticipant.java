/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.participant;

import java.util.Optional;

/**
 * Participates in generating {@link org.eclipse.cdt.core.parser.IScannerInfo}
 * objects for the files being compiled in order to support the CDT indexer/
 * syntax highlighting.<br>
 * Generation of the {@code IScannerInfo} objects is done by analyzing a
 * {@code compile_commands.json} file produced by cmake.
 * <p>
 * This interface is the expected interface for extensions provided for the
 * {@code org.eclipse.cdt.cmake.is.core.detectionParticipant} extension point.
 * </p>
 *
 * @author Martin Weber
 */
public interface IToolDetectionParticipant {

	/**
	 * Gets the parser for the tool-specific command-line arguments.
	 *
	 * @return the parser, never {@code null}
	 */
	IToolCommandlineParser getParser();

	/**
	 * Gets whether this object can handle NTFS file system paths in the compiler
	 * argument in addition to a Linux path (which has forward slashes to separate
	 * path name components). If {@code true} the detection logic will also try to
	 * match path name with backslashes and will try to expand windows short paths
	 * like <code>C:/pr~1/aa~1.exe</code>.
	 */
	boolean canHandleNtfsPaths();

	/**
	 * Gets, whether the parser for the tool arguments can properly parse the
	 * specified command-line string.
	 *
	 * @param commandLine    the command line to match
	 * @param matchBackslash whether to match on file system paths with backslashes
	 *                       in the compiler argument or to match an paths with
	 *                       forward slashes
	 * @return An empty {@code Optional} if the tool/compiler handled by this object
	 *         does not match the first argument (the tool name) from the
	 *         command-line string. Otherwise, if the tool name matches, a
	 *         MatchResult holding the de-composed command-line is returned.
	 */
	Optional<MatchResult> basenameMatches(String commandLine, boolean matchBackslash);

	/**
	 * Gets, whether the parser for the tool arguments can properly parse the
	 * specified command-line string. This may be time-consuming, since it creates a
	 * Matcher object on each invocation.
	 *
	 * @param commandLine    the command-line to match
	 * @param matchBackslash whether to match on file system paths with backslashes
	 *                       in the compiler argument or to match an paths with
	 *                       forward slashes
	 * @param versionRegex   a regular expression that matches the version string in
	 *                       the name of the tool to detect.
	 * @return An empty {@code Optional} if the tool/compiler handled by this object
	 *         does not match the first argument (the tool name) from the
	 *         command-line string. Otherwise, if the tool name matches, a
	 *         MatchResult holding the de-composed command-line is returned.
	 */
	Optional<MatchResult> basenameWithVersionMatches(String commandLine, boolean matchBackslash, String versionRegex);

	/**
	 * Gets, whether the parser for the tool arguments can properly parse the
	 * specified command-line string.
	 *
	 * @param commandLine    the command-line to match
	 * @param matchBackslash whether to match on file system paths with backslashes
	 *                       in the compiler argument or to match an paths with
	 *                       forward slashes
	 * @return An empty {@code Optional} if the tool/compiler handled by this object
	 *         does not match the first argument (the tool name) from the
	 *         command-line string. Otherwise, if the tool name matches, a
	 *         MatchResult holding the de-composed command-line is returned.
	 */
	Optional<MatchResult> basenameWithExtensionMatches(String commandLine, boolean matchBackslash);

	/**
	 * Gets, whether the parser for the tool arguments can properly parse the
	 * specified command-line string. This may be time-consuming, since it creates a
	 * Matcher object on each invocation.
	 *
	 * @param commandLine    the command-line to match
	 * @param matchBackslash whether to match on file system paths with backslashes
	 *                       in the compiler argument or to match an paths with
	 *                       forward slashes
	 * @param versionRegex   a regular expression that matches the version string in
	 *                       the name of the tool to detect.
	 * @return An empty {@code Optional} if the tool/compiler handled by this object
	 *         does not match the first argument (the tool name) from the
	 *         command-line string. Otherwise, if the tool name matches, a
	 *         MatchResult holding the de-composed command-line is returned.
	 */
	Optional<MatchResult> basenameWithVersionAndExtensionMatches(String commandLine, boolean matchBackslash,
			String versionRegex);

	/**
	 * The result of matching a command-line string.
	 */
	public static class MatchResult {
		private final String command;
		private final String arguments;

		/**
		 * @param command   the command from the command-line, without the argument
		 *                  string. If the command contains space characters, the
		 *                  surrounding quotes must have been removed,
		 * @param arguments the remaining arguments from the command-line, without the
		 *                  command
		 */
		public MatchResult(String command, String arguments) {
			this.command = command;
			this.arguments = arguments;
		}

		/**
		 * Gets the command from the command-line, without the argument string. If the
		 * command contains space characters, the surrounding quotes have been removed,
		 */
		public String getCommand() {
			return this.command;
		}

		/**
		 * Gets the remaining arguments from the command-line, without the command.
		 */
		public String getArguments() {
			return this.arguments;
		}
	} // MatchResult
}