/*******************************************************************************
 * Copyright (c) 2017 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.autotools.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.autotools.core.internal.Activator;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

public class AutotoolsBuildConfiguration extends CBuildConfiguration {

	public static final String AUTOTOOLS_GENERATOR = "autotools.generator"; //$NON-NLS-1$
	public static final String AUTOTOOLS_ARGUMENTS = "autotools.arguments"; //$NON-NLS-1$
	public static final String BUILD_COMMAND = "autotools.command.build"; //$NON-NLS-1$
	public static final String CLEAN_COMMAND = "autotools.command.clean"; //$NON-NLS-1$

	public AutotoolsBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
	}

	public AutotoolsBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		super(config, name, toolChain, "run"); // TODO: why "run" //$NON-NLS-1$
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		
		IProject project = getProject();
		
		execute(Arrays.asList(new String[] { "autoreconf", "--install" }), project.getLocation(), console, monitor); //$NON-NLS-1$ //$NON-NLS-2$
		execute(Arrays.asList(new String[] { "./configure" }), project.getLocation(), console, monitor); //$NON-NLS-1$
		execute(Arrays.asList(new String[] { "make" }), project.getLocation(), console, monitor); //$NON-NLS-1$
		
		return new IProject[] { project };
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		execute(Arrays.asList(new String[] { "make", "clean" }), getProject().getLocation(), console, monitor); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void execute(List<String> command, IPath dir, IConsole console, IProgressMonitor monitor) throws CoreException {
		String cmd = command.get(0);

		if (Platform.getOS().equals(Platform.OS_WIN32) && !(cmd.endsWith(".exe") && !cmd.endsWith(".bat"))) { //$NON-NLS-1$ //$NON-NLS-2$
			// Maybe a shell script, see if we can launch it in sh
			// TODO this probably should be generalized in CBuildConfiguration
			Path shPath = findCommand("sh"); //$NON-NLS-1$
			if (shPath != null) {
				List<String> shCommand = new ArrayList<>();
				shCommand.add(shPath.toString());
				shCommand.add("-c"); //$NON-NLS-1$
				shCommand.add("\"" + String.join(" ", command) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				command = shCommand;
			}
		} else {
			Path cmdPath = findCommand(cmd);
			if (cmdPath != null) {
				cmd = cmdPath.toString();
				command.set(0, cmd);
			}
		}

		ProcessBuilder builder = new ProcessBuilder(command).directory(dir.toFile());
		setBuildEnvironment(builder.environment());

		try {
			// TODO Error parsers
			Process process = builder.start();
			watchProcess(process, console);
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus("Error executing: " + String.join(" ", command), e)); //$NON-NLS-2$
		}

		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

}
