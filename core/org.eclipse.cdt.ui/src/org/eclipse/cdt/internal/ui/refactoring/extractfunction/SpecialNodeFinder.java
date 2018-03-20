/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;

import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;

/**
 * @author Daniel Marty IFS
 */
public class SpecialNodeFinder {
	private final List<IASTNode> nodes;
	private boolean returnStatement;
	private boolean constExpr;
	private boolean lValue;
	private boolean unknownType;
	private boolean unOp;
	private boolean lambda;

	SpecialNodeFinder(List<IASTNode> nodes) {
		this.nodes = nodes;
	}

	public void findNodes() {
		if (nodes.size() == 1) {
			IASTNode firstNode = nodes.get(0);
			if (firstNode instanceof ICPPASTLiteralExpression) {
				ICPPASTLiteralExpression node = (ICPPASTLiteralExpression) firstNode;
				constExpr = node.getEvaluation().isConstantExpression();
			}
			if (firstNode instanceof IASTUnaryExpression) {
				IASTUnaryExpression unaryExpression = (IASTUnaryExpression) firstNode;
				unOp = unaryExpression.getOperator() == IASTUnaryExpression.op_amper;
				return;
			}

			if (firstNode instanceof ICPPASTExpression) {
				ICPPASTExpression cppExpr = (ICPPASTExpression) firstNode;
				if (cppExpr.isLValue()) {
					lValue = true;
				} else {
					IType exprType = cppExpr.getExpressionType();
					if (exprType instanceof ICPPUnknownType || exprType instanceof IProblemType) {
						unknownType = true;
					}
				}
			} else if (firstNode instanceof IASTExpression) {
				lValue = ((IASTExpression) firstNode).isLValue();
			}

			if (firstNode instanceof ICPPASTLambdaExpression) {
				lambda = true;
				return;
			}
		}
		for (IASTNode node : nodes) {
			node.accept(new ASTVisitor() {
				{
					shouldVisitStatements = true;
				}

				@Override
				public int visit(IASTStatement statement) {
					if (statement instanceof IASTReturnStatement) {
						if (ASTQueries.findAncestorWithType(statement,
								ICPPASTLambdaExpression.class) == null) {
							returnStatement = true;
						}
					}
					return PROCESS_CONTINUE;
				}

			});
		}
	}

	public boolean hasReturnStatement() {
		return returnStatement;
	}

	public boolean isConstExpr() {
		return constExpr;
	}

	public boolean isLvalue() {
		return lValue;
	}

	public boolean isUnknownType() {
		return unknownType;
	}

	public boolean isUnOp() {
		return unOp;
	}

	public boolean isLambda() {
		return lambda;
	}
}