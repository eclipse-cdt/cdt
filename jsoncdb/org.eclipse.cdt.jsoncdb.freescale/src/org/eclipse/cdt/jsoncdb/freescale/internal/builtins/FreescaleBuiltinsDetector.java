/*******************************************************************************
 * Copyright (c) 2018-2020 Martin Weber,
 *                    2023 Thomas Kucharcyzk.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.jsoncdb.freescale.internal.builtins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.jsoncdb.core.IParserPreferences;
import org.eclipse.cdt.jsoncdb.core.IParserPreferencesAccess;
import org.eclipse.cdt.jsoncdb.core.participant.IRawSourceFileInfo;
import org.eclipse.cdt.jsoncdb.core.participant.builtins.IBuiltinsOutputProcessor;
import org.eclipse.cdt.jsoncdb.core.participant.builtins.OutputSniffer;
import org.eclipse.cdt.jsoncdb.freescale.internal.Plugin;
import org.eclipse.cdt.jsoncdb.freescale.participant.builtins.IBuiltinsDetectionBehaviorFilebased;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.osgi.framework.FrameworkUtil;

/**
 * Detects Freescales preprocessor macros built-ins
 *
 * @author Thomas Kucharczyk
 */
public class FreescaleBuiltinsDetector {
	/** error marker ID */
	public static final String MARKER_ID = Plugin.PLUGIN_ID + "FreescaleBuiltinsDetectorMarker"; //$NON-NLS-1$

	private final String sourceFileExtension;
	private final String command;
	private final List<String> builtinsDetectionArgs;
	private final IBuiltinsDetectionBehaviorFilebased builtinsDetectionBehavior;

	private IProject project;
	private java.nio.file.Path buildDirectory;

	/**
	 * @param builtinsDetectionBehavior how compiler built-ins are to be detected
	 * @param command                   the compiler command (argument # 0)
	 * @param builtinsDetectionArgs     the compiler arguments from the command-line
	 *                                  that affect built-in detection. For the GNU
	 *                                  compilers, these are options like
	 *                                  {@code --sysroot} and options that specify
	 *                                  the language's standard (e.g.
	 *                                  {@code -std=c++17}).
	 * @param sourceFileExtension       the extension of the source file name
	 */
	public FreescaleBuiltinsDetector(IBuiltinsDetectionBehaviorFilebased builtinsDetectionBehavior, String command,
			List<String> builtinsDetectionArgs, String sourceFileExtension) {
		this.sourceFileExtension = Objects.requireNonNull(sourceFileExtension, "sourceFileExtension"); //$NON-NLS-1$
		this.builtinsDetectionBehavior = Objects.requireNonNull(builtinsDetectionBehavior, "builtinsDetectionBehavior"); //$NON-NLS-1$
		this.command = Objects.requireNonNull(command, "command"); //$NON-NLS-1$
		this.builtinsDetectionArgs = Objects.requireNonNull(builtinsDetectionArgs, "builtinsDetectionArgs"); //$NON-NLS-1$
	}

	/**
	 * Runs built-in detection.
	 *
	 * @param project            the project
	 * @param buildDirectory	the build root directory. This is the working directory of the compiler process. Temporary,
	 * 							but cacheable input files for the compiler are generated her also
	 * @param launcher           the launcher for the compiler process
	 * @param console            the console to print the compiler output to or
	 *                           <code>null</code> if no console output is requested.
	 * @throws CoreException
	 */
	public IRawSourceFileInfo detectBuiltins(IProject project, java.nio.file.Path buildDirectory,
			ICommandLauncher launcher, IConsole console, IProgressMonitor monitor) throws CoreException {
		this.project = Objects.requireNonNull(project, "project"); //$NON-NLS-1$
		this.buildDirectory = Objects.requireNonNull(buildDirectory, "buildDirectory"); //$NON-NLS-1$

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		RawSourceFileInfo result = new RawSourceFileInfo();

		final List<String> argList = getCompilerArguments();
		argList.addAll(builtinsDetectionArgs);

		console = startOutputConsole(console);

		launcher.setProject(project);
		launcher.showCommand(console != null);
		final Process proc = launcher.execute(new Path(command), argList.toArray(new String[argList.size()]), getEnvp(),
				new Path(this.buildDirectory.toString()), monitor);
		if (proc != null) {
			try {
				// Close the input of the process since we will never write to it
				proc.getInputStream().close();
			} catch (IOException e) {
			}
			// NOTE: we need 2 of these, since the output streams are not synchronized,
			// causing loss of the output processors' internal state
			final IBuiltinsOutputProcessor bopOut = builtinsDetectionBehavior.createCompilerOutputProcessor();
			final IBuiltinsOutputProcessor bopErr = builtinsDetectionBehavior.createCompilerOutputProcessor();
			long start = System.currentTimeMillis();
			int state = launcher.waitAndRead(
					new OutputSniffer(bopOut, console == null ? null : console.getOutputStream(), result),
					new OutputSniffer(bopErr, console == null ? null : console.getErrorStream(), result), monitor);

			// parse file output (Freescale compiler can't output to stdout and windows doesn't have a filename equivalent of it)
			final IBuiltinsOutputProcessor bopFile = builtinsDetectionBehavior.createCompilerOutputProcessor();
			try {
				final File file = builtinsDetectionBehavior.getFile();
				if (file.isFile()) {
					final BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));

					String line;
					while ((line = reader.readLine()) != null) {
						bopFile.processLine(line, result);
					}
					reader.close();
				}
			} catch (IOException e) {
				String errMsg = String.format(Messages.CompilerBuiltinsDetector_errmsg_file_parsing_failed,
						e.getMessage());
				createMarker(errMsg);
			}

			if (console != null) {
				final ConsoleOutputStream cis = console.getInfoStream();
				try {
					cis.write(String.format(Messages.CompilerBuiltinsDetector_msg_detection_finished,
							System.currentTimeMillis() - start).getBytes());
					cis.write("\n".getBytes()); //$NON-NLS-1$
				} catch (IOException ignore) {
				}
			}
			if (state != ICommandLauncher.COMMAND_CANCELED) {
				try {
					// check exit status
					final int exitValue = proc.exitValue();
					if (exitValue != 0 && !builtinsDetectionBehavior.suppressErrormessage()) {
						// compiler had errors...
						String errMsg = String.format(Messages.CompilerBuiltinsDetector_errmsg_command_failed, command,
								exitValue);
						createMarker(errMsg);
					}
				} catch (IllegalThreadStateException e) {
					// Bug 580045 - reused launcher race condition
					String warnMsg = String.format(Messages.CompilerBuiltinsDetector_msg_unexpectedly_still_running,
							command);

					if (console != null) {
						final ConsoleOutputStream cis = console.getInfoStream();
						try {
							cis.write(warnMsg.getBytes());
							cis.write("\n".getBytes()); //$NON-NLS-1$
						} catch (IOException ignore) {
						}
					}
					createMarker(warnMsg);
					Plugin.getDefault().getLog().log(Status.warning(warnMsg, e));
				}
			}
		} else {
			// process start failed
			createMarker(launcher.getErrorMessage());
		}
		return result;
	}

	/**
	 * Gets the compiler-arguments corresponding to the builtinDetection.
	 */
	private List<String> getCompilerArguments() {
		List<String> args = new ArrayList<>();
		args.addAll(builtinsDetectionBehavior.getBuiltinsOutputEnablingArgs());
		String inputFile = getInputFile();
		if (inputFile != null) {
			args.add(inputFile);
		}
		return args;
	}

	/**
	 * Get environment variables from configuration as array of "var=value".
	 *
	 * @return String array of environment variables in format "var=value". Does not
	 *         return {@code null}.
	 */
	private String[] getEnvp() {
		// On POSIX (Linux, UNIX) systems reset language variables to default (English)
		// with UTF-8 encoding since GNU compilers can handle only UTF-8 characters.
		// Include paths with locale characters will be handled properly regardless
		// of the language as long as the encoding is set to UTF-8.
		// English language is set for parser because it relies on English messages
		// in the output of the 'gcc -v'.
		String[] strings = {
				// override for GNU gettext
				"LANGUAGE" + "=en", //$NON-NLS-1$//$NON-NLS-2$
				// for other parts of the system libraries
				"LC_ALL" + "=C.UTF-8" }; //$NON-NLS-1$ //$NON-NLS-2$

		return strings;
	}

	/**
	 * Gets a path to the source file which is the input for the compiler. The file
	 * will be created with no content in the build directory.
	 *
	 * @return the full file system path of the source file
	 */
	private String getInputFile() {
		String specFileName = "detect_compiler_builtins" + '.' + sourceFileExtension; //$NON-NLS-1$
		java.nio.file.Path specFile = buildDirectory.resolve(specFileName);
		if (!Files.exists(specFile)) {
			try {
				// In the typical case it is sufficient to have an empty file.
				Files.createDirectories(specFile.getParent()); // no build ran yet, must create dirs
				Files.createFile(specFile);
			} catch (IOException e) {
				Plugin.getDefault().getLog().log(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, "getInputFile()", e)); //$NON-NLS-1$
			}
		}

		return specFile.toString();
	}

	private void createMarker(String message) throws CoreException {
		IMarker marker = project.createMarker(MARKER_ID);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
		marker.setAttribute(IMarker.MESSAGE, message);
	}

	/**
	 * Creates and starts the output console.
	 *
	 * @return CDT console or <code>null</code>
	 *
	 * @throws CoreException
	 */
	private IConsole startOutputConsole(IConsole console) throws CoreException {
		IParserPreferences prefs = EclipseContextFactory
				.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext())
				.get(IParserPreferencesAccess.class).getWorkspacePreferences();
		if (console != null && prefs.getAllocateConsole()) {
			console.start(project);
			try {
				final ConsoleOutputStream cis = console.getInfoStream();
				String msg;
				msg = String.format(Messages.CompilerBuiltinsDetector_msg_detection_start,
						SimpleDateFormat.getTimeInstance().format(new Date()), project.getName());
				cis.write(msg.getBytes());
				cis.write("\n".getBytes()); //$NON-NLS-1$
			} catch (IOException ignore) {
			}
			return console;
		}
		return null; // no console to allocate
	}

}
