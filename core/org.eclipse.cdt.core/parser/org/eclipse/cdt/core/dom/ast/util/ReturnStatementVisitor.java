/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.util;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;

/**
 * An ASTVisitor that visits every return statement in a function
 * body and calls onReturnStatement() on it.
 *
 * @since 6.3
 */
public abstract class ReturnStatementVisitor extends ASTVisitor {
	private final IASTFunctionDefinition fFunction;

	/**
	 * Constructs a ReturnStatementVisitor that will visit the
	 * body of a function.
	 * @param function the function to be visited
	 */
	protected ReturnStatementVisitor(IASTFunctionDefinition function) {
		shouldVisitStatements = true;
		shouldVisitDeclarations = true;
		shouldVisitExpressions = true;
		this.fFunction = function;
	}

	/**
	 * Gets the function being visited.
	 */
	protected IASTFunctionDefinition getFunction() {
		return fFunction;
	}

	/**
	 * Called when a return statement is encountered in the function body.
	 * @param stmt the return statement that was encountered
	 */
	protected abstract void onReturnStatement(IASTReturnStatement stmt);

	@Override
	public int visit(IASTDeclaration element) {
		if (element != fFunction)
			return PROCESS_SKIP; // skip inner functions
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTExpression expr) {
		if (expr instanceof ICPPASTLambdaExpression) {
			return PROCESS_SKIP;
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTStatement stmt) {
		if (stmt instanceof IASTReturnStatement) {
			onReturnStatement((IASTReturnStatement) stmt);
			return PROCESS_SKIP;
		}
		return PROCESS_CONTINUE;
	}
}
