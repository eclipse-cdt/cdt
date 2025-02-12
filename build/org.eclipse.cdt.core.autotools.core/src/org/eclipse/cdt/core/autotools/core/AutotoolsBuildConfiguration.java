/*******************************************************************************
 * Copyright (c) 2017 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.autotools.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public class AutotoolsBuildConfiguration extends CBuildConfiguration {

	public static final String AUTOTOOLS_GENERATOR = "autotools.generator"; //$NON-NLS-1$
	public static final String AUTOTOOLS_ARGUMENTS = "autotools.arguments"; //$NON-NLS-1$
	public static final String BUILD_COMMAND = "autotools.command.build"; //$NON-NLS-1$
	public static final String CLEAN_COMMAND = "autotools.command.clean"; //$NON-NLS-1$

	public AutotoolsBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
	}

	public AutotoolsBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain, String launchMode,
			ILaunchTarget launchTarget) throws CoreException {
		super(config, name, toolChain, launchMode, launchTarget);
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {

		IProject project = getProject();

		executeRemote(List.of("autoreconf", "--install"), project.getLocation(), //$NON-NLS-1$//$NON-NLS-2$
				console, monitor);

		String configure = "./configure"; //$NON-NLS-1$
		java.nio.file.Path cmdPath = java.nio.file.Path.of(project.getLocation().toString(), configure);
		if (cmdPath.toFile().exists()) {
			configure = cmdPath.toAbsolutePath().toString();
		}
		executeRemote(List.of(configure), new Path(getBuildDirectory().toString()), console, monitor);

		executeRemote(List.of("make"), new Path(getBuildDirectory().toString()), console, monitor); //$NON-NLS-1$

		return new IProject[] { project };
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		executeRemote(List.of("make", "clean"), new Path(getBuildDirectory().toString()), console, monitor); //$NON-NLS-1$//$NON-NLS-2$
	}

	protected void executeRemote(List<String> command, IPath processCwd, IConsole console, IProgressMonitor monitor)
			throws CoreException {

		IProject project = getProject();

		String commandJoined = String.join(" ", command); //$NON-NLS-1$
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream infoStream = console.getInfoStream();

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());

				IEnvironmentVariable[] env = new IEnvironmentVariable[0];

				infoStream.write("Building in: " + processCwd.toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				infoStream.write("Running: " + commandJoined + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				Process p = startBuildProcess(command, env, processCwd, console, monitor);
				if (p == null) {
					console.getErrorStream().write("Error executing: " + commandJoined); //$NON-NLS-1$
					throw new CoreException(Status.error("Error executing: " + commandJoined)); //$NON-NLS-1$
				}

				watchProcess(new IConsoleParser[] { epm }, monitor);
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

		} catch (IOException e) {
			throw new CoreException(Status.error("Error executing: " + commandJoined, e)); //$NON-NLS-1$
		}

	}

}
