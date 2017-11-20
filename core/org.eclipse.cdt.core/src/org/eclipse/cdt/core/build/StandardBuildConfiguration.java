/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

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

	private String[] buildCommand;
	private String[] cleanCommand;
	private IContainer buildContainer;
	private IEnvironmentVariable[] envVars;

	public StandardBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
		applyProperties();
		setupEnvVars();
	}

	public StandardBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			String launchMode) throws CoreException {
		super(config, name, toolChain, launchMode);
		setupEnvVars();
	}

	private void applyProperties() {
		String container = getProperty(BUILD_CONTAINER);
		if (container != null && !container.trim().isEmpty()) {
			IPath containerLoc = new org.eclipse.core.runtime.Path(container);
			if (containerLoc.segmentCount() == 1) {
				buildContainer = ResourcesPlugin.getWorkspace().getRoot().getProject(containerLoc.segment(0));
			} else {
				buildContainer = ResourcesPlugin.getWorkspace().getRoot().getFolder(containerLoc);
			}
		}

		String buildCmd = getProperty(BUILD_COMMAND);
		if (buildCmd != null && !buildCmd.trim().isEmpty()) {
			buildCommand = buildCmd.split(" "); //$NON-NLS-1$
		}

		String cleanCmd = getProperty(CLEAN_COMMAND);
		if (cleanCmd != null && !cleanCmd.trim().isEmpty()) {
			cleanCommand = cleanCmd.split(" "); //$NON-NLS-1$
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

	public void setBuildContainer(IContainer buildContainer) {
		this.buildContainer = buildContainer;
		setProperty(BUILD_CONTAINER, buildContainer.getFullPath().toString());
	}

	public void setBuildCommand(String[] buildCommand) {
		this.buildCommand = buildCommand;
		setProperty(BUILD_COMMAND, String.join(" ", buildCommand)); //$NON-NLS-1$
	}

	public void setCleanCommand(String[] cleanCommand) {
		this.cleanCommand = cleanCommand;
	}

	private void createBuildContainer(IContainer container, IProgressMonitor monitor) throws CoreException {
		IContainer parent = container.getParent();
		if (!(parent instanceof IProject) && !parent.exists()) {
			createBuildContainer(parent, monitor);
		}

		if (container instanceof IFolder) {
			((IFolder) container).create(IResource.FORCE | IResource.DERIVED, true, monitor);
		}
	}

	@Override
	public IContainer getBuildContainer() throws CoreException {
		if (buildContainer == null) {
			return super.getBuildContainer();
		} else {
			if (!(buildContainer instanceof IProject) && !buildContainer.exists()) {
				createBuildContainer(buildContainer, new NullProgressMonitor());
			}
		}
		return buildContainer != null ? buildContainer : super.getBuildContainer();
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
			}
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

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			outStream.write(String.format(Messages.StandardBuildConfiguration_0, buildDir.toString()));

			List<String> command;
			if (buildCommand != null) {
				command = Arrays.asList(buildCommand);
				Path make = findCommand(buildCommand[0]);
				command.set(0, make.toString());
			} else {
				command = new ArrayList<>();
				command.add(findCommand("make").toString()); //$NON-NLS-1$
				if (!getBuildContainer().equals(getProject())) {
					Path makefile = Paths.get(getProject().getFile("Makefile").getLocationURI()); //$NON-NLS-1$
					Path relative = getBuildDirectory().relativize(makefile);
					command.add("-f"); //$NON-NLS-1$
					command.add(relative.toString());
				}
				command.add("all"); //$NON-NLS-1$
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getProject().getLocationURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());
				// run make
				console.getOutputStream().write(String.format("%s\n", String.join(" ", command))); //$NON-NLS-1$ //$NON-NLS-2$
				ProcessBuilder processBuilder = new ProcessBuilder(command)
						.directory(getBuildDirectory().toFile());
				setBuildEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
				IConsoleParser[] consoleParsers = new IConsoleParser[] { epm, this };
				watchProcess(process, consoleParsers);
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			outStream.write(String.format(Messages.StandardBuildConfiguration_1, buildDir.toString()));

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

			ConsoleOutputStream outStream = console.getOutputStream();

			List<String> command;
			if (cleanCommand != null) {
				command = Arrays.asList(cleanCommand);
			} else {
				command = new ArrayList<>();
				command.add(findCommand("make").toString()); //$NON-NLS-1$
				if (!getBuildContainer().equals(getProject())) {
					Path makefile = Paths.get(getProject().getFile("Makefile").getLocationURI()); //$NON-NLS-1$
					Path relative = getBuildDirectory().relativize(makefile);
					command.add("-f"); //$NON-NLS-1$
					command.add(relative.toString());
				}
				command.add("clean"); //$NON-NLS-1$
			}

			// run make
			outStream.write(String.format("%s\n", String.join(" ", command))); //$NON-NLS-1$ //$NON-NLS-2$
			ProcessBuilder processBuilder = new ProcessBuilder(command)
					.directory(getBuildDirectory().toFile());
			setBuildEnvironment(processBuilder.environment());
			Process process = processBuilder.start();
			watchProcess(process, console);

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Building " + project.getName(), e)); //$NON-NLS-1$
		}
	}

}
