/*******************************************************************************
 * Copyright (c) 2016, 2022 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.build.Messages;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * A Standard Build Configuration that simply calls a specified command for
 * build and clean. By default, it calls 'make all' and 'make clean'.
 *
 * @since 6.2
 */
public class StandardBuildConfiguration extends CBuildConfiguration {

	/**
	 * @since 6.4
	 */
	public static final String BUILD_CONTAINER = "stdbuild.build.container"; //$NON-NLS-1$
	/**
	 * @since 6.4
	 */
	public static final String BUILD_COMMAND = "stdbuild.build.command"; //$NON-NLS-1$
	/**
	 * @since 6.4
	 */
	public static final String CLEAN_COMMAND = "stdbuild.clean.command"; //$NON-NLS-1$

	private static final String DEFAULT_BUILD_COMMAND = "make"; //$NON-NLS-1$
	private static final String DEFAULT_CLEAN_COMMAND = "make clean"; //$NON-NLS-1$

	private String buildCommand = DEFAULT_BUILD_COMMAND;
	private String cleanCommand = DEFAULT_CLEAN_COMMAND;
	private IContainer buildContainer;
	private IEnvironmentVariable[] envVars;
	private Stack<String> directoryStack = new Stack<>();

	public StandardBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
		applyProperties();
		setupEnvVars();
	}

	/**
	 * @since 9.0
	 */
	public StandardBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain, String launchMode,
			ILaunchTarget launchTarget) throws CoreException {
		super(config, name, toolChain, launchMode, launchTarget);
		applyProperties();
		setupEnvVars();
	}

	private IContainer getContainer(String containerPath) {
		if (containerPath != null && !containerPath.trim().isEmpty()) {
			IPath containerLoc = new org.eclipse.core.runtime.Path(containerPath);
			if (containerLoc.segmentCount() == 1) {
				return ResourcesPlugin.getWorkspace().getRoot().getProject(containerLoc.segment(0));
			} else {
				return ResourcesPlugin.getWorkspace().getRoot().getFolder(containerLoc);
			}
		}
		return null;
	}

	private void applyProperties() {
		setBuildContainer(getProperty(BUILD_CONTAINER));

		String buildCommand = getProperty(BUILD_COMMAND);
		if (buildCommand != null && !buildCommand.isBlank()) {
			this.buildCommand = buildCommand;
		} else {
			this.buildCommand = DEFAULT_BUILD_COMMAND;
		}

		String cleanCommand = getProperty(CLEAN_COMMAND);
		if (cleanCommand != null && !cleanCommand.isBlank()) {
			this.cleanCommand = cleanCommand;
		} else {
			this.cleanCommand = DEFAULT_CLEAN_COMMAND;
		}
	}

	private void setupEnvVars() throws CoreException {
		IToolChain toolchain = getToolChain();
		List<IEnvironmentVariable> vars = new ArrayList<>();

		String[] cc = toolchain.getCompileCommands(GCCLanguage.getDefault());
		if (cc != null && cc.length > 0) {
			vars.add(new EnvironmentVariable("CC", cc[0])); //$NON-NLS-1$
		}

		String[] cxx = toolchain.getCompileCommands(GPPLanguage.getDefault());
		if (cxx != null && cxx.length > 0) {
			vars.add(new EnvironmentVariable("CXX", cxx[0])); //$NON-NLS-1$
		}

		String mode = getLaunchMode();
		if (mode != null && !mode.isEmpty()) {
			vars.add(new EnvironmentVariable("BUILD_MODE", mode)); //$NON-NLS-1$
		}

		envVars = vars.toArray(new IEnvironmentVariable[0]);
	}

	@Override
	public IEnvironmentVariable[] getVariables() {
		return envVars;
	}

	/**
	 * Set the build container based on the full path starting from workspace root.
	 *
	 * @param containerPath Path from workspace root.
	 */
	private void setBuildContainer(String containerPath) {
		IContainer container = null;
		if (containerPath != null && !containerPath.trim().isEmpty()) {
			container = getContainer(containerPath);
		}
		setBuildContainer(container);
	}

	public void setBuildContainer(IContainer buildContainer) {
		this.buildContainer = buildContainer;
		if (buildContainer == null) {
			setProperty(BUILD_CONTAINER, ""); // overwrite old property value.
		} else {
			setProperty(BUILD_CONTAINER, buildContainer.getFullPath().toString());
		}
	}

	/**
	 * Set the build command to use. The string will be converted to
	 * command line arguments using {@link CommandLineUtil}
	 *
	 * @since 9.0
	 */
	public void setBuildCommand(String buildCommand) {
		if (buildCommand != null && !buildCommand.isBlank()) {
			this.buildCommand = buildCommand;
			setProperty(BUILD_COMMAND, buildCommand);
		} else {
			this.buildCommand = DEFAULT_BUILD_COMMAND;
			removeProperty(BUILD_COMMAND);
		}
	}

	/**
	 * Set the build command to use. The string will be converted to
	 * command line arguments using {@link CommandLineUtil}
	 *
	 * @since 9.0
	 */
	public void setCleanCommand(String cleanCommand) {
		if (cleanCommand != null && !cleanCommand.isBlank()) {
			this.cleanCommand = cleanCommand;
			setProperty(CLEAN_COMMAND, cleanCommand);
		} else {
			this.cleanCommand = DEFAULT_CLEAN_COMMAND;
			removeProperty(CLEAN_COMMAND);
		}
	}

	@Override
	public IContainer getBuildContainer() throws CoreException {
		if (buildContainer == null) {
			setBuildContainer(getDefaultBuildContainer());
		}
		return buildContainer;
	}

	/**
	 * @since 6.4
	 */
	public IContainer getDefaultBuildContainer() throws CoreException {
		return super.getBuildContainer();
	}

	@Override
	public String getProperty(String name) {
		String prop = super.getProperty(name);
		if (prop != null) {
			return prop;
		}

		switch (name) {
		case BUILD_CONTAINER:
			try {
				return getBuildContainer().getFullPath().toString();
			} catch (CoreException e) {
				CCorePlugin.log(e.getStatus());
				return null;
			}
		case BUILD_COMMAND:
			return buildCommand;
		case CLEAN_COMMAND:
			return cleanCommand;
		}

		return null;
	}

	@Override
	public boolean setProperties(Map<String, String> properties) {
		if (!super.setProperties(properties)) {
			return false;
		}
		applyProperties();
		return true;
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream infoStream = console.getInfoStream();

			Path buildDir = getBuildDirectory();

			infoStream.write(String.format(Messages.StandardBuildConfiguration_0, buildDir.toString()));

			String[] parsedBuildCommand = CommandLineUtil.argumentsToArray(buildCommand);
			List<String> command = new ArrayList<>();
			command.add(parsedBuildCommand[0]);

			if (!getBuildContainer().equals(getProject())) {
				Path makefile = Paths.get(getProject().getFile("Makefile").getLocationURI()); //$NON-NLS-1$
				Path relative = getBuildDirectory().relativize(makefile);
				command.add("-f"); //$NON-NLS-1$
				command.add(relative.toString());
			}

			for (int i = 1; i < parsedBuildCommand.length; i++) {
				command.add(parsedBuildCommand[i]);
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getProject().getLocationURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());
				// run make
				console.getOutputStream()
						.write(String.format("%s\n", CommandLineUtil.argumentsToString(command, false))); //$NON-NLS-1$

				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(
						getBuildDirectory().toString());
				Process p = startBuildProcess(command, envVars, workingDir, console, monitor);

				if (p == null) {
					console.getErrorStream().write(String.format(Messages.StandardBuildConfiguration_Failure, "")); //$NON-NLS-1$
					return null;
				}

				IConsoleParser[] consoleParsers = new IConsoleParser[] { epm, this };
				watchProcess(consoleParsers, monitor);

				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

				infoStream.write(String.format(Messages.StandardBuildConfiguration_1, epm.getErrorCount(),
						epm.getWarningCount(), buildDir.toString()));
			}
			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Building " + project.getName(), e)); //$NON-NLS-1$
		}
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream infoStream = console.getInfoStream();

			Path buildDir = getBuildDirectory();

			infoStream.write(String.format(Messages.StandardBuildConfiguration_0, buildDir.toString()));

			List<String> command = new ArrayList<>();
			List<String> buildCommand;
			if (cleanCommand != null) {
				buildCommand = Arrays.asList(CommandLineUtil.argumentsToArray(cleanCommand));
			} else {
				buildCommand = Arrays.asList(CommandLineUtil.argumentsToArray(DEFAULT_CLEAN_COMMAND));
			}

			command.add(buildCommand.get(0));

			// we need to add -f if the makefile isn't in the build directory
			if (!getBuildContainer().equals(getProject())) {
				Path makefile = Paths.get(getProject().getFile("Makefile").getLocationURI()); //$NON-NLS-1$
				Path relative = getBuildDirectory().relativize(makefile);
				command.add("-f"); //$NON-NLS-1$
				command.add(relative.toString());
			}

			for (int i = 1; i < buildCommand.size(); ++i) {
				command.add(buildCommand.get(i));
			}

			// run make
			infoStream.write(String.format("%s\n", CommandLineUtil.argumentsToString(command, false))); //$NON-NLS-1$

			org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(
					getBuildDirectory().toString());
			Process p = startBuildProcess(command, envVars, workingDir, console, monitor);
			if (p == null) {
				console.getErrorStream().write(String.format(Messages.StandardBuildConfiguration_Failure, "")); //$NON-NLS-1$
				return;
			}

			watchProcess(console, monitor);

			infoStream.write(Messages.CBuildConfiguration_BuildComplete);

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Building " + project.getName(), e)); //$NON-NLS-1$
		}
	}

	private abstract class DirectoryPatternParser {
		private final Pattern pattern;

		public DirectoryPatternParser(String regex) {
			this.pattern = Pattern.compile(regex);
		}

		public void processLine(String line) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				recordDirectoryChange(matcher);
			}
		}

		abstract protected void recordDirectoryChange(Matcher matcher);
	}

	private final List<DirectoryPatternParser> enteringDirectoryPatterns = List.of( //
			//
			new DirectoryPatternParser("make\\[(.*)\\]: Entering directory [`'](.*)'") { //$NON-NLS-1$
				@Override
				protected void recordDirectoryChange(Matcher matcher) {
					int level;
					try {
						level = Integer.valueOf(matcher.group(1)).intValue();
					} catch (NumberFormatException e) {
						level = 0;
					}
					String dir = matcher.group(2);
					/*
					 * Sometimes make screws up the output, so "leave" events can't be seen. Double-check
					 * level here.
					 */
					int parseLevel = directoryStack.size();
					for (; level < parseLevel; level++) {
						if (!directoryStack.empty()) {
							directoryStack.pop();
						}
					}
					directoryStack.push(dir);
				}
			},

			// This is emitted by GNU make using options -w or --print-directory.
			new DirectoryPatternParser("make: Entering directory [`'](.*)'") { //$NON-NLS-1$
				@Override
				protected void recordDirectoryChange(Matcher matcher) {
					String dir = matcher.group(1);
					directoryStack.push(dir);
				}
			},

			//
			new DirectoryPatternParser("make(\\[.*\\])?: Leaving directory") { //$NON-NLS-1$
				@Override
				protected void recordDirectoryChange(Matcher matcher) {
					if (!directoryStack.empty()) {
						directoryStack.pop();
					}
				}
			}

	);

	@Override
	public boolean processLine(String line) {
		enteringDirectoryPatterns.forEach(p -> p.processLine(line));
		return super.processLine(line);
	}

	@Override
	public URI getBuildDirectoryURI() throws CoreException {
		if (!directoryStack.isEmpty()) {
			return Path.of(directoryStack.peek()).toUri();
		} else {
			return super.getBuildDirectoryURI();
		}
	}
}
