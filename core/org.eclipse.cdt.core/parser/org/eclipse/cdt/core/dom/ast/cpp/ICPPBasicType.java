/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBasicType;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPBasicType extends IBasicType {
	/**
	 * @deprecated, don't use the constant, more flags may be added for supporting future c++ standards.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int LAST = IS_LONG_LONG;

	/**
	 * @since 4.0
	 * @deprecated use {@link #getModifiers()}, instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public int getQualifierBits();

	/**
	 * @deprecated Use the type-safe version getKind(), instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_bool = ICPPASTSimpleDeclSpecifier.t_bool;

	/**
	 * @deprecated Use the type-safe version getKind(), instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_wchar_t = ICPPASTSimpleDeclSpecifier.t_wchar_t;

	/**
	 * Get the built-in type's pseudo-destructor.
	 * The pseudo-destructor is the function named by e.g. an id-expression
	 * of the form "T().~T" when instantiated with T mapped to a built-in type.
	 * @since 6.5
	 */
	public ICPPFunction getPseudoDestructor();
}
