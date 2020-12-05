/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;
import org.eclipse.cdt.cmake.core.properties.IOsOverrides;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

/**
 * Builds lists of command-line arguments and environment variables for build-script generation
 * and/or for performing the actual build of a project.
 *
 * @author Martin Weber
 */
class CommandDescriptorBuilder {

	private final ICMakeProperties cmakeProperties;
	private final IOsOverridesSelector overridesSelector;

	/**
	 * @param cmakeProperties the project properties related to the cmake command
	 */
	CommandDescriptorBuilder(ICMakeProperties cmakeProperties, IOsOverridesSelector overridesSelector) {
		this.cmakeProperties = Objects.requireNonNull(cmakeProperties);
		this.overridesSelector = Objects.requireNonNull(overridesSelector);
	}

	/**
	 * Builds the command-line for cmake to generate the build-scripts.
	 *
	 * @param toolChainFile the cmake toolchain file or {@code null} if cmake should use the native
	 *                      tools it will find in the PATH environment variable
	 *
	 * @return the command-line arguments and environment to invoke cmake.
	 * @throws CoreException
	 */
	CommandDescriptor makeGenerateCommandline(Path toolChainFile) throws CoreException {
		List<String> args = new ArrayList<>();
		List<String> env = new ArrayList<>();

		// defaults for all OSes...
		args.add("cmake"); //$NON-NLS-1$
		/* add general settings */
		if (cmakeProperties.isWarnNoDev())
			args.add("-Wno-dev"); //$NON-NLS-1$
		if (cmakeProperties.isDebugTryCompile())
			args.add("--debug-trycompile"); //$NON-NLS-1$
		if (cmakeProperties.isDebugOutput())
			args.add("--debug-output"); //$NON-NLS-1$
		if (cmakeProperties.isTrace())
			args.add("--trace"); //$NON-NLS-1$
		if (cmakeProperties.isWarnUnitialized())
			args.add("--warn-unitialized"); //$NON-NLS-1$
		if (cmakeProperties.isWarnUnused())
			args.add("--warn-unused"); //$NON-NLS-1$
		{
			String file = cmakeProperties.getCacheFile();
			if (!(file == null || file.isBlank())) {
				appendArguments(args, "-C", file); //$NON-NLS-1$
			}
		}

		final String[] extraArgs = CommandLineUtil.argumentsToArrayWindowsStyle(cmakeProperties.getExtraArguments());
		appendArguments(args, extraArgs);
		/* add settings for the build platform */
		appendCMakeOsOverrideArgs(args, overridesSelector.getOsOverrides(cmakeProperties));

		/* at last, add our requirements that override extra args specified by the user... */
		{
			// set argument for build type..
			String bt = cmakeProperties.getBuildType();
			if (!(bt == null || bt.isBlank())) {
				args.add("-DCMAKE_BUILD_TYPE=" + bt); //$NON-NLS-1$
			}
			// tell cmake to write compile commands to a JSON file
			args.add("-DCMAKE_EXPORT_COMPILE_COMMANDS=ON"); //$NON-NLS-1$
		}
		if (toolChainFile != null) {
			args.add("-DCMAKE_TOOLCHAIN_FILE=" + toolChainFile.toString()); //$NON-NLS-1$
		}

		return new CommandDescriptor(args, env);
	}

	/**
	 * Builds the command-line for cmake to build the project. The first argument will be the
	 * cmake-command.
	 *
	 * @return the command-line arguments and environment to invoke cmake.
	 * @throws CoreException
	 */
	CommandDescriptor makeBuildCommandline(String buildscriptTarget) throws CoreException {
		List<String> args = new ArrayList<>();
		List<String> env = new ArrayList<>();

		IOsOverrides osOverrides = overridesSelector.getOsOverrides(cmakeProperties);
		if (osOverrides.getUseDefaultCommand()) {
			args.add("cmake"); //$NON-NLS-1$
		} else {
			IStringVariableManager varManager = VariablesPlugin.getDefault().getStringVariableManager();
			String cmd = varManager.performStringSubstitution(osOverrides.getCommand());
			args.add(cmd);
		}
		appendArguments(args, "--build", ".", "--target", buildscriptTarget); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// TODO parallel build: use CMAKE_BUILD_PARALLEL_LEVEL envvar (since cmake 3.12)
		// TODO verbose build: use VERBOSE envvar (since cmake 3.14)
		// TODO stop on first error: query CMakeGenerator object for argument
		return new CommandDescriptor(args, env);
	}

	/**
	 * Appends the additional arguments to pass on the cmake command-line. Performs variable
	 * substitutions.
	 *
	 * @param argList  the list to append cmake-arguments to
	 * @param moreArgs the arguments to substitute and append
	 * @throws CoreException if unable to resolve the value of one or more variables
	 */
	private static void appendArguments(List<String> argList, String... moreArgs) throws CoreException {
		IStringVariableManager mgr = VariablesPlugin.getDefault().getStringVariableManager();
		for (String arg : moreArgs) {
			String expanded = mgr.performStringSubstitution(arg);
			argList.add(expanded);
		}
	}

	/**
	 * Appends arguments specific to the given OS preferences for build-script generation. The first
	 * argument in the list will be replaced by the cmake command from the specified preferences, if
	 * given.
	 *
	 * @param args  the list to append cmake-arguments to.
	 * @param prefs the generic OS specific cmake build properties to convert and append.
	 * @throws CoreException if unable to resolve the value of one or more variables
	 */
	private static void appendCMakeOsOverrideArgs(List<String> args, final IOsOverrides prefs) throws CoreException {
		// replace cmake command, if given
		if (!prefs.getUseDefaultCommand()) {
			IStringVariableManager varManager = VariablesPlugin.getDefault().getStringVariableManager();
			String cmd = varManager.performStringSubstitution(prefs.getCommand());
			args.set(0, cmd);
		}
		appendArguments(args, "-G", prefs.getGenerator().getCMakeName()); //$NON-NLS-1$
		final String[] extraArgs = CommandLineUtil.argumentsToArrayWindowsStyle(prefs.getExtraArguments());
		appendArguments(args, extraArgs);
	}

	/**
	 * Command-lin arguments and additional environment variables to be used to run a process.
	 * @author Martin Weber
	 */
	static final class CommandDescriptor {
		private final List<String> arguments;
		private final List<String> environment;

		/**
		 * @param arguments
		 * @param environment the list of environment variables in variable=value format to pass to
		 *                    the process.
		 */
		CommandDescriptor(List<String> arguments, List<String> environment) {
			this.arguments = Objects.requireNonNull(arguments);
			this.environment = Objects.requireNonNull(environment);
		}

		/**
		 * Gets the command-line arguments for the process.
		 */
		public List<String> getArguments() {
			return arguments;
		}

		/**
		 * Gets the list of environment variables in variable=value format to pass to the process.
		 */
		public List<String> getEnvironment() {
			return environment;
		}
	}
}
