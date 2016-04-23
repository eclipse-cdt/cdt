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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class CMakeBuilder extends IncrementalProjectBuilder {

	public static final String ID = Activator.getId() + ".cmakeBuilder"; //$NON-NLS-1$

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			IConsole console = CCorePlugin.getDefault().getConsole();
			ConsoleOutputStream outStream = console.getOutputStream();
			
			CMakeBuildConfiguration cmakeConfig = project.getActiveBuildConfig()
					.getAdapter(CMakeBuildConfiguration.class);
			Path buildDir = cmakeConfig.getBuildDirectory();

			if (!Files.exists(buildDir.resolve("Makefile"))) { //$NON-NLS-1$
				// TODO assuming cmake is in the path here, probably need a
				// preference in case it isn't.
				List<String> command = Arrays.asList("cmake", //$NON-NLS-1$
						"-DCMAKE_EXPORT_COMPILE_COMMANDS=ON", new File(project.getLocationURI()).getAbsolutePath());
				ProcessBuilder processBuilder = new ProcessBuilder(command).directory(buildDir.toFile());
				Process process = processBuilder.start();
				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
				//console.monitor(process, null, buildDir);
			}

			// TODO need to figure out which builder to call. Hardcoding to make
			// for now.
			List<String> command = Arrays.asList("make");
			ProcessBuilder processBuilder = new ProcessBuilder(command).directory(buildDir.toFile()); //$NON-NLS-1$
			Process process = processBuilder.start();
			outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
			//console.monitor(process, null, buildDir);

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus("Building " + project.getName(), e));
		}
	}

}
