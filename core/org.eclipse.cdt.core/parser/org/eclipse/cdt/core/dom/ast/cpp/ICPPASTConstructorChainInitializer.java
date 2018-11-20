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
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * Represents a member initializer:
 * <pre> class X {
 *     int a;
 *     X();
 * };
 * X::X : a(0) {}  // a(0) is a member initializer.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTConstructorChainInitializer extends IASTInitializer, ICPPASTPackExpandable, IASTNameOwner {
	public static final ICPPASTConstructorChainInitializer[] EMPTY_CONSTRUCTORCHAININITIALIZER_ARRAY = new ICPPASTConstructorChainInitializer[0];

	public static final ASTNodeProperty MEMBER_ID = new ASTNodeProperty(
			"ICPPASTConstructorChainInitializer.MEMBER_ID [IASTName]"); //$NON-NLS-1$

	public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty(
			"ICPPASTConstructorChainInitializer.INITIALIZER [IASTInitializer]"); //$NON-NLS-1$

	/**
	 * Returns the name of the member.
	 */
	public IASTName getMemberInitializerId();

	/**
	 * Returns the initializer for the member
	 * @since 5.2
	 */
	public IASTInitializer getInitializer();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTConstructorChainInitializer copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTConstructorChainInitializer copy(CopyStyle style);

	/**
	 * Not allowed on frozen ast.
	 */
	public void setMemberInitializerId(IASTName name);

	/**
	 * Not allowed on frozen ast.
	 * @since 5.2
	 */
	public void setInitializer(IASTInitializer initializer);

	/**
	 * @deprecated Replaced by {@link #getInitializer()}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IASTExpression getInitializerValue();

	/**
	 * @deprecated Replaced by {@link #setInitializer(IASTInitializer)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void setInitializerValue(IASTExpression expression);
}
