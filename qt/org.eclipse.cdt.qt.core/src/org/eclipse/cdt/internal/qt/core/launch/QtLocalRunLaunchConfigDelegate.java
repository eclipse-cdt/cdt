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

import org.eclipse.cdt.internal.qt.core.QtPlugin;
import org.eclipse.cdt.internal.qt.core.build.QtBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

public class QtLocalRunLaunchConfigDelegate extends LaunchConfigurationDelegate {

	public static final String TYPE_ID = QtPlugin.ID + ".launchConfigurationType"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		new Job("Running Qt App") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					QtBuildConfiguration qtBuildConfig = getQtBuildConfiguration(configuration, mode, monitor);

					// get the executable
					IFolder buildFolder = qtBuildConfig.getBuildFolder();
					IFile exeFile;
					switch (Platform.getOS()) {
					case Platform.OS_MACOSX:
						// TODO this is mac local specific and really should be in the config
						// TODO also need to pull the app name out of the pro file name
						IFolder appFolder = buildFolder.getFolder("main.app");
						IFolder contentsFolder = appFolder.getFolder("Contents");
						IFolder macosFolder = contentsFolder.getFolder("MacOS");
						exeFile = macosFolder.getFile("main");
						break;
					case Platform.OS_WIN32:
						IFolder releaseFolder = buildFolder.getFolder("release");
						exeFile = releaseFolder.getFile("main.exe");
						break;
					default:
						return new Status(IStatus.ERROR, QtPlugin.ID, "platform not supported: " + Platform.getOS());
					}

					ProcessBuilder builder = new ProcessBuilder(exeFile.getLocation().toFile().getAbsolutePath())
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
					return new Status(IStatus.ERROR, QtPlugin.ID, "running", e);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		QtBuildConfiguration qtBuildConfig = getQtBuildConfiguration(configuration, mode, monitor);

		// Set it as active
		IProject project = qtBuildConfig.getProject();
		IProjectDescription desc = project.getDescription();
		desc.setActiveBuildConfig(qtBuildConfig.getBuildConfiguration().getName());
		project.setDescription(desc, monitor);

		// And build
		return super.buildForLaunch(configuration, mode, monitor);
	}

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		// 1. Extract project from configuration
		// TODO dependencies too.
		IProject project = configuration.getMappedResources()[0].getProject();
		return new IProject[] { project };
	}

	private QtBuildConfiguration getQtBuildConfiguration(ILaunchConfiguration configuration, String mode,
			IProgressMonitor monitor) throws CoreException {
		// Find the Qt build config
		IProject project = configuration.getMappedResources()[0].getProject();
		String os = Platform.getOS();
		String arch = Platform.getOSArch();
		return QtBuildConfiguration.getConfig(project, os, arch, mode, monitor);
	}

}
