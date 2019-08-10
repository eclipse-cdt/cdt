/*******************************************************************************
 * Copyright (c) 2013, 2016 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *    Marc Khouzam (Ericsson) - Update for remote debugging support (bug 450080)
 *******************************************************************************/
package org.eclipse.cdt.debug.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.internal.debug.application.DebugAttachedExecutable;
import org.eclipse.cdt.internal.debug.application.DebugCoreFile;
import org.eclipse.cdt.internal.debug.application.DebugExecutable;
import org.eclipse.cdt.internal.debug.application.DebugRemoteExecutable;
import org.eclipse.cdt.internal.debug.application.JobContainer;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private static final String STANDALONE_QUALIFIER = "org.eclipse.cdt.debug.application"; //$NON-NLS-1$
	private static final String LAST_LAUNCH = "lastLaunch"; //$NON-NLS-1$
	private ILaunchConfiguration config;

	private class StartupException extends FileNotFoundException {
		private static final long serialVersionUID = 1L;

		public StartupException(String s) {
			super();
		}
	}

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		//        configurer.setInitialSize(new Point(400, 300));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowMenuBar(true);
		configurer.setShowProgressIndicator(true);
		configurer.setTitle(Messages.Debugger_Title);
	}

	@Override
	public void postWindowCreate() {
		super.postWindowCreate();
		try {
			IRunnableWithProgress op = new PostWindowCreateRunnable();
			new ProgressMonitorDialog(getWindowConfigurer().getWindow().getShell()).run(true, true, op);
		} catch (InvocationTargetException e) {
			// handle exception
		} catch (InterruptedException e) {
			// handle cancelation
		}
	}

	// Private method to search for executable names on PATH
	private String findExecutable(String input) {
		String result = input;

		Path x = new Path(input);
		try {
			if (!x.isAbsolute() && x.segmentCount() == 1) {
				String command = "which " + input; //$NON-NLS-1$
				Process p = null;
				InputStream in = null;
				try {
					p = ProcessFactory.getFactory().exec(command);
					in = p.getInputStream();
					InputStreamReader reader = new InputStreamReader(in);
					BufferedReader br = new BufferedReader(reader);
					String line = br.readLine();
					if (line != null)
						result = line;
				} finally {
					if (in != null)
						in.close();
					if (p != null)
						p.destroy();
				}
			}
		} catch (IOException e) {
			// do nothing
		}
		return result;
	}

	private void appendChar(StringBuilder builder, int repeat, char c) {
		for (int i = 0; i < repeat; i++) {
			builder.append(c);
		}
	}

	private boolean needsEscaping(String input) {
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '"') {
				return true;
			}
		}
		return false;
	}

	private String escapeArg(String arg) {
		if (!arg.isEmpty() && !needsEscaping(arg)) {
			return arg;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append('"');
		for (int i = 0; i < arg.length(); i++) {
			int numberOfBackslashes = 0;
			while (i < arg.length() && arg.charAt(i) == '\\') {
				i++;
				numberOfBackslashes++;
			}

			if (i == arg.length()) {
				appendChar(buffer, numberOfBackslashes * 2, '\\');
				break;
			} else if (arg.charAt(i) == '"') {
				appendChar(buffer, numberOfBackslashes * 2 + 1, '\\');
				buffer.append(arg.charAt(i));
			} else {
				appendChar(buffer, numberOfBackslashes * 2, '\\');
				buffer.append(arg.charAt(i));
			}
		}
		buffer.append('"');
		return buffer.toString();
	}

	public class PostWindowCreateRunnable implements IRunnableWithProgress {

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			monitor.beginTask(Messages.InitializingDebugger, 10);
			boolean attachExecutable = false;
			String executable = null;
			String corefile = null;
			String buildLog = null;
			String arguments = null;
			String remoteAddress = null;
			String remotePort = null;
			String pid = null;
			String[] args = Platform.getCommandLineArgs();

			try {
				for (int i = 0; i < args.length; ++i) {
					if ("-application".equals(args[i])) //$NON-NLS-1$
						i++; // ignore the application specifier
					else if ("-product".equals(args[i])) //$NON-NLS-1$
						i++; // ignore the product specifier
					else if ("-b".equals(args[i])) { //$NON-NLS-1$
						++i;
						if (i < args.length)
							buildLog = args[i];
					} else if ("-a".equals(args[i])) { //$NON-NLS-1$
						attachExecutable = true;
						// Make sure 'executable' is still null in case we are dealing with a remote
						// session that is also an attach, as the -r flag could have been set first
						executable = null;

						// Check for optional pid
						if (i + 1 < args.length) {
							if (!args[i + 1].startsWith("-")) { //$NON-NLS-1$
								++i;
								pid = args[i];
							}
						}
					} else if ("-c".equals(args[i])) { //$NON-NLS-1$
						++i;
						corefile = ""; //$NON-NLS-1$
						executable = ""; //$NON-NLS-1$
						if (i < args.length)
							corefile = args[i];
					} else if ("-r".equals(args[i])) { //$NON-NLS-1$
						++i;
						remoteAddress = ""; //$NON-NLS-1$
						if (!attachExecutable)
							executable = ""; //$NON-NLS-1$
						if (i < args.length) {
							String[] params = args[i].split(":"); //$NON-NLS-1$
							if (params.length == 2) {
								remoteAddress = params[0];
								remotePort = params[1];
							}
						}
					} else if ("-e".equals(args[i])) { //$NON-NLS-1$
						++i;
						if (i < args.length)
							executable = findExecutable(args[i]);
						++i;
						StringBuilder argBuffer = new StringBuilder();
						// Remaining values are arguments to the executable
						if (i < args.length)
							argBuffer.append(escapeArg(args[i++]));
						while (i < args.length) {
							argBuffer.append(" "); //$NON-NLS-1$
							argBuffer.append(escapeArg(args[i++]));
						}
						arguments = argBuffer.toString();
					}
				}
				// Verify any core file or executable path is valid.
				if (corefile != null) {
					File executableFile = null;
					if (executable != null) {
						executableFile = new File(executable);
						executable = executableFile.getCanonicalPath();
					}
					File coreFile = new File(corefile);
					corefile = coreFile.getCanonicalPath();
					if (executableFile == null || !executableFile.exists() || !coreFile.exists()) {
						final CoreFileInfo info = new CoreFileInfo("", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ $NON-NLS-2$ $NON-NLS-3$
						final IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
								Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist, null);
						final String executablePath = executable;
						final String coreFilePath = corefile;

						Display.getDefault().syncExec(() -> {

							CoreFileDialog dialog = new CoreFileDialog(getWindowConfigurer().getWindow().getShell(), 0,
									executablePath, coreFilePath);
							dialog.setBlockOnOpen(true);
							if (dialog.open() == IDialogConstants.OK_ID) {
								CoreFileInfo info2 = dialog.getCoreFileInfo();
								info.setHostPath(info2.getHostPath());
								info.setCoreFilePath(info2.getCoreFilePath());
							} else {
								ErrorDialog.openError(null, Messages.DebuggerInitializingProblem, null, errorStatus,
										IStatus.ERROR | IStatus.WARNING);
							}
						});
						// Check and see if we failed above and if so, quit
						if (info.getHostPath().isEmpty()) {
							monitor.done();
							// throw internal exception which will be caught below
							throw new StartupException(errorStatus.getMessage());
						}
						executable = info.getHostPath();
						corefile = info.getCoreFilePath();
					}
				} else if (remoteAddress != null) {
					// Verify what we can about the port, address and executable.
					File executableFile = null;
					if (executable != null) {
						executableFile = new File(executable);
						executable = executableFile.getCanonicalPath();
					}

					Integer port = null;
					try {
						port = Integer.parseInt(remotePort);
					} catch (NumberFormatException e) {
						port = null;
					}

					if ((!attachExecutable && (executableFile == null || !executableFile.exists()))
							|| remoteAddress.length() == 0 || port == null) {
						final RemoteExecutableInfo[] info = new RemoteExecutableInfo[1];
						final IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
								Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist, null);
						final String executablePath = executable;
						final String addressStr = remoteAddress;
						final String portStr = remotePort;
						final String buildLogPath = buildLog;
						final boolean attach = attachExecutable;

						Display.getDefault().syncExec(() -> {

							RemoteExecutableDialog dialog = new RemoteExecutableDialog(
									getWindowConfigurer().getWindow().getShell(), executablePath, buildLogPath,
									addressStr, portStr, attach);
							dialog.setBlockOnOpen(true);
							if (dialog.open() == IDialogConstants.OK_ID) {
								info[0] = dialog.getExecutableInfo();
							} else {
								info[0] = null;
								ErrorDialog.openError(null, Messages.DebuggerInitializingProblem, null, errorStatus,
										IStatus.ERROR | IStatus.WARNING);
							}
						});
						// Check and see if we failed above and if so, quit
						if (info[0] == null) {
							monitor.done();
							// throw internal exception which will be caught below
							throw new StartupException(errorStatus.getMessage());
						}
						executable = info[0].getHostPath();
						buildLog = info[0].getBuildLog();
						remoteAddress = info[0].getAddress();
						remotePort = info[0].getPort();
						attachExecutable = info[0].isAttach();
					}
				} else if (executable != null) {
					File executableFile = new File(executable);
					executable = executableFile.getCanonicalPath();
					File buildLogFile = null;
					if (buildLog != null) {
						buildLogFile = new File(buildLog);
						buildLog = buildLogFile.getCanonicalPath();
					}
					if (!executableFile.exists() || (buildLogFile != null && !buildLogFile.exists())) {
						final NewExecutableInfo info = new NewExecutableInfo("", "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ $NON-NLS-2$ $NON-NLS-3$
						final IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
								Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist, null);
						final String executablePath = executable;
						final String executableArgs = arguments;
						final String buildLogPath = buildLog;

						Display.getDefault().syncExec(() -> {

							NewExecutableDialog dialog = new NewExecutableDialog(
									getWindowConfigurer().getWindow().getShell(), 0, executablePath, buildLogPath,
									executableArgs);
							dialog.setBlockOnOpen(true);
							if (dialog.open() == IDialogConstants.OK_ID) {
								NewExecutableInfo info2 = dialog.getExecutableInfo();
								info.setHostPath(info2.getHostPath());
								info.setArguments(info2.getArguments());
							} else {
								ErrorDialog.openError(null, Messages.DebuggerInitializingProblem, null, errorStatus,
										IStatus.ERROR | IStatus.WARNING);
							}
						});
						// Check and see if we failed above and if so, quit
						if (info.getHostPath().isEmpty()) {
							monitor.done();
							// throw internal exception which will be caught below
							throw new StartupException(errorStatus.getMessage());
						}
						executable = info.getHostPath();
						arguments = info.getArguments();
					}
				}
				monitor.worked(1);
				if (remoteAddress != null && remoteAddress.length() > 0 && remotePort != null
						&& remotePort.length() > 0) {
					config = DebugRemoteExecutable.createLaunchConfig(monitor, buildLog, executable, remoteAddress,
							remotePort, attachExecutable);
				} else if (attachExecutable) {
					config = DebugAttachedExecutable.createLaunchConfig(monitor, buildLog, pid);
				} else if (corefile != null && corefile.length() > 0) {
					config = DebugCoreFile.createLaunchConfig(monitor, buildLog, executable, corefile);
				} else if (executable != null && executable.length() > 0) {
					config = DebugExecutable.importAndCreateLaunchConfig(monitor, executable, buildLog, arguments,
							true);
				} else {
					// No executable specified, look for last launch
					// and offer that to the end-user.
					monitor.subTask(Messages.RestorePreviousLaunch);
					String memento = ResourcesPlugin.getWorkspace().getRoot()
							.getPersistentProperty(new QualifiedName(STANDALONE_QUALIFIER, LAST_LAUNCH));
					if (memento != null)
						config = DebugExecutable.getLaunchManager().getLaunchConfiguration(memento);
					String oldExecutable = ""; //$NON-NLS-1$
					String oldArguments = ""; //$NON-NLS-1$
					String oldBuildLog = ""; //$NON-NLS-1$
					if (config != null) {
						oldExecutable = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, ""); //$NON-NLS-1$
						oldArguments = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
						oldBuildLog = config.getAttribute(ICDTStandaloneDebugLaunchConstants.BUILD_LOG_LOCATION, ""); //$NON-NLS-1$
					}
					final NewExecutableInfo info = new NewExecutableInfo("", "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ $NON-NLS-2$ $NON-NLS-3$
					final IStatus errorStatus = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0,
							Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist, null);
					final String executablePath = oldExecutable;
					final String executableArgs = oldArguments;
					final String buildLogPath = oldBuildLog;
					// Bring up New Executable dialog with values from
					// the last launch.
					Display.getDefault().syncExec(() -> {

						NewExecutableDialog dialog = new NewExecutableDialog(
								getWindowConfigurer().getWindow().getShell(), 0, executablePath, buildLogPath,
								executableArgs);
						dialog.setBlockOnOpen(true);
						if (dialog.open() == IDialogConstants.OK_ID) {
							NewExecutableInfo info2 = dialog.getExecutableInfo();
							info.setHostPath(info2.getHostPath());
							info.setArguments(info2.getArguments());
							info.setBuildLog(info2.getBuildLog());
						} else {
							ErrorDialog.openError(null, Messages.DebuggerInitializingProblem, null, errorStatus,
									IStatus.ERROR | IStatus.WARNING);
						}
					});
					// Check and see if we failed above and if so, quit
					if (info.getHostPath().isEmpty()) {
						monitor.done();
						// throw internal exception which will be caught below
						throw new StartupException(errorStatus.getMessage());
					}
					executable = info.getHostPath();
					arguments = info.getArguments();
					buildLog = info.getBuildLog();
					// If no last configuration or user has changed
					// the executable, we need to create a new configuration
					// and remove artifacts from the old one.
					if (config == null || !executable.equals(oldExecutable))
						config = DebugExecutable.importAndCreateLaunchConfig(monitor, executable, buildLog, arguments,
								true);
					ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
					wc.setAttribute(ICDTStandaloneDebugLaunchConstants.BUILD_LOG_LOCATION, buildLog);
					if (arguments != null)
						wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, arguments);
					config = wc.doSave();

					monitor.worked(7);
				}
				if (config != null) {
					final JobContainer LaunchJobs = new JobContainer();
					Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {

						@Override
						public void scheduled(IJobChangeEvent event) {
							Job job = event.getJob();
							if (job.getName().contains(config.getName()))
								LaunchJobs.setLaunchJob(job);
						}

						@Override
						public void done(IJobChangeEvent event) {
						}
					});
					monitor.subTask(Messages.LaunchingConfig);
					Display.getDefault().syncExec(() -> DebugUITools.launch(config, ILaunchManager.DEBUG_MODE));
					if (LaunchJobs.getLaunchJob() != null) {
						try {
							LaunchJobs.getLaunchJob().join();
						} catch (InterruptedException e) {
							IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
									Messages.LaunchInterruptedError, e);
							ResourcesPlugin.getPlugin().getLog().log(status);
						}
					}
				}
			} catch (InterruptedException e) {
				throw e; // rethrow exception
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (StartupException e) {
				// do nothing..just quit
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				monitor.done();
			}
		}

	}

	@Override
	public void postWindowClose() {
		if (ResourcesPlugin.getWorkspace() != null)
			disconnectFromWorkspace();
		super.postWindowClose();
	}

	private void disconnectFromWorkspace() {

		// save the workspace
		final MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 1, Messages.ProblemSavingWorkbench, null);
		try {
			final ProgressMonitorDialog p = new ProgressMonitorDialog(null);
			IRunnableWithProgress runnable = monitor -> {
				try {
					status.merge(ResourcesPlugin.getWorkspace().save(true, monitor));
				} catch (CoreException e) {
					status.merge(e.getStatus());
				}
			};
			p.run(true, false, runnable);
		} catch (InvocationTargetException e) {
			status.merge(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1, Messages.InternalError, e.getTargetException()));
		} catch (InterruptedException e) {
			status.merge(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1, Messages.InternalError, e));
		}

		ErrorDialog.openError(null, Messages.ProblemsSavingWorkspace, null, status, IStatus.ERROR | IStatus.WARNING);

		if (!status.isOK()) {
			ResourcesPlugin.getPlugin().getLog().log(status);
		}

	}

}
