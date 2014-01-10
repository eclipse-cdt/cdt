/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
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
			String executable = "";
			String buildLog = null;
			String arguments = null;
			String[] args = Platform.getCommandLineArgs();
//			System.out.println("program args length is " + args.length);
			try {
				for (int i = 0; i < args.length; ++i) {
					//				System.out.println("arg <" + i + "> is " + args[i]);
					if ("-application".equals(args[i]))
						i++; // ignore the application specifier
					else if ("-b".equals(args[i])) {
						++i;
						if (i < args.length)
							buildLog = args[i];
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
				}
				monitor.worked(1);
				if (executable.length() > 0) {
					config = DebugExecutable.importAndCreateLaunchConfig(monitor, executable, buildLog, arguments);
				} else {
//					System.out.println("restore previous launch");
					monitor.subTask(Messages.RestorePreviousLaunch);
					String defaultProjectName = "Executables"; //$NON-NLS-1$
					ICProject cProject = CoreModel.getDefault().getCModel()
							.getCProject(defaultProjectName);
					String memento = cProject.getProject().getPersistentProperty(new QualifiedName(STANDALONE_QUALIFIER, LAST_LAUNCH));
					if (memento != null)
						config = DebugExecutable.getLaunchManager().getLaunchConfiguration(memento);
					if (config == null) {
						System.out.println(Messages.LaunchMissingError);
					}
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
		super.postWindowClose();
		if (ResourcesPlugin.getWorkspace() != null)
			disconnectFromWorkspace();
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
