/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface implemented by toolchain integrators to perform the actual build.
 * 
 * @author Doug Schaefer
 * @since 8.0
 */
public interface IBuildRunner {

	/**
	 * Perform the build.
	 * 
	 * @param kind kind from the IncrementalProjectBuilder
	 * @param project project being built
	 * @param configuration configuration being built
	 * @param console console to use for build output
	 * @param markerGenerator generator to add markers for build problems 
	 * @param monitor progress monitor
	 * @throws CoreException standard core exception of something goes wrong
	 */
	public boolean invokeBuild(int kind, IProject project, IConfiguration configuration,
			IBuilder builder, IConsole console, IMarkerGenerator markerGenerator,
			IncrementalProjectBuilder projectBuilder, IProgressMonitor monitor) throws CoreException;
	
}
