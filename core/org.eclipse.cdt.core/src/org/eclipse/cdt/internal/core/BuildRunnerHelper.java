/*******************************************************************************
 * Copyright (c) 2012, 2016 Andrew Gvozdev and others.
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
 *     IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.RefreshScopeManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.errorparsers.FixitManager;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Helper class attempting to unify interactions with build console,
 * such as style of console output and handling of console output parsers.
 *
 * As of CDT 8.1, this class is experimental, internal and work in progress.
 * <strong>API is unstable and subject to change.</strong>
 */
public class BuildRunnerHelper implements Closeable {
	private static final String PROGRESS_MONITOR_QUALIFIER = CCorePlugin.PLUGIN_ID + ".progressMonitor"; //$NON-NLS-1$
	private static final int PROGRESS_MONITOR_SCALE = 100;
	private static final int TICKS_STREAM_PROGRESS_MONITOR = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_EXECUTE_PROGRAM = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_PARSE_OUTPUT = 1 * PROGRESS_MONITOR_SCALE;

	private IProject project;

	private IConsole console = null;
	private ErrorParserManager errorParserManager = null;
	private StreamProgressMonitor streamProgressMonitor = null;
	private OutputStream stdout = null;
	private OutputStream stderr = null;
	private OutputStream consoleOut = null;
	private OutputStream consoleInfo = null;

	private long startTime = 0;
	private long endTime = 0;

	private QualifiedName progressPropertyName = null;

	private ICommandLauncher launcher;
	private IPath buildCommand;
	private String[] args;
	private URI workingDirectoryURI;
	String[] envp;

	private boolean isStreamsOpen = false;
	boolean isCancelled = false;

	/**
	 * Constructor.
	 */
	public BuildRunnerHelper(IProject project) {
		this.project = project;
	}

	/**
	 * Set parameters for the launch.
	 * @param envp - String[] array of environment variables in format "var=value" suitable for using
	 *    as "envp" with Runtime.exec(String[] cmdarray, String[] envp, File dir)
	 */
	public void setLaunchParameters(ICommandLauncher launcher, IPath buildCommand, String[] args,
			URI workingDirectoryURI, String[] envp) {
		this.launcher = launcher;
		launcher.setProject(project);
		// Print the command for visual interaction.
		launcher.showCommand(true);

		this.buildCommand = buildCommand;
		this.args = args;
		this.workingDirectoryURI = workingDirectoryURI;
		this.envp = envp;
	}

	/**
	 * Open and set up streams for use by {@link BuildRunnerHelper}.
	 * This must be followed by {@link #close()} to close the streams. Use try...finally for that.
	 *
	 * @param epm - ErrorParserManger for error parsing and coloring errors on the console
	 * @param buildOutputParsers - list of console output parsers or {@code null}.
	 * @param con - the console.
	 * @param monitor - progress monitor in the initial state where {@link IProgressMonitor#beginTask(String, int)}
	 *    has not been called yet.
	 * @throws CoreException
	 */
	public void prepareStreams(ErrorParserManager epm, List<IConsoleParser> buildOutputParsers, IConsole con,
			IProgressMonitor monitor) throws CoreException {
		errorParserManager = epm;
		console = con;

		// Visualize the flow of the streams:
		//
		//                    console <- EPM
		//                                ^
		//                         IConsoleParsers (includes EPM + other parsers)
		//                                ^
		//    null <- StreamMomitor <= Sniffer <= Process (!!! the flow starts here!)
		//

		isStreamsOpen = true;

		consoleOut = console.getOutputStream();
		// stdout/stderr get to the console through ErrorParserManager
		errorParserManager.setOutputStream(consoleOut);

		List<IConsoleParser> parsers = new ArrayList<>();
		// Using ErrorParserManager as console parser helps to avoid intermixing buffered streams
		// as ConsoleOutputSniffer waits for EOL to send a line to console parsers
		// separately for each stream.
		parsers.add(errorParserManager);
		if (buildOutputParsers != null) {
			parsers.addAll(buildOutputParsers);
		}

		Integer lastWork = null;
		if (buildCommand != null && project != null) {
			progressPropertyName = getProgressPropertyName(buildCommand, args);
			lastWork = (Integer) project.getSessionProperty(progressPropertyName);
		}
		if (lastWork == null) {
			lastWork = TICKS_STREAM_PROGRESS_MONITOR;
		}

		streamProgressMonitor = new StreamProgressMonitor(monitor, null, lastWork.intValue());
		ConsoleOutputSniffer sniffer = new ConsoleOutputSniffer(streamProgressMonitor, streamProgressMonitor,
				parsers.toArray(new IConsoleParser[parsers.size()]));
		stdout = sniffer.getOutputStream();
		stderr = sniffer.getErrorStream();
	}

	/**
	 * @return the output stream to connect stdout of a process
	 */
	public OutputStream getOutputStream() {
		return stdout;
	}

	/**
	 * @return the output stream to connect stderr of a process
	 */
	public OutputStream getErrorStream() {
		return stderr;
	}

	/**
	 * Remove problem markers created for the resource by previous build.
	 *
	 * @param rc - resource to remove its markers.
	 * @param monitor - progress monitor in the initial state where {@link IProgressMonitor#beginTask(String, int)}
	 *    has not been called yet.
	 * @throws CoreException
	 */
	public void removeOldMarkers(IResource rc, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			try {
				if (rc != null) {
					monitor.subTask(CCorePlugin.getFormattedString("BuildRunnerHelper.removingMarkers", //$NON-NLS-1$
							rc.getFullPath().toString()));
					rc.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
				}
			} catch (CoreException e) {
				// ignore
			}
			if (project != null) {
				// Remove markers which source is this project from other projects
				try {
					IWorkspace workspace = project.getWorkspace();
					IMarker[] markers = workspace.getRoot().findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true,
							IResource.DEPTH_INFINITE);
					String projectName = project.getName();
					List<IMarker> markersList = new ArrayList<>();
					for (IMarker marker : markers) {
						if (projectName.equals(marker.getAttribute(IMarker.SOURCE_ID))) {
							markersList.add(marker);
						}
					}
					if (markersList.size() > 0) {
						workspace.deleteMarkers(markersList.toArray(new IMarker[markersList.size()]));
						FixitManager.getInstance().deleteMarkers(markersList.toArray(new IMarker[markersList.size()]));
					}
				} catch (CoreException e) {
					// ignore
				}
			}

		} finally {
			monitor.done();
		}
	}

	/**
	 * Launch build command and process console output.
	 *
	 * @param monitor - progress monitor in the initial state where {@link IProgressMonitor#beginTask(String, int)}
	 *    has not been called yet.
	 * @throws CoreException
	 * @throws IOException
	 */
	public int build(IProgressMonitor monitor) throws CoreException, IOException {
		Assert.isNotNull(launcher, "Launch parameters must be set before calling this method"); //$NON-NLS-1$
		Assert.isNotNull(errorParserManager, "Streams must be created and connected before calling this method"); //$NON-NLS-1$

		int status = ICommandLauncher.ILLEGAL_COMMAND;

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask("", TICKS_EXECUTE_PROGRAM + TICKS_PARSE_OUTPUT); //$NON-NLS-1$

			isCancelled = false;
			String pathFromURI = null;
			if (workingDirectoryURI != null) {
				pathFromURI = EFSExtensionManager.getDefault().getPathFromURI(workingDirectoryURI);
			}
			if (pathFromURI == null) {
				// fallback to CWD
				pathFromURI = System.getProperty("user.dir"); //$NON-NLS-1$
			}
			IPath workingDirectory = new Path(pathFromURI);

			String errMsg = null;
			monitor.subTask(CCorePlugin.getFormattedString("BuildRunnerHelper.invokingCommand", //$NON-NLS-1$
					guessCommandLine(buildCommand.toString(), args)));
			Process p = launcher.execute(buildCommand, args, envp, workingDirectory, monitor);
			monitor.worked(TICKS_EXECUTE_PROGRAM);
			if (p != null) {
				try {
					// Close the input of the Process explicitly.
					// We will never write to it.
					p.getOutputStream().close();
				} catch (IOException e) {
				}

				status = launcher.waitAndRead(stdout, stderr, monitor);
				monitor.worked(TICKS_PARSE_OUTPUT);
				if (status != ICommandLauncher.OK) {
					errMsg = launcher.getErrorMessage();
				} else if (p.exitValue() != 0) {
					errMsg = CCorePlugin.getFormattedString("BuildRunnerHelper.commandNonZeroExitCode", //$NON-NLS-1$
							new String[] { guessCommandLine(buildCommand.toString(), args),
									Integer.toString(p.exitValue()) });
				}
			} else {
				errMsg = launcher.getErrorMessage();
			}

			if (errMsg != null && !errMsg.isEmpty()) {
				stderr.write(errMsg.getBytes());
			}

			isCancelled = monitor.isCanceled();
			if (!isCancelled && project != null) {
				project.setSessionProperty(progressPropertyName, Integer.valueOf(streamProgressMonitor.getWorkDone()));
			}
		} catch (Exception e) {
			CCorePlugin.log(e);
		} finally {
			monitor.done();
		}
		return status;
	}

	/**
	 * Close all streams except console Info stream which is handled by {@link #greeting(String)}/{@link #goodbye()}.
	 */
	@Override
	public void close() throws IOException {
		if (!isStreamsOpen)
			return;

		try {
			if (stdout != null)
				stdout.close();
		} catch (Exception e) {
			CCorePlugin.log(e);
		} finally {
			stdout = null;
			try {
				if (stderr != null)
					stderr.close();
			} catch (Exception e) {
				CCorePlugin.log(e);
			} finally {
				stderr = null;
				try {
					if (streamProgressMonitor != null)
						streamProgressMonitor.close();
				} catch (Exception e) {
					CCorePlugin.log(e);
				} finally {
					streamProgressMonitor = null;
					try {
						if (consoleOut != null)
							consoleOut.close();
					} catch (Exception e) {
						CCorePlugin.log(e);
					} finally {
						consoleOut = null;
					}
				}
			}
		}
		isStreamsOpen = false;
	}

	/**
	 * Refresh project in the workspace.
	 *
	 * @param configName - the configuration to refresh
	 * @param monitor - progress monitor in the initial state where {@link IProgressMonitor#beginTask(String, int)}
	 *    has not been called yet.
	 */
	public void refreshProject(String configName, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask(CCorePlugin.getFormattedString("BuildRunnerHelper.refreshingProject", project.getName()), //$NON-NLS-1$
					IProgressMonitor.UNKNOWN);
			monitor.subTask(""); //$NON-NLS-1$

			// Do not allow the cancel of the refresh, since the builder is external
			// to Eclipse, files may have been created/modified and we will be out-of-sync.
			// The caveat is for huge projects, it may take sometimes at every build.
			// Use the refresh scope manager to refresh
			RefreshScopeManager refreshManager = RefreshScopeManager.getInstance();
			IWorkspaceRunnable runnable = refreshManager.getRefreshRunnable(project, configName);
			ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e) {
			// ignore exceptions
		} finally {
			monitor.done();
		}
	}

	/**
	 * Print a standard greeting to the console.
	 * Note that start time of the build is recorded by this method.
	 *
	 * This method may open an Info stream which must be closed by call to {@link #goodbye()}
	 * after all informational messages are printed.
	 *
	 * @param kind - kind of build. {@link IncrementalProjectBuilder} constants such as
	 *    {@link IncrementalProjectBuilder#FULL_BUILD} should be used.
	 */
	public void greeting(int kind) {
		String msg = CCorePlugin.getFormattedString("BuildRunnerHelper.buildProject", //$NON-NLS-1$
				new String[] { buildKindToString(kind), project.getName() });
		greeting(msg);
	}

	/**
	 * Print a standard greeting to the console.
	 * Note that start time of the build is recorded by this method.
	 *
	 * This method may open an Info stream which must be closed by call to {@link #goodbye()}
	 * after all informational messages are printed.
	 *
	 * @param kind - kind of build. {@link IncrementalProjectBuilder} constants such as
	 *    {@link IncrementalProjectBuilder#FULL_BUILD} should be used.
	 * @param cfgName - configuration name.
	 * @param toolchainName - tool-chain name.
	 * @param isSupported - flag indicating if tool-chain is supported on the system.
	 */
	public void greeting(int kind, String cfgName, String toolchainName, boolean isSupported) {
		greeting(buildKindToString(kind), cfgName, toolchainName, isSupported);
	}

	/**
	 * Print a standard greeting to the console.
	 * Note that start time of the build is recorded by this method.
	 *
	 * This method may open an Info stream which must be closed by call to {@link #goodbye()}
	 * after all informational messages are printed.
	 *
	 * @param kind - kind of build as a String.
	 * @param cfgName - configuration name.
	 * @param toolchainName - tool-chain name.
	 * @param isSupported - flag indicating if tool-chain is supported on the system.
	 */
	public void greeting(String kind, String cfgName, String toolchainName, boolean isSupported) {
		String msg = CCorePlugin.getFormattedString("BuildRunnerHelper.buildProjectConfiguration", //$NON-NLS-1$
				new String[] { kind, cfgName, project.getName() });
		greeting(msg);

		if (!isSupported) {
			String errMsg = CCorePlugin.getFormattedString("BuildRunnerHelper.unsupportedConfiguration", //$NON-NLS-1$
					new String[] { cfgName, toolchainName });
			printLine(errMsg);
		}
	}

	/**
	 * Print the specified greeting to the console.
	 * Note that start time of the build is recorded by this method.
	 *
	 * This method may open an Info stream which must be closed by call to {@link #goodbye()}
	 * after all informational messages are printed.
	 */
	public void greeting(String msg) {
		startTime = System.currentTimeMillis();
		if (consoleInfo == null) {
			try {
				consoleInfo = console.getInfoStream();
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		toConsole(BuildRunnerHelper.timestamp(startTime) + "**** " + msg + " ****"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Print a standard footer to the console and close Info stream (must be open with one of {@link #greeting(String)} calls).
	 * That prints duration of the build determined by start time recorded in {@link #greeting(String)}.
	 *
	 * <br><strong>Important: {@link #close()} the streams BEFORE calling this method to properly flush all outputs</strong>
	 */
	public void goodbye() {
		Assert.isTrue(startTime != 0, "Start time must be set before calling this method."); //$NON-NLS-1$
		Assert.isTrue(consoleInfo != null,
				"consoleInfo must be open with greetings(...) call before using this method."); //$NON-NLS-1$

		//Count Errors/Warnings
		int errorCount = errorParserManager.getErrorCount();
		int warningCount = errorParserManager.getWarningCount();

		endTime = System.currentTimeMillis();
		String duration = durationToString(endTime - startTime);
		String msg = ""; //$NON-NLS-1$
		if (isCancelled) {
			msg = CCorePlugin.getFormattedString("BuildRunnerHelper.buildCancelled", duration); //$NON-NLS-1$
		} else if (errorCount > 0) {
			msg = CCorePlugin.getFormattedString("BuildRunnerHelper.buildFailed", new String[] { duration, //$NON-NLS-1$
					Integer.toString(errorCount), Integer.toString(warningCount) });
		} else {
			msg = CCorePlugin.getFormattedString("BuildRunnerHelper.buildFinished", new String[] { duration, //$NON-NLS-1$
					Integer.toString(errorCount), Integer.toString(warningCount) });
		}
		String goodbye = '\n' + timestamp(endTime) + msg + '\n';

		try {
			toConsole(goodbye);
		} finally {
			try {
				consoleInfo.close();
			} catch (Exception e) {
				CCorePlugin.log(e);
			} finally {
				consoleInfo = null;
			}
		}
	}

	/**
	 * Print the given message to the console.
	 * @param msg - message to print.
	 */
	public void printLine(String msg) {
		Assert.isNotNull(errorParserManager, "Streams must be created and connected before calling this method"); //$NON-NLS-1$
		errorParserManager.processLine(msg);
	}

	/**
	 * Compose command line that presumably will be run by launcher.
	 */
	private static String guessCommandLine(String command, String[] args) {
		StringBuilder buf = new StringBuilder(command + ' ');
		if (args != null) {
			for (String arg : args) {
				buf.append(arg);
				buf.append(' ');
			}
		}
		return buf.toString().trim();
	}

	/**
	 * Print a message to the console info output. Note that this message is colored
	 * with the color assigned to "Info" stream.
	 * @param msg - message to print.
	 */
	private void toConsole(String msg) {
		Assert.isNotNull(console, "Streams must be created and connected before calling this method"); //$NON-NLS-1$
		try {
			consoleInfo.write((msg + "\n").getBytes()); //$NON-NLS-1$
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
	}

	/**
	 * Qualified name to keep previous value of build duration in project session properties.
	 */
	private static QualifiedName getProgressPropertyName(IPath buildCommand, String[] args) {
		String name = "buildCommand." + buildCommand.toString(); //$NON-NLS-1$
		if (args != null) {
			for (String arg : args) {
				name = name + ' ' + arg;
			}
		}
		return new QualifiedName(PROGRESS_MONITOR_QUALIFIER, name);
	}

	/**
	 * Get environment variables from configuration as array of "var=value" suitable
	 * for using as "envp" with Runtime.exec(String[] cmdarray, String[] envp, File dir)
	 *
	 * @param envMap - map of environment variables
	 * @return String array of environment variables in format "var=value"
	 */
	public static String[] envMapToEnvp(Map<String, String> envMap) {
		// Convert into envp strings
		List<String> strings = new ArrayList<>(envMap.size());
		for (Entry<String, String> entry : envMap.entrySet()) {
			strings.add(entry.getKey() + '=' + entry.getValue());
		}

		return strings.toArray(new String[strings.size()]);
	}

	/**
	 * Get environment variables from configuration as array of "var=value" suitable
	 * for using as "envp" with Runtime.exec(String[] cmdarray, String[] envp, File dir)
	 *
	 * @param cfgDescription - configuration description.
	 * @return String array of environment variables in format "var=value". Does not return {@code null}.
	 */
	public static String[] getEnvp(ICConfigurationDescription cfgDescription) {
		IEnvironmentVariableManager mngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IEnvironmentVariable[] vars = mngr.getVariables(cfgDescription, true);
		// Convert into envp strings
		List<String> strings = new ArrayList<>(vars.length);
		for (IEnvironmentVariable var : vars) {
			strings.add(var.getName() + '=' + var.getValue());
		}

		return strings.toArray(new String[strings.size()]);
	}

	/**
	 * Convert duration to human friendly format.
	 */
	@SuppressWarnings("nls")
	private static String durationToString(long duration) {
		String result = "";
		long days = TimeUnit.MILLISECONDS.toDays(duration);
		if (days > 0) {
			result += days + "d,";
		}
		long hours = TimeUnit.MILLISECONDS.toHours(duration) % 24;
		if (hours > 0) {
			result += hours + "h:";
		}
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
		if (minutes > 0) {
			result += minutes + "m:";
		}
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
		if (seconds > 0) {
			result += seconds + "s.";
		}
		long milliseconds = TimeUnit.MILLISECONDS.toMillis(duration) % 1000;
		result += milliseconds + "ms";

		return result;
	}

	/**
	 * Supply timestamp to prefix informational messages.
	 */
	@SuppressWarnings("nls")
	private static String timestamp(long time) {
		return new SimpleDateFormat("HH:mm:ss").format(new Date(time)) + " ";
	}

	/**
	 * Convert build kind to human friendly format.
	 */
	private static String buildKindToString(int kind) {
		switch (kind) {
		case IncrementalProjectBuilder.FULL_BUILD:
			return CCorePlugin.getResourceString("BuildRunnerHelper.build"); //$NON-NLS-1$
		case IncrementalProjectBuilder.INCREMENTAL_BUILD:
			return CCorePlugin.getResourceString("BuildRunnerHelper.incrementalBuild"); //$NON-NLS-1$
		case IncrementalProjectBuilder.AUTO_BUILD:
			return CCorePlugin.getResourceString("BuildRunnerHelper.autoBuild"); //$NON-NLS-1$
		case IncrementalProjectBuilder.CLEAN_BUILD:
			return CCorePlugin.getResourceString("BuildRunnerHelper.cleanBuild"); //$NON-NLS-1$
		default:
			return CCorePlugin.getResourceString("BuildRunnerHelper.build"); //$NON-NLS-1$
		}
	}
}
