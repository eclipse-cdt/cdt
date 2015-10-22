/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.console.ArduinoConsoleService;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class is responsible for generating the Makefile for the current build config.
 */
public class ArduinoBuilder extends IncrementalProjectBuilder {

	public static final String ID = Activator.getId() + ".arduinoBuilder"; //$NON-NLS-1$

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);

			ArduinoConsoleService consoleService = Activator.getConsoleService();
			consoleService.writeOutput(String.format("\nBuilding %s\n", project.getName()));

			ArduinoBuildConfiguration config = getBuildConfig().getAdapter(ArduinoBuildConfiguration.class);
			config.generateMakeFile(monitor);

			ProcessBuilder processBuilder = new ProcessBuilder().command(config.getBuildCommand())
					.directory(config.getBuildDirectory());
			config.setEnvironment(processBuilder.environment());
			Process process = processBuilder.start();

			consoleService.monitor(process, config.getBuildConsoleParsers(), config.getBuildFolder());

			if (process.exitValue() == 0) {
				showSizes(config, consoleService);
			}

			config.getBuildFolder().refreshLocal(IResource.DEPTH_INFINITE, monitor);
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
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);

			ArduinoConsoleService consoleService = Activator.getConsoleService();
			consoleService.writeOutput(String.format("\nCleaning %s\n", project.getName()));

			ArduinoBuildConfiguration config = getBuildConfig().getAdapter(ArduinoBuildConfiguration.class);

			ProcessBuilder processBuilder = new ProcessBuilder().command(config.getCleanCommand())
					.directory(config.getBuildDirectory());
			config.setEnvironment(processBuilder.environment());
			Process process = processBuilder.start();

			consoleService.monitor(process, config.getBuildConsoleParsers(), config.getBuildFolder());

			config.getBuildFolder().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Build error", e));
		}
	}

	private void showSizes(ArduinoBuildConfiguration config, ArduinoConsoleService console) throws CoreException {
		try {
			int codeSize = -1;
			int dataSize = -1;

			String codeSizeRegex = config.getCodeSizeRegex();
			Pattern codeSizePattern = codeSizeRegex != null ? Pattern.compile(codeSizeRegex) : null;
			String dataSizeRegex = config.getDataSizeRegex();
			Pattern dataSizePattern = dataSizeRegex != null ? Pattern.compile(dataSizeRegex) : null;

			if (codeSizePattern == null && dataSizePattern == null) {
				return;
			}

			int maxCodeSize = config.getMaxCodeSize();
			int maxDataSize = config.getMaxDataSize();

			ProcessBuilder processBuilder = new ProcessBuilder().command(config.getSizeCommand())
					.directory(config.getBuildDirectory()).redirectErrorStream(true);
			config.setEnvironment(processBuilder.environment());
			Process process = processBuilder.start();
			try (BufferedReader processOut = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				for (String line = processOut.readLine(); line != null; line = processOut.readLine()) {
					if (codeSizePattern != null) {
						Matcher matcher = codeSizePattern.matcher(line);
						if (matcher.matches()) {
							codeSize += Integer.parseInt(matcher.group(1));
						}
					}
					if (dataSizePattern != null) {
						Matcher matcher = dataSizePattern.matcher(line);
						if (matcher.matches()) {
							dataSize += Integer.parseInt(matcher.group(1));
						}
					}
				}
			}

			console.writeOutput("Program store usage: " + codeSize);
			if (maxCodeSize > 0) {
				console.writeOutput(" of maximum " + maxCodeSize);
			}
			console.writeOutput(" bytes\n");

			if (maxDataSize >= 0) {
				console.writeOutput("Initial RAM usage: " + dataSize);
				if (maxCodeSize > 0) {
					console.writeOutput(" of maximum " + maxDataSize);
				}
				console.writeOutput(" bytes\n");
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Checking sizes", e));
		}
	}

}
