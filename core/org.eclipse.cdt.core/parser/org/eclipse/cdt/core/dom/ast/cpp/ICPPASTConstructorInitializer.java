/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;

/**
 * Represents a potentially empty list of initializers in parenthesis: ( initializer-list? )
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTConstructorInitializer extends IASTInitializer {

	/**
	 * @since 5.2
	 */
	public static final ASTNodeProperty ARGUMENT = new ASTNodeProperty(
			"ICPPASTConstructorInitializer.ARGUMENT - [IASTInitializerClause]"); //$NON-NLS-1$

	/**
	 * Returns the arguments of this initializer, never <code>null</code>.
	 * An argument can be of type {@link IASTInitializerList}.
	 * 
	 * @since 5.2
	 */
	public IASTInitializerClause[] getArguments();
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTConstructorInitializer copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTConstructorInitializer copy(CopyStyle style);


	/**
	 * Not allowed on frozen ast.
	 * @since 5.2
	 */
	public void setArguments(IASTInitializerClause[] args);

	/**
	 * @deprecated Replaced by {@link #getArguments()}.
	 */
	@Deprecated
	public IASTExpression getExpression();

	/**
	 * @deprecated Replaced by {@link #setArguments(IASTInitializerClause[])}.
	 */
	@Deprecated
	public void setExpression(IASTExpression expression);
	
	@Deprecated
	public static final ASTNodeProperty EXPRESSION = ARGUMENT;
}
