/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.build.IConsoleService;
import org.eclipse.cdt.internal.qt.core.QtPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class QtBuilder extends IncrementalProjectBuilder {

	public static final String ID = QtPlugin.ID + ".qtBuilder"; //$NON-NLS-1$

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			IConsoleService console = QtPlugin.getService(IConsoleService.class);
			QtBuildConfiguration qtConfig = getBuildConfig().getAdapter(QtBuildConfiguration.class);

			IFolder buildFolder = qtConfig.getBuildFolder();
			createFolder(buildFolder, monitor);

			IFile makeFile = buildFolder.getFile("Makefile"); //$NON-NLS-1$
			if (!makeFile.exists()) {
				// Need to run qmake
				List<String> command = new ArrayList<>();
				command.add(qtConfig.getQmakeCommand());

				String config = qtConfig.getQmakeConfig();
				if (config != null) {
					command.add(config);
				}

				IFile projectFile = qtConfig.getProject().getFile("main.pro");
				command.add(projectFile.getLocation().toOSString());

				Process process = new ProcessBuilder(command).directory(new File(buildFolder.getLocationURI())).start();
				StringBuffer msg = new StringBuffer();
				for (String arg : command) {
					msg.append(arg).append(' ');
				}
				msg.append('\n');
				console.writeOutput(msg.toString());
				console.monitor(process, null, buildFolder);
			}

			// run make
			Process process = new ProcessBuilder("make").directory(new File(buildFolder.getLocationURI())).start(); //$NON-NLS-1$
			console.writeOutput("make\n"); //$NON-NLS-1$
			console.monitor(process, null, buildFolder);

			buildFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, QtPlugin.ID, "Building " + project.getName(), e));
		}
	}

	private void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
		IContainer parent = folder.getParent();
		if (!parent.exists()) {
			createFolder((IFolder) parent, monitor);
		}
		if (!folder.exists()) {
			folder.create(IResource.FORCE | IResource.DERIVED, true, monitor);
		}
	}
}
