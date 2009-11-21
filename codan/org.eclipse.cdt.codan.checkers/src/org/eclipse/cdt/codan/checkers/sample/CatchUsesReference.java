/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.codan.checkers.sample;

import org.eclipse.cdt.codan.core.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;

/**
 * @author Alena
 *
 */
public class CatchUsesReference extends AbstractIndexAstChecker {
	private static final String ER_ID = "org.eclipse.cdt.codan.checkers.sample.CatchUsesReference";

	public void processAst(IASTTranslationUnit ast) {
		// traverse the ast using the visitor pattern.
		ast.accept(new OnCatch());
	}
	
	class OnCatch extends ASTVisitor {
		OnCatch() {
			shouldVisitStatements = true;
		}
		public int visit(IASTStatement stmt) {
			if (stmt instanceof ICPPASTTryBlockStatement) {
				ICPPASTTryBlockStatement tblock = (ICPPASTTryBlockStatement) stmt;
				ICPPASTCatchHandler[] catchHandlers = tblock.getCatchHandlers();
				for (int i = 0; i < catchHandlers.length; i++) {
					ICPPASTCatchHandler catchHandler = catchHandlers[i];
					if (usesReference(catchHandler)) {
						reportProblem(ER_ID, catchHandler.getDeclaration(), "Catch clause uses reference in declaration of exception");
					}
				}
		
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
		/**
		 * @param catchHandler
		 * @return
		 */
		private boolean usesReference(ICPPASTCatchHandler catchHandler) {
			IASTDeclaration declaration = catchHandler.getDeclaration();
			if (declaration instanceof IASTSimpleDeclaration) {
				IASTDeclarator[] declarators = ((IASTSimpleDeclaration) declaration).getDeclarators();
				for (int i = 0; i < declarators.length; i++) {
					IASTDeclarator d = declarators[i];
					IASTPointerOperator[] pointerOperators = d.getPointerOperators();
					for (int j = 0; j < pointerOperators.length; j++) {
						IASTPointerOperator po = pointerOperators[j];
						if (po instanceof ICPPASTReferenceOperator) {
							return true;
						}
						
					}
				}
			}
			return false;
		}
	}
	

}
