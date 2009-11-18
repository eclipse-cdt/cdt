/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * Models static assertions: <code> static_assert(false, "message");</code>
 * 
 * @since 5.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTStaticAssertDeclaration extends IASTDeclaration {
	public static final ASTNodeProperty CONDITION = new ASTNodeProperty(
			"ICPPASTStaticAssertDeclaration.CONDITION [IASTExpression]"); //$NON-NLS-1$
	public static final ASTNodeProperty MESSAGE = new ASTNodeProperty(
			"ICPPASTStaticAssertDeclaration.MESSAGE [ICPPASTLiteralExpression]"); //$NON-NLS-1$

	/**
	 * Returns the condition of the assertion
	 */
	IASTExpression getCondition();
	
	/**
	 * Returns the message of the assertion, or potentially <code>null</code> when using content assist.
	 */
	ICPPASTLiteralExpression getMessage();
}
