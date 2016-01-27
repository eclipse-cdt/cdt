/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.build;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.build.core.IConsoleService;
import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.Messages;
import org.eclipse.cdt.qt.core.QtBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class QtBuilder extends IncrementalProjectBuilder {

	public static final String ID = Activator.ID + ".qtBuilder"; //$NON-NLS-1$

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			IConsoleService console = Activator.getService(IConsoleService.class);
			QtBuildConfiguration qtConfig = getBuildConfig().getAdapter(QtBuildConfiguration.class);
			if (qtConfig == null) {
				// Qt hasn't been configured yet print a message and bale
				console.writeError(Messages.QtBuilder_0);
				return null;
			}
			IToolChain toolChain = qtConfig.getToolChain();

			Path buildDir = qtConfig.getBuildDirectory();
			if (!buildDir.resolve("Makefile").toFile().exists()) { //$NON-NLS-1$
				// Need to run qmake
				List<String> command = new ArrayList<>();
				command.add(qtConfig.getQmakeCommand());

				String config = qtConfig.getQmakeConfig();
				if (config != null) {
					command.add(config);
				}

				IFile projectFile = qtConfig.getProject().getFile("main.pro"); //$NON-NLS-1$
				command.add(projectFile.getLocation().toOSString());

				ProcessBuilder processBuilder = new ProcessBuilder(command).directory(buildDir.toFile());
				toolChain.setEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
				StringBuffer msg = new StringBuffer();
				for (String arg : command) {
					msg.append(arg).append(' ');
				}
				msg.append('\n');
				console.writeOutput(msg.toString());
				console.monitor(process, null, buildDir);
			}

			// run make
			// TODO obviously hardcoding here
			boolean isWin = Platform.getOS().equals(Platform.OS_WIN32);
			String make = isWin ? "C:/Qt/Tools/mingw492_32/bin/mingw32-make" : "make"; //$NON-NLS-1$ //$NON-NLS-2$
			ProcessBuilder procBuilder = new ProcessBuilder(make).directory(buildDir.toFile());
			if (isWin) {
				// Need to put the toolchain into env
				Map<String, String> env = procBuilder.environment();
				String path = env.get("PATH"); //$NON-NLS-1$
				path = "C:/Qt/Tools/mingw492_32/bin;" + path; //$NON-NLS-1$
				env.put("PATH", path); //$NON-NLS-1$
			}
			toolChain.setEnvironment(procBuilder.environment());
			Process process = procBuilder.start();
			console.writeOutput("make\n"); //$NON-NLS-1$
			console.monitor(process, null, buildDir);

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, "Building " + project.getName(), e)); //$NON-NLS-1$
		}
	}

}
