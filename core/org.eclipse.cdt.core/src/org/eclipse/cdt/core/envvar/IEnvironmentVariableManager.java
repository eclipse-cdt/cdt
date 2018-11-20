/*******************************************************************************
 * Copyright (c) 2005, 2013 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.envvar;

import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IBuildConfiguration;

/**
 *
 * this interface represent the environment variable provider - the main entry-point
 * to be used for querying the build environment
 *
 * @since 3.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEnvironmentVariableManager {

	/**
	 *
	 *
	 * @return the reference to the IBuildEnvironmentVariable interface representing
	 * the variable of a given name or null
	 * @param name environment variable name
	 * if environment variable names are case insensitive in the current OS,
	 * the environment variable provider will query the getVariable method of suppliers always
	 * passing it the upper-case variable name not depending on the case of the variableName
	 * passed to the IEnvironmentVariableProvider.getVariable() method. This will prevent the
	 * supplier from answering different values for the same variable given the names that differ
	 * only by case. E.g. if the current OS does not support case sensitive variables both of the
	 * calls below:
	 *
	 *   provider.getVariable("FOO",level,includeParentContexts);
	 *   provider.getVariable("foo",level,includeParentContexts);
	 *
	 * will result in asking suppliers for the "FOO" variable
	 */
	public IEnvironmentVariable getVariable(String name, ICConfigurationDescription cfg, boolean resolveMacros);

	/**
	 *
	 * if environment variable names are case insensitive in the current OS,
	 * the environment variable provider will remove the duplicates of the variables if their names
	 * differ only by case
	 *
	 * @return the array of IBuildEnvironmentVariable that represents the environment variables (the
	 *   array may contain null values)
	 */
	public IEnvironmentVariable[] getVariables(ICConfigurationDescription cfg, boolean resolveMacros);

	/**
	 * Returns a list of environment variables for the given build configuration.
	 *
	 * @param config the build configuration
	 * @param resolveMacros whether to resolve macros in the variable values
	 * @return the list of environment variables
	 * @since 6.0
	 */
	public IEnvironmentVariable[] getVariables(IBuildConfiguration config, boolean resolveMacros);

	/**
	 * Returns the named environment variable in the given build configuration.
	 *
	 * @param name the name of the environment variable
	 * @param config the build configuration
	 * @param resolveMacros whether to resolve macros
	 * @return the environment variable
	 * @since 6.0
	 */
	public IEnvironmentVariable getVariable(String name, IBuildConfiguration config, boolean resolveMacros);

	/**
	 * Set the environment for a given build configuration.
	 *
	 * @param env environment variable map
	 * @param config build configuration
	 * @param resolveMacros whether to resolve macros
	 * @since 6.0
	 */
	public void setEnvironment(Map<String, String> env, IBuildConfiguration config, boolean resolveMacros);

	/**
	 *
	 * @return the String representing default system delimiter. That is the ":" for Unix-like
	 * systems and the ";" for Win32 systems. This method will be used by the
	 * tool-integrator provided variable supplier e.g. in order to concatenate the list of paths into the
	 * environment variable, etc.
	 */
	public String getDefaultDelimiter();

	/**
	 * @return true if the OS supports case sensitive variables (Unix-like systems) or false
	 * if it does not (Win32 systems)
	 */
	public boolean isVariableCaseSensitive();

	public IContributedEnvironment getContributedEnvironment();
}
