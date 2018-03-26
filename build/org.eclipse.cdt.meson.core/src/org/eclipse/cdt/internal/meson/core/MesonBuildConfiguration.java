/*******************************************************************************
 * Copyright (c) 2015, 2018 QNX Software Systems and others.
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
import java.util.Map.Entry;
import java.util.Properties;

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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
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
	
	public IProject[] build(int kind, Map<String, String> args, String[] ninjaEnv, String[] ninjaArgs, IConsole console, IProgressMonitor monitor)
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
				org.eclipse.core.runtime.Path cmdPath = new org.eclipse.core.runtime.Path("/bin/sh"); //$NON-NLS-1$
				
				List<String> argsList = new ArrayList<>();
				
				// if we have env variables, use "env" command with modifications specified after to
				// add to environment without replacing it (e.g. losing default path)
				String envStr = getProperty(IMesonConstants.MESON_ENV);
				if (envStr != null) {
					cmdPath = new org.eclipse.core.runtime.Path("/usr/bin/env"); //$NON-NLS-1$
					argsList.addAll(MesonUtils.stripEnvVars(envStr));
					argsList.add("/bin/sh"); //$NON-NLS-1$
				}
				argsList.add("-c"); //$NON-NLS-1$
				
				StringBuilder b = new StringBuilder();
				b.append("meson"); //$NON-NLS-1$

				String userArgs = getProperty(IMesonConstants.MESON_ARGUMENTS);
				if (userArgs != null) {
					b.append(" "); //$NON-NLS-1$
					b.append(userArgs);
				}
				String projOptions = getProperty(IMesonConstants.MESON_PROJECT_OPTIONS);
				if (projOptions != null) {
					b.append(" "); //$NON-NLS-1$
					b.append(projOptions);
				}
				b.append(" "); //$NON-NLS-1$
				b.append(getBuildDirectory().toString());
				argsList.add(b.toString());

				ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);
				
				launcher.setProject(getProject());
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher)launcher).setBuildConfiguration(this);
				}
				
				monitor.subTask(Messages.MesonBuildConfiguration_RunningMeson);

				outStream.write(String.join(" ", envStr != null ? ("env " + envStr) : "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"sh -c \"meson", userArgs != null ? userArgs : "", projOptions != null ? projOptions : "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								getBuildDirectory().getParent().getParent().toString() + "\"\n")); //$NON-NLS-1$
				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(getBuildDirectory().getParent().getParent().toString());
				Process p = launcher.execute(cmdPath, argsList.toArray(new String[0]), new String[0], workingDir, monitor);
				if (p == null || launcher.waitAndRead(outStream, outStream, SubMonitor.convert(monitor)) != ICommandLauncher.OK) {
					String errMsg = p == null ? "" : launcher.getErrorMessage(); //$NON-NLS-1$
					console.getErrorStream().write(String.format(Messages.MesonBuildConfiguration_RunningMesonFailure, errMsg));
					return null;
				}
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

				List<String> envList = new ArrayList<>();
				if (ninjaEnv != null) {
					envList.addAll(Arrays.asList(ninjaEnv));
				}
				String[] env = envList.toArray(new String[0]);

				ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);

				launcher.setProject(getProject());
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher)launcher).setBuildConfiguration(this);
				}

				monitor.subTask(Messages.MesonBuildConfiguration_RunningNinja);

				org.eclipse.core.runtime.Path shPath = new org.eclipse.core.runtime.Path("sh"); //$NON-NLS-1$
				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(getBuildDirectory().toString());
				
				List<String> argList = new ArrayList<>();
				argList.add("-c"); //$NON-NLS-1$
				StringBuilder b = new StringBuilder();
				b.append(buildCommand);
				if (ninjaArgs == null) {
					b.append(" -v"); //$NON-NLS-1$
				} else {
					for (String arg : ninjaArgs) {
						b.append(" "); //$NON-NLS-1$
						b.append(arg);
					}
				}
				argList.add(b.toString());

				Process p = launcher.execute(shPath, argList.toArray(new String[0]), env, workingDir, monitor);
				if (p != null && launcher.waitAndRead(epm.getOutputStream(), epm.getOutputStream(), SubMonitor.convert(monitor)) != ICommandLauncher.OK) {
					String errMsg = launcher.getErrorMessage();
					console.getErrorStream().write(String.format(Messages.MesonBuildConfiguration_RunningNinjaFailure, errMsg));
					return null;
				}
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			// Process compile_commands.json file and generate Scanner info
			refreshScannerInfo();

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
			
			outStream.write(String.format(Messages.MesonBuildConfiguration_BuildingIn, buildDir.toString()));
			
			if (!Files.exists(buildDir.resolve("build.ninja"))) { //$NON-NLS-1$
				console.getOutputStream().write(Messages.MesonBuildConfiguration_NoNinjaFileToClean);
				return;
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());


				String cleanCommand = getProperty(IMesonConstants.CLEAN_COMMAND);
				if (cleanCommand == null) {
					cleanCommand = "ninja clean -v"; //$NON-NLS-1$
				}
				String[] command = cleanCommand.split(" "); //$NON-NLS-1$

				IPath cmd = new org.eclipse.core.runtime.Path("sh");
				
				List<String> argList = new ArrayList<>();
				argList.add("-c"); //$NON-NLS-1$
				argList.add(cleanCommand);

				ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);

				launcher.setProject(getProject());
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher)launcher).setBuildConfiguration(this);
				}

				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(buildDir.toString());

				String[] env = new String[0];

				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
				Process p = launcher.execute(cmd, argList.toArray(new String[0]), env, workingDir, monitor);
				if (p == null || launcher.waitAndRead(epm.getOutputStream(), epm.getOutputStream(), SubMonitor.convert(monitor)) != ICommandLauncher.OK) {
					String errMsg = launcher.getErrorMessage();
					console.getErrorStream().write(String.format(Messages.MesonBuildConfiguration_RunningNinjaFailure, errMsg));
					return;
				}
			}
			
			outStream.write(String.format(Messages.MesonBuildConfiguration_BuildingComplete, buildDir.toString()));

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus(String.format(Messages.MesonBuildConfiguration_Cleaning, project.getName()), e));
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
				throw new CoreException(
						Activator.errorStatus(String.format(Messages.MesonBuildConfiguration_ProcCompCmds, project.getName()), e));
			}
		}
	}
	

}
