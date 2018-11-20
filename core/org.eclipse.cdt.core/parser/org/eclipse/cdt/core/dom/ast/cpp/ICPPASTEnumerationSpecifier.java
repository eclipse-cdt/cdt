/*******************************************************************************
 * Copyright (c) 2010, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;

/**
 * <code>enum struct : unsigned int {...}</code>
 *
 * @since 5.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTEnumerationSpecifier extends IASTEnumerationSpecifier, ICPPASTDeclSpecifier {
	public static final ASTNodeProperty BASE_TYPE = new ASTNodeProperty(
			"ICPPASTEnumerationSpecifier.BASE_TYPE [ICPPASTDeclSpecifier]"); //$NON-NLS-1$

	/**
	 * @since 6.6
	 */
	public enum ScopeStyle {
		CLASS, STRUCT, NONE
	}

	@Override
	public ICPPASTEnumerationSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTEnumerationSpecifier copy(CopyStyle style);

	/**
	 * Not allowed on frozen AST.
	 * @deprecated Use setScopeToken instead
	 * If {@code isScoped == true} is passed, the {@code ScopeToken.CLASS} scope token is assumed.
	 */
	@Deprecated
	public void setIsScoped(boolean isScoped);

	/**
	 * Not allowed on frozen AST.
	 * @since 6.6
	 */
	public void setScopeStyle(ScopeStyle scopeStyle);

	/**
	 * @since 6.6
	 */
	public ScopeStyle getScopeStyle();

	/**
	 * An enum is scoped if it uses the enumeration head {@code enum class} or {@code enum struct}.
	 */
	public boolean isScoped();

	/**
	 * Not allowed on frozen AST.
	 */
	public void setIsOpaque(boolean isOpaque);

	/**
	 * An opaque specifier does not have a body.
	 */
	public boolean isOpaque();

	/**
	 * Not allowed on frozen ast.
	 */
	public void setBaseType(ICPPASTDeclSpecifier baseType);

	/**
	 * Returns the base type for this enum or {@code null} if it was not specified.
	 */
	public ICPPASTDeclSpecifier getBaseType();

	/**
	 * Returns the scope containing the enumerators of this enumeration,
	 * or {@code null} if the specifier is opaque.
	 */
	public ICPPScope getScope();
}
