/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.debug.application;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.debug.application.ICDTStandaloneDebugLaunchConstants;
import org.eclipse.cdt.debug.application.Messages;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class DebugExecutable {

	private static final String GCC_BUILTIN_PROVIDER_ID = "org.eclipse.cdt.managedbuilder.core.GCCBuiltinSpecsDetector"; //$NON-NLS-1$
	private static final String GCC_COMPILE_OPTIONS_PROVIDER_ID = "org.eclipse.cdt.debug.application.DwarfLanguageSettingsProvider"; //$NON-NLS-1$
	private static final String GCC_BUILD_OPTIONS_PROVIDER_ID = "org.eclipse.cdt.managedbuilder.core.GCCBuildCommandParser"; //$NON-NLS-1$
	private static final String STANDALONE_QUALIFIER = "org.eclipse.cdt.debug.application"; //$NON-NLS-1$
	private static final String LAST_LAUNCH = "lastLaunch"; //$NON-NLS-1$

	public DebugExecutable() {
	}

	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
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
	public static ILaunchConfiguration importAndCreateLaunchConfig(IProgressMonitor monitor, String executable,
			String buildLog, String arguments, boolean startup) throws CoreException, InterruptedException {
		ILaunchConfiguration config = null;
		File executableFile = new File(executable);
		String defaultProjectName = "Executables"; //$NON-NLS-1$
		ICProject cProject = CoreModel.getDefault().getCModel().getCProject(defaultProjectName);
		// if a valid executable is specified, remove any executables already loaded in workspace
		if (startup && cProject.exists() && executableFile.exists()) {
			monitor.subTask(Messages.RemoveOldExecutable);
			IProject proj = cProject.getProject();
			Collection<Executable> elist = ExecutablesManager.getExecutablesManager().getExecutablesForProject(proj);
			Executable[] executables = new Executable[elist.size()];
			elist.toArray(executables);
			@SuppressWarnings("unused")
			IStatus rc = ExecutablesManager.getExecutablesManager().removeExecutables(executables,
					new NullProgressMonitor());
			// Remove all old members of the Executables project from the last run
			IResource[] resources = proj.members();
			for (IResource resource : resources) {
				resource.delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT | IResource.FORCE, new NullProgressMonitor());
			}

			monitor.worked(1);
			// Find last launch if one exists
			String memento = ResourcesPlugin.getWorkspace().getRoot()
					.getPersistentProperty(new QualifiedName(STANDALONE_QUALIFIER, LAST_LAUNCH));
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

			// Delete all launch configurations that specify the project we are about to delete
			ILaunchConfiguration lconfigs[] = getLaunchManager().getLaunchConfigurations();
			for (ILaunchConfiguration lconfig : lconfigs) {
				if (lconfig.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(proj.getName())) //$NON-NLS-1$
					lconfig.delete();
			}

			// Delete project because we have deleted .cproject and settings files
			// by this point so just create a new Executables C project to use for
			// importing the new executable.
			proj.delete(true, new NullProgressMonitor());
			monitor.worked(1);
		}
		final String[] fileNames = { executable };
		Job importJob = new Job(Messages.ExecutablesView_ImportExecutables) {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				ExecutablesManager.getExecutablesManager().importExecutables(fileNames, monitor);
				return Status.OK_STATUS;
			}
		};
		monitor.subTask(Messages.ImportExecutable);
		importJob.schedule();
		importJob.join();
		monitor.worked(3);
		if (importJob.getResult() == Status.OK_STATUS) {
			//				 See if the default project exists
			Collection<Executable> executables = ExecutablesManager.getExecutablesManager().getExecutables();
			for (Executable exec : executables) {
				if (exec.getName().contains(executableFile.getName()))
					cProject = CoreModel.getDefault().getCModel().getCProject(exec.getProject().getName());
			}

			if (cProject.exists()) {
				File buildLogFile = null;
				final IProject project = cProject.getProject();

				final ICProjectDescriptionManager projDescManager = CCorePlugin.getDefault()
						.getProjectDescriptionManager();

				ICProjectDescription projectDescription = projDescManager.getProjectDescription(project,
						ICProjectDescriptionManager.GET_WRITABLE);

				monitor.subTask(Messages.SetLanguageProviders);
				final ICConfigurationDescription ccd = projectDescription.getActiveConfiguration();
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
					langProviderIds = Arrays.copyOf(langProviderIds, langProviderIds.length + 1);
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
					langProviderIds = Arrays.copyOf(langProviderIds, langProviderIds.length + 1);
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
							langProviderIds = Arrays.copyOf(langProviderIds, langProviderIds.length + 1);
							langProviderIds[langProviderIds.length - 1] = GCC_BUILD_OPTIONS_PROVIDER_ID;
						}
					}
				}

				// Create all the LanguageSettingsProviders
				List<ILanguageSettingsProvider> providers = LanguageSettingsManager
						.createLanguageSettingsProviders(langProviderIds);

				// Update the providers for the configuration.
				((ILanguageSettingsProvidersKeeper) ccd).setLanguageSettingProviders(providers);

				monitor.worked(1);

				// Update the project description.
				projDescManager.setProjectDescription(project, projectDescription);

				// Serialize the language settings for the project now in case we don't run a
				// language settings provider which will do this in shutdown.
				ICProjectDescription projDescReadOnly = projDescManager.getProjectDescription(project, false);
				LanguageSettingsManager.serializeLanguageSettings(projDescReadOnly);

				monitor.worked(1);

				if (!("".equals(executable))) //$NON-NLS-1$
					// We need to parse the macro compile options if they exist.  We need to lock the
					// workspace when we do this so we don't have multiple copies of our GCCCompilerOptionsParser
					// LanguageSettingsProvider and we end up filling in the wrong one.
					project.getWorkspace().run(new CompilerOptionParser(project, executable),
							ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE,
							new NullProgressMonitor());

				if (buildLogFile != null)
					// We need to parse the build log to get compile options.  We need to lock the
					// workspace when we do this so we don't have multiple copies of GCCBuildOptionsParser
					// LanguageSettingsProvider and we end up filling in the wrong one.
					project.getWorkspace().run(new BuildOptionsParser(project, buildLogFile),
							ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE,
							new NullProgressMonitor());
			}

			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
				final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
				if (workbenchWindow != null) {
					final IWorkbenchPage activePage = workbenchWindow.getActivePage();
					if (activePage != null)
						activePage.closeAllEditors(false);
				}
			}

			config = createConfiguration(executable, arguments, buildLog, true);
			// If we are starting up the debugger, save the executable as the default executable to use
			if (startup) {
				String memento = config.getMemento();
				ResourcesPlugin.getWorkspace().getRoot()
						.setPersistentProperty(new QualifiedName(STANDALONE_QUALIFIER, LAST_LAUNCH), memento);
			}
			monitor.worked(1);
		} else {
			System.out.println("Import job failed"); //$NON-NLS-1$
			return null;
		}
		return config;
	}

	protected static ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP);
	}

	protected static ILaunchConfiguration createConfiguration(String bin, String arguments, String buildLog,
			boolean save) {
		ILaunchConfiguration config = null;
		try {
			String progName = bin;
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
					getLaunchManager().generateLaunchConfigurationName(bin));

			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, progName);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "Executables"); //$NON-NLS-1$
			wc.setAttribute(ICDTStandaloneDebugLaunchConstants.BUILD_LOG_LOCATION, buildLog);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);

			if (arguments != null) {
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, arguments);
			}

			// Use the PWD as the working directory for the application being launched
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, System.getProperty("user.dir")); //$NON-NLS-1$

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
