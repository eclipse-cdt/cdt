/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;

/**
 * Lambda expression, introduced in C++0x.
 * 
 * @since 5.3
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTLambdaExpression extends IASTExpression, IASTImplicitNameOwner {
	ASTNodeProperty CAPTURE = new ASTNodeProperty("ICPPASTLambdaExpression - CAPTURE [ICPPASTCapture]"); //$NON-NLS-1$
	ASTNodeProperty DECLARATOR = new ASTNodeProperty("ICPPASTLambdaExpression - DECLARATOR [ICPPASTFunctionDeclarator]"); //$NON-NLS-1$
	ASTNodeProperty BODY = new ASTNodeProperty("ICPPASTLambdaExpression - BODY [IASTCompoundStatement]"); //$NON-NLS-1$

	/**
	 * The capture default can be by copy, by reference or unspecified.
	 */
	enum CaptureDefault {UNSPECIFIED, BY_COPY, BY_REFERENCE}

	/**
	 * Returns the capture default for this lambda expression.
	 */
	CaptureDefault getCaptureDefault();
	
	/** 
	 * Returns the array of captures for this lambda expression.
	 */
	ICPPASTCapture[] getCaptures();

	/**
	 * Returns an implicit name that represents the closure type.
	 */
	IASTImplicitName getClosureTypeName();

	/**
	 * Returns the lambda declarator for this lambda expression, or <code>null</code>
	 * in case it was not specified.
	 */
	ICPPASTFunctionDeclarator getDeclarator();
	
	/**
	 * Returns an implicit name that represents the implicit function call operator of
	 * the closure.
	 */
	IASTImplicitName getFunctionCallOperatorName();
	
	/**
	 * Returns the compound statement of this lambda expression. Can be <code>null</code>
	 * when creating AST for content assist.
	 */
	IASTCompoundStatement getBody();
	
	
	/**
	 * Not allowed on frozen AST.
	 * @see #getCaptureDefault()
	 */
	void setCaptureDefault(CaptureDefault value);

	/**
	 * Not allowed on frozen AST.
	 * @see #getCaptures()
	 */
	void addCapture(ICPPASTCapture capture);
	
	/**
	 * Not allowed on frozen AST.
	 * @see #getDeclarator()
	 */
	void setDeclarator(ICPPASTFunctionDeclarator dtor);

	/**
	 * Not allowed on frozen AST.
	 * @see #getBody()
	 */
	void setBody(IASTCompoundStatement body);
}
