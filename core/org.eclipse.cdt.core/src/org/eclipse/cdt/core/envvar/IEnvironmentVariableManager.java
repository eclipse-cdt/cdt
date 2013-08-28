/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.envvar;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;



/**
 * 
 * this interface represent the environment variable provider - the main entry-point
 * to be used for querying the build environment
 * 
 * @since 3.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEnvironmentVariableManager{

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

