/*******************************************************************************
 * Copyright (c) 2015, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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

	public IMesonToolChainFile getToolChainFile() {
		return toolChainFile;
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
		return build(kind, args, null, null, console, monitor);
	}

	public IProject[] build(int kind, Map<String, String> args, String[] ninjaEnv, String[] ninjaArgs, IConsole console,
			IProgressMonitor monitor) throws CoreException {
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
				List<String> commandList = new ArrayList<>();

				String envStr = getProperty(IMesonConstants.MESON_ENV);
				List<IEnvironmentVariable> envVars = new ArrayList<>();
				if (envStr != null) {
					List<String> envList = MesonUtils.stripEnvVars(envStr);
					for (String s : envList) {
						int index = s.indexOf("="); //$NON-NLS-1$
						if (index == -1) {
							envVars.add(new EnvironmentVariable(s));
						} else {
							envVars.add(new EnvironmentVariable(s.substring(0, index), s.substring(index + 1)));
						}
					}
				}

				commandList.add("meson"); //$NON-NLS-1$

				String userArgs = getProperty(IMesonConstants.MESON_ARGUMENTS);
				if (userArgs != null && !userArgs.isEmpty()) {
					commandList.addAll(Arrays.asList(userArgs.split(" ")));
				}
				String projOptions = getProperty(IMesonConstants.MESON_PROJECT_OPTIONS);
				if (projOptions != null && !projOptions.isEmpty()) {
					commandList.addAll(Arrays.asList(projOptions.split(" ")));
				}
				commandList.add(getBuildDirectory().toString());

				monitor.subTask(Messages.MesonBuildConfiguration_RunningMeson);

				outStream.write(String.join(" ", envStr != null ? ("env " + envStr) : "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"sh -c \"meson", userArgs != null ? userArgs : "", projOptions != null ? projOptions : "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						getBuildDirectory().getParent().getParent().toString() + "\"\n")); //$NON-NLS-1$

				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(
						getBuildDirectory().getParent().getParent().toString());
				Process p = startBuildProcess(commandList, envVars.toArray(new IEnvironmentVariable[0]), workingDir,
						console, monitor);
				if (p == null) {
					console.getErrorStream()
							.write(String.format(Messages.MesonBuildConfiguration_RunningMesonFailure, "")); //$NON-NLS-1$
					return null;
				}

				watchProcess(p, console);
			}

			if (!Files.exists(buildDir.resolve("build.ninja"))) { //$NON-NLS-1$
				console.getErrorStream().write(String.format(Messages.MesonBuildConfiguration_NoNinjaFile, "")); //$NON-NLS-1$
				return null;
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());

				String buildCommand = getProperty(IMesonConstants.BUILD_COMMAND);
				if (buildCommand == null || buildCommand.isEmpty()) {
					buildCommand = "ninja"; //$NON-NLS-1$
				}

				monitor.subTask(Messages.MesonBuildConfiguration_RunningNinja);

				List<String> commandList = new ArrayList<>();

				List<IEnvironmentVariable> envList = new ArrayList<>();
				if (ninjaEnv != null) {
					for (String s : ninjaEnv) {
						int index = s.indexOf("="); //$NON-NLS-1$
						if (index == -1) {
							envList.add(new EnvironmentVariable(s));
						} else {
							envList.add(new EnvironmentVariable(s.substring(0, index), s.substring(index + 1)));
						}
					}
				}
				IEnvironmentVariable[] env = envList.toArray(new IEnvironmentVariable[0]);

				commandList.add(buildCommand);
				if (ninjaArgs == null) {
					commandList.add("-v"); //$NON-NLS-1$
				} else {
					for (String arg : ninjaArgs) {
						if (!arg.isEmpty()) {
							commandList.add(arg);
						}
					}
				}

				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(
						getBuildDirectory().toString());

				Process p = startBuildProcess(commandList, env, workingDir, console, monitor);
				if (p == null) {
					console.getErrorStream()
							.write(String.format(Messages.MesonBuildConfiguration_RunningNinjaFailure, "")); //$NON-NLS-1$
					return null;
				}

				watchProcess(p, new IConsoleParser[] { epm });
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			// Process compile_commands.json file and generate Scanner info
			refreshScannerInfo();

			outStream.write(String.format(Messages.MesonBuildConfiguration_BuildingComplete, buildDir.toString()));

			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(Activator
					.errorStatus(String.format(Messages.MesonBuildConfiguration_Building, project.getName()), e));
		}
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			outStream.write(String.format(Messages.MesonBuildConfiguration_BuildingIn, buildDir.toString()));

			if (!Files.exists(buildDir.resolve("build.ninja"))) { //$NON-NLS-1$
				console.getOutputStream().write(Messages.MesonBuildConfiguration_NoNinjaFileToClean);
				return;
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());

				List<String> commandList = new ArrayList<>();
				String cleanCommand = getProperty(IMesonConstants.CLEAN_COMMAND);
				if (cleanCommand == null) {
					commandList.add("ninja"); //$NON-NLS-1$
					commandList.add("clean"); //$NON-NLS-1$
					commandList.add("-v"); //$NON-NLS-1$
				} else {
					commandList.addAll(Arrays.asList(cleanCommand.split(" "))); //$NON-NLS-1$
				}

				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(buildDir.toString());

				IEnvironmentVariable[] env = new IEnvironmentVariable[0];

				outStream.write(String.join(" ", commandList) + '\n'); //$NON-NLS-1$
				Process p = startBuildProcess(commandList, env, workingDir, console, monitor);
				if (p == null) {
					console.getErrorStream()
							.write(String.format(Messages.MesonBuildConfiguration_RunningNinjaFailure, "")); //$NON-NLS-1$
					return;
				}

				watchProcess(p, console);
			}

			outStream.write(String.format(Messages.MesonBuildConfiguration_BuildingComplete, buildDir.toString()));

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(Activator
					.errorStatus(String.format(Messages.MesonBuildConfiguration_Cleaning, project.getName()), e));
		}
	}

	@Override
	public void refreshScannerInfo() throws CoreException {
		Job job = new Job(Messages.MesonBuildConfiguration_RefreshingScannerInfo) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					processCompileCommandsFile(monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		// TODO: should this have a scheduling rule??
		job.schedule();
	}

	private void processCompileCommandsFile(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		Path commandsFile = getBuildDirectory().resolve("compile_commands.json"); //$NON-NLS-1$
		if (Files.exists(commandsFile)) {
			List<Job> jobsList = new ArrayList<>();
			monitor.setTaskName(Messages.MesonBuildConfiguration_ProcCompJson);
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
				throw new CoreException(Activator.errorStatus(
						String.format(Messages.MesonBuildConfiguration_ProcCompCmds, project.getName()), e));
			}
		}
	}

}
