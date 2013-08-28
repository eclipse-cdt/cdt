/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.envvar;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;

/**
 * This interface is to be implemented by the tool-integrator for supplying the configuration-specific
 * environment.
 *
 * @since 3.0
 */
public interface IConfigurationEnvironmentVariableSupplier {
	/**
	 * @param variableName - the variable name.
	 * @param configuration - configuration.
	 * @param provider the instance of the environment variable provider to be used for querying the
	 *    environment variables from within the supplier. The supplier should use this provider to obtain
	 *    the already defined environment instead of using the "default" provider returned by the
	 *    ManagedBuildManager.getEnvironmentVariableProvider().
	 *    The provider passed to a supplier will ignore searching the variables for the levels
	 *    higher than the current supplier level, will query only the lower-precedence suppliers
	 *    for the current level and will query all suppliers for the lower levels.
	 *    This is done to avoid infinite loops that could be caused if the supplier calls the provider
	 *    and the provider in turn calls that supplier again. Also the supplier should not know anything
	 *    about the environment variables defined for the higher levels.
	 * @return The reference to the IBuildEnvironmentVariable interface representing
	 *    the variable of a given name or {@code null} if the variable is not defined.
	 */
	IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration, IEnvironmentVariableProvider provider);

	/**
	 * @param configuration - configuration.
	 * @param provider - the instance of the environment variable provider to be used for querying the
	 *    environment variables from within the supplier. The supplier should use this provider to obtain
	 *    the already defined environment instead of using the "default" provider returned by the
	 *    ManagedBuildManager.getEnvironmentVariableProvider().
	 *    The provider passed to a supplier will ignore searching the variables for the levels
	 *    higher than the current supplier level, will query only the lower-precedence suppliers
	 *    for the current level and will query all suppliers for the lower levels.
	 *    This is done to avoid infinite loops that could be caused if the supplier calls the provider
	 *    and the provider in turn calls that supplier again. Also the supplier should not know anything
	 *    about the environment variables defined for the higher levels.
	 * @return The array of IBuildEnvironmentVariable that represents the environment variables.
	 *    The array may contain {@code null} values.
	 */
	IBuildEnvironmentVariable[] getVariables(IConfiguration configuration, IEnvironmentVariableProvider provider);
}

