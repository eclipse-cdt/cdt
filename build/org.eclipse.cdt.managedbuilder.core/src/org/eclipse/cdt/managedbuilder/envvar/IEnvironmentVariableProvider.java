/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.envvar;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;

/**
 * 
 * this interface represent the environment variable provider - the main entry-point
 * to be used for querying the build environment
 * 
 * @since 3.0
 */
public interface IEnvironmentVariableProvider{

	/**
	 * 
	 * 
	 * @return the reference to the IBuildEnvironmentVariable interface representing 
	 * the variable of a given name
	 * @param variableName environment variable name
	 * if environment variable names are case insensitive in the current OS, 
	 * the environment variable provider will query the getVariable method of suppliers always
	 * passing it the uppercase variable name not depending on the case of the variableName
	 * passed to the IEnvironmentVariableProvider.getVariable() method. This will prevent the 
	 * supplier from answering different values for the same variable given the names that differ
	 * only by case. E.g. if the current OS does not support case sensitive variables both of the 
	 * calls below:
	 *
	 *   provider.getVariable("FOO",level,includeParentContexts);
	 *   provider.getVariable("foo",level,includeParentContexts);
	 *
	 * will result in asking suppliers for the "FOO" variable
	 *
	 * @param level could be one of the following:
	 *    1. IConfiguration to represent the configuration
	 *    2. IManagedProject to represent the managed project
	 *    3. IWorkspace to represent the workspace
	 *    4. null to represent the system environment passed to eclipse
	 */
	public IBuildEnvironmentVariable getVariable(
				String variableName, Object level, boolean includeParentLevels, boolean resolveMacros);

	/**
	 *
	 * if environment variable names are case insensitive in the current OS, 
	 * the environment variable provider will remove the duplicates of the variables if their names
	 * differ only by case
	 *
	 * @return the array of IBuildEnvironmentVariable that represents the environment variables 
	 */
	public IBuildEnvironmentVariable[] getVariables(
				Object level, boolean includeParentLevels, boolean resolveMacros);

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

	/**
	 * This method is defined to be used basically by the UI classes and should not be used by the
	 * tool-integrator
	 * @return the array of the provider-internal suppliers for the given level
	 */
	IEnvironmentVariableSupplier[] getSuppliers(Object level);


	/**
	 * returns the array of String that holds the build paths of the specified type
	 * @param configuration represent the configuration for which the paths were changed
	 * @param buildPathType can be set to one of the IEnvVarBuildPath.BUILDPATH _xxx 
	 * (the IEnvVarBuildPath will represent the build environment variables, see also 
	 * the "Specifying the Includes and Library paths environment variables",
	 * the "envVarBuildPath schema" and the "Expected CDT/MBS code changes" sections)
	 */
	String[] getBuildPaths(IConfiguration configuration, int buildPathType);

	/**
	 *
	 * adds the listener that will return notifications about the include and library paths changes. 
	 * The ManagedBuildManager will register the change listener and will notify all registered
	 * Scanned Info Change Listeners about the include paths change. 
	 */ 
	void subscribe(
		IEnvironmentBuildPathsChangeListener listener);

	/**
	 *
	 * removes the include and library paths change listener 
	 */ 
	void unsubscribe(
		IEnvironmentBuildPathsChangeListener listener);
}

