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

/**
 * 
 * @since 3.0
 */
public interface IEnvironmentVariableSupplier {
	
	/**
	 *
	 * @param name the variable name
	 * @param context the context
	 * @return the reference to the IBuildEnvironmentVariable interface representing 
	 * the variable of a given name
	 */
	IBuildEnvironmentVariable getVariable(String name, Object context);
	
	/**
	 *
	 * @param context the context
	 * @return the array of IBuildEnvironmentVariable that represents the environment variables 
	 */
	IBuildEnvironmentVariable[] getVariables(Object context);
}
