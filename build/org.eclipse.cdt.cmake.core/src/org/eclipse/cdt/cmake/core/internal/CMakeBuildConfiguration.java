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
import java.io.OutputStream;
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
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
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

	private void saveToolChainFile() {
		Preferences settings = getSettings();
		settings.put(TOOLCHAIN_FILE, toolChainFile.getPath().toString());
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	protected String[] convertEnvironment(IEnvironmentVariable[] vars) {
		String[] results = new String[vars.length];
		for (int i = 0; i < vars.length; i++)
			results[i] = vars[i].getName() + "=" + vars[i].getValue();
		return results;
	}

	public static class Command {
		private IPath commandPath;
		private String[] commandArgs;

		public Command(String cmd, String[] args) {
			this.commandPath = new org.eclipse.core.runtime.Path(cmd);
			this.commandArgs = (args == null) ? new String[0] : args;
		}

		public Command(org.eclipse.core.runtime.Path cmdPath, String[] args) {
			this.commandPath = cmdPath;
			this.commandArgs = (args == null) ? new String[0] : args;
		}

		public Command(String cmd) {
			this(cmd, null);
		}

		public IPath getCommandPath() {
			return commandPath;
		}

		public String[] getCommandArgs() {
			return commandArgs;
		}

		public String toString() {
			StringBuffer buf = new StringBuffer(commandPath.toString());
			for (String s : commandArgs)
				buf.append(" ").append(s);
			return buf.toString();
		}
	}

	protected void printToConsole(Command command, String[] env, org.eclipse.core.runtime.Path buildTargetDirPath,
			ICommandLauncher cm, IConsole console)throws IOException, CoreException {
		ConsoleOutputStream outs = console.getOutputStream();
		// Start the process that executes the command if ICommandLauncher
		outs.write("----Command Launcher Debug----\n");
		outs.write("Command launcher=" + cm.getClass().toString());
		outs.write("\n");
		outs.write("Command: " + command);
		outs.write("\n");
		outs.write("Environment: " + Arrays.asList(env));
		outs.write("\n");
		outs.write("Build Path: " + buildTargetDirPath);
		outs.write("\n");
		outs.write("----End Command Launcher Debug----\n");
	}

	protected void execCommands(Command[] commands, IConsole console, IConsoleParser[] consoleParsers,
			IProgressMonitor monitor) throws IOException, CoreException {
		IToolChain tc = this.getToolChain();
		ICommandLauncher cm = tc.getCommandLauncher();
		org.eclipse.core.runtime.Path buildTargetDirPath = new org.eclipse.core.runtime.Path(
				getBuildDirectory().toFile().getPath());
		cm.setProject(getProject());
		for (int i = 0; i < commands.length; i++) {
			Command command = commands[i];
			IPath commandPath = command.getCommandPath();
			String[] commandArgs = command.getCommandArgs();
			// Set error message to the command + args
			cm.setErrorMessage(
					"Error in build/clean for command=" + commandPath + ";args=" + Arrays.asList(commandArgs));
			// Convert the environmentvariables from the tool chain
			String[] env = convertEnvironment(tc.getVariables());
			// printCommand to console
			printToConsole(command, env, buildTargetDirPath, cm, console);

			Process process = cm.execute(commandPath, commandArgs, env, buildTargetDirPath, monitor);
			// Watch/wait process for completion with console parsers and console
			watchProcess(process, (consoleParsers == null) ? new IConsoleParser[0] : consoleParsers, console);
		}
	}

	protected Command[] getGeneratorCommands(int kind, Map<String, String> args) {
		String generator = getProperty(CMAKE_GENERATOR);
		if (generator == null) {
			generator = "Ninja"; //$NON-NLS-1$
		}
		Path cmakePath = findCommand("cmake"); //$NON-NLS-1$
		String command = (cmakePath != null) ? cmakePath.toString() : "cmake";
		List<String> cArgs = new ArrayList<String>();

		cArgs.add("-G"); //$NON-NLS-1$
		cArgs.add(generator);

		if (toolChainFile != null) {
			cArgs.add("-DCMAKE_TOOLCHAIN_FILE=" + toolChainFile.getPath().toString()); //$NON-NLS-1$
		}

		switch (getLaunchMode()) {
		// TODO what to do with other modes
		case "debug": //$NON-NLS-1$
			cArgs.add("-DCMAKE_BUILD_TYPE=Debug"); //$NON-NLS-1$
			break;
		}

		cArgs.add("-DCMAKE_EXPORT_COMPILE_COMMANDS=ON"); //$NON-NLS-1$

		String userArgs = getProperty(CMAKE_ARGUMENTS);
		if (userArgs != null) {
			cArgs.addAll(Arrays.asList(userArgs.trim().split("\\s+"))); //$NON-NLS-1$
		}

		cArgs.add("-DCMAKE_SYSTEM_NAME=Generic");

		cArgs.add(new File(getProject().getLocationURI()).getAbsolutePath());

		cArgs.add(0, command);

		return new Command[] { new Command(command, cArgs.toArray(new String[cArgs.size()])) };

	}

	protected Command[] getBuildCommands(int kind, Map<String, String> args) {
		String generator = getProperty(CMAKE_GENERATOR);
		if (generator == null) {
			generator = "Ninja"; //$NON-NLS-1$
		}
		String buildCommand = getProperty(BUILD_COMMAND);
		if (buildCommand == null) {
			if (generator.equals("Ninja")) { //$NON-NLS-1$
				buildCommand = "ninja"; //$NON-NLS-1$
			} else {
				buildCommand = "make"; //$NON-NLS-1$
			}
		}
		String[] commands = buildCommand.split(" "); //$NON-NLS-1$

		Path cmdPath = findCommand(commands[0]);
		if (cmdPath != null) {
			commands[0] = cmdPath.toString();
		}
		String[] argsArr = new String[commands.length - 1];
		System.arraycopy(commands, 1, argsArr, 0, argsArr.length);

		return new Command[] { new Command(commands[0], argsArr) };
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		return doBuild(kind, args, console, monitor);
	}

	protected IProject[] doBuild(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		try {

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();
			Path buildDir = getBuildDirectory();
			outStream.write(String.format(Messages.CMakeBuildConfiguration_BuildingIn, buildDir.toString()));

			if (!Files.exists(buildDir.resolve("CMakeFiles"))) { //$NON-NLS-1$
				// get generator commands
				Command[] generatorCommands = getGeneratorCommands(kind, args);
				// execute generator commands
				execCommands(generatorCommands, console, null, monitor);
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				// get build commands
				Command[] buildCommands = getBuildCommands(kind, args);
				// execute build commands
				execCommands(buildCommands, console, new IConsoleParser[] { epm }, monitor);
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			// Load compile_commands.json file
			processCompileCommandsFile(monitor);

			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(Activator
					.errorStatus(String.format(Messages.CMakeBuildConfiguration_Building, project.getName()), e));
		}
	}

	protected Command[] getCleanCommands() {
		String generator = getProperty(CMAKE_GENERATOR);
		if (generator == null) {
			generator = "Ninja"; //$NON-NLS-1$
		}
		String cleanCommand = getProperty(CLEAN_COMMAND);
		if (cleanCommand == null) {
			if (generator == null || generator.equals("Ninja")) { //$NON-NLS-1$
				cleanCommand = "ninja clean"; //$NON-NLS-1$
			} else {
				cleanCommand = "make clean"; //$NON-NLS-1$
			}
		}
		String[] commands = cleanCommand.split(" "); //$NON-NLS-1$

		Path cmdPath = findCommand(commands[0]);
		if (cmdPath != null) {
			commands[0] = cmdPath.toString();
		}

		String[] argsArr = new String[commands.length - 1];
		System.arraycopy(commands, 1, argsArr, 0, argsArr.length);

		return new Command[] { new Command(commands[0], argsArr) };
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		doClean(console, monitor);
	}

	protected void doClean(IConsole console, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			// delete markers
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();
			// If CMakeFiles does not exist in buildDir, just write to outputstream and
			// return
			if (!Files.exists(buildDir.resolve("CMakeFiles"))) { //$NON-NLS-1$
				outStream.write(Messages.CMakeBuildConfiguration_NotFound);
				return;
			}
			// Get clean commands
			Command[] cleanCommands = getCleanCommands();
			// Exec clean commands
			execCommands(cleanCommands, console, null, monitor);
			// Refresh project
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(Activator
					.errorStatus(String.format(Messages.CMakeBuildConfiguration_Cleaning, project.getName()), e));
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
				throw new CoreException(Activator.errorStatus(
						String.format(Messages.CMakeBuildConfiguration_ProcCompCmds, project.getName()), e));
			}
		}
	}

}
