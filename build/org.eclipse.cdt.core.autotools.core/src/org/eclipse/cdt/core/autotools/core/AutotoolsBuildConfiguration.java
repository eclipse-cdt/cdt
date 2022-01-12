/*******************************************************************************
 * Copyright (c) 2017 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.autotools.core;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.autotools.core.internal.Activator;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ICModelMarker;
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

	public AutotoolsBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			String launchMode) {
		super(config, name, toolChain, launchMode);
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {

		IProject project = getProject();

		execute(Arrays.asList(new String[] { "autoreconf", "--install" }), project.getLocation(), console, monitor); //$NON-NLS-1$ //$NON-NLS-2$
		executeRemote(Arrays.asList(new String[] { "./configure" }), project.getLocation(), console, monitor); //$NON-NLS-1$
		executeRemote(Arrays.asList(new String[] { "make" }), project.getLocation(), console, monitor); //$NON-NLS-1$

		return new IProject[] { project };
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		executeRemote(Arrays.asList(new String[] { "make", "clean" }), getProject().getLocation(), console, monitor); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void execute(List<String> command, IPath dir, IConsole console, IProgressMonitor monitor)
			throws CoreException {
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
			throw new CoreException(Activator.errorStatus("Error executing: " + String.join(" ", command), e)); //$NON-NLS-1$ //$NON-NLS-2$
		}

		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	protected void executeRemote(List<String> command, IPath dir, IConsole console, IProgressMonitor monitor)
			throws CoreException {

		IProject project = getProject();

		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			String cmd = command.get(0);

			if (cmd.startsWith(".")) { //$NON-NLS-1$
				Path cmdPath = Paths.get(dir.toString(), cmd);
				if (cmdPath.toFile().exists()) {
					command.set(0, cmdPath.toAbsolutePath().toString());
				}
			}

			outStream.write("Building in: " + buildDir.toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());

				IEnvironmentVariable[] env = new IEnvironmentVariable[0];

				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(
						getBuildDirectory().toString());

				Process p = startBuildProcess(command, env, workingDir, console, monitor);
				if (p == null) {
					console.getErrorStream().write(String.format("Error executing: {0}", String.join(" ", command))); //$NON-NLS-1$ //$NON-NLS-2$
					throw new CoreException(
							Activator.errorStatus("Error executing: " + String.join(" ", command), null)); //$NON-NLS-1$ //$NON-NLS-2$
				}

				watchProcess(p, new IConsoleParser[] { epm });
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus("Error executing: " + String.join(" ", command), e)); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

}
