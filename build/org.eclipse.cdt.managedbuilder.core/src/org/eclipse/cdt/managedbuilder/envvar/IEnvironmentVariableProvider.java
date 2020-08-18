/*******************************************************************************
 * Copyright (c) 2005, 2012 Intel Corporation and others.
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
package org.eclipse.cdt.managedbuilder.envvar;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;

/**
 * this interface represent the environment variable provider - the main entry-point
 * to be used for querying the build environment
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEnvironmentVariableProvider {

	/**
	 * Get variable for the given configuration, normally including those defined in project properties
	 * and workspace preferences.
	 *
	 * See also {@code CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(String name, ICConfigurationDescription cfg, boolean resolveMacros)}
	 *
	 * @param variableName - name of the variable (not including $ sign).
	 * @param cfg - configuration or {@code null} for workspace preferences only.
	 * @param resolveMacros - if {@code true} expand macros, {@code false} otherwise.
	 * @return variable defined for the configuration or the workspace.
	 *    Returns {@code null} if variable is not defined.
	 */
	public IEnvironmentVariable getVariable(String variableName, IConfiguration cfg, boolean resolveMacros);

	/**
	 * Get variables for the given configuration, normally including those defined in project properties
	 * and workspace preferences.
	 *
	 * See also {@code CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(ICConfigurationDescription cfg, boolean resolveMacros)}
	 *
	 * @param cfg - configuration or {@code null} for workspace preferences only.
	 * @param resolveMacros - if {@code true} expand macros, {@code false} otherwise.
	 * @return array of variables defined for the configuration or the workspace.
	 *    Returns an empty array if no variables are defined.
	 */
	public IEnvironmentVariable[] getVariables(IConfiguration cfg, boolean resolveMacros);

	/**
	 * @return the String representing default system delimiter. That is the ":" for Unix-like
	 * systems and the ";" for Win32 systems. This method will be used by the
	 * tool-integrator provided variable supplier e.g. in order to concatenate the list of paths into the
	 * environment variable, etc.
	 */
	public String getDefaultDelimiter();

	/**
	 * This method is defined to be used basically by the UI classes and should not be used by the
	 * tool-integrator
	 * @return the array of the provider-internal suppliers for the given level
	 */
	IEnvironmentVariableSupplier[] getSuppliers(Object level);

	/**
	 * @return the array of String that holds the build paths of the specified type
	 *
	 * @param configuration represent the configuration for which the paths were changed
	 * @param buildPathType can be set to one of the IEnvVarBuildPath.BUILDPATH _xxx
	 * (the IEnvVarBuildPath will represent the build environment variables, see also
	 * the "Specifying the Includes and Library paths environment variables",
	 * the "envVarBuildPath schema" and the "Expected CDT/MBS code changes" sections)
	 */
	String[] getBuildPaths(IConfiguration configuration, int buildPathType);

	/**
	 * adds the listener that will return notifications about the include and library paths changes.
	 * The ManagedBuildManager will register the change listener and will notify all registered
	 * Scanned Info Change Listeners about the include paths change.
	 */
	void subscribe(IEnvironmentBuildPathsChangeListener listener);

	/**
	 * removes the include and library paths change listener
	 */
	void unsubscribe(IEnvironmentBuildPathsChangeListener listener);
}
