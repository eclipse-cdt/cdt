/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.model;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

/**
 * Abstract class for checkers that do all the work on function definition level
 */
public abstract class AbstractAstFunctionChecker extends
		AbstractIndexAstChecker {
	public void processAst(IASTTranslationUnit ast) {
		// traverse the ast using the visitor pattern.
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			public int visit(IASTDeclaration element) {
				if (element instanceof IASTFunctionDefinition) {
					processFunction((IASTFunctionDefinition) element);
					return PROCESS_CONTINUE; // this is to support gcc extension
												// for enclosed functions
				}
				if (element instanceof IASTSimpleDeclaration) {
					IASTDeclSpecifier declSpecifier = ((IASTSimpleDeclaration) element)
							.getDeclSpecifier();
					if (declSpecifier instanceof ICPPASTCompositeTypeSpecifier) {
						return PROCESS_CONTINUE; // c++ methods
					}
				}
				return PROCESS_SKIP;
			}
		});
	}

	/**
	 * Process function.
	 * 
	 * @param func
	 *            - ast node representing function definition
	 */
	protected abstract void processFunction(IASTFunctionDefinition func);
}
