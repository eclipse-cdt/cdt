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

import org.eclipse.cdt.codan.core.model.ICheckerWithPreferences;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * Abstract class for checkers that do all the work on function definition level
 */
public abstract class AbstractAstFunctionChecker extends AbstractIndexAstChecker implements ICheckerWithPreferences {
	@Override
	public void processAst(IASTTranslationUnit ast) {
		// traverse the ast using the visitor pattern.
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration element) {
				if (element instanceof IASTFunctionDefinition) {
					processFunction((IASTFunctionDefinition) element);
				}
				// visit all nodes to support inner functions within class definitions 
				// and gcc extensions
				return PROCESS_CONTINUE;
			}
		});
	}

	/**
	 * Process function.
	 * 
	 * @param func
	 *        - ast node representing function definition
	 */
	protected abstract void processFunction(IASTFunctionDefinition func);
}
