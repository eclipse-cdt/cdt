/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.launch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.build.QtBuildConfiguration;
import org.eclipse.cdt.internal.qt.core.build.QtBuildConfigurationFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;
import org.eclipse.launchbar.core.target.launch.LaunchConfigurationTargetedDelegate;
import org.eclipse.launchbar.core.target.launch.TargetedLaunch;

public class QtLocalRunLaunchConfigDelegate extends LaunchConfigurationTargetedDelegate {

	public static final String TYPE_ID = Activator.ID + ".launchConfigurationType"; //$NON-NLS-1$

	@Override
	public ITargetedLaunch getLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target)
			throws CoreException {
		// TODO sourcelocator?
		return new TargetedLaunch(configuration, mode, target, null);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		new Job("Running Qt App") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ILaunchTarget target = ((ITargetedLaunch) launch).getLaunchTarget();
					QtBuildConfiguration qtBuildConfig = getQtBuildConfiguration(configuration, mode, target, monitor);

					// get the executable
					Path buildFolder = qtBuildConfig.getBuildDirectory();
					Path exeFile;
					switch (Platform.getOS()) {
					case Platform.OS_MACOSX:
						// TODO this is mac local specific and really should be
						// in the config
						// TODO also need to pull the app name out of the pro
						// file name
						Path appFolder = buildFolder.resolve("main.app");
						Path contentsFolder = appFolder.resolve("Contents");
						Path macosFolder = contentsFolder.resolve("MacOS");
						exeFile = macosFolder.resolve("main");
						break;
					case Platform.OS_WIN32:
						Path releaseFolder = buildFolder.resolve("release");
						exeFile = releaseFolder.resolve("main.exe");
						break;
					default:
						return new Status(IStatus.ERROR, Activator.ID, "platform not supported: " + Platform.getOS());
					}

					ProcessBuilder builder = new ProcessBuilder(exeFile.toString())
							.directory(qtBuildConfig.getProject().getLocation().toFile());

					// need to add the Qt libraries to the env
					Map<String, String> env = builder.environment();
					Path libPath = qtBuildConfig.getQtInstall().getLibPath();
					switch (Platform.getOS()) {
					case Platform.OS_MACOSX:
						String libPathEnv = env.get("DYLD_LIBRARY_PATH");
						if (libPathEnv == null) {
							libPathEnv = libPath.toString();
						} else {
							libPathEnv = libPath.toString() + File.pathSeparator + libPathEnv;
						}
						env.put("DYLD_LIBRARY_PATH", libPathEnv);
						break;
					case Platform.OS_WIN32:
						String path = env.get("PATH");
						// TODO really need a bin path
						// and resolve doesn't work properly on Windows
						path = "C:/Qt/5.5/mingw492_32/bin;" + path;
						env.put("PATH", path);
						break;
					}

					Process process = builder.start();
					DebugPlugin.newProcess(launch, process, "main");
				} catch (IOException e) {
					return new Status(IStatus.ERROR, Activator.ID, "running", e);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		QtBuildConfiguration qtBuildConfig = getQtBuildConfiguration(configuration, mode, target, monitor);

		// Set it as active
		IProject project = qtBuildConfig.getProject();
		IProjectDescription desc = project.getDescription();
		desc.setActiveBuildConfig(qtBuildConfig.getBuildConfiguration().getName());
		project.setDescription(desc, monitor);

		// And build
		return superBuildForLaunch(configuration, mode, monitor);
	}

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		// 1. Extract project from configuration
		// TODO dependencies too.
		IProject project = configuration.getMappedResources()[0].getProject();
		return new IProject[] { project };
	}

	private QtBuildConfiguration getQtBuildConfiguration(ILaunchConfiguration configuration, String mode,
			ILaunchTarget target, IProgressMonitor monitor) throws CoreException {
		// Find the Qt build config
		IProject project = configuration.getMappedResources()[0].getProject();
		return QtBuildConfigurationFactory.getConfig(project, mode, target, monitor);
	}

}
