/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

/**
 * In c++ the a function definition for a constructor may contain member initializers.
 * @since 5.1
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTFunctionDefinition extends IASTFunctionDefinition {

	/**
 	 * <code>MEMBER_INITIALIZER</code> is the role of a member initializer in the function definition.
	 */
	@SuppressWarnings("nls")
	public static final ASTNodeProperty MEMBER_INITIALIZER = new ASTNodeProperty(
			"ICPPASTFunctionDefinition.MEMBER_INITIALIZER - Role of a member initializer");

	/**
	 * Returns the array of associated member initializers.
	 */
	public ICPPASTConstructorChainInitializer[] getMemberInitializers();

	/**
	 * Adds a member initializer to this function definition.
	 */
	public void addMemberInitializer(ICPPASTConstructorChainInitializer initializer);
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTFunctionDefinition copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTFunctionDefinition copy(CopyStyle style);

	/**
	 * Make this a defaulted function definition, e.g.: C::C() = default;
	 * @since 5.3
	 */
	public void setIsDefaulted(boolean isDefaulted);

	/**
	 * Returns whether this is a defaulted function definition.
	 * @since 5.3
	 */
	public boolean isDefaulted();

	/**
	 * Make this a deleted function definition, e.g.: void f() = delete;
	 * @since 5.3
	 */
	public void setIsDeleted(boolean isDeleted);

	/**
	 * Returns whether this is a deleted function definition.
	 * @since 5.3
	 */
	public boolean isDeleted();
}
