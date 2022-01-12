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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;

/**
 * Functional cast expressions:
 *   simple-type-specifier (expression-list?)
 *   simple-type-specifier braced-init-list
 *   typename-specifier (expression-list?)
 *   typename-specifier braced-init-list
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTSimpleTypeConstructorExpression extends ICPPASTExpression {
	/**
	 * @since 5.2
	 */
	public static final ASTNodeProperty TYPE_SPECIFIER = new ASTNodeProperty(
			"ICPPASTSimpleTypeConstructorExpression.TYPE_SPECIFIER [ICPPASTSimpleDeclSpecifier]"); //$NON-NLS-1$
	/**
	 * @since 5.2
	 */
	public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty(
			"ICPPASTSimpleTypeConstructorExpression.INITIALIZER [IASTInitializer]"); //$NON-NLS-1$

	/**
	 * Returns the declaration specifier that specifies the type.
	 * @since 5.2
	 */
	public ICPPASTDeclSpecifier getDeclSpecifier();

	/**
	 * Returns the argument for initialization. Can be {@link ICPPASTConstructorInitializer} or
	 * {@link ICPPASTInitializerList}
	 * @since 5.2
	 */
	public IASTInitializer getInitializer();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTSimpleTypeConstructorExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTSimpleTypeConstructorExpression copy(CopyStyle style);

	/**
	 * Not allowed on frozen ast.
	 * @since 5.2
	 */
	public void setDeclSpecifier(ICPPASTDeclSpecifier declSpec);

	/**
	 * Not allowed on frozen ast.
	 * @since 5.2
	 */
	public void setInitializer(IASTInitializer initializer);

	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_unspecified = 0;
	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_void = 1;
	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_char = 2;
	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_int = 3;
	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_float = 4;
	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_double = 5;
	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_bool = 6;
	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_wchar_t = 7;
	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_short = 8;
	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_long = 9;
	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_signed = 10;
	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_unsigned = 11;

	/**
	 * @deprecated Replaced by {@link #getDeclSpecifier()}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public int getSimpleType();

	/**
	 * @deprecated Replaced by {@link #setDeclSpecifier(ICPPASTDeclSpecifier)}
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void setSimpleType(int value);

	/**
	 * @deprecated Replaced by {@link #INITIALIZER}.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final ASTNodeProperty INITIALIZER_VALUE = INITIALIZER;

	/**
	 * @deprecated Replaced by {@link #getInitializer()}
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IASTExpression getInitialValue();

	/**
	 * @deprecated Replaced by {@link #setInitializer(IASTInitializer)}
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void setInitialValue(IASTExpression expression);
}
