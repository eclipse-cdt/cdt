/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.util.Map;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This is the root interface for "new style" CDT build configurations. Adapting
 * IBuildConfiguration to this interface will get you one of these. From here,
 * adapt to the specific interface that you need and the configuration will
 * provide one.
 * 
 * @since 6.0
 */
public interface ICBuildConfiguration extends IAdaptable, IScannerInfoProvider {

	/**
	 * Returns the resources build configuration that this CDT build
	 * configuration is associated with.
	 * 
	 * @return resources build configuration
	 */
	IBuildConfiguration getBuildConfiguration() throws CoreException;

	/**
	 * Build Configurations are configurations for a given toolchain.
	 * 
	 * @return the toolchain for this build configuration
	 */
	IToolChain getToolChain() throws CoreException;

	IEnvironmentVariable getVariable(String name) throws CoreException;

	IEnvironmentVariable[] getVariables() throws CoreException;

	IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor) throws CoreException;

	void clean(IConsole console, IProgressMonitor monitor) throws CoreException;
	
}
