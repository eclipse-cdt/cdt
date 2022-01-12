/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Binding for c++ functions.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICPPFunction extends IFunction, ICPPBinding {
	/**
	 * Does this function have the mutable storage class specifier
	 */
	public boolean isMutable();

	/**
	 * Is this an inline function
	 */
	@Override
	public boolean isInline();

	/**
	 * Returns whether this function is declared as extern "C".
	 * @since 5.0
	 */
	public boolean isExternC();

	/**
	 * Returns whether this function is declared constexpr.
	 * @since 5.5
	 */
	public boolean isConstexpr();

	/**
	 * Returns the exception specification for this function or <code>null</code> if there
	 * is no exception specification.
	 * @since 5.1
	 */
	public IType[] getExceptionSpecification();

	/**
	 * Returns the function's type.
	 * Any placeholders in the type are resolved.
	 * If the type contains placeholders and a function definition is not available to
	 * resolve them, a ProblemType is returned (call sites that do not need the
	 * placeholders resolved should call getDeclaredType() instead).
	 * @since 5.1
	 */
	@Override
	public ICPPFunctionType getType();

	/**
	 * Returns the function's declared type.
	 * This is the function's type without any placeholders resolved.
	 * @since 6.3
	 */
	public ICPPFunctionType getDeclaredType();

	/**
	 * @since 5.2
	 */
	@Override
	public ICPPParameter[] getParameters();

	/**
	 * @since 5.2
	 */
	public int getRequiredArgumentCount();

	/**
	 * @since 5.2
	 */
	public boolean hasParameterPack();

	/**
	 * Returns whether this is a function with a deleted function definition.
	 * @since 5.3
	 */
	public boolean isDeleted();
}
