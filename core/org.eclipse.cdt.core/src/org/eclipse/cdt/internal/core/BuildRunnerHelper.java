/*******************************************************************************
 * Copyright (c) 2012, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
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
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.RefreshScopeManager;
import org.eclipse.cdt.utils.EFSExtensionManager;
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
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Helper class attempting to unify interactions with build console,
 * such as style of console output and handling of console output parsers.
 *
 * As of CDT 8.1, this class is experimental, internal and work in progress.
 * <strong>API is unstable and subject to change.</strong>
 */
public class BuildRunnerHelper implements Closeable {
	private static final String PROGRESS_MONITOR_QUALIFIER = CCorePlugin.PLUGIN_ID + ".progressMonitor"; //$NON-NLS-1$
	private static final int MONITOR_SCALE = 100;

	private IProject project;

	private IConsole console = null;
	private ErrorParserManager errorParserManager = null;
	private StreamMonitor streamMon = null;
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
	public void setLaunchParameters(ICommandLauncher launcher, IPath buildCommand, String[] args, URI workingDirectoryURI, String[] envp) {
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
	public void prepareStreams(ErrorParserManager epm, List<IConsoleParser> buildOutputParsers, IConsole con, IProgressMonitor monitor) throws CoreException {
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

		List<IConsoleParser> parsers = new ArrayList<IConsoleParser>();
		// Using ErrorParserManager as console parser helps to avoid intermixing buffered streams
		// as ConsoleOutputSniffer waits for EOL to send a line to console parsers
		// separately for each stream.
		parsers.add(errorParserManager);
		if (buildOutputParsers != null) {
			parsers.addAll(buildOutputParsers);
		}

		Integer lastWork = null;
		if (buildCommand != null) {
			progressPropertyName = getProgressPropertyName(buildCommand, args);
			lastWork = (Integer)project.getSessionProperty(progressPropertyName);
		}
		if (lastWork == null) {
			lastWork = MONITOR_SCALE;
		}

		streamMon = new StreamMonitor(monitor, null, lastWork.intValue());
		ConsoleOutputSniffer sniffer = new ConsoleOutputSniffer(streamMon, streamMon, parsers.toArray(new IConsoleParser[parsers.size()]));
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
			monitor.subTask(CCorePlugin.getFormattedString("BuildRunnerHelper.removingMarkers", rc.getFullPath().toString())); //$NON-NLS-1$
			rc.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false,  IResource.DEPTH_INFINITE);
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
			monitor.beginTask("", 2 * MONITOR_SCALE); //$NON-NLS-1$

			isCancelled = false;
			String pathFromURI = EFSExtensionManager.getDefault().getPathFromURI(workingDirectoryURI);
			if(pathFromURI == null) {
				// fallback to CWD
				pathFromURI = System.getProperty("user.dir"); //$NON-NLS-1$
			}
			IPath workingDirectory = new Path(pathFromURI);

			String errMsg = null;
			Process p = launcher.execute(buildCommand, args, envp, workingDirectory, monitor);
			if (p != null) {
				try {
					// Close the input of the Process explicitly.
					// We will never write to it.
					p.getOutputStream().close();
				} catch (IOException e) {
				}
				// Before launching give visual cues via the monitor
				monitor.subTask(CCorePlugin.getFormattedString("BuildRunnerHelper.invokingCommand", launcher.getCommandLine())); //$NON-NLS-1$

				status = launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(monitor, 1 * MONITOR_SCALE));
				if (status != ICommandLauncher.OK) {
					errMsg = launcher.getErrorMessage();
				}
			} else {
				errMsg = launcher.getErrorMessage();
			}

			if (errMsg != null && !errMsg.isEmpty()) {
				stderr.write(errMsg.getBytes());
			}

			isCancelled = monitor.isCanceled();
			if (!isCancelled) {
				project.setSessionProperty(progressPropertyName, new Integer(streamMon.getWorkDone()));
			}
		} finally {
			monitor.done();
		}
		return status;
	}

	/**
	 * Close all streams.
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
			try {
				if (stderr != null)
					stderr.close();
			} catch (Exception e) {
				CCorePlugin.log(e);
			} finally {
				try {
					if (streamMon != null)
						streamMon.close();
				} catch (Exception e) {
					CCorePlugin.log(e);
				} finally {
					try {
						if (consoleOut != null)
							consoleOut.close();
					} catch (Exception e) {
						CCorePlugin.log(e);
					}
				}
			}
		}
		isStreamsOpen = false;
	}

	/**
	 * Refresh project in the workspace.
	 *
	 * @param monitor - progress monitor in the initial state where {@link IProgressMonitor#beginTask(String, int)}
	 *    has not been called yet.
	 */
	public void refreshProject(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask(CCorePlugin.getResourceString("BuildRunnerHelper.updatingProject"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

			// Do not allow the cancel of the refresh, since the builder is external
			// to Eclipse, files may have been created/modified and we will be out-of-sync.
			// The caveat is for huge projects, it may take sometimes at every build.
			// Use the refresh scope manager to refresh
			RefreshScopeManager refreshManager = RefreshScopeManager.getInstance();
			IWorkspaceRunnable runnable = refreshManager.getRefreshRunnable(project);
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
	 * @param kind - kind of build. {@link IncrementalProjectBuilder} constants such as
	 *    {@link IncrementalProjectBuilder#FULL_BUILD} should be used.
	 */
	public void greeting(int kind) {
		String msg = CCorePlugin.getFormattedString("BuildRunnerHelper.buildProject",  //$NON-NLS-1$
					new String[] { buildKindToString(kind), project.getName() });
		greeting(msg);
	}

	/**
	 * Print a standard greeting to the console.
	 * Note that start time of the build is recorded by this method.
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
	 * @param kind - kind of build as a String.
	 * @param cfgName - configuration name.
	 * @param toolchainName - tool-chain name.
	 * @param isSupported - flag indicating if tool-chain is supported on the system.
	 */
	public void greeting(String kind, String cfgName, String toolchainName, boolean isSupported) {
		String msg = CCorePlugin.getFormattedString("BuildRunnerHelper.buildProjectConfiguration",  //$NON-NLS-1$
				new String[] { kind, cfgName, project.getName() });
		greeting(msg);

		if (!isSupported ){
			String errMsg = CCorePlugin.getFormattedString("BuildRunnerHelper.unsupportedConfiguration", //$NON-NLS-1$
					new String[] { cfgName, toolchainName });
			printLine(errMsg);
		}
	}

	/**
	 * Print the specified greeting to the console.
	 * Note that start time of the build is recorded by this method.
	 */
	public void greeting(String msg) {
		startTime = System.currentTimeMillis();
		toConsole(BuildRunnerHelper.timestamp(startTime) + "**** " + msg + " ****"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Print a standard footer to the console.
	 * That prints duration of the build determined by start time recorded in {@link #greeting(String)}.
	 *
	 * <br><strong>Important: {@link #close()} the streams BEFORE calling this method to properly flush all outputs</strong>
	 */
	public void goodbye() {
		Assert.isTrue(startTime != 0, "Start time must be set before calling this method"); //$NON-NLS-1$
		Assert.isTrue(!isStreamsOpen, "Close streams before calling this method."); //$NON-NLS-1$

		endTime = System.currentTimeMillis();
		String duration = durationToString(endTime - startTime);
		String msg = isCancelled ? CCorePlugin.getFormattedString("BuildRunnerHelper.buildCancelled", duration) //$NON-NLS-1$
				: CCorePlugin.getFormattedString("BuildRunnerHelper.buildFinished", duration); //$NON-NLS-1$
		String goodbye = '\n' + timestamp(endTime) + msg + '\n';
		toConsole(goodbye);
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
	 * Print a message to the console info output. Note that this message is colored
	 * with the color assigned to "Info" stream.
	 * @param msg - message to print.
	 */
	private void toConsole(String msg) {
		Assert.isNotNull(console, "Streams must be created and connected before calling this method"); //$NON-NLS-1$
		try {
			if (consoleInfo == null) {
				consoleInfo = console.getInfoStream();
			}
			consoleInfo.write((msg+"\n").getBytes()); //$NON-NLS-1$
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
	}

	/**
	 * Qualified name to keep previous value of build duration in project session properties.
	 */
	private static QualifiedName getProgressPropertyName(IPath buildCommand, String[] args) {
		String name = buildCommand.toString();
		if (args != null) {
			for (String arg : args) {
				name = name + ' ' + arg;
			}
		}
		return new QualifiedName(PROGRESS_MONITOR_QUALIFIER, name);
	}

	/**
	 * Convert map of environment variables to array of "var=value"
	 *
	 * @param envMap - map of environment variables
	 * @return String array of environment variables in format "var=value" suitable for using
	 *    as "envp" with Runtime.exec(String[] cmdarray, String[] envp, File dir)
	 */
	public static String[] envMapToEnvp(Map<String, String> envMap) {
		// Convert into env strings
		List<String> strings= new ArrayList<String>(envMap.size());
		for (Entry<String, String> entry : envMap.entrySet()) {
			StringBuffer buffer= new StringBuffer(entry.getKey());
			buffer.append('=').append(entry.getValue());
			strings.add(buffer.toString());
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
			result +=  hours + "h:";
		}
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
		if (minutes > 0) {
			result +=  minutes + "m:";
		}
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
		if (seconds > 0) {
			result +=  seconds + "s.";
		}
		long milliseconds = TimeUnit.MILLISECONDS.toMillis(duration) % 1000;
		result +=  milliseconds + "ms";

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
