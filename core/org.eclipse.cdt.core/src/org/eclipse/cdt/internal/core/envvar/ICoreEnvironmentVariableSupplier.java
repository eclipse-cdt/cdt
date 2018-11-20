/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
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
package org.eclipse.cdt.internal.core.envvar;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;

/**
 *
 * @since 3.0
 */
public interface ICoreEnvironmentVariableSupplier {

	/**
	 *
	 * @param name the variable name
	 * @param context the context
	 * @return the reference to the IBuildEnvironmentVariable interface representing
	 * the variable of a given name
	 */
	IEnvironmentVariable getVariable(String name, Object context);

	/**
	 *
	 * @param context the context
	 * @return the array of IBuildEnvironmentVariable that represents the environment variables
	 */
	IEnvironmentVariable[] getVariables(Object context);

	boolean appendEnvironment(Object context);
}
