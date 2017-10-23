/*******************************************************************************
 * Copyright (c) 2017 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.autotools.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.autotools.core.internal.Activator;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class AutotoolsBuildConfiguration extends CBuildConfiguration {

	public static final String AUTOTOOLS_GENERATOR = "autotools.generator"; //$NON-NLS-1$
	public static final String AUTOTOOLS_ARGUMENTS = "autotools.arguments"; //$NON-NLS-1$
	public static final String BUILD_COMMAND = "autotools.command.build"; //$NON-NLS-1$
	public static final String CLEAN_COMMAND = "autotools.command.clean"; //$NON-NLS-1$

	public AutotoolsBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
	}

	public AutotoolsBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		super(config, name, toolChain, "run"); // TODO: why "run"
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		
		IProject project = getProject();
		
		execute(Arrays.asList(new String[] { "autoreconf", "--install" }), project.getLocation(), console, monitor);
		execute(Arrays.asList(new String[] { "./configure" }), project.getLocation(), console, monitor);
		execute(Arrays.asList(new String[] { "make" }), project.getLocation(), console, monitor);
		
		return new IProject[] { project };
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		execute(Arrays.asList(new String[] { "make", "clean" }), getProject().getLocation(), console, monitor);
	}

	protected void execute(List<String> command, IPath dir, IConsole console, IProgressMonitor monitor) throws CoreException {
		
		ProcessBuilder builder = new ProcessBuilder(command).directory(dir.toFile());

		try {
			Process process = builder.start();
			watchProcess(process, new IConsoleParser[0], console);
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus("Error executing: " + String.join(" ",  command), e));
		}

		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

}
