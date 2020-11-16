/*******************************************************************************
 * Copyright (c) 2016-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.cdt.cmake.is.core.internal.ParserDetection;
import org.eclipse.cdt.cmake.is.core.internal.ParserDetection.DetectorWithMethod;
import org.eclipse.cdt.cmake.is.core.internal.ParserDetection.ParserDetectionResult;
import org.eclipse.cdt.cmake.is.core.internal.Plugin;
import org.eclipse.cdt.cmake.is.core.internal.StringUtil;
import org.eclipse.cdt.cmake.is.core.internal.builtins.CompilerBuiltinsDetector;
import org.eclipse.cdt.cmake.is.core.participant.DefaultToolDetectionParticipant;
import org.eclipse.cdt.cmake.is.core.participant.IRawIndexerInfo;
import org.eclipse.cdt.cmake.is.core.participant.IToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.participant.IToolCommandlineParser.IResult;
import org.eclipse.cdt.cmake.is.core.participant.IToolDetectionParticipant;
import org.eclipse.cdt.cmake.is.core.participant.builtins.IBuiltinsDetectionBehavior;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.osgi.framework.FrameworkUtil;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * Parses the file 'compile_commands.json' produced by cmake when option
 * {@code -DCMAKE_EXPORT_COMPILE_COMMANDS=ON} is given and generates information
 * about preprocessor symbols and include paths of the files being compiled for
 * the CDT indexer.
 *
 * @author Martin Weber
 */
public class CompileCommandsJsonParser {
	private static final boolean DEBUG_TIME = Boolean
			.parseBoolean(Platform.getDebugOption(Plugin.PLUGIN_ID + "/debug/performance")); //$NON-NLS-1$
	private static final boolean DEBUG_ENTRIES = Boolean
			.parseBoolean(Platform.getDebugOption(Plugin.PLUGIN_ID + "/debug/detected.entries")); //$NON-NLS-1$
	/**
	 * property to store the file modification time of the "compile_commands.json"
	 * file
	 */
	private static final QualifiedName TIMESTAMP_COMPILE_COMMANDS_PROPERTY = new QualifiedName(null,
			"timestamp:compile_commands.json"); //$NON-NLS-1$

	private static final String WORKBENCH_WILL_NOT_KNOW_ALL_MSG = Messages.CompileCommandsJsonParser_MSG_WORKBENCH_WILL_NOT_KNOW;

	private static final String MARKER_ID = Plugin.PLUGIN_ID + ".CompileCommandsJsonParserMarker"; //$NON-NLS-1$

	private ParseRequest parseRequest;

	private final IParserPreferencesAccess prefsAccess;

	/**
	 * last known working tool detector and its tool option parsers or {@code null},
	 * if unknown (to speed up parsing)
	 */
	private DetectorWithMethod lastDetector;

	/**
	 * markers for commands without a cmdline parser for the tool. just for error
	 * reporting, no technical effect
	 */
	private Set<String> knownUnsupportedTools = new HashSet<>();

	/**
	 * the raw scanner info results for each source file (source file name ->
	 * IResult)
	 */
	private Map<String, IRawIndexerInfo> fileResults;

	/**
	 * minimized set of CompilerBuiltinsDetector to run. (detector key ->
	 * CompilerBuiltinsDetector). Key is created by
	 * {@link #makeBuiltinsDetectorKey(String, List, String)}.
	 */
	private Map<String, CompilerBuiltinsDetector> builtinDetectorsToRun;

	/**
	 * the built-ins detectors for each source file (source file name -> detector
	 * key)
	 */
	private Map<String, String> fileToBuiltinDetectorLinks;

	/**
	 * Creates a new object that will try to parse the {@code compile_commands.json}
	 * file in the build directory specified in the {@code ParseRequest#getBuildConfiguration()}.
	 *
	 * @param parseRequest  the parser configuration
	 */
	public CompileCommandsJsonParser(ParseRequest parseRequest) {
		this.parseRequest = Objects.requireNonNull(parseRequest, "parseRequest"); //$NON-NLS-1$
		prefsAccess = EclipseContextFactory.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext())
				.get(IParserPreferencesAccess.class);
	}

	/**
	 * Parses the content of the 'compile_commands.json' file corresponding to the
	 * specified configuration, if time-stamps differ.
	 * @param monitor  the job's progress monitor
	 *
	 * @return {@code true} if the json file did change since the last invocation of
	 *         this method (new setting entries were discovered), otherwise
	 *         {@code false}
	 * @throws CoreException
	 */
	private boolean processJsonFile(IProgressMonitor monitor) throws CoreException {
		final IFile jsonFile = parseRequest.getFile();
		final IProject project = jsonFile.getProject();

		project.deleteMarkers(MARKER_ID, false, IResource.DEPTH_INFINITE);

		long tsJsonModified = jsonFile.getModificationStamp();
		if (tsJsonModified == IResource.NULL_STAMP) {
			// no json file was produced in the build
			final String msg = String.format(Messages.CompileCommandsJsonParser_errmsg_file_not_found,
					jsonFile.getLocationURI(), WORKBENCH_WILL_NOT_KNOW_ALL_MSG);
			createMarker(project, msg);
			return false;
		}
		// file exists on disk...
		IContainer buildRootFolder = jsonFile.getParent();
		Long sessionLastModified = (Long) buildRootFolder.getSessionProperty(TIMESTAMP_COMPILE_COMMANDS_PROPERTY);
		if (sessionLastModified == null || sessionLastModified.longValue() < tsJsonModified) {
			// must parse json file...
			monitor.setTaskName(Messages.CompileCommandsJsonParser_msg_processing);

			try (Reader in = new InputStreamReader(jsonFile.getContents(true))) {
				// parse file...
				Gson gson = new Gson();
				CommandEntry[] sourceFileInfos = gson.fromJson(in, CommandEntry[].class);
				for (CommandEntry sourceFileInfo : sourceFileInfos) {
					processCommandEntry(sourceFileInfo, jsonFile);
				}
			} catch (JsonSyntaxException | JsonIOException ex) {
				// file format error
				final String msg = String.format(Messages.CompileCommandsJsonParser_errmsg_not_json,
						jsonFile.getLocationURI(), WORKBENCH_WILL_NOT_KNOW_ALL_MSG);
				createMarker(jsonFile, msg);
				return false;
			} catch (IOException ex) {
				final String msg = String.format(Messages.CompileCommandsJsonParser_errmsg_read_error,
						jsonFile.getLocationURI(), WORKBENCH_WILL_NOT_KNOW_ALL_MSG);
				createMarker(jsonFile, msg);
				return false;
			}

			detectBuiltins(monitor);
			// store time-stamp
			buildRootFolder.setSessionProperty(TIMESTAMP_COMPILE_COMMANDS_PROPERTY, tsJsonModified);
			return true;
		}
		return false;
	}

	/**
	 * Processes an entry from a {@code compile_commands.json} file and stores a
	 * {@link ICLanguageSettingEntry} for the file given the specified map.
	 *
	 * @param sourceFileInfo parsed command entry of a compile_commands.json file
	 * @param jsonFile       the JSON file being parsed (for marker creation only)
	 *
	 * @throws CoreException if marker creation failed
	 */
	private void processCommandEntry(CommandEntry sourceFileInfo, IFile jsonFile) throws CoreException {
		// NOTE that this is the absolute file system path of the source file in
		// CMake-notation (directory separator are forward slashes, even on windows)
		final String file = sourceFileInfo.getFile();
		final String cmdLine = sourceFileInfo.getCommand();
		if (file != null && !file.isEmpty() && cmdLine != null && !cmdLine.isEmpty()) {
			ParserDetection.ParserDetectionResult pdr = fastDetermineDetector(cmdLine);
			if (pdr != null) {
				// found a matching command-line parser
				final IToolCommandlineParser parser = pdr.getDetectorWithMethod().getToolDetectionParticipant()
						.getParser();
				// cwdStr is the absolute working directory of the compiler in
				// CMake-notation (fileSep are forward slashes)
				final String cwdStr = sourceFileInfo.getDirectory();
				IPath cwd = cwdStr != null ? Path.fromOSString(cwdStr) : new Path(""); //$NON-NLS-1$
				IResult result = parser.processArgs(cwd, StringUtil.trimLeadingWS(pdr.getReducedCommandLine()));
				// remember result together with file name
				rememberFileResult(file, result);

				final Optional<IBuiltinsDetectionBehavior> builtinDetection = parser.getIBuiltinsDetectionBehavior();
				if (builtinDetection.isPresent()) {
					rememberBuiltinsDetection(file, builtinDetection.get(), pdr.getCommandLine().getCommand(),
							result.getBuiltinDetectionArgs());
				}
			} else {
				// no matching parser found

				// complain only once if no cmdline parser for the tool is known (fortran,
				// assembler, etc)
				int idx = cmdLine.indexOf(' ');
				String unkownMarker = (idx != -1 ? cmdLine.substring(0, idx) : cmdLine)
						+ FilenameUtils.getExtension(file);
				if (knownUnsupportedTools.contains(unkownMarker)) {
					return;
				}
				knownUnsupportedTools.add(unkownMarker);

				final String msg = String.format(Messages.CompileCommandsJsonParser_errmsg_no_parser_for_commandline,
						cmdLine, WORKBENCH_WILL_NOT_KNOW_ALL_MSG);
				createMarker(jsonFile, msg);
			}
			return;
		}
		// unrecognized entry, skipping
		final String msg = String.format(Messages.CompileCommandsJsonParser_errmsg_unexpected_json, jsonFile,
				WORKBENCH_WILL_NOT_KNOW_ALL_MSG);
		createMarker(jsonFile, msg);
	}

	/**
	 * @param monitor
	 * @throws CoreException
	 */
	private void detectBuiltins(IProgressMonitor monitor) throws CoreException {
		if (builtinDetectorsToRun.isEmpty())
			return;
		monitor.setTaskName(Messages.CompileCommandsJsonParser_msg_detecting_builtins);

		final IFile jsonFile = parseRequest.getFile();
		// cmake puts file 'compile_commands.json' directly below the build directory..
		java.nio.file.Path buildDir = java.nio.file.Path.of(jsonFile.getParent().getLocationURI());

		// run each built-in detector and collect the results..
		Map<String, IRawIndexerInfo> builtinDetectorsResults = new HashMap<>();
		for (Entry<String, CompilerBuiltinsDetector> entry : builtinDetectorsToRun.entrySet()) {
			IRawIndexerInfo result = entry.getValue().detectBuiltins(jsonFile.getProject(), buildDir,
					parseRequest.getLauncher(), parseRequest.getConsole(), monitor);
			// store detector key with result
			builtinDetectorsResults.put(entry.getKey(), result);
		}
		// all built-in detectors have been run at this point, reduce memory footprint
		builtinDetectorsToRun.clear();

		// most of the time we get different String objects for different source files
		// that have the same sequence of characters. So reduce the number of String
		// objects by pooling them..
		Map<String, String> strPool = new HashMap<>();
		Function<String, String> stringPooler = v -> {
			String old = strPool.putIfAbsent(v, v);
			return old == null ? v : old;
		};

		// merge built-in results with source file results
		for (Entry<String, String> link : fileToBuiltinDetectorLinks.entrySet()) {
			String sourceFileName = link.getKey();
			IRawIndexerInfo fileResult = fileResults.get(sourceFileName);
			IRawIndexerInfo builtinDetectorsResult = builtinDetectorsResults.get(link.getValue());
			mergeResultsForFile(stringPooler, sourceFileName, fileResult, builtinDetectorsResult);
		}
	}

	/**
	 * Merges preprocessor symbols and macros for a source file with compiler
	 * built-in preprocessor symbols and macros and passes the to the
	 * {@code IIndexerInfoConsumer} that was specified in the constructor.
	 *
	 * @param fileResult             source file preprocessor symbols and macros
	 * @param builtinDetectorsResult compiler built-in preprocessor symbols and
	 *                               macros
	 * @param stringPooler           a function that returns a String from a pool
	 *                               for a given String
	 * @param sourceFileName         the name of the source file
	 */
	private void mergeResultsForFile(Function<String, String> stringPooler, String sourceFileName,
			IRawIndexerInfo fileResult, IRawIndexerInfo builtinDetectorsResult) {
		/*
		 * Handling of -U and -D is ambivalent here. - The GCC man page states: '-U name
		 * Cancel any previous definition of name, either built in or provided with a -D
		 * option.' - The POSIX c99 man page states: '-U name Remove any initial
		 * definition of name.' We implement handling of defines according to GCC here.
		 */
		Map<String, String> builtinDefines = new HashMap<>(builtinDetectorsResult.getDefines());
		for (String name : fileResult.getUndefines()) {
			String value;
			if ((value = builtinDefines.remove(name)) != null) {
				if (DEBUG_ENTRIES)
					System.out.printf("      Removed define: %s=%s%n", name, value); //$NON-NLS-1$
			}
		}

		Map<String, String> effectiveDefines = Stream
				.concat(builtinDefines.entrySet().stream(), fileResult.getDefines().entrySet().stream())
				.collect(Collectors.toMap(stringPooler.compose(Map.Entry::getKey),
						stringPooler.compose(Map.Entry::getValue)));
		List<String> includePaths = Stream
				.concat(fileResult.getIncludePaths().stream(), builtinDetectorsResult.getIncludePaths().stream())
				.map(stringPooler).collect(Collectors.toList());
		List<String> systemIncludePaths = Stream
				.concat(fileResult.getSystemIncludePaths().stream(),
						builtinDetectorsResult.getSystemIncludePaths().stream())
				.map(stringPooler).collect(Collectors.toList());
		List<String> macroFiles = fileResult.getMacroFiles();
		List<String> includeFiles = fileResult.getIncludeFiles();

		// feed the paths and defines with the file name to the indexer..
		parseRequest.getIndexerInfoConsumer().acceptSourceFileInfo(sourceFileName, systemIncludePaths, effectiveDefines,
				includePaths, macroFiles, includeFiles);
	}

	private static void createMarker(IResource rc, String message) throws CoreException {
		IMarker marker = rc.createMarker(MARKER_ID);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.LOCATION, CompileCommandsJsonParser.class.getName());
	}

	/**
	 * Determines the parser detector that can parse the specified command-line.<br>
	 * Tries to be fast: That is, it tries the last known working detector first and
	 * will perform expensive detection required under windows only if needed.
	 *
	 * @param line the command line to process
	 *
	 * @return {@code null} if none of the detectors matched the tool name in the
	 *         specified command-line string. Otherwise, if the tool name matches, a
	 *         {@code ParserDetectionResult} holding the de-composed command-line is
	 *         returned.
	 */
	private ParserDetectionResult fastDetermineDetector(String line) {
		final IParserPreferences prefs = prefsAccess.getWorkspacePreferences();
		// try last known matching detector first...
		if (lastDetector != null) {
			Optional<DefaultToolDetectionParticipant.MatchResult> matchResult = Optional.empty();
			final IToolDetectionParticipant detector = lastDetector.getToolDetectionParticipant();
			switch (lastDetector.getHow()) {
			case BASENAME:
				matchResult = detector.basenameMatches(line, lastDetector.isMatchBackslash());
				break;
			case WITH_EXTENSION:
				matchResult = detector.basenameWithExtensionMatches(line, lastDetector.isMatchBackslash());
				break;
			case WITH_VERSION:
				if (prefs.getTryVersionSuffix()) {
					matchResult = detector.basenameWithVersionMatches(line, lastDetector.isMatchBackslash(),
							prefs.getVersionSuffixPattern());
				}
				break;
			case WITH_VERSION_EXTENSION:
				if (prefs.getTryVersionSuffix()) {
					matchResult = detector.basenameWithVersionAndExtensionMatches(line, lastDetector.isMatchBackslash(),
							prefs.getVersionSuffixPattern());
				}
				break;
			default:
				break;
			}
			if (matchResult.isPresent()) {
				return new ParserDetection.ParserDetectionResult(lastDetector, matchResult.get());
			} else {
				lastDetector = null; // invalidate last working detector
			}
		}

		// no working detector found, determine a new one...
		String versionPattern = prefs.getTryVersionSuffix() ? prefs.getVersionSuffixPattern() : null;
		ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector(line, versionPattern,
				File.separatorChar == '\\');
		if (result != null) {
			// cache last working detector
			lastDetector = result.getDetectorWithMethod();
		}
		return result;
	}

	/**
	 * Parses the {@code compile_commands.json} file in the build directory of the
	 * build configuration if necessary and generates indexer information. If the JSON file did not change since the last invocation
	 * of this method on the same build configuration, parsing of the file will be skipped; that is:
	 * Method {@link IIndexerInfoConsumer#accept()} is not invoked.
	 * @param monitor  the job's progress monitor
	 *
	 * @return {@code true} if the {@code compile_commands.json} file did change
	 *         since the last invocation of this method, otherwise {@code false}. If
	 *         {@code true}, new scanner information was detected and the CDT
	 *         indexer should be notified.
	 * @throws CoreException
	 */
	public boolean parse(IProgressMonitor monitor) throws CoreException {
		long start = 0;
		fileResults = new HashMap<>();
		builtinDetectorsToRun = new HashMap<>();
		fileToBuiltinDetectorLinks = new HashMap<>();

		try {
			if (DEBUG_TIME) {
				System.out.printf("Parsing file '%s' ...%n", //$NON-NLS-1$
						parseRequest.getFile().getLocationURI().getPath());
				start = System.currentTimeMillis();
			}
			return processJsonFile(monitor);
		} finally {
			parseRequest.getIndexerInfoConsumer().shutdown();
			if (DEBUG_TIME) {
				long end = System.currentTimeMillis();
				System.out.printf("Parsed file '%s' in %dms%n", //$NON-NLS-1$
						parseRequest.getFile().getLocationURI().getPath(), end - start);
			}
			// clean up
			builtinDetectorsToRun = null;
			fileResults = null;
			fileToBuiltinDetectorLinks = null;
		}
	}

	/**
	 * Creates a Map-key suitable to minimize the set of CompilerBuiltinsDetector to
	 * run.
	 *
	 * @param compilerCommand      the command name of the compiler
	 * @param builtinDetectionArgs the compiler arguments from the command-line that
	 *                             affect built-in detection. For the GNU compilers,
	 *                             these are options like {@code --sysroot} and
	 *                             options that specify the language's standard
	 *                             ({@code -std=c++17}.
	 * @param sourceFileExtension  the extension of the source file name
	 * @return a Map-key suitable to minimize the set of CompilerBuiltinsDetector to
	 *         run
	 */
	@SuppressWarnings("nls")
	private static String makeBuiltinsDetectorKey(String compilerCommand, List<String> builtinDetectionArgs,
			String sourceFileExtension) {
		switch (sourceFileExtension) {
		case "C":
		case "cc":
		case "cpp":
		case "CPP":
		case "cp":
		case "cxx":
		case "c++":
			// make sure we run built-ins detection only once for C++ files..
			sourceFileExtension = "cpp";
			break;
		}
		return compilerCommand + "#" + sourceFileExtension + "#"
		// make sure we run the compiler for built-ins detection for each set of args
		// that affect built-ins detection..
				+ String.join(" ", builtinDetectionArgs);
	}

	/**
	 * @param file
	 * @param result
	 */
	private void rememberFileResult(String sourceFileName, IRawIndexerInfo result) {
		fileResults.put(sourceFileName, result);
	}

	/**
	 * @param sourceFileName       the name of the source file
	 * @param compilerCommand      the command name of the compiler
	 * @param builtinDetectionArgs the compiler arguments from the command-line that
	 *                             affect built-in detection. For the GNU compilers,
	 *                             these are options like {@code --sysroot} and
	 *                             options that specify the language's standard
	 *                             ({@code -std=c++17}.
	 */
	private void rememberBuiltinsDetection(String sourceFileName, IBuiltinsDetectionBehavior builtinsDetectionBehavior,
			String compilerCommand, List<String> builtinDetectionArgs) {
		String extension = FilenameUtils.getExtension(sourceFileName);
		String key = makeBuiltinsDetectorKey(compilerCommand, builtinDetectionArgs, extension);
		if (!builtinDetectorsToRun.containsKey(key)) {
			CompilerBuiltinsDetector detector = new CompilerBuiltinsDetector(builtinsDetectionBehavior, compilerCommand,
					builtinDetectionArgs, extension);
			builtinDetectorsToRun.put(key, detector);
		}
		// remember the built-ins detector for the source file
		fileToBuiltinDetectorLinks.put(sourceFileName, key);
	}

}
