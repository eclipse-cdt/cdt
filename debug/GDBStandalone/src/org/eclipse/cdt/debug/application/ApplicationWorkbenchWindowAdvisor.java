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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuildCommandParser;
import org.eclipse.cdt.utils.elf.parser.GNUElfParser;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private static final String GCC_BUILTIN_PROVIDER_ID = "org.eclipse.cdt.managedbuilder.core.GCCBuiltinSpecsDetector"; //$NON-NLS-1$
	private static final String GCC_COMPILE_OPTIONS_PROVIDER_ID = "org.eclipse.cdt.debug.application.DwarfLanguageSettingsProvider"; //$NON-NLS-1$
	private static final String GCC_BUILD_OPTIONS_PROVIDER_ID = "org.eclipse.cdt.managedbuilder.core.GCCBuildCommandParser"; //$NON-NLS-1$ 
	private static final String STANDALONE_QUALIFIER = "org.eclipse.cdt.debug.application"; //$NON-NLS-1$
	private static final String LAST_LAUNCH = "lastLaunch"; //$NON-NLS-1$
	private ILaunchConfiguration config;

	private class JobContainer {
		private Job launchJob;
		public Job getLaunchJob() {
			return launchJob;
		}

		public void setLaunchJob(Job job) {
			this.launchJob = job;
		}
	}

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
	
	private class CompilerOptionParser implements IWorkspaceRunnable {
		
		private final IProject project;
		private final String executable;
		
		public CompilerOptionParser (IProject project, String executable) {
			this.project = project;
			this.executable = executable;
		}

		@Override
		public void run(IProgressMonitor monitor) {
			try {
				// Calculate how many source files we have to process and use that as a basis
				// for our work estimate.
				GNUElfParser binParser = new GNUElfParser();
				IBinaryFile bf = binParser
						.getBinary(new Path(executable));
				ISymbolReader reader = (ISymbolReader)bf.getAdapter(ISymbolReader.class);
				String[] sourceFiles = reader
						.getSourceFiles();
				monitor.beginTask(Messages.GetCompilerOptions, sourceFiles.length * 2 + 1);
				
				for (String sourceFile : sourceFiles) {
					IPath sourceFilePath = new Path(
							sourceFile);
					String sourceName = sourceFilePath
							.lastSegment();
					IContainer c = createFromRoot(project,
							new Path(sourceFile));
					Path sourceNamePath = new Path(
							sourceName);
					IFile source = c
							.getFile(sourceNamePath);
					if (!source.isLinked())
						source.createLink(sourceFilePath, 0,
							null);
					monitor.worked(1);
				}
				
//				// Find the GCCCompileOptions LanguageSettingsProvider for the configuration.
//				IWorkingDirectoryTracker cwdTracker = new CWDTracker();
//				ICProjectDescriptionManager projDescManager = CCorePlugin
//						.getDefault().getProjectDescriptionManager();
//				ICProjectDescription projDesc = projDescManager
//						.getProjectDescription(project,
//								false);
//				ICConfigurationDescription ccdesc = projDesc
//						.getActiveConfiguration();
//				GCCCompileOptionsParser parser = null;
//				if (ccdesc instanceof ILanguageSettingsProvidersKeeper) {
//					ILanguageSettingsProvidersKeeper keeper = (ILanguageSettingsProvidersKeeper)ccdesc;
//					List<ILanguageSettingsProvider> list = keeper.getLanguageSettingProviders();
//					for (ILanguageSettingsProvider p : list) {
//						//						System.out.println("language settings provider " + p.getId());
//						if (p.getId().equals(GCC_COMPILE_OPTIONS_PROVIDER_ID)) {
//							parser = (GCCCompileOptionsParser)p;
//						}
//					}
//				}
//				// Start up the parser and process lines generated from the .debug_macro section.
//				parser.startup(ccdesc, cwdTracker);
//				// Get compile options for each source file and process via the parser
//				// to generate LanguageSettingsEntries.
//				if (reader instanceof
//						ICompileOptionsFinder) {
//					ICompileOptionsFinder f =
//							(ICompileOptionsFinder) reader;
//					for (String fileName : sourceFiles) {
//						parser.setCurrentResourceName(fileName);
////						String cmdline = f.getCompileOptions(fileName);
////						System.out.println("Command line is " + cmdline);
//						parser.processLine(f
//								.getCompileOptions(fileName));
//						monitor.worked(1);
//					}
//					parser.shutdown(); // this will serialize the data to an xml file and create an event.
//					monitor.worked(1);
//				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			monitor.done();
		}

	};

	private class BuildOptionsParser implements IWorkspaceRunnable, IMarkerGenerator {
		
		private final IProject project;
		private final File buildLog;
		
		public BuildOptionsParser (IProject project, File buildLog) {
			this.project = project;
			this.buildLog = buildLog;
		}

		@Override
		public void run(IProgressMonitor monitor) {
			monitor.beginTask(Messages.GetBuildOptions, 10);
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(buildLog));
				// Calculate how many source files we have to process and use that as a basis
				monitor.beginTask(Messages.GetBuildOptions, 10);
				
				// Find the GCCBuildCommandParser for the configuration.
				ICProjectDescriptionManager projDescManager = CCorePlugin
						.getDefault().getProjectDescriptionManager();
				ICProjectDescription projDesc = projDescManager
						.getProjectDescription(project,
								false);
				ICConfigurationDescription ccdesc = projDesc
						.getActiveConfiguration();
				GCCBuildCommandParser parser = null;
				if (ccdesc instanceof ILanguageSettingsProvidersKeeper) {
					ILanguageSettingsProvidersKeeper keeper = (ILanguageSettingsProvidersKeeper)ccdesc;
					List<ILanguageSettingsProvider> list = keeper.getLanguageSettingProviders();
					for (ILanguageSettingsProvider p : list) {
						//						System.out.println("language settings provider " + p.getId());
						if (p.getId().equals(GCC_BUILD_OPTIONS_PROVIDER_ID)) {
							parser = (GCCBuildCommandParser)p;
						}
					}
				}
				ErrorParserManager epm = new ErrorParserManager(project, this, new String[]{"org.eclipse.cdt.core.CWDLocator"}); //$NON-NLS-1$
				// Start up the parser and process lines generated from the .debug_macro section.
				parser.startup(ccdesc, epm);
				monitor.beginTask(Messages.GetBuildOptions, 10);
				String line = br.readLine();
				while (line != null) {
					parser.processLine(line);
					line = br.readLine();
				}
				parser.shutdown();
				if (br != null)
					br.close();
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			monitor.done();
		}

		@Override
		public void addMarker(IResource file, int lineNumber, String errorDesc,
				int severity, String errorVar) {
			// do nothing
		}

		@Override
		public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
			// do nothing
		}

	};

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
							final NewExecutableInfo info = new NewExecutableInfo("", "", ""); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
							final IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, 
									Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist, null);
							final String executablePath = executable;
							final String executableArgs = arguments;

							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {

									NewExecutableDialog dialog = new NewExecutableDialog(getWindowConfigurer().getWindow().getShell(),
											0, executablePath, executableArgs);
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
					config = importAndCreateLaunchConfig(monitor, executable, buildLog, arguments);
				} else {
//					System.out.println("restore previous launch");
					monitor.subTask(Messages.RestorePreviousLaunch);
					String defaultProjectName = "Executables"; //$NON-NLS-1$
					ICProject cProject = CoreModel.getDefault().getCModel()
							.getCProject(defaultProjectName);
					String memento = cProject.getProject().getPersistentProperty(new QualifiedName(STANDALONE_QUALIFIER, LAST_LAUNCH));
					if (memento != null)
						config = getLaunchManager().getLaunchConfiguration(memento);
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

		/**
		 * Import given executable into the Executables project then create a launch configuration.
		 * 
		 * @param monitor
		 * @param executable
		 * @param buildLog
		 * @param arguments
		 * @throws CoreException
		 * @throws InterruptedException
		 */
		private ILaunchConfiguration importAndCreateLaunchConfig(IProgressMonitor monitor,
				String executable, String buildLog, String arguments)
				throws CoreException, InterruptedException {
			ILaunchConfiguration config = null;
			File executableFile = new File(executable);
			String defaultProjectName = "Executables"; //$NON-NLS-1$
			ICProject cProject = CoreModel.getDefault().getCModel()
					.getCProject(defaultProjectName);
			// if a valid executable is specified, remove any executables already loaded in workspace
			if (cProject.exists() && executableFile.exists()) {
				monitor.subTask(Messages.RemoveOldExecutable);
				IProject proj = cProject.getProject();
				Collection<Executable> elist = ExecutablesManager.getExecutablesManager().getExecutablesForProject(proj);
				Executable[] executables = new Executable[elist.size()];
				elist.toArray(executables);
				@SuppressWarnings("unused")
				IStatus rc = ExecutablesManager.getExecutablesManager().removeExecutables(executables, new NullProgressMonitor());
				// Remove all old members of the Executables project from the last run
				IResource[] resources = proj.members();
				for (IResource resource : resources) {
					resource.delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT|IResource.FORCE, new NullProgressMonitor());
				}
				monitor.worked(1);
				// Find last launch if one exists
				String memento = proj.getPersistentProperty(new QualifiedName(STANDALONE_QUALIFIER, LAST_LAUNCH));
//						System.out.println("memento is " + memento);
				if (memento != null) {
					ILaunchConfiguration lastConfiguration = getLaunchManager().getLaunchConfiguration(memento);
					try {
						lastConfiguration.getType();
						if (lastConfiguration.exists())
							lastConfiguration.delete();
					} catch (CoreException e) {
						// do nothing
					}
				}
				monitor.worked(1);
			}
			final String[] fileNames = { executable };
			Job importJob = new Job(Messages.ExecutablesView_ImportExecutables) {

				@Override
				public IStatus run(IProgressMonitor monitor) {
					ExecutablesManager.getExecutablesManager().importExecutables(
							fileNames, monitor);
					return Status.OK_STATUS;
				}
			};
			monitor.subTask(Messages.ImportExecutable);
			importJob.schedule();
			importJob.join();
			monitor.worked(3);
			if (importJob.getResult() == Status.OK_STATUS) {
//						System.out.println("importJob successful");
				//				 See if the default project exists
				cProject = CoreModel.getDefault().getCModel()
						.getCProject(defaultProjectName);
				if (cProject.exists()) {
					File buildLogFile = null;
					final IProject project = cProject.getProject();

					final ICProjectDescriptionManager projDescManager = CCorePlugin
							.getDefault().getProjectDescriptionManager();

					ICProjectDescription projectDescription = projDescManager
							.getProjectDescription(project,
									ICProjectDescriptionManager.GET_WRITABLE);

					monitor.subTask(Messages.SetLanguageProviders);
					final ICConfigurationDescription ccd = projectDescription
							.getActiveConfiguration();
					String[] langProviderIds = ((ILanguageSettingsProvidersKeeper) ccd)
							.getDefaultLanguageSettingsProvidersIds();
					boolean found = false;
					for (int i = 0; i < langProviderIds.length; ++i) {
						if (langProviderIds[i].equals(GCC_BUILTIN_PROVIDER_ID)) {
							found = true;
							break;
						}
					}
					// Look for the GCC builtin LanguageSettingsProvider id.  If it isn't already
					// there, add it.
					if (!found) {
						langProviderIds = Arrays.copyOf(langProviderIds,
								langProviderIds.length + 1);
						langProviderIds[langProviderIds.length - 1] = GCC_BUILTIN_PROVIDER_ID;
					}
					found = false;
					for (int i = 0; i < langProviderIds.length; ++i) {
						if (langProviderIds[i].equals(GCC_COMPILE_OPTIONS_PROVIDER_ID)) {
							found = true;
							break;
						}
					}
					// Look for our macro parser provider id.  If it isn't added already, do so now.
					if (!found) {
						langProviderIds = Arrays.copyOf(langProviderIds,
								langProviderIds.length + 1);
						langProviderIds[langProviderIds.length - 1] = GCC_COMPILE_OPTIONS_PROVIDER_ID;
					}

					if (buildLog != null) {
						File f = new File(buildLog);
						if (f.exists()) {
							buildLogFile = f;
							found = false;
							for (int i = 0; i < langProviderIds.length; ++i) {
								if (langProviderIds[i].equals(GCC_BUILD_OPTIONS_PROVIDER_ID)) {
									found = true;
									break;
								}
							}
							// Look for our macro parser provider id.  If it isn't added already, do so now.
							if (!found) {
								langProviderIds = Arrays.copyOf(langProviderIds,
										langProviderIds.length + 1);
								langProviderIds[langProviderIds.length - 1] = GCC_BUILD_OPTIONS_PROVIDER_ID;
							}
						}
					}

//							System.out.println("creating language settings providers");
					// Create all the LanguageSettingsProviders
					List<ILanguageSettingsProvider> providers = LanguageSettingsManager
							.createLanguageSettingsProviders(langProviderIds);

					// Update the ids and providers for the configuration.
					((ILanguageSettingsProvidersKeeper) ccd)
					.setDefaultLanguageSettingsProvidersIds(langProviderIds);
					((ILanguageSettingsProvidersKeeper) ccd)
					.setLanguageSettingProviders(providers);
					
					monitor.worked(1);

//							System.out.println("before setProjectDescription");
					
					// Update the project description.
					projDescManager.setProjectDescription(project,
							projectDescription);
					
//							System.out.println("after setProjectDescription");

					monitor.worked(1);
					
					if (!("".equals(executable)))
						// We need to parse the macro compile options if they exist.  We need to lock the
						// workspace when we do this so we don't have multiple copies of our GCCCompilerOptionsParser
						// LanguageSettingsProvider and we end up filling in the wrong one.
						project.getWorkspace().run(new CompilerOptionParser(project, executable), 
								ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());

					if (buildLogFile != null)	
						// We need to parse the build log to get compile options.  We need to lock the
						// workspace when we do this so we don't have multiple copies of GCCBuildOptionsParser
						// LanguageSettingsProvider and we end up filling in the wrong one.
						project.getWorkspace().run(new BuildOptionsParser(project, buildLogFile), 
								ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
				}
				
//						System.out.println("about to close all editors");
				IWorkbench workbench = PlatformUI.getWorkbench();
				if (workbench != null) {
					final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
					if (workbenchWindow != null) {
						final IWorkbenchPage activePage = workbenchWindow.getActivePage();
						if (activePage != null)
							activePage.closeAllEditors(false);
					}
				}
//						System.out.println("about to create launch configuration");
				config = createConfiguration(executable, arguments, true);
				String memento = config.getMemento();
				cProject.getProject().setPersistentProperty(new QualifiedName(STANDALONE_QUALIFIER, LAST_LAUNCH), memento);
				monitor.worked(1);
			} else {
				System.out.println("Import job failed");
				return null;
			}
			return config;
		}
		
	}
	private IContainer createFromRoot(IProject exeProject, IPath path)
			throws CoreException {
		int segmentCount = path.segmentCount() - 1;
		IContainer currentFolder = exeProject;

		for (int i = 0; i < segmentCount; i++) {
			currentFolder = currentFolder.getFolder(new Path(path.segment(i)));
			if (!currentFolder.exists()) {
				((IFolder) currentFolder).create(IResource.VIRTUAL
						| IResource.DERIVED, true, new NullProgressMonitor());
			}
		}

		return currentFolder;
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


	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(
				"org.eclipse.cdt.launch.applicationLaunchType"); //$NON-NLS-1$
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected ILaunchConfiguration createConfiguration(String bin,
			String arguments, boolean save) {
//		System.out.println("creating launch configuration");
		ILaunchConfiguration config = null;
		try {
			String projectName = bin;
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(
					null,
					getLaunchManager().generateLaunchConfigurationName(bin));

			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
					projectName);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					"Executables"); //$NON-NLS-1$
			wc.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					(String) null);
			if (arguments != null)
				wc.setAttribute(
						ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
						arguments);
			if (save) {
				config = wc.doSave();
			} else {
				config = wc;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return config;
	}

}
