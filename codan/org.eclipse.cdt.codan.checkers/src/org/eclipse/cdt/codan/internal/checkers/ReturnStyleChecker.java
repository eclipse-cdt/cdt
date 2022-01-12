/*******************************************************************************
 * Copyright (c) 2010, 2012 Marc-Andre Laperle and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

public class ReturnStyleChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.ReturnStyleProblem"; //$NON-NLS-1$

	@Override
	public boolean runInEditor() {
		return true;
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTReturnStatement) {
					boolean isValidStyle = false;
					IASTNode[] children = statement.getChildren();
					if (children.length == 0) {
						isValidStyle = true;
					} else if (children.length == 1 && children[0] instanceof IASTUnaryExpression) {
						IASTUnaryExpression unaryExpression = (IASTUnaryExpression) children[0];
						if (unaryExpression.getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
							isValidStyle = true;
						}
					}
					if (!isValidStyle) {
						reportProblem(ERR_ID, statement);
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
