/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This represents a function in the program. A function is also a scope
 * for other bindings.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IFunction extends IBinding {
	/**
	 * Returns the formal parameters of the function.
	 */
	public IParameter[] getParameters();

	/**
	 * Returns the function scope
	 */
	public IScope getFunctionScope();

	/**
	 * Returns the IFunctionType for this function
	 */
	public IFunctionType getType();

	/**
	 * Returns {@code true} if the function has the static storage-class specifier
	 * similarly for extern, auto, register.
	 */
	public boolean isStatic();

	public boolean isExtern();

	public boolean isAuto();

	public boolean isRegister();

	/**
	 * Returns {@code true} if the function is inline.
	 */
	public boolean isInline();

	/**
	 * Returns {@code true} if this function takes variable arguments.
	 */
	public boolean takesVarArgs();

	/**
	 * Returns {@code true} if this function never returns. Based on 'noreturn' attribute in
	 * the function declaration.
	 * @since 5.4
	 */
	public boolean isNoReturn();
}
