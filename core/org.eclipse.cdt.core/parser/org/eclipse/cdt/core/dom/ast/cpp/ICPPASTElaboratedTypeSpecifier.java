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
 *     John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;

/**
 * Elaborated types specifier in C++ [dcl.type.elab].
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTElaboratedTypeSpecifier extends IASTElaboratedTypeSpecifier, ICPPASTDeclSpecifier {
	/**
	 * {@code k_class} represents elaborated class declaration.
	 */
	public static final int k_class = IASTElaboratedTypeSpecifier.k_last + 1;

	/**
	 * {@code k_last} is defined for subinterfaces.
	 */
	public static final int k_last = k_class;

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTElaboratedTypeSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTElaboratedTypeSpecifier copy(CopyStyle style);
}
