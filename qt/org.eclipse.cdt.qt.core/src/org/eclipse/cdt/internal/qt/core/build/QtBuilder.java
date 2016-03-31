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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.build.BuildCommandRunner;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.Messages;
import org.eclipse.cdt.qt.core.IQtBuildConfiguration;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class QtBuilder extends ACBuilder {

	public static final String ID = Activator.ID + ".qtBuilder"; //$NON-NLS-1$

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false,  IResource.DEPTH_INFINITE);

			IConsole console = CCorePlugin.getDefault().getConsole();
			console.start(project);
			ConsoleOutputStream errStream = console.getErrorStream();
			ConsoleOutputStream outStream = console.getOutputStream();

			ICBuildConfiguration cconfig = getBuildConfig().getAdapter(ICBuildConfiguration.class);
			IQtBuildConfiguration qtConfig = cconfig.getAdapter(QtBuildConfiguration.class);
			if (qtConfig == null) {
				// Qt hasn't been configured yet print a message and bale
				errStream.write(Messages.QtBuilder_0);
				return null;
			}

			Path makeCommand = getMakeCommand(getBuildConfig());
			if (makeCommand == null) {
				errStream.write("'make' not found.\n");
				return null;
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, qtConfig.getBuildDirectory().toUri(), this,
					qtConfig.getToolChain().getErrorParserIds())) {
				BuildCommandRunner runner = new BuildCommandRunner(project, console, epm);

				Path buildDir = qtConfig.getBuildDirectory();
				if (!buildDir.resolve("Makefile").toFile().exists()) { //$NON-NLS-1$
					// Need to run qmake
					List<String> command = new ArrayList<>();
					command.add(qtConfig.getQmakeCommand().toString());

					String config = qtConfig.getQmakeConfig();
					if (config != null) {
						command.add(config);
					}

					IFile projectFile = qtConfig.getBuildConfiguration().getProject()
							.getFile(project.getName() + ".pro"); //$NON-NLS-1$
					command.add(projectFile.getLocation().toOSString());

					ProcessBuilder processBuilder = new ProcessBuilder(command)
							.directory(qtConfig.getBuildDirectory().toFile());
					CCorePlugin.getDefault().getBuildEnvironmentManager().setEnvironment(processBuilder.environment(),
							getBuildConfig(), true);
					Process process = processBuilder.start();

					StringBuffer msg = new StringBuffer();
					for (String arg : command) {
						msg.append(arg).append(' ');
					}
					msg.append('\n');
					outStream.write(msg.toString());

					runner.monitor(process);
				}

				// run make
				ProcessBuilder procBuilder = new ProcessBuilder(makeCommand.toString()).directory(buildDir.toFile());
				CCorePlugin.getDefault().getBuildEnvironmentManager().setEnvironment(procBuilder.environment(),
						getBuildConfig(), true);
				Process process = procBuilder.start();
				outStream.write(makeCommand.toString() + '\n');
				runner.monitor(process);
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			// clear the scanner info cache
			// TODO be more surgical about what to clear based on what was
			// built.
			// qtConfig.clearScannerInfoCache();

			outStream.write("Complete.\n");
			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, "Building " + project.getName(), e)); //$NON-NLS-1$
		}
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false,  IResource.DEPTH_INFINITE);

			IConsole console = CCorePlugin.getDefault().getConsole();
			console.start(getProject());
			ConsoleOutputStream errStream = console.getErrorStream();
			ConsoleOutputStream outStream = console.getOutputStream();

			ICBuildConfiguration cconfig = getBuildConfig().getAdapter(ICBuildConfiguration.class);
			IQtBuildConfiguration qtConfig = cconfig.getAdapter(QtBuildConfiguration.class);
			if (qtConfig == null) {
				// Qt hasn't been configured yet print a message and bale
				errStream.write(Messages.QtBuilder_0);
				return;
			}

			Path makeCommand = getMakeCommand(getBuildConfig());
			if (makeCommand == null) {
				errStream.write("'make' not found.\n");
				return;
			}

			Path buildDir = qtConfig.getBuildDirectory();

			try (ErrorParserManager epm = new ErrorParserManager(project, qtConfig.getBuildDirectory().toUri(), this,
					qtConfig.getToolChain().getErrorParserIds())) {
				BuildCommandRunner runner = new BuildCommandRunner(project, console, epm);
				// run make
				ProcessBuilder procBuilder = new ProcessBuilder(makeCommand.toString(), "clean") //$NON-NLS-1$
						.directory(buildDir.toFile());
				CCorePlugin.getDefault().getBuildEnvironmentManager().setEnvironment(procBuilder.environment(),
						getBuildConfig(), true);
				Process process = procBuilder.start();
				outStream.write(makeCommand.toString() + "clean\n"); //$NON-NLS-1$
				runner.monitor(process);
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			// clear the scanner info cache
			// TODO be more surgical about what to clear based on what was
			// built.
			// qtConfig.clearScannerInfoCache();

			outStream.write("Complete.\n");
			// TODO Auto-generated method stub
			super.clean(monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, "Cleaning " + project.getName(), e)); //$NON-NLS-1$
		}
	}

	public Path getMakeCommand(IBuildConfiguration config) {
		Path makeCommand = findCommand(getBuildConfig(), "make"); //$NON-NLS-1$
		if (makeCommand == null) {
			makeCommand = findCommand(getBuildConfig(), "mingw32-make"); //$NON-NLS-1$
		}
		return makeCommand;
	}

	public Path findCommand(IBuildConfiguration config, String command) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command += ".exe"; //$NON-NLS-1$
		}
		Map<String, String> env = new HashMap<>(System.getenv());
		CCorePlugin.getDefault().getBuildEnvironmentManager().setEnvironment(env, config, true);
		String[] path = env.get("PATH").split(File.pathSeparator); //$NON-NLS-1$
		for (String dir : path) {
			Path commandPath = Paths.get(dir, command);
			if (Files.exists(commandPath)) {
				return commandPath;
			}
		}
		return null;
	}

}
