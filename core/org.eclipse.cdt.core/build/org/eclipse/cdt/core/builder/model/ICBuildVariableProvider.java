/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.model;

/**
 * Class that makes build variables available to the
 * build process.
 * <p>
 * Intended for use in situations where generic build
 * variables (ex, system root, etc.) can be provided
 * for particular project types, or generically.
 * <p>
 * If possible the build variable provider should create
 * a build variable that does not need to refer back to
 * the provider for resolution. The default CBuildVariable
 * implementation provides support for this type of
 * build variable.
 * </p>
 * If the build variable will need information from the
 * provider in order to resolve it's value, then the
 * build variables will need to keep a reference to it's
 * provider (or some other class that can resolve the
 * variable protion of the build variable.)  The default
 * CBuildVariable implementation supports this type
 * of build variable as well, through use of the 
 * ICBuildVariableResolver interface.
 * <p>
 * See also the <a href="../../../../../../CBuildConfig.html">CBuildConfig</a>
 * extension point documentation.
 * <p>
 * @see ICBuildVariable
 * @see ICBuildVariableResolver
 * @see CBuildVariable
 */
public interface ICBuildVariableProvider {

	/**
	 * Get the list of build variables made available
	 * through this provider.
	 * 
	 * @return build variables.
	 */
	ICBuildVariable[] getVariables();

}
