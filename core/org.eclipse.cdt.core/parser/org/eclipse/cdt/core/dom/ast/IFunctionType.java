/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IFunctionType extends IType {
	/**
	 * Returns the return type of this function type
	 */
	public IType getReturnType();

	/**
	 * Returns the adjusted parameter types
	 * ISO C99 6.7.5.3, ISO C++98 8.3.4-3
	 */
	public IType[] getParameterTypes();

	/**
	 * Whether the function type takes variable number of arguments.
	 * @since 5.10
	 */
	public boolean takesVarArgs();
}
