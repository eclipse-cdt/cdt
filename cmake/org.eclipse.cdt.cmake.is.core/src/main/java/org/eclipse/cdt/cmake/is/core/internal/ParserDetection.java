/*******************************************************************************
 * Copyright (c) 2017-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.cdt.cmake.is.core.internal.builtins.GccBuiltinDetectionBehavior;
import org.eclipse.cdt.cmake.is.core.internal.builtins.MaybeGccBuiltinDetectionBehavior;
import org.eclipse.cdt.cmake.is.core.participant.Arglets;
import org.eclipse.cdt.cmake.is.core.participant.DefaultToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.participant.DefaultToolDetectionParticipant;
import org.eclipse.cdt.cmake.is.core.participant.IArglet;
import org.eclipse.cdt.cmake.is.core.participant.IToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.participant.IToolDetectionParticipant;
import org.eclipse.cdt.cmake.is.core.participant.IToolDetectionParticipant.MatchResult;
import org.eclipse.cdt.cmake.is.core.participant.ResponseFileArglets;
import org.eclipse.cdt.cmake.is.core.participant.builtins.IBuiltinsDetectionBehavior;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Utility classes and methods to detect a parser for a compiler given on a
 * command-line string.
 *
 * @author Martin Weber
 *
 */
@SuppressWarnings("nls")
public final class ParserDetection {
	private static final ILog log = Plugin.getDefault().getLog();
	private static final boolean DEBUG_PARTCIPANT_DETECTION = Boolean
			.parseBoolean(Platform.getDebugOption(Plugin.PLUGIN_ID + "/debug/participant"));

	/**
	 * tool detectors and their tool option parsers for each tool of interest that
	 * takes part in the current build. The Matcher detects whether a command line
	 * is an invocation of the tool.
	 */
	private static List<IToolDetectionParticipant> parserDetectors;

	static void init() {
		if (parserDetectors == null) {
			parserDetectors = new ArrayList<>(22);

			/** Names of known tools along with their command line argument parsers */
			final IArglet[] gcc_args = { new Arglets.IncludePath_C_POSIX(), new Arglets.MacroDefine_C_POSIX(),
					new Arglets.MacroUndefine_C_POSIX(),
					// not defined by POSIX, but does not harm..
					new Arglets.SystemIncludePath_C(), new Arglets.LangStd_GCC(), new Arglets.Sysroot_GCC(),
					new Arglets.IncludeFile_GCC(), new Arglets.MacrosFile_GCC() };

			IBuiltinsDetectionBehavior btbGccMaybee = new MaybeGccBuiltinDetectionBehavior();
			IBuiltinsDetectionBehavior btbGcc = new GccBuiltinDetectionBehavior();

			// POSIX compatible C compilers =================================
			{
				final IToolCommandlineParser cc = new DefaultToolCommandlineParser(new ResponseFileArglets.At(),
						btbGccMaybee, gcc_args);
				parserDetectors.add(new DefaultToolDetectionParticipant("cc", true, "exe", cc));
			}
			// POSIX compatible C++ compilers ===============================
			{
				final IToolCommandlineParser cxx = new DefaultToolCommandlineParser(new ResponseFileArglets.At(),
						btbGccMaybee, gcc_args);
				parserDetectors.add(new DefaultToolDetectionParticipant("c\\+\\+", true, "exe", cxx));
			}

			// GNU C compatible compilers ====
			{
				final IToolCommandlineParser gcc = new DefaultToolCommandlineParser(new ResponseFileArglets.At(),
						btbGcc, gcc_args);
				parserDetectors.add(new DefaultToolDetectionParticipant("gcc", true, "exe", gcc));
				parserDetectors.add(new DefaultToolDetectionParticipant("clang", true, "exe", gcc));
				// cross compilers, e.g. arm-none-eabi-gcc ====
				parserDetectors.add(new DefaultToolDetectionParticipant("\\S+?-gcc", true, "exe", gcc));
			}
			// GNU C++ compatible compilers ====
			{
				final IToolCommandlineParser gxx = new DefaultToolCommandlineParser(new ResponseFileArglets.At(),
						btbGcc, gcc_args);
				parserDetectors.add(new DefaultToolDetectionParticipant("g\\+\\+", true, "exe", gxx));
				parserDetectors.add(new DefaultToolDetectionParticipant("clang\\+\\+", true, "exe", gxx));
				// cross compilers, e.g. arm-none-eabi-g++ ====
				parserDetectors.add(new DefaultToolDetectionParticipant("\\S+?-g\\+\\+", true, "exe", gxx));
			}
			{
				// cross compilers, e.g. arm-none-eabi-c++ ====
				final IToolCommandlineParser cxx = new DefaultToolCommandlineParser(new ResponseFileArglets.At(),
						btbGccMaybee, gcc_args);
				parserDetectors.add(new DefaultToolDetectionParticipant("\\S+?-c\\+\\+", true, "exe", cxx));
			}

			// compilers from extension points
			loadExtentionsSorted(parserDetectors::add);
		}
	}

	/**
	 * @param consumer consumes the newly loaded IToolDetectionParticipant objects
	 */
	private static void loadExtentionsSorted(Consumer<? super IToolDetectionParticipant> consumer) {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("org.eclipse.cdt.cmake.is.core.detectionParticipant");
		Map<IToolDetectionParticipant, Integer> sortMap = new HashMap<>();
		for (IConfigurationElement e : elements) {
			try {
				Object obj = e.createExecutableExtension("class");
				String attr = e.getAttribute("order");
				Integer order = Integer.valueOf(Integer.MAX_VALUE);
				try {
					order = Integer.parseUnsignedInt(Optional.ofNullable(attr).orElse("100000"));
					order = Integer.max(10000, order);
				} catch (NumberFormatException takeMax) {
				}
				if (obj instanceof IToolDetectionParticipant) {
					sortMap.put((IToolDetectionParticipant) obj, order);
				}
			} catch (CoreException ex) {
				log.log(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, e.getNamespaceIdentifier(), ex));
			}
		}
		// sort by order and add to list
		sortMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).forEach(consumer);
	}

	/** Just static methods */
	private ParserDetection() {
	}

	/**
	 * Determines the parser detector that can parse the specified command-line.
	 *
	 * @param line                the command line to process
	 * @param versionSuffixRegex  the regular expression to match a version suffix
	 *                            in the compiler name or {@code null} to not try to
	 *                            detect the compiler with a version suffix
	 * @param tryWindowsDetectors whether to also try the detectors for ms windows
	 *                            OS
	 *
	 * @return {@code null} if none of the detectors matches the tool name in the
	 *         specified command-line string. Otherwise, if the tool name matches, a
	 *         {@code ParserDetectionResult} holding the remaining command-line
	 *         string (without the portion that matched) is returned.
	 */
	public static ParserDetectionResult determineDetector(String line, String versionSuffixRegex,
			boolean tryWindowsDetectors) {
		ParserDetectionResult result;
		if (DEBUG_PARTCIPANT_DETECTION) {
			System.out.printf("> Command-line '%s'%n", line);
			System.out.printf("> Looking up detector for command '%s ...'%n",
					line.substring(0, Math.min(40, line.length())));
		}
		// try default detectors
		result = determineDetector0(line, versionSuffixRegex, false);
		if (result == null && tryWindowsDetectors) {
			// try with backslash as file separator on windows
			result = determineDetector0(line, versionSuffixRegex, true);
			if (result == null) {
				// try workaround for windows short file names
				final String shortPathExpanded = expandShortFileName(line);
				result = determineDetector0(shortPathExpanded, versionSuffixRegex, false);
				if (result == null) {
					// try with backslash as file separator on windows
					result = determineDetector0(shortPathExpanded, versionSuffixRegex, true);
				}
			}
		}
		if (result != null) {
			if (DEBUG_PARTCIPANT_DETECTION)
				System.out.printf("< Found detector for command '%s': %s (%s)%n", result.getCommandLine().getCommand(),
						result.getDetectorWithMethod().getToolDetectionParticipant().getParser().getClass()
								.getSimpleName(),
						result.getDetectorWithMethod().getHow());
		}
		return result;
	}

	/**
	 * Determines a C-compiler-command line parser that is able to parse the
	 * relevant arguments in the specified command line.
	 *
	 * @param commandLine        the command line to process
	 * @param versionSuffixRegex the regular expression to match a version suffix in
	 *                           the compiler name or {@code null} to not try to
	 *                           detect the compiler with a version suffix
	 * @param matchBackslash     whether to match on file system paths with
	 *                           backslashes in the compiler argument or to match an
	 *                           paths with forward slashes
	 * @return {@code null} if none of the detectors matches the tool name in the
	 *         specified command-line string. Otherwise, if the tool name matches, a
	 *         {@code ParserDetectionResult} holding the de-compose command-line is
	 *         returned.
	 */
	private static ParserDetectionResult determineDetector0(String commandLine, String versionSuffixRegex,
			boolean matchBackslash) {
		init();

		Optional<DefaultToolDetectionParticipant.MatchResult> cmdline;
		// try basenames
		for (IToolDetectionParticipant pd : parserDetectors) {
			if (DEBUG_PARTCIPANT_DETECTION)
				System.out.printf("  Trying participant %s (%s)%n", pd, DetectorWithMethod.DetectionMethod.BASENAME);
			if ((cmdline = pd.basenameMatches(commandLine, matchBackslash)).isPresent()) {
				return new ParserDetectionResult(
						new DetectorWithMethod(pd, DetectorWithMethod.DetectionMethod.BASENAME, matchBackslash),
						cmdline.get());
			}
		}
		if (versionSuffixRegex != null) {
			// try with version pattern
			for (IToolDetectionParticipant pd : parserDetectors) {
				if (DEBUG_PARTCIPANT_DETECTION)
					System.out.printf("  Trying participant %s (%s)%n", pd,
							DetectorWithMethod.DetectionMethod.WITH_VERSION);
				if ((cmdline = pd.basenameWithVersionMatches(commandLine, matchBackslash, versionSuffixRegex))
						.isPresent()) {
					return new ParserDetectionResult(
							new DetectorWithMethod(pd, DetectorWithMethod.DetectionMethod.WITH_VERSION, matchBackslash),
							cmdline.get());
				}
			}
		}
		// try with extension
		for (IToolDetectionParticipant pd : parserDetectors) {
			if (DEBUG_PARTCIPANT_DETECTION)
				System.out.printf("  Trying participant %s (%s)%n", pd,
						DetectorWithMethod.DetectionMethod.WITH_EXTENSION);
			if ((cmdline = pd.basenameWithExtensionMatches(commandLine, matchBackslash)).isPresent()) {
				return new ParserDetectionResult(
						new DetectorWithMethod(pd, DetectorWithMethod.DetectionMethod.WITH_EXTENSION, matchBackslash),
						cmdline.get());
			}
		}
		if (versionSuffixRegex != null) {
			// try with extension and version
			for (IToolDetectionParticipant pd : parserDetectors) {
				if (DEBUG_PARTCIPANT_DETECTION)
					System.out.printf("  Trying participant %s (%s)%n", pd,
							DetectorWithMethod.DetectionMethod.WITH_VERSION_EXTENSION);
				if ((cmdline = pd.basenameWithVersionAndExtensionMatches(commandLine, matchBackslash,
						versionSuffixRegex)).isPresent()) {
					return new ParserDetectionResult(new DetectorWithMethod(pd,
							DetectorWithMethod.DetectionMethod.WITH_VERSION_EXTENSION, matchBackslash), cmdline.get());
				}
			}
		}

		return null;
	}

	/**
	 * Tries to convert windows short file names for the compiler executable (like
	 * {@code AVR-G_~1.EXE}) into their long representation. This is a workaround
	 * for a <a href="https://gitlab.kitware.com/cmake/cmake/issues/16138">bug in
	 * CMake under windows</a>.<br>
	 * See <a href="https://github.com/15knots/cmake4eclipse/issues/31">issue #31
	 */
	private static String expandShortFileName(String commandLine) {
		if (commandLine.indexOf('~', 6) == -1) {
			// not a short file name
			return commandLine;
		}
		String command;
		StringBuilder commandLine2 = new StringBuilder();
		// split at first space character
		int idx = commandLine.indexOf(' ');
		if (idx != -1) {
			command = commandLine.substring(0, idx);
			commandLine2.append(commandLine.substring(idx));
		} else {
			command = commandLine;
		}
		// convert to long file name and retry lookup
		try {
			command = new File(command).getCanonicalPath();
			commandLine2.insert(0, command);
			return commandLine2.toString();
		} catch (IOException e) {
			log.log(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, command, e));
		}
		return commandLine;
	}

	// has public scope for unittest purposes
	public static class DetectorWithMethod {
		public enum DetectionMethod {
			BASENAME, WITH_VERSION, WITH_EXTENSION, WITH_VERSION_EXTENSION;
		}

		/**
		 * the DefaultToolDetectionParticipant that matched the name of the tool on a
		 * given command-line
		 */
		private final IToolDetectionParticipant detector;
		/** describes the method that was used to match */
		private final DetectionMethod how;
		private final boolean matchBackslash;

		/**
		 * @param detector       the DefaultToolDetectionParticipant that matched the
		 *                       name of the tool on a given command-line
		 * @param how            describes the method that was used to match
		 * @param matchBackslash whether the match is on file system paths with
		 *                       backslashes in the compiler argument or to match an
		 *                       paths with forward slashes
		 */
		public DetectorWithMethod(IToolDetectionParticipant detector, DetectionMethod how, boolean matchBackslash) {
			if (detector == null)
				throw new NullPointerException("detector"); //$NON-NLS-1$
			if (how == null)
				throw new NullPointerException("how"); //$NON-NLS-1$
			this.detector = detector;
			this.how = how;
			this.matchBackslash = matchBackslash;
		}

		/**
		 * Gets the DefaultToolDetectionParticipant that matched the name of the tool on
		 * a given command-line.
		 *
		 * @return the detector, never {@code null}
		 */
		public IToolDetectionParticipant getToolDetectionParticipant() {
			return detector;
		}

		/**
		 * Gets the method that was used to match.
		 *
		 * @return the detection method, never {@code null}
		 */
		public DetectionMethod getHow() {
			return how;
		}

		/**
		 * @return the matchBackslash
		 */
		public boolean isMatchBackslash() {
			return matchBackslash;
		}

	}

	// has public scope for unittest purposes
	public static class ParserDetectionResult {

		private final DetectorWithMethod detectorWMethod;
		private final MatchResult commandLine;

		/**
		 * @param detectorWMethod the DefaultToolDetectionParticipant that matched the
		 *                        name of the tool on a given command-line
		 * @param commandLine     the de-composed command-line, after the matcher has
		 *                        matched the tool name
		 */
		public ParserDetectionResult(DetectorWithMethod detectorWMethod,
				DefaultToolDetectionParticipant.MatchResult commandLine) {
			this.detectorWMethod = detectorWMethod;
			this.commandLine = commandLine;
		}

		/**
		 * Gets the de-composed command-line.
		 */
		public MatchResult getCommandLine() {
			return commandLine;
		}

		/**
		 * Gets the remaining arguments of the command-line, after the matcher has
		 * matched the tool name (i.e. without the command).
		 */
		public String getReducedCommandLine() {
			return this.commandLine.getArguments();
		}

		/**
		 * Gets the DefaultToolDetectionParticipant that matched the name of the tool on
		 * a given command-line
		 *
		 * @return the detectorWMethod
		 */
		public DetectorWithMethod getDetectorWithMethod() {
			return detectorWMethod;
		}
	}

}
