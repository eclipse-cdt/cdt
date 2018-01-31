/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.core;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildCommandLauncher;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.meson.core.Activator;
import org.eclipse.cdt.meson.core.IMesonConstants;
import org.eclipse.cdt.meson.core.IMesonToolChainFile;
import org.eclipse.cdt.meson.core.IMesonToolChainManager;
import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.google.gson.Gson;

public class MesonBuildConfiguration extends CBuildConfiguration {

	private static final String TOOLCHAIN_FILE = "cdt.meson.toolchainfile"; //$NON-NLS-1$

	private IMesonToolChainFile toolChainFile;

	public MesonBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);

		IMesonToolChainManager manager = Activator.getService(IMesonToolChainManager.class);
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

	public MesonBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		this(config, name, toolChain, null, "run"); //$NON-NLS-1$
	}

	public MesonBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			IMesonToolChainFile toolChainFile, String launchMode) {
		super(config, name, toolChain, launchMode);

		this.toolChainFile = toolChainFile;
		if (toolChainFile != null) {
			saveToolChainFile();
		}
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
		return Platform.getOS().equals(toolchain.getProperty(IToolChain.ATTR_OS))
				&& Platform.getOSArch().equals(toolchain.getProperty(IToolChain.ATTR_ARCH));
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			outStream.write(String.format(Messages.MesonBuildConfiguration_BuildingIn, buildDir.toString()));

			// Make sure we have a toolchain file if cross
			if (toolChainFile == null && !isLocal()) {
				IMesonToolChainManager manager = Activator.getService(IMesonToolChainManager.class);
				toolChainFile = manager.getToolChainFileFor(getToolChain());
				
				if (toolChainFile == null) {
					// error
					console.getErrorStream().write(Messages.MesonBuildConfiguration_NoToolchainFile);
					return null;
				}
			}

			boolean runMeson = !Files.exists(buildDir.resolve("build.ninja")); //$NON-NLS-1$
			if (runMeson) { // $NON-NLS-1$
				Path path = findCommand("meson"); //$NON-NLS-1$
				if (path == null) {
					path = Paths.get("meson"); //$NON-NLS-1
				}

				List<String> argsList = new ArrayList<>();
				
				String userArgs = getProperty(IMesonConstants.MESON_ARGUMENTS);
				if (userArgs != null) {
					argsList.addAll(Arrays.asList(userArgs.trim().split("\\s+"))); //$NON-NLS-1$
				}
				
				argsList.add(getBuildDirectory().toString());

				Map<String, String> envMap = System.getenv();
				List<String> envList = new ArrayList<>();
				for (Map.Entry<String, String> entry : envMap.entrySet()) {
					envList.add(entry.getKey() + "=" + entry.getValue());
				}
				String envStr = getProperty(IMesonConstants.MESON_ENV);
				if (envStr != null) {
					envList.addAll(stripEnvVars(envStr));
				}
				String[] env = envList.toArray(new String[0]);

				ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);
				
				launcher.setProject(getProject());
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher)launcher).setBuildConfiguration(this);
				}

				monitor.subTask(Messages.MesonBuildConfiguration_RunningMeson);
				
				org.eclipse.core.runtime.Path mesonPath = new org.eclipse.core.runtime.Path(path.toString());
				outStream.write(String.join(" ", envStr != null ? envStr : "", //$NON-NLS-1$ //$NON-NLS-2$ 
						mesonPath.toString(), userArgs, "\n")); //$NON-NLS-1$
				outStream.write(getBuildDirectory() + "\n"); //$NON-NLS-1$
				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(getBuildDirectory().getParent().getParent().toString());
				
				launcher.execute(mesonPath, argsList.toArray(new String[0]), env, workingDir, monitor);
				if (launcher.waitAndRead(outStream, outStream, SubMonitor.convert(monitor)) != ICommandLauncher.OK) {
					String errMsg = launcher.getErrorMessage();
					console.getErrorStream().write(String.format(Messages.MesonBuildConfiguration_RunningMesonFailure, errMsg));
					return null;
				};
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());
				
				String buildCommand = getProperty(IMesonConstants.BUILD_COMMAND);
				if (buildCommand == null || buildCommand.isEmpty()) {
					buildCommand = "ninja";
				}

				String[] env = new String[0];

				ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);
				
				launcher.setProject(getProject());
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher)launcher).setBuildConfiguration(this);
				}

				monitor.subTask(Messages.MesonBuildConfiguration_RunningMeson);
				
				org.eclipse.core.runtime.Path ninjaPath = new org.eclipse.core.runtime.Path(buildCommand);
				outStream.write(String.join(" ", ninjaPath.toString() + '\n')); //$NON-NLS-1$
				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(getBuildDirectory().toString());
				
				launcher.execute(ninjaPath, new String[] {"-v"}, env, workingDir, monitor); //$NON-NLS-1$
				if (launcher.waitAndRead(epm.getOutputStream(), epm.getOutputStream(), SubMonitor.convert(monitor)) != ICommandLauncher.OK) {
					String errMsg = launcher.getErrorMessage();
					console.getErrorStream().write(String.format(Messages.MesonBuildConfiguration_RunningNinjaFailure, errMsg));
					return null;
				};
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			// Load compile_commands.json file
			processCompileCommandsFile(monitor);

			outStream.write(String.format(Messages.MesonBuildConfiguration_BuildingComplete, buildDir.toString()));

			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus(String.format(Messages.MesonBuildConfiguration_Building, project.getName()), e));
		}
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());


				String cleanCommand = getProperty(IMesonConstants.CLEAN_COMMAND);
				if (cleanCommand == null) {
					cleanCommand = "ninja clean"; //$NON-NLS-1$
				}
				String[] command = cleanCommand.split(" "); //$NON-NLS-1$

				Path cmdPath = findCommand(command[0]);
				if (cmdPath != null) {
					command[0] = cmdPath.toString();
				}

				IPath cmd = new org.eclipse.core.runtime.Path(command[0]);
				ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);

				launcher.setProject(getProject());
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher)launcher).setBuildConfiguration(this);
				}

				String[] commandargs = new String[0];
				if (command.length > 1) {
					commandargs = Arrays.copyOfRange(command, 1, command.length);
				}
				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(buildDir.toString());

				String[] env = new String[0];

				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
				launcher.execute(cmd, commandargs, env, workingDir, monitor);
				if (launcher.waitAndRead(epm.getOutputStream(), epm.getOutputStream(), SubMonitor.convert(monitor)) != ICommandLauncher.OK) {
					String errMsg = launcher.getErrorMessage();
					console.getErrorStream().write(String.format(Messages.MesonBuildConfiguration_RunningNinjaFailure, errMsg));
					return;
				};
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus(String.format(Messages.MesonBuildConfiguration_Cleaning, project.getName()), e));
		}
	}

	private void processCompileCommandsFile(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		Path commandsFile = getBuildDirectory().resolve("compile_commands.json"); //$NON-NLS-1$
		if (Files.exists(commandsFile)) {
			monitor.setTaskName(Messages.MesonBuildConfiguration_ProcCompJson);
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
						Activator.errorStatus(String.format(Messages.MesonBuildConfiguration_ProcCompCmds, project.getName()), e));
			}
		}
	}
	
	/**
	 * Strip a command of VAR=VALUE pairs that appear ahead or behind the command and add
	 * them to a list of environment variables.
	 *
	 * @param command - command to strip
	 * @param envVars - ArrayList to add environment variables to
	 * @return stripped command
	 */
	public static List<String> stripEnvVars(String envString) {
		Pattern p1 = Pattern.compile("(\\w+[=]\\\".*?\\\").*"); //$NON-NLS-1$
		Pattern p2 = Pattern.compile("(\\w+[=]'.*?').*"); //$NON-NLS-1$
		Pattern p3 = Pattern.compile("(\\w+[=][^\\s]+).*"); //$NON-NLS-1$
		boolean finished = false;
		List<String> envVars = new ArrayList<>();
		while (!finished) {
			Matcher m1 = p1.matcher(envString);
			if (m1.matches()) {
				envString = envString.replaceFirst("\\w+[=]\\\".*?\\\"","").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				String s = m1.group(1).trim();
				envVars.add(s.replaceAll("\\\"", "")); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				Matcher m2 = p2.matcher(envString);
				if (m2.matches()) {
					envString = envString.replaceFirst("\\w+[=]'.*?'", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
					String s = m2.group(1).trim();
					envVars.add(s.replaceAll("'", "")); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					Matcher m3 = p3.matcher(envString);
					if (m3.matches()) {
						envString = envString.replaceFirst("\\w+[=][^\\s]+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
						envVars.add(m3.group(1).trim());
					} else {
						finished = true;
					}
				}
			}
		}
		return envVars;
	}

}
