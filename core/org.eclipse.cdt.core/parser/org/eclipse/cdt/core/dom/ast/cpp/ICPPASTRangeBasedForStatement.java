/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * Represents a range-based for loop.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.3
 */
public interface ICPPASTRangeBasedForStatement extends IASTStatement, IASTImplicitNameOwner {
	public static final ASTNodeProperty DECLARATION = new ASTNodeProperty(
			"ICPPASTRangeBasedForStatement.DECLARATION [IASTDeclaration]"); //$NON-NLS-1$
	public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty(
			"ICPPASTRangeBasedForStatement.INITIALIZER [IASTInitializerClause]"); //$NON-NLS-1$
	public static final ASTNodeProperty BODY = new ASTNodeProperty(
			"ICPPASTRangeBasedForStatement.BODY [IASTStatement]"); //$NON-NLS-1$


	/**
	 * Returns the for-range-declaration 
	 */
	IASTDeclaration getDeclaration();
	
	/**
	 * Returns the for-range-initializer.
	 */
	IASTInitializerClause getInitializerClause();
	
	/**
	 * Returns the statement of this for-loop.
	 */
	IASTStatement getBody();

	/**
	 * Returns the scope defined by this for-loop.
	 */
	public IScope getScope();

	@Override
	public ICPPASTRangeBasedForStatement copy();

	@Override
	public ICPPASTRangeBasedForStatement copy(CopyStyle style);


	/**
	 * Not allowed on frozen AST.
	 */
	void setDeclaration(IASTDeclaration decl);

	/**
	 * Not allowed on frozen AST.
	 */
    void setInitializerClause(IASTInitializerClause statement);
    
	/**
	 * Not allowed on frozen AST.
	 */
	public void setBody(IASTStatement statement);
}
