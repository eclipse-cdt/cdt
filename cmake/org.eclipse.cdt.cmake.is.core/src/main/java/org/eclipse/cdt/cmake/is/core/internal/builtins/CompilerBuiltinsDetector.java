/*******************************************************************************
 * Copyright (c) 2018-2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal.builtins;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.cdt.cmake.is.core.builtins.IBuiltinsDetectionBehavior;
import org.eclipse.cdt.cmake.is.core.builtins.IBuiltinsOutputProcessor;
import org.eclipse.cdt.cmake.is.core.builtins.OutputSniffer;
import org.eclipse.cdt.cmake.is.core.internal.Plugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

/**
 * Detects preprocessor macros and include paths that are built-in to a
 * compiler.
 *
 * @author Martin Weber
 */
public class CompilerBuiltinsDetector {
	/**
	 * console ID for extension point org.eclipse.cdt.core.CBuildConsole (see
	 * plugin.xml)
	 */
	private static final String CONSOLE_ID = Plugin.PLUGIN_ID + ".detectorConsole";
	/** error marker ID */
	private static final String MARKER_ID = Plugin.PLUGIN_ID + ".CompilerBuiltinsDetectorMarker";

	private ICConfigurationDescription cfgDescription;

	/** environment variables, lazily instantiated */
	private String[] envp;

	private final String languageId;

	private final String command;

	private List<String> builtinsDetectionArgs;

	private final IBuiltinsDetectionBehavior builtinsDetectionBehavior;

	/**
	 * @param cfgDescription            configuration description.
	 * @param languageId                language id
	 * @param builtinsDetectionBehavior how compiler built-ins are to be detected
	 * @param command                   the compiler command (argument # 0)
	 * @param builtinsDetectionArgs     the compiler arguments from the command-line
	 *                                  that affect built-in detection. For the GNU
	 *                                  compilers, these are options like
	 *                                  {@code --sysroot} and options that specify
	 *                                  the language's standard ({@code -std=c++17}.
	 */
	public CompilerBuiltinsDetector(ICConfigurationDescription cfgDescription, String languageId,
			IBuiltinsDetectionBehavior builtinsDetectionBehavior, String command, List<String> builtinsDetectionArgs) {
		this.languageId = Objects.requireNonNull(languageId, "languageId");
		this.builtinsDetectionBehavior = Objects.requireNonNull(builtinsDetectionBehavior, "builtinsDetectionBehavior");
		this.command = Objects.requireNonNull(command, "command");
		this.builtinsDetectionArgs = Objects.requireNonNull(builtinsDetectionArgs, "builtinsDetectionArgs");
		this.cfgDescription = Objects.requireNonNull(cfgDescription);
	}

	/**
	 * Gets the language ID of this detector.
	 */
	public String getLanguageId() {
		return languageId;
	}

	/**
	 * Run built-in detection builtinsDetectionArgs.
	 *
	 * @param withConsole whether to show a console for the builtinsDetectionArgs
	 *                    output
	 *
	 * @throws CoreException
	 */
	public List<ICLanguageSettingEntry> run(boolean withConsole) throws CoreException {
		ProcessingContext entries = new ProcessingContext();

		final List<String> argList = getCompilerArguments(languageId);
		argList.addAll(builtinsDetectionArgs);

		IConsole console = null;
		if (withConsole) {
			console = startOutputConsole();
		}

		IProject project = cfgDescription.getProjectDescription().getProject();
		// get the launcher that runs in docker container, if any
		ICommandLauncher launcher = ManagedBuildManager.getConfigurationForDescription(cfgDescription)
				.getEditableBuilder().getCommandLauncher();
		launcher.setProject(project);
		launcher.showCommand(console != null);
		final NullProgressMonitor monitor = new NullProgressMonitor();
		final IPath builderCWD = cfgDescription.getBuildSetting().getBuilderCWD();
		IPath buildRoot = ResourcesPlugin.getWorkspace().getRoot().getFolder(builderCWD).getLocation();
		final Process proc = launcher.execute(new Path(command), argList.toArray(new String[argList.size()]), getEnvp(),
				buildRoot, monitor);
		if (proc != null) {
			try {
				// Close the input of the process since we will never write to it
				proc.getOutputStream().close();
			} catch (IOException e) {
			}
			// NOTE: we need 2 of these, since the output streams are not synchronized,
			// causing loss of
			// the internal processor state
			final IBuiltinsOutputProcessor bopOut = builtinsDetectionBehavior.createCompilerOutputProcessor();
			final IBuiltinsOutputProcessor bopErr = builtinsDetectionBehavior.createCompilerOutputProcessor();
			int state = launcher.waitAndRead(
					new OutputSniffer(bopOut, console == null ? null : console.getOutputStream(), entries),
					new OutputSniffer(bopErr, console == null ? null : console.getErrorStream(), entries), monitor);
			if (state != ICommandLauncher.COMMAND_CANCELED) {
				// check exit status
				final int exitValue = proc.exitValue();
				if (exitValue != 0 && !builtinsDetectionBehavior.suppressErrormessage()) {
					// compiler had errors...
					String errMsg = String.format("%1$s exited with status %2$d.", command, exitValue);
					createMarker(errMsg);
				}
			}
		} else {
			// process start failed
			createMarker(launcher.getErrorMessage());
		}
		return entries.getSettingEntries();
	}

	/**
	 * Gets the compiler-arguments corresponding to the specified language ID and
	 * the builtinDetection.
	 */
	private List<String> getCompilerArguments(String languageId) {
		List<String> args = new ArrayList<>();
		args.addAll(builtinsDetectionBehavior.getBuiltinsOutputEnablingArgs());
		String inputFile = getInputFile(languageId);
		if (inputFile != null) {
			args.add(inputFile);
		}
		return args;
	}

	/**
	 * Get array of environment variables in format "var=value".
	 */
	private String[] getEnvp() {
		if (envp == null) {
			// On POSIX (Linux, UNIX) systems reset language variables to default
			// (English)
			// with UTF-8 encoding since GNU compilers can handle only UTF-8
			// characters.
			// Include paths with locale characters will be handled properly
			// regardless
			// of the language as long as the encoding is set to UTF-8.
			// English language is set for parser because it relies on English
			// messages
			// in the output of the 'gcc -v' builtinsDetectionArgs.

			List<String> env = new ArrayList<>(Arrays.asList(getEnvp(cfgDescription)));
			for (Iterator<String> iterator = env.iterator(); iterator.hasNext();) {
				String var = iterator.next();
				if (var.startsWith("LANGUAGE" + '=') || var.startsWith("LC_ALL" + '=')) {
					iterator.remove();
				}
			}
			env.add("LANGUAGE" + "=en"); // override for GNU gettext //$NON-NLS-1$
			env.add("LC_ALL" + "=C.UTF-8"); // for other parts of the //$NON-NLS-1$
											// system libraries
			envp = env.toArray(new String[env.size()]);
		}
		return envp;
	}

	/**
	 * Get environment variables from configuration as array of "var=value" suitable
	 * for using as "envp" with Runtime.exec(String[] cmdarray, String[] envp, File
	 * dir)
	 *
	 * @param cfgDescription configuration description.
	 * @return String array of environment variables in format "var=value". Does not
	 *         return {@code null}.
	 */
	private static String[] getEnvp(ICConfigurationDescription cfgDescription) {
		IEnvironmentVariableManager mngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IEnvironmentVariable[] vars = mngr.getVariables(cfgDescription, true);
		// Convert into envp strings
		Set<String> strings = new HashSet<>(vars.length);
		for (IEnvironmentVariable var : vars) {
			strings.add(var.getName() + '=' + var.getValue());
		}
		// On POSIX (Linux, UNIX) systems reset language variables to default
		// (English)
		// with UTF-8 encoding since GNU compilers can handle only UTF-8 characters.
		// Include paths with locale characters will be handled properly regardless
		// of the language as long as the encoding is set to UTF-8.
		// English language is set for parser because it relies on English messages
		// in the output of the 'gcc -v' builtinsDetectionArgs.
		strings.add("LANGUAGE" + "=en"); // override for GNU gettext
		strings.add("LC_ALL" + "=C.UTF-8"); // for other parts of the system
											// libraries

		return strings.toArray(new String[strings.size()]);
	}

	/**
	 * Get path to source file which is the input for the compiler.
	 *
	 * @param languageId the language ID.
	 * @return full path to the source file.
	 */
	private String getInputFile(String languageId) {
		String specExt = builtinsDetectionBehavior.getInputFileExtension(languageId);
		if (specExt != null) {
			String specFileName = "detect_compiler_builtins" + '.' + specExt;

			final IPath builderCWD = cfgDescription.getBuildSetting().getBuilderCWD();
			IPath fileLocation = ResourcesPlugin.getWorkspace().getRoot().getFolder(builderCWD).getLocation()
					.append(specFileName);

			File specFile = new java.io.File(fileLocation.toOSString());
			if (!specFile.exists()) {
				try {
					// In the typical case it is sufficient to have an empty file.
					specFile.getParentFile().mkdirs(); // no build ran yet, must create dirs
					specFile.createNewFile();
				} catch (IOException e) {
					Plugin.getDefault().getLog().log(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, "getInputFile()", e));
				}
			}

			return fileLocation.toString();
		}
		return null;
	}

	private void createMarker(String message) throws CoreException {
		IMarker marker = cfgDescription.getProjectDescription().getProject().createMarker(MARKER_ID);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
		marker.setAttribute(IMarker.MESSAGE, message);
	}

	/**
	 * Creates and starts the provider console.
	 *
	 * @return CDT console or <code>null</code>
	 *
	 * @throws CoreException
	 */
	private IConsole startOutputConsole() throws CoreException {
		IConsole console = null;

		ILanguage ld = LanguageManager.getInstance().getLanguage(languageId);
		if (ld != null) {
			String consoleId = CONSOLE_ID + '.' + languageId;
			console = CCorePlugin.getDefault().getConsole(CONSOLE_ID, consoleId, null, null);
			final IProject project = cfgDescription.getProjectDescription().getProject();
			console.start(project);
			try {
				final ConsoleOutputStream cis = console.getInfoStream();
				cis.write(SimpleDateFormat.getTimeInstance().format(new Date()).getBytes());
				cis.write(" Detecting compiler built-ins: ".getBytes());
				cis.write(project.getName().getBytes());
				cis.write("::".getBytes());
				cis.write(cfgDescription.getConfiguration().getName().getBytes());
				cis.write(" for ".getBytes());
				cis.write(ld.getName().getBytes());
				cis.write("\n".getBytes());
			} catch (IOException ignore) {
			}
		}

		return console;
	}

}
