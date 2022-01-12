/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 * 	   Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPFunctionType extends IFunctionType {
	/**
	 * Returns {@code true} for a constant method.
	 */
	public boolean isConst();

	/**
	 * Returns {@code true} for a volatile method.
	 */
	public boolean isVolatile();

	/**
	 * Returns {@code true} for a method declared with a ref-qualifier.
	 * @since 5.9
	 */
	public boolean hasRefQualifier();

	/**
	 * Returns {@code true} if the type of the implicit object parameter is an rvalue reference.
	 * @since 5.9
	 */
	public boolean isRValueReference();

	/**
	 * Returns the evaluation object for the noexcept specifier or null if there is no noexcept specifier.
	 * @since 6.7
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public ICPPEvaluation getNoexceptSpecifier();

	/**
	 * Whether the function type takes variable number of arguments.
	 * @since 5.2
	 */
	@Override
	public boolean takesVarArgs();

	/**
	 * @deprecated function types don't relate to this pointers at all.
	 * @noreference This method is not intended to be referenced by clients and should be removed.
	 */
	@Deprecated
	public IPointerType getThisType();
}
