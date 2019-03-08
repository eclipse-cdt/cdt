/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;

/**
 * C++ adds a few things to function declarators.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICPPASTFunctionDeclarator extends IASTStandardFunctionDeclarator, ICPPASTDeclarator {
	/**
	 * @since 5.9
	 */
	public enum RefQualifier {
		LVALUE, RVALUE
	}

	/**
	 * Used as return value for {@link #getExceptionSpecification()}.
	 * @since 5.1
	 */
	public static final IASTTypeId[] NO_EXCEPTION_SPECIFICATION = {};

	/**
	 * Used as return value for {@link #getVirtSpecifiers()}.
	 * @since 5.7
	 */
	public static final ICPPASTVirtSpecifier[] NO_VIRT_SPECIFIERS = {};

	/**
	 * Represents a 'noexcept' specification without an expression.
	 * @since 5.5
	 */
	public static final ICPPASTLiteralExpression NOEXCEPT_DEFAULT = new CPPASTLiteralExpression(
			ICPPASTLiteralExpression.lk_true, Keywords.cTRUE);

	public static final ASTNodeProperty EXCEPTION_TYPEID = new ASTNodeProperty(
			"ICPPASTFunctionDeclarator.EXCEPTION_TYPEID [IASTTypeId]"); //$NON-NLS-1$
	/** @since 5.5 */
	public static final ASTNodeProperty NOEXCEPT_EXPRESSION = new ASTNodeProperty(
			"ICPPASTFunctionDeclarator.NOEXCEPT_EXPRESSION [ICPPASTExpression]"); //$NON-NLS-1$
	/** @since 5.2 */
	public static final ASTNodeProperty TRAILING_RETURN_TYPE = new ASTNodeProperty(
			"ICPPASTFunctionDeclarator.TRAILING_RETURN_TYPE [IASTTypeId]"); //$NON-NLS-1$
	/** @since 5.7 */
	public static final ASTNodeProperty VIRT_SPECIFIER = new ASTNodeProperty(
			"ICPPASTFunctionDeclarator.VIRT_SPECIFIER [ICPPASTVirtSpecifier]"); //$NON-NLS-1$

	/**
	 * Is this a const method?
	 */
	public boolean isConst();

	/**
	 * Sets the method to be const or not.
	 */
	public void setConst(boolean value);

	/**
	 * Is this a volatile method?
	 */
	public boolean isVolatile();

	/**
	 * Sets the method to be volatile or not.
	 */
	public void setVolatile(boolean value);

	/**
	 * When used as a lambda declarator, it can be mutable.
	 * @since 5.3
	 */
	public boolean isMutable();

	/**
	 * When used as a lambda declarator, it can be mutable.
	 * @since 5.3
	 */
	public void setMutable(boolean value);

	/**
	 * Is the method pure virtual?
	 */
	public boolean isPureVirtual();

	/**
	 * Sets this method to be pure virtual.
	 */
	public void setPureVirtual(boolean isPureVirtual);

	/**
	 * Returns the ref-qualifier.
	 * @since 5.9
	 */
	public RefQualifier getRefQualifier();

	/**
	 * Sets the ref-qualifier.
	 * @since 5.9
	 */
	public void setRefQualifier(RefQualifier value);

	/**
	 * @since 5.2
	 */
	@Override
	public ICPPASTParameterDeclaration[] getParameters();

	/**
	 * Returns an array of type-ids representing the exception specification. The return value
	 * {@link #NO_EXCEPTION_SPECIFICATION} indicates that no exceptions are specified, whereas
	 * {@link IASTTypeId#EMPTY_TYPEID_ARRAY} is used for an empty exception specification.
	 */
	public IASTTypeId[] getExceptionSpecification();

	/**
	 * Add an exception specification type Id.
	 */
	public void addExceptionSpecificationTypeId(IASTTypeId typeId);

	/**
	 * Configures the declarator with an empty exception specification (as opposed to having none).
	 *
	 * @since 5.1
	 */
	public void setEmptyExceptionSpecification();

	/**
	 * Returns the noexcept expression, {@link #NOEXCEPT_DEFAULT} if the noexcept specification
	 * does not contain an expression, or {@code null} the noexcept specification is not present.
	 * See C++11 5.4.1.
	 * @since 5.5
	 */
	public ICPPASTExpression getNoexceptExpression();

	/**
	 * Returns the noexcept evaluation, or {@code null} if no noexcept specification is present, or
	 * or an evaluation representing {@code noexcept(true)} in case of an empty exception specification.
	 * @since 6.7
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public ICPPEvaluation getNoexceptEvaluation();

	/**
	 * Sets the noexcept expression.
	 * @since 5.5
	 */
	public void setNoexceptExpression(ICPPASTExpression expression);

	/**
	 * Returns the trailing return type as in <code> auto f() -> int </code>, or <code>null</code>.
	 * @since 5.2
	 */
	public IASTTypeId getTrailingReturnType();

	/**
	 * Trailing return type as in <code> auto f() -> int </code>.
	 * @since 5.2
	 */
	public void setTrailingReturnType(IASTTypeId typeId);

	/**
	 * Get function scope this node represents. Returns <code>null</code>, if this declarator
	 * does not declare a function-prototype or function-definition.
	 */
	@Override
	public ICPPFunctionScope getFunctionScope();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTFunctionDeclarator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTFunctionDeclarator copy(CopyStyle style);

	/**
	 * Returns whether this function is declared override.
	 *
	 * @since 5.5
	 */
	public boolean isOverride();

	/**
	 * Returns whether this function is declared final.
	 *
	 * @since 5.5
	 */
	public boolean isFinal();

	/**
	 * Returns the virt-specifiers of this function.
	 * @since 5.7
	 */
	public ICPPASTVirtSpecifier[] getVirtSpecifiers();

	/**
	 * Add a virt-specifiers to this function.
	 * @since 5.7
	 */
	public void addVirtSpecifier(ICPPASTVirtSpecifier virtSpecifier);

	/**
	 * Set virt-specifiers of this function.
	 * @since 6.6
	 */
	public void setVirtSpecifiers(ICPPASTVirtSpecifier[] newVirtSpecifiers);

	/**
	 * @deprecated Not used.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final ASTNodeProperty CONSTRUCTOR_CHAIN_MEMBER = new ASTNodeProperty(
			"ICPPASTFunctionDeclarator.CONSTRUCTOR_CHAIN_MEMBER - Role of a Constructor Chain Initializer"); //$NON-NLS-1$

	/**
	 * @deprecated Use {@link ICPPASTFunctionDefinition#getMemberInitializers}, instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public ICPPASTConstructorChainInitializer[] getConstructorChain();

	/**
	 * @deprecated Use {@link ICPPASTFunctionDefinition#addMemberInitializer(ICPPASTConstructorChainInitializer)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void addConstructorToChain(ICPPASTConstructorChainInitializer initializer);

	/**
	 * @since 5.5
	 * @deprecated Use {@link #addVirtSpecifier(ICPPASTVirtSpecifier)} instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void setFinal(boolean isFinal);

	/**
	 * @since 5.5
	 * @deprecated Use {@link #addVirtSpecifier(ICPPASTVirtSpecifier)} instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void setOverride(boolean isOverride);
}
