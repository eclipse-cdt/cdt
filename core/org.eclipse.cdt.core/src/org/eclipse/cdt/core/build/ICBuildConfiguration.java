/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.IBinary;
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
	 * CDT doesn't like that the Platform default config name is an empty string.
	 * It needs a real name for the name of the build directory, for example.
	 */
	public static String DEFAULT_NAME = "default"; //$NON-NLS-1$
	
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
	
	/**
	 * Ids for the Binary Parsers to use when checking whether a file is a
	 * binary that can be launched.
	 * 
	 * @return binary parser ids
	 * @throws CoreException
	 */
	String getBinaryParserId() throws CoreException;

	/**
	 * Return a build environment variable with a given name.
	 * 
	 * @param name
	 *            build environment variable name
	 * @return value of the build environment variable.
	 * @throws CoreException
	 */
	IEnvironmentVariable getVariable(String name) throws CoreException;

	/**
	 * Return all of the build environment variables for this configuration.
	 * 
	 * @return
	 * @throws CoreException
	 */
	IEnvironmentVariable[] getVariables() throws CoreException;

	/**
	 * Perform the build.
	 * 
	 * @param kind
	 *            build type
	 * @param args
	 *            build arguments
	 * @param console
	 *            console to show build output
	 * @param monitor
	 *            progress monitor
	 * @return the list of projects for which this builder would like deltas the
	 *         next time it is run or <code>null</code> if none
	 * @throws CoreException
	 */
	IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor) throws CoreException;

	/**
	 * Perform clean.
	 * 
	 * @param console
	 *            console to show clean output
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 */
	void clean(IConsole console, IProgressMonitor monitor) throws CoreException;

	/**
	 * The binaries produced by the build.
	 * 
	 * @return binaries produced by the build.
	 * @throws CoreException
	 * @since 6.1
	 */
	default IBinary[] getBuildOutput() throws CoreException {
		return null;
	}

	/**
	 * Set the environment for the builds. Generally the environment from a
	 * ProcessBuilder would be passed here.
	 * 
	 * @param env
	 *            build environment
	 * @since 6.1
	 */
	default void setBuildEnvironment(Map<String, String> env) {
	}

	/**
	 * Set the properties for this build configuration. These will often come
	 * from launch configurations which have build settings as attributes.
	 * 
	 * @param properties
	 *            build properties
	 * @return whether the properties have changed
	 * @since 6.2
	 */
	default boolean setProperties(Map<String, String> properties) {
		return false;
	}

	/**
	 * Return the properties for this build configuration.
	 * 
	 * @return default properties
	 * @since 6.2
	 */
	default Map<String, String> getProperties() {
		return getDefaultProperties();
	}

	/**
	 * Returns the default values for the properties.
	 * 
	 * @since 6.2
	 */
	default Map<String, String> getDefaultProperties() {
		return new HashMap<>();
	}

	/**
	 * Returns whether this build configuration supports the given build
	 * properties.
	 * 
	 * @param properties
	 *            build properties
	 * @return whether this build configuration supports those properties
	 * @since 6.2
	 */
	default boolean supportsProperties(Map<String, String> properties) {
		return false;
	}

}
