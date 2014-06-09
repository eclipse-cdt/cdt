/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.internal.debug.application.DebugAttachedExecutable;
import org.eclipse.cdt.internal.debug.application.DebugCoreFile;
import org.eclipse.cdt.internal.debug.application.DebugExecutable;
import org.eclipse.cdt.internal.debug.application.JobContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
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
		/**
		 * 
		 */
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

//	private class CWDTracker implements IWorkingDirectoryTracker {
//
//		@Override
//		public URI getWorkingDirectoryURI() {
//			return null;
//		}
//
//	}
	


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

	public class PostWindowCreateRunnable implements IRunnableWithProgress {

		@Override
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			monitor.beginTask(Messages.InitializingDebugger, 10);
			boolean attachExecutable = false;
			String executable = null;
			String corefile = null;
			String buildLog = null;
			String arguments = null;
			String[] args = Platform.getCommandLineArgs();
//			System.out.println("program args length is " + args.length);
			try {
				for (int i = 0; i < args.length; ++i) {
//									System.out.println("arg <" + i + "> is " + args[i]);
					if ("-application".equals(args[i]))
						i++; // ignore the application specifier
					else if ("-product".equals(args[i]))
						i++; // ignore the product specifier
					else if ("-b".equals(args[i])) {
						++i;
						if (i < args.length)
							buildLog = args[i];
					}
					else if ("-a".equals(args[i])) {
						attachExecutable = true;
					}
					else if ("-c".equals(args[i])) {
						++i;
						corefile = "";
						executable = "";
						if (i < args.length)
							corefile = args[i];
					}
					else if ("-e".equals(args[i])) {
						++i;
						if (i < args.length)
							executable = args[i];
						++i;
						StringBuffer argBuffer = new StringBuffer();
						// Remaining values are arguments to the executable
						if (i < args.length)
							argBuffer.append(args[i++]);
						while (i < args.length) {
							argBuffer.append(" ");
							argBuffer.append(args[i++]);
						}
						arguments = argBuffer.toString();
					}
				}
				// Verify any core file or executable path is valid.
				if (corefile != null) {
					File executableFile = new File(executable);
					File coreFile = new File(corefile);
					if (!executableFile.exists() || !coreFile.exists()) {
						final CoreFileInfo info = new CoreFileInfo("", "", ""); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
						final IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, 
								Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist, null);
						final String executablePath = executable;
						final String coreFilePath = buildLog;

						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {

								CoreFileDialog dialog = new CoreFileDialog(getWindowConfigurer().getWindow().getShell(),
										0, executablePath, coreFilePath);
								dialog.setBlockOnOpen(true);
								if (dialog.open() == IDialogConstants.OK_ID) {
									CoreFileInfo info2 = dialog.getCoreFileInfo();
									info.setHostPath(info2.getHostPath());
									info.setCoreFilePath(info2.getCoreFilePath());
								} else {
									ErrorDialog.openError(null,
											Messages.DebuggerInitializingProblem, null, errorStatus,
											IStatus.ERROR | IStatus.WARNING);
								}
							}
						});
						// Check and see if we failed above and if so, quit
						if (info.getHostPath().equals("")) {
							monitor.done();
							// throw internal exception which will be caught below
							throw new StartupException(errorStatus.getMessage());
						}
						executable = info.getHostPath();
						corefile = info.getCoreFilePath();
					}
				} else if (executable != null) {
					File executableFile = new File(executable);
					if (!executableFile.exists()) {
						final NewExecutableInfo info = new NewExecutableInfo("", "", "", ""); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
						final IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, 
								Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist, null);
						final String executablePath = executable;
						final String executableArgs = arguments;
						final String buildLogPath = buildLog;

						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {

								NewExecutableDialog dialog = new NewExecutableDialog(getWindowConfigurer().getWindow().getShell(),
										0, executablePath, buildLogPath, executableArgs);
								dialog.setBlockOnOpen(true);
								if (dialog.open() == IDialogConstants.OK_ID) {
									NewExecutableInfo info2 = dialog.getExecutableInfo();
									info.setHostPath(info2.getHostPath());
									info.setArguments(info2.getArguments());
								} else {
									ErrorDialog.openError(null,
											Messages.DebuggerInitializingProblem, null, errorStatus,
											IStatus.ERROR | IStatus.WARNING);
								}
							}
						});
						// Check and see if we failed above and if so, quit
						if (info.getHostPath().equals("")) {
							monitor.done();
							// throw internal exception which will be caught below
							throw new StartupException(errorStatus.getMessage());
						}
						executable = info.getHostPath();
						arguments = info.getArguments();
					}
				}
				monitor.worked(1);
				if (attachExecutable) {
					config = DebugAttachedExecutable.createLaunchConfig(monitor, buildLog);
				} else if (corefile != null && corefile.length() > 0) {
					config = DebugCoreFile.createLaunchConfig(monitor, buildLog, executable, corefile);
				} else if (executable != null && executable.length() > 0) {
					config = DebugExecutable.importAndCreateLaunchConfig(monitor, executable, buildLog, arguments, true);
				} else {
					// No executable specified, look for last launch
					// and offer that to the end-user.
					monitor.subTask(Messages.RestorePreviousLaunch);
					String memento = ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(new QualifiedName(STANDALONE_QUALIFIER, LAST_LAUNCH));
					if (memento != null)
						config = DebugExecutable.getLaunchManager().getLaunchConfiguration(memento);
					String oldExecutable = "";
					String oldArguments = "";
					String oldBuildLog = "";
					if (config != null) {
						oldExecutable = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, ""); //$NON-NLS-1$
						oldArguments = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
						oldBuildLog = config.getAttribute(ICDTStandaloneDebugLaunchConstants.BUILD_LOG_LOCATION, ""); //$NON-NLS-1$
					}
					final NewExecutableInfo info = new NewExecutableInfo("", "", "", ""); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
					final IStatus errorStatus = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, 
							Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist, null);
					final String executablePath = oldExecutable;
					final String executableArgs = oldArguments;
					final String buildLogPath = oldBuildLog;
					// Bring up New Executable dialog with values from
					// the last launch.
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {

							NewExecutableDialog dialog = new NewExecutableDialog(getWindowConfigurer().getWindow().getShell(),
									0, executablePath, buildLogPath, executableArgs);
							dialog.setBlockOnOpen(true);
							if (dialog.open() == IDialogConstants.OK_ID) {
								NewExecutableInfo info2 = dialog.getExecutableInfo();
								info.setHostPath(info2.getHostPath());
								info.setArguments(info2.getArguments());
								info.setBuildLog(info2.getBuildLog());
							} else {
								ErrorDialog.openError(null,
										Messages.DebuggerInitializingProblem, null, errorStatus,
										IStatus.ERROR | IStatus.WARNING);
							}
						}
					});
					// Check and see if we failed above and if so, quit
					if (info.getHostPath().equals("")) {
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
						config = DebugExecutable.importAndCreateLaunchConfig(monitor, executable, buildLog, arguments, true);
					ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
					wc.setAttribute(ICDTStandaloneDebugLaunchConstants.BUILD_LOG_LOCATION,
							buildLog);
					if (arguments != null)
						wc.setAttribute(
								ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
								arguments);
					config = wc.doSave();

					monitor.worked(7);
				}
				if (config != null) {
//					System.out.println("about to add job change listener");
					final JobContainer LaunchJobs = new JobContainer();
					Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {

						@Override
						public void scheduled(IJobChangeEvent event) {
							Job job = event.getJob();
//							System.out.println("Job name is " + job.getName());
							if (job.getName().contains(config.getName()))
								LaunchJobs.setLaunchJob(job);
						}

						@Override
						public void done(IJobChangeEvent event) {
//							System.out.println("Job " + event.getJob().getName() + " is done");
						}
					});
					monitor.subTask(Messages.LaunchingConfig);
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							DebugUITools.launch(config, ILaunchManager.DEBUG_MODE);
//							System.out.println("about to join " + LaunchJobs.getLaunchJob());
						}
					});
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
//				System.out.println("end");
			} catch (InterruptedException e) {
//				System.out.println("Interrupted exception");
				throw e; // rethrow exception
			} catch (CoreException e) {
//				System.out.println("Core Exception");
				e.printStackTrace();
			} catch (StartupException e) {
				// do nothing..just quit
			} catch (Exception e) {
//				System.out.println("Exception");
				e.printStackTrace();
			} finally {
//				System.out.println("Finally");
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
		final MultiStatus status = new MultiStatus(
				Activator.PLUGIN_ID, 1,
				Messages.ProblemSavingWorkbench, null);
		try {
			final ProgressMonitorDialog p = new ProgressMonitorDialog(
					null);
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void  run(IProgressMonitor monitor) {
					try {
						status.merge(ResourcesPlugin
								.getWorkspace().save(true, monitor));
					} catch (CoreException e) {
						status.merge(e.getStatus());
					}
				}
			};
			p.run(true, false, runnable);
		} catch (InvocationTargetException e) {
			status.merge(new Status(IStatus.ERROR,
					Activator.PLUGIN_ID, 1,
					Messages.InternalError, 
					e.getTargetException()));
		} catch (InterruptedException e) {
			status.merge(new Status(IStatus.ERROR,
					Activator.PLUGIN_ID, 1,
					Messages.InternalError, e));
		}

		ErrorDialog.openError(null,
				Messages.ProblemsSavingWorkspace, null, status,
				IStatus.ERROR | IStatus.WARNING);

		if (!status.isOK()) {
			ResourcesPlugin.getPlugin().getLog().log(status);
		}

	}	

}
