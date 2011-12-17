/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * Catch handler used for try block statements or for functions with try block.
 * @see ICPPASTFunctionWithTryBlock
 * @see ICPPASTTryBlockStatement
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTCatchHandler extends IASTStatement {
	public static final ICPPASTCatchHandler[] EMPTY_CATCHHANDLER_ARRAY = {};

	/**
	 * <code>DECLARATION</code> represents the nested declaration within the catch handler.
	 */
	public static final ASTNodeProperty DECLARATION = new ASTNodeProperty(
			"ICPPASTCatchHandler.DECLARATION - Nested declaration within catch handler"); //$NON-NLS-1$

	/**
	 * <code>CATCH_BODY</code> represents the nested (compound) statement.
	 */
	public static final ASTNodeProperty CATCH_BODY = new ASTNodeProperty(
			"ICPPASTCatchHandler.CATCH_BODY - Nested compound statement for catch body"); //$NON-NLS-1$

	/**
	 * Set is catch all handler.
	 */
	public void setIsCatchAll(boolean isEllipsis);

	/**
	 * Is this catch handler for all exceptions?
	 */
	public boolean isCatchAll();

	/**
	 * Set the catch body.
	 */
	public void setCatchBody(IASTStatement compoundStatement);

	/**
	 * Get the catch body.
	 */
	public IASTStatement getCatchBody();

	/**
	 * Set the declaration.
	 */
	public void setDeclaration(IASTDeclaration decl);

	/**
	 * Get the declaration.
	 */
	public IASTDeclaration getDeclaration();

	/**
	 * Get the scope represented by this catch handler.
	 * @since 5.1
	 */
	public IScope getScope();
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTCatchHandler copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTCatchHandler copy(CopyStyle style);
}
