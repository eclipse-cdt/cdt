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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildCommandLauncher;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.google.gson.Gson;

public class CMakeBuildConfiguration extends CBuildConfiguration {

	public static final String CMAKE_GENERATOR = "cmake.generator"; //$NON-NLS-1$
	public static final String CMAKE_ARGUMENTS = "cmake.arguments"; //$NON-NLS-1$
	public static final String CMAKE_ENV = "cmake.environment"; //$NON-NLS-1$
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
		} else {
			toolChainFile = manager.getToolChainFileFor(getToolChain());
			if (toolChainFile != null) {
				saveToolChainFile();
			}
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
			saveToolChainFile();
		}
	}

	public ICMakeToolChainFile getToolChainFile() {
		return toolChainFile;
	}

	private void saveToolChainFile() {
		Preferences settings = getSettings();
		settings.put(TOOLCHAIN_FILE, toolChainFile.getPath().toString());
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	private boolean isLocal() throws CoreException {
		IToolChain toolchain = getToolChain();
		return (Platform.getOS().equals(toolchain.getProperty(IToolChain.ATTR_OS))
				|| "linux-container".equals(toolchain.getProperty(IToolChain.ATTR_OS))) //$NON-NLS-1$
				&& (Platform.getOSArch().equals(toolchain.getProperty(IToolChain.ATTR_ARCH)));
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		
		try {
			String generator = getProperty(CMAKE_GENERATOR);
			if (generator == null) {
				generator = "Ninja"; //$NON-NLS-1$
			}

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			outStream.write(String.format(Messages.CMakeBuildConfiguration_BuildingIn, buildDir.toString()));

			// Make sure we have a toolchain file if cross
			if (toolChainFile == null && !isLocal()) {
				ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
				toolChainFile = manager.getToolChainFileFor(getToolChain());
				
				if (toolChainFile == null) {
					// error
					console.getErrorStream().write(Messages.CMakeBuildConfiguration_NoToolchainFile);
					return null;
				}
			}

			boolean runCMake;
			switch (generator) {
			case "Ninja": //$NON-NLS-1$
				runCMake = !Files.exists(buildDir.resolve("build.ninja")); //$NON-NLS-1$
				break;
			default:
				runCMake = !Files.exists(buildDir.resolve("CMakeFiles")); //$NON-NLS-1$
			}

			if (runCMake) { // $NON-NLS-1$
				org.eclipse.core.runtime.Path cmdPath = new org.eclipse.core.runtime.Path("/usr/bin/env"); //$NON-NLS-1$
				
				List<String> argList = new ArrayList<>();

				argList.add("sh"); //$NON-NLS-1$
				argList.add("-c"); //$NON-NLS-1$
				
				StringBuffer b = new StringBuffer();
				b.append("cmake"); //$NON-NLS-1$
				b.append(" "); //$NON-NLS-1$
				b.append("-G "); //$NON-NLS-1$
				b.append(generator);

				if (toolChainFile != null) {
					b.append(" "); //$NON-NLS-1$
					b.append("-DCMAKE_TOOLCHAIN_FILE=" + toolChainFile.getPath().toString()); //$NON-NLS-1$
				}

				switch (getLaunchMode()) {
				// TODO what to do with other modes
				case "debug": //$NON-NLS-1$
					b.append(" "); //$NON-NLS-1$
					b.append("-DCMAKE_BUILD_TYPE=Debug"); //$NON-NLS-1$
					break;
				case "run": //$NON-NLS-1$
					b.append(" "); //$NON-NLS-1$
					b.append("-DCMAKE_BUILD_TYPE=Release"); //$NON-NLS-1$
					break;
				}
				b.append(" "); //$NON-NLS-1$
				b.append("-DCMAKE_EXPORT_COMPILE_COMMANDS=ON"); //$NON-NLS-1$
				
				String userArgs = getProperty(CMAKE_ARGUMENTS);
				if (userArgs != null) {
					b.append(" "); //$NON-NLS-1$
					b.append(userArgs);
				}

				b.append(" "); //$NON-NLS-1$
				b.append(new File(project.getLocationURI()).getAbsolutePath());
				
				argList.add(b.toString());

				ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);
				
				outStream.write(String.join(" ", argList) + '\n'); //$NON-NLS-1$
				
				launcher.setProject(getProject());
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher)launcher).setBuildConfiguration(this);
				}
				
				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(getBuildDirectory().toString());
				Process p = launcher.execute(cmdPath, argList.toArray(new String[0]), new String[0], workingDir, monitor);
				if (p == null || launcher.waitAndRead(outStream, outStream, SubMonitor.convert(monitor)) != ICommandLauncher.OK) {
					String errMsg = p == null ? "" : launcher.getErrorMessage(); //$NON-NLS-1$
					console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, errMsg));
					return null;
				}
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());
				
				IPath cmdPath = new org.eclipse.core.runtime.Path("/usr/bin/env"); //$NON-NLS-1$
				
				List<String> argsList = new ArrayList<>();
				
				String envStr = getProperty(CMAKE_ENV);
				if (envStr != null) {
					argsList.addAll(CMakeUtils.stripEnvVars(envStr));
				}
				
				argsList.add("sh"); //$NON-NLS-1$
				argsList.add("-c"); //$NON-NLS-1$
				
				String buildCommand = getProperty(BUILD_COMMAND);
				if (buildCommand == null) {
					buildCommand = "cmake --build ."; //$NON-NLS-1$
					if ("Ninja".equals(generator)) {
						buildCommand += " -- -v"; //$NON-NLS-1$
					}
				}
				argsList.add(buildCommand);

				ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);
				
				launcher.setProject(getProject());
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher)launcher).setBuildConfiguration(this);
					console.getOutputStream().write(((ICBuildCommandLauncher)launcher).getConsoleHeader());
				}
				
				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(getBuildDirectory().toString());

				outStream.write(String.join(" ", argsList) + '\n'); //$NON-NLS-1$
				Process p = launcher.execute(cmdPath, argsList.toArray(new String[0]), new String[0], workingDir, monitor);
				if (p != null && launcher.waitAndRead(epm.getOutputStream(), epm.getOutputStream(), SubMonitor.convert(monitor)) != ICommandLauncher.OK) {
					String errMsg = launcher.getErrorMessage();
					console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, errMsg));
					return null;
				}

				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

				// Load compile_commands.json file
				processCompileCommandsFile(monitor);

				outStream.write(String.format(Messages.CMakeBuildConfiguration_BuildingComplete, epm.getErrorCount(), 
						epm.getWarningCount(), buildDir.toString()));
			}

			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus(String.format(Messages.CMakeBuildConfiguration_Building, project.getName()), e));
		}
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			String generator = getProperty(CMAKE_GENERATOR);

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			if (!Files.exists(buildDir.resolve("CMakeFiles"))) { //$NON-NLS-1$
				outStream.write(Messages.CMakeBuildConfiguration_NotFound);
				return;
			}

			IPath cmd = new org.eclipse.core.runtime.Path("/usr/bin/env"); //$NON-NLS-1$
			
			List<String> argList = new ArrayList<>();
			argList.add("sh"); //$NON-NLS-1$
			argList.add("-c"); //$NON-NLS-1$
			
			String cleanCommand = getProperty(CLEAN_COMMAND);
			if (cleanCommand == null) {
				if (generator == null || generator.equals("Ninja")) { //$NON-NLS-1$
					cleanCommand = "ninja clean"; //$NON-NLS-1$
				} else {
					cleanCommand = "make clean"; //$NON-NLS-1$
				}
			}
			argList.add(cleanCommand);
			
			ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);

			launcher.setProject(getProject());
			if (launcher instanceof ICBuildCommandLauncher) {
				((ICBuildCommandLauncher)launcher).setBuildConfiguration(this);
			}

			org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(buildDir.toString());

			String[] env = new String[0];

			outStream.write(String.join(" ", argList) + '\n'); //$NON-NLS-1$
			Process p = launcher.execute(cmd, argList.toArray(new String[0]), env, workingDir, monitor);
			if (p == null || launcher.waitAndRead(outStream, outStream, SubMonitor.convert(monitor)) != ICommandLauncher.OK) {
				String errMsg = launcher.getErrorMessage();
				console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, errMsg));
				return;
			}
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus(String.format(Messages.CMakeBuildConfiguration_Cleaning, project.getName()), e));
		}
	}

	private void processCompileCommandsFile(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		Path commandsFile = getBuildDirectory().resolve("compile_commands.json"); //$NON-NLS-1$
		if (Files.exists(commandsFile)) {
			List<Job> jobsList = new ArrayList<>();
			monitor.setTaskName(Messages.CMakeBuildConfiguration_ProcCompJson);
			try (FileReader reader = new FileReader(commandsFile.toFile())) {
				Gson gson = new Gson();
				CompileCommand[] commands = gson.fromJson(reader, CompileCommand[].class);
				Map<String, CompileCommand> dedupedCmds = new HashMap<>();
				for (CompileCommand command : commands) {
					dedupedCmds.put(command.getFile(), command);
				}
				for (CompileCommand command : dedupedCmds.values()) {
					processLine(command.getCommand(), jobsList);
				}
				for (Job j : jobsList) {
					try {
						j.join();
					} catch (InterruptedException e) {
						// ignore
					}
				}
				shutdown();
			} catch (IOException e) {
				throw new CoreException(
						Activator.errorStatus(String.format(Messages.CMakeBuildConfiguration_ProcCompCmds, project.getName()), e));
			}
		}
	}

}
