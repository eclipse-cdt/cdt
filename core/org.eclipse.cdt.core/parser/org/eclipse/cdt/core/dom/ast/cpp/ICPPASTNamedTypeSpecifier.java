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

import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;

/**
 * C++ adds the capability of qualifying a named type specifier w/the keyword
 * typename.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTNamedTypeSpecifier extends IASTNamedTypeSpecifier, ICPPASTDeclSpecifier {
	/**
	 * Was typename token consumed?
	 *
	 * @return boolean
	 */
	public boolean isTypename();

	/**
	 * Set this value.
	 *
	 * @param value
	 *            boolean
	 */
	public void setIsTypename(boolean value);

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTNamedTypeSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTNamedTypeSpecifier copy(CopyStyle style);
}
