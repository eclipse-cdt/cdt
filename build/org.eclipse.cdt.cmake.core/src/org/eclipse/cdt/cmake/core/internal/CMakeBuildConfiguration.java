/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class CMakeBuildConfiguration extends CBuildConfiguration {

	public CMakeBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
	}

	public CMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		super(config, name, toolChain);
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false,  IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();
			
			Path buildDir = getBuildDirectory();

			if (!Files.exists(buildDir.resolve("Makefile"))) { //$NON-NLS-1$
				// TODO assuming cmake is in the path here, probably need a
				// preference in case it isn't.
				List<String> command = Arrays.asList("cmake", //$NON-NLS-1$
						"-DCMAKE_EXPORT_COMPILE_COMMANDS=ON", new File(project.getLocationURI()).getAbsolutePath()); //$NON-NLS-1$
				ProcessBuilder processBuilder = new ProcessBuilder(command).directory(buildDir.toFile());
				Process process = processBuilder.start();
				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
				watchProcess(process, new IConsoleParser[0], console);
			}

			// TODO need to figure out which builder to call. Hardcoding to make
			// for now.
			List<String> command = Arrays.asList("make"); //$NON-NLS-1$
			ProcessBuilder processBuilder = new ProcessBuilder(command).directory(buildDir.toFile());
			Process process = processBuilder.start();
			outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
			
			// TODO error parsers
			watchProcess(process, new IConsoleParser[0], console);

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus(String.format("Building %s", project.getName()), e));
		}
	}
	
	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
