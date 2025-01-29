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

import org.eclipse.cdt.cmake.core.properties.ICMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

/**
 * Builds lists of command-line arguments and environment variables for build-script generation
 * and/or for performing the actual build of a project.
 *
 * @author Martin Weber
 */
public class CommandDescriptorBuilder {

	private final ICMakeProperties cmakeProperties;

	/**
	 * @param cmakeProperties the project properties related to the cmake command
	 */
	public CommandDescriptorBuilder(ICMakeProperties cmakeProperties) {
		this.cmakeProperties = Objects.requireNonNull(cmakeProperties);
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
	public CommandDescriptor makeCMakeCommandline(Path toolChainFile) throws CoreException {
		List<String> args = new ArrayList<>();
		List<String> env = new ArrayList<>();

		// cmake command
		IStringVariableManager varManager = VariablesPlugin.getDefault().getStringVariableManager();
		args.add(varManager.performStringSubstitution(cmakeProperties.getCommand()));

		/* add general settings */
		if (cmakeProperties.isWarnNoDev())
			args.add("-Wno-dev"); //$NON-NLS-1$
		if (cmakeProperties.isDebugTryCompile())
			args.add("--debug-trycompile"); //$NON-NLS-1$
		if (cmakeProperties.isDebugOutput())
			args.add("--debug-output"); //$NON-NLS-1$
		if (cmakeProperties.isTrace())
			args.add("--trace"); //$NON-NLS-1$
		if (cmakeProperties.isWarnUninitialized())
			args.add("--warn-uninitialized"); //$NON-NLS-1$
		if (cmakeProperties.isWarnUnusedVars())
			args.add("--warn-unused-vars"); //$NON-NLS-1$
		{
			String file = cmakeProperties.getCacheFile();
			if (!(file == null || file.isBlank())) {
				args.add("-C"); //$NON-NLS-1$
				args.add(file);
			}
		}

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

		args.add("-G"); //$NON-NLS-1$
		final ICMakeGenerator generator = cmakeProperties.getGenerator();
		args.add(generator.getCMakeName());

		CommandDescriptorBuilder.appendCMakeArguments(args, cmakeProperties.getExtraArguments());

		return new CommandDescriptor(args, env);
	}

	/**
	 * Builds the command-line for cmake to build the project. The first argument will be the
	 * cmake-command.
	 *
	 * @return the command-line arguments and environment to invoke cmake.
	 * @throws CoreException
	 */
	public CommandDescriptor makeCMakeBuildCommandline(String buildscriptTarget) throws CoreException {
		List<String> args = new ArrayList<>();
		List<String> env = new ArrayList<>();

		IStringVariableManager varManager = VariablesPlugin.getDefault().getStringVariableManager();
		String cmd = varManager.performStringSubstitution(cmakeProperties.getCommand());
		args.add(cmd);
		args.add("--build"); //$NON-NLS-1$
		args.add("."); //$NON-NLS-1$
		args.add("--target"); //$NON-NLS-1$
		args.add(buildscriptTarget);
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
	private static void appendCMakeArguments(List<String> argList, final List<String> moreArgs) throws CoreException {
		IStringVariableManager mgr = VariablesPlugin.getDefault().getStringVariableManager();
		for (String arg : moreArgs) {
			String expanded = mgr.performStringSubstitution(arg);
			argList.add(expanded);
		}
	}

	/**
	 * Command-line arguments and additional environment variables to be used to run a process.
	 * @author Martin Weber
	 */
	public static final class CommandDescriptor {
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
		 *
		 * @return a non-empty list containing at least the name of the command to invoke.
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
