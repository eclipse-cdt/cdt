/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.google.gson.Gson;

public class CMakeBuildConfiguration extends CBuildConfiguration {

	public static final String CMAKE_GENERATOR = "cmake.generator"; //$NON-NLS-1$
	public static final String CMAKE_ARGUMENTS = "cmake.arguments"; //$NON-NLS-1$
	public static final String BUILD_COMMAND = "cmake.command.build"; //$NON-NLS-1$
	public static final String CLEAN_COMMAND = "cmake.command.clean"; //$NON-NLS-1$

	private static final String TOOLCHAIN_FILE = "cdt.cmake.toolchainfile"; //$NON-NLS-1$

	private ICMakeToolChainFile toolChainFile;

	public CMakeBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);

		ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
		Preferences settings = getSettings();
		String pathStr = settings.get(TOOLCHAIN_FILE, ""); //$NON-NLS-1$
		if (!pathStr.isEmpty()) {
			Path path = Paths.get(pathStr);
			toolChainFile = manager.getToolChainFile(path);
		}
	}

	public CMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		this(config, name, toolChain, null, "run"); //$NON-NLS-1$
	}

	public CMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			ICMakeToolChainFile toolChainFile, String launchMode) {
		super(config, name, toolChain, launchMode);
		this.toolChainFile = toolChainFile;

		if (toolChainFile != null) {
			Preferences settings = getSettings();
			settings.put(TOOLCHAIN_FILE, toolChainFile.getPath().toString());
			try {
				settings.flush();
			} catch (BackingStoreException e) {
				Activator.log(e);
			}
		}
	}

	public ICMakeToolChainFile getToolChainFile() {
		return toolChainFile;
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		try {
			Map<String, String> properties = getProperties();
			String generator = properties.get(CMAKE_GENERATOR);
			if (generator == null) {
				generator = "Unix Makefiles"; //$NON-NLS-1$
			}

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			outStream.write(String.format(Messages.CMakeBuildConfiguration_BuildingIn, buildDir.toString()));

			if (!Files.exists(buildDir.resolve("CMakeFiles"))) { //$NON-NLS-1$
				List<String> command = new ArrayList<>();

				// TODO location of CMake out of preferences if not found here
				Path cmakePath = findCommand("cmake"); //$NON-NLS-1$
				if (cmakePath != null) {
					command.add(cmakePath.toString());
				} else {
					command.add("cmake"); //$NON-NLS-1$
				}

				command.add("-G"); //$NON-NLS-1$
				command.add(generator);

				if (toolChainFile != null) {
					command.add("-DCMAKE_TOOLCHAIN_FILE=" + toolChainFile.getPath().toString()); //$NON-NLS-1$
				}

				switch (getLaunchMode()) {
				// TODO what to do with other modes
				case "debug": //$NON-NLS-1$
					command.add("-DCMAKE_BUILD_TYPE=Debug"); //$NON-NLS-1$
					break;
				}

				command.add("-DCMAKE_EXPORT_COMPILE_COMMANDS=ON"); //$NON-NLS-1$
				
				String userArgs = properties.get(CMAKE_ARGUMENTS);
				if (userArgs != null) {
					command.addAll(Arrays.asList(userArgs.trim().split("\\s+"))); //$NON-NLS-1$
				}

				command.add(new File(project.getLocationURI()).getAbsolutePath());

				ProcessBuilder processBuilder = new ProcessBuilder(command).directory(buildDir.toFile());
				setBuildEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
				watchProcess(process, new IConsoleParser[0], console);
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				String buildCommand = properties.get(BUILD_COMMAND);
				if (buildCommand == null) {
					if (generator.equals("Ninja")) { //$NON-NLS-1$
						buildCommand = "ninja"; //$NON-NLS-1$
					} else {
						buildCommand = "make"; //$NON-NLS-1$
					}
				}
				String[] command = buildCommand.split(" "); //$NON-NLS-1$

				Path cmdPath = findCommand(command[0]);
				if (cmdPath != null) {
					command[0] = cmdPath.toString();
				}

				ProcessBuilder processBuilder = new ProcessBuilder(command).directory(buildDir.toFile());
				setBuildEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
				watchProcess(process, new IConsoleParser[] { epm }, console);
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			// Load compile_commands.json file
			processCompileCommandsFile(monitor);

			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus(String.format(Messages.CMakeBuildConfiguration_Building, project.getName()), e));
		}
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			Map<String, String> properties = getProperties();
			String generator = properties.get(CMAKE_GENERATOR);

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			if (!Files.exists(buildDir.resolve("CMakeFiles"))) { //$NON-NLS-1$
				outStream.write(Messages.CMakeBuildConfiguration_NotFound);
				return;
			}

			String cleanCommand = properties.get(CLEAN_COMMAND);
			if (cleanCommand == null) {
				if (generator != null && generator.equals("Ninja")) { //$NON-NLS-1$
					cleanCommand = "ninja clean"; //$NON-NLS-1$
				} else {
					cleanCommand = "make clean"; //$NON-NLS-1$
				}
			}
			String[] command = cleanCommand.split(" "); //$NON-NLS-1$

			Path cmdPath = findCommand(command[0]);
			if (cmdPath != null) {
				command[0] = cmdPath.toString();
			}

			ProcessBuilder processBuilder = new ProcessBuilder(command).directory(buildDir.toFile());
			Process process = processBuilder.start();
			outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
			watchProcess(process, new IConsoleParser[0], console);

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus(String.format(Messages.CMakeBuildConfiguration_Cleaning, project.getName()), e));
		}
	}

	private void processCompileCommandsFile(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		Path commandsFile = getBuildDirectory().resolve("compile_commands.json"); //$NON-NLS-1$
		if (Files.exists(commandsFile)) {
			monitor.setTaskName(Messages.CMakeBuildConfiguration_ProcCompJson);
			try (FileReader reader = new FileReader(commandsFile.toFile())) {
				Gson gson = new Gson();
				CompileCommand[] commands = gson.fromJson(reader, CompileCommand[].class);
				Map<String, CompileCommand> dedupedCmds = new HashMap<>();
				for (CompileCommand command : commands) {
					dedupedCmds.put(command.getFile(), command);
				}
				for (CompileCommand command : dedupedCmds.values()) {
					processLine(command.getCommand());
				}
				shutdown();
			} catch (IOException e) {
				throw new CoreException(
						Activator.errorStatus(String.format(Messages.CMakeBuildConfiguration_ProcCompCmds, project.getName()), e));
			}
		}
	}

}
