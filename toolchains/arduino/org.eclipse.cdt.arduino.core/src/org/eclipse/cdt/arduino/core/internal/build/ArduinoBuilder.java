/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.build;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.console.ArduinoConsoleService;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class is responsible for generating the Makefile for the current build
 * config.
 */
public class ArduinoBuilder extends IncrementalProjectBuilder {

	public static final String ID = Activator.getId() + ".arduinoBuilder"; //$NON-NLS-1$

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			ArduinoConsoleService consoleService = Activator.getConsoleService();
			consoleService.writeOutput(String.format("\nBuilding %s\n", project.getName()));

			ArduinoBuildConfiguration config = getBuildConfig().getAdapter(ArduinoBuildConfiguration.class);
			config.generateMakeFile(monitor);

			IFolder buildFolder = config.getBuildFolder();
			Process process = Runtime.getRuntime().exec(config.getBuildCommand(), config.getEnvironment(),
					new File(buildFolder.getLocationURI()));

			consoleService.monitor(process, null);

			buildFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Build error", e));
		}

		// TODO if there are references we want to watch, return them here
		return new IProject[] { project };
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		try {
			IProject project = getProject();
			ArduinoConsoleService consoleService = Activator.getConsoleService();
			consoleService.writeOutput(String.format("\nCleaning %s\n", project.getName()));

			ArduinoBuildConfiguration config = getBuildConfig().getAdapter(ArduinoBuildConfiguration.class);

			IFolder buildFolder = config.getBuildFolder();
			Process process = Runtime.getRuntime().exec(config.getCleanCommand(), config.getEnvironment(),
					new File(buildFolder.getLocationURI()));

			consoleService.monitor(process, null);

			buildFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Build error", e));
		}
	}

}
