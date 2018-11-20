/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat, Inc.
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
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.debug.application.Messages;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class DebugAttachedExecutable {

	private static final String GCC_BUILTIN_PROVIDER_ID = "org.eclipse.cdt.managedbuilder.core.GCCBuiltinSpecsDetector"; //$NON-NLS-1$
	private static final String GCC_COMPILE_OPTIONS_PROVIDER_ID = "org.eclipse.cdt.debug.application.DwarfLanguageSettingsProvider"; //$NON-NLS-1$
	private static final String GCC_BUILD_OPTIONS_PROVIDER_ID = "org.eclipse.cdt.managedbuilder.core.GCCBuildCommandParser"; //$NON-NLS-1$
	private static final String DEBUG_PROJECT_ID = "org.eclipse.cdt.debug"; //$NON-NLS-1$

	public DebugAttachedExecutable() {
	}

	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	// Create a new project that doesn't already exist.  Use the base project name and add
	// a numerical suffix as needed.
	private static IProject createCProjectForExecutable(String projectName)
			throws OperationCanceledException, CoreException {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject newProjectHandle = workspace.getRoot().getProject(projectName);

		int projectSuffix = 2;
		while (newProjectHandle.exists()) {
			newProjectHandle = workspace.getRoot().getProject(projectName + projectSuffix);
			projectSuffix++;
		}

		IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocation(null);

		IProject newProject = CCorePlugin.getDefault().createCProject(description, newProjectHandle, null,
				DEBUG_PROJECT_ID);

		return newProject;
	}

	/**
	 * Import given executable into the Executables project then create a launch configuration.
	 *
	 * @param monitor
	 * @param buildLog
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public static ILaunchConfiguration createLaunchConfig(IProgressMonitor monitor, String buildLog)
			throws CoreException, InterruptedException {
		return createLaunchConfig(monitor, buildLog, null);
	}

	/**
	 * Import given executable into the Executables project then create a launch configuration.
	 *
	 * @param monitor
	 * @param buildLog
	 * @param pid
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public static ILaunchConfiguration createLaunchConfig(IProgressMonitor monitor, String buildLog, String pid)
			throws CoreException, InterruptedException {
		ILaunchConfiguration config = null;
		String defaultProjectName = "Executables"; //$NON-NLS-1$

		// Create a new Executablesnn project
		IProject project = createCProjectForExecutable(defaultProjectName);

		monitor.worked(3);
		File buildLogFile = null;

		final ICProjectDescriptionManager projDescManager = CCorePlugin.getDefault().getProjectDescriptionManager();

		ICProjectDescription projectDescription = projDescManager.getProjectDescription(project,
				ICProjectDescriptionManager.GET_WRITABLE);

		monitor.subTask(Messages.SetLanguageProviders);
		final ICConfigurationDescription ccd = projectDescription.getActiveConfiguration();
		String[] langProviderIds = ((ILanguageSettingsProvidersKeeper) ccd).getDefaultLanguageSettingsProvidersIds();
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

		if (buildLogFile != null)
			// We need to parse the build log to get compile options.  We need to lock the
			// workspace when we do this so we don't have multiple copies of GCCBuildOptionsParser
			// LanguageSettingsProvider and we end up filling in the wrong one.
			project.getWorkspace().run(new BuildOptionsParser(project, buildLogFile),
					ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());

		//					System.out.println("about to close all editors");
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
			if (workbenchWindow != null) {
				final IWorkbenchPage activePage = workbenchWindow.getActivePage();
				if (activePage != null)
					activePage.closeAllEditors(false);
			}
		}

		config = createConfiguration(pid, true);
		monitor.worked(1);
		return config;
	}

	protected static ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_ATTACH);
	}

	protected static ILaunchConfiguration createConfiguration(boolean save) {
		return createConfiguration(null, save);
	}

	protected static ILaunchConfiguration createConfiguration(String pid, boolean save) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
					getLaunchManager().generateLaunchConfigurationName("CDT_DBG_ATTACH")); //$NON-NLS-1$

			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "Executables"); //$NON-NLS-1$
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);

			if (pid != null) {
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, Integer.valueOf(pid));
			}

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
