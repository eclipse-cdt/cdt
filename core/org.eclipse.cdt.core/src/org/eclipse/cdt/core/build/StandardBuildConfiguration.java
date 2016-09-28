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
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A Standard Build Configuration that simply calls a specified command for
 * build and clean. By default, it calls 'make all' and 'make clean'.
 * 
 * @since 6.2
 */
public class StandardBuildConfiguration extends CBuildConfiguration {

	private String[] buildCommand = { "make", "all" }; //$NON-NLS-1$ //$NON-NLS-2$
	private String[] cleanCommand = { "make", "clean" }; //$NON-NLS-1$ //$NON-NLS-2$
	private IContainer buildContainer;

	public StandardBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
	}

	public StandardBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			String launchMode) {
		super(config, name, toolChain);
	}

	public void setBuildContainer(IContainer buildContainer) {
		this.buildContainer = buildContainer;
	}

	public void setBuildCommand(String[] buildCommand) {
		this.buildCommand = buildCommand;
	}

	public void setCleanCommand(String[] cleanCommand) {
		this.cleanCommand = cleanCommand;
	}

	@Override
	public IContainer getBuildContainer() throws CoreException {
		// If a container isn't set, assume build bits can go anywhere in the
		// project
		return buildContainer != null ? buildContainer : getProject();
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			outStream.write(String.format("Building in: %s\n", buildDir.toString()));

			String[] command = new String[buildCommand.length];
			Path make = findCommand(buildCommand[0]);
			command[0] = make.toString();
			System.arraycopy(buildCommand, 1, command, 1, buildCommand.length - 1);

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				// run make
				console.getOutputStream().write(String.format("%s\n", String.join(" ", command))); //$NON-NLS-1$ //$NON-NLS-2$
				ProcessBuilder processBuilder = new ProcessBuilder(command)
						.directory(getBuildDirectory().toFile());
				setBuildEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
				IConsoleParser[] consoleParsers = new IConsoleParser[] { epm, this };
				watchProcess(process, consoleParsers, console);
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

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

			String[] command = new String[cleanCommand.length];
			Path make = findCommand(cleanCommand[0]);
			command[0] = make.toString();
			System.arraycopy(cleanCommand, 1, command, 1, cleanCommand.length - 1);

			// run make
			outStream.write(String.format("%s\n", String.join(" ", command))); //$NON-NLS-1$ //$NON-NLS-2$
			ProcessBuilder processBuilder = new ProcessBuilder(command)
					.directory(getBuildDirectory().toFile());
			setBuildEnvironment(processBuilder.environment());
			Process process = processBuilder.start();
			watchProcess(process, new IConsoleParser[0], console);

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Building " + project.getName(), e)); //$NON-NLS-1$
		}
	}

}
