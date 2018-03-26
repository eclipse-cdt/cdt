/*******************************************************************************
 * Copyright (c) 2008, 2017 Institute for Software, HSR Hochschule fuer Technik
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
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;

import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

class IllegalReturnSelectionChecker extends AbstractSelectionChecker {
	private final List<IASTNode> nodes;
	private boolean returnStmnts;
	private boolean insideLoop;
	private boolean successful;
	private boolean passedExit;
	private boolean hasDefault;
	private boolean noReturnAttribute;

	enum ExitPointStatus {
		NO_EXIT_POINTS, SAME_EXIT_POINTS, DIFFERENT_EXIT_POINTS
	}

	public IllegalReturnSelectionChecker(List<IASTNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public boolean check() {
		if (!hasReturn(nodes)) {
			return true;
		} else if (insideLoop) {
			return false;
		}
		for (IASTNode node : nodes) {
			if (node instanceof IASTStatement) {
				if (hasReturnsOnAllPaths((IASTStatement) node)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasReturnsOnAllPaths(IASTStatement statement) {
		if (isExitPoint(statement)) {
			return true;
		} else if (statement instanceof IASTCompoundStatement) {
			IASTCompoundStatement compoundStatement = (IASTCompoundStatement) statement;
			for (IASTStatement css : compoundStatement.getStatements()) {
				if (hasReturnsOnAllPaths(css)) {
					return true;
				}
			}
			errorMessage = Messages.ExtractFunctionRefactoring_IllegalReturnSelectionCompound;
			return false;
		} else if (statement instanceof IASTIfStatement) {
			IASTIfStatement ifStatement = (IASTIfStatement) statement;
			if (hasReturnsOnAllPaths(ifStatement.getThenClause())
					&& hasReturnsOnAllPaths(ifStatement.getElseClause())) {
				return true;
			}
			errorMessage = Messages.ExtractFunctionRefactoring_IllegalReturnSelectionIf;
			return false;
		} else if (statement instanceof IASTSwitchStatement) {
			IASTSwitchStatement switchStatement = (IASTSwitchStatement) statement;
			passedExit = false;
			hasDefault = false;
			successful = true;
			switchStatement.accept(new ASTVisitor() {
				{
					shouldVisitStatements = true;
				}

				@Override
				public int visit(IASTStatement statement) {
					if (isExitPoint(statement)) {
						passedExit = true;
					} else if (statement instanceof IASTCaseStatement) {
						passedExit = false;
					} else if (statement instanceof IASTBreakStatement) {
						if (!passedExit) {
							successful = false;
							return PROCESS_SKIP;
						}
					} else if (statement instanceof IASTDefaultStatement) {
						hasDefault = true;
						passedExit = false;
					}
					return PROCESS_CONTINUE;
				}
			});
			if (!hasDefault) {
				errorMessage = Messages.ExtractFunctionRefactoring_IllegalReturnSelectionDefault;
				return false;
			} else if (!successful || !passedExit) {
				errorMessage = Messages.ExtractFunctionRefactoring_IllegalReturnSelectionSwitch;
				return false;
			} else {
				return true;
			}
		}
		errorMessage = Messages.ExtractFunctionRefactoring_IllegalReturnSelection;
		return false;
	}

	private boolean hasReturn(List<IASTNode> nodes) {
		returnStmnts = false;
		insideLoop = false;
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
							returnStmnts = true;
							if (isInsideLoop(statement)) {
								insideLoop = true;
							}
							return PROCESS_SKIP;
						}
					}
					return PROCESS_CONTINUE;
				}
			});
		}
		return returnStmnts;
	}

	private boolean isInsideLoop(IASTStatement statement) {
		if (ASTQueries.findAncestorWithType(statement, IASTForStatement.class) != null) {
			errorMessage = Messages.ExtractFunctionRefactoring_IllegalReturnSelectionFor;
			return true;
		} else if (ASTQueries.findAncestorWithType(statement, ICPPASTRangeBasedForStatement.class) != null) {
			errorMessage = Messages.ExtractFunctionRefactoring_IllegalReturnSelectionFor;
			return true;
		} else if (ASTQueries.findAncestorWithType(statement, IASTDoStatement.class) != null) {
			errorMessage = Messages.ExtractFunctionRefactoring_IllegalReturnSelectionDo;
			return true;
		} else if (ASTQueries.findAncestorWithType(statement, IASTWhileStatement.class) != null) {
			errorMessage = Messages.ExtractFunctionRefactoring_IllegalReturnSelectionWhile;
			return true;
		}
		return false;
	}

	private boolean isExitPoint(IASTStatement statement) {
		if (statement == null) {
			return false;
		}
		return isThrowStatement(statement) || isExitStatement(statement)
				|| statement instanceof IASTReturnStatement;
	}

	private boolean isThrowStatement(IASTNode statement) {
		if (!(statement instanceof IASTExpressionStatement))
			return false;
		IASTExpression expression = ((IASTExpressionStatement) statement).getExpression();
		if (!(expression instanceof IASTUnaryExpression))
			return false;
		return ((IASTUnaryExpression) expression).getOperator() == IASTUnaryExpression.op_throw;
	}

	private boolean isExitStatement(IASTNode statement) {
		statement.accept(new ASTVisitor() {
			{
				shouldVisitImplicitNames = true;
				shouldVisitImplicitDestructorNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (name instanceof IASTImplicitName) {
					IBinding binding = name.resolveBinding();
					if (binding instanceof IFunction && ((IFunction) binding).isNoReturn()) {
						noReturnAttribute = true;
						return PROCESS_ABORT;
					}
				}
				return PROCESS_CONTINUE;
			}
		});
		if (noReturnAttribute) {
			return true;
		}
		if (!(statement instanceof IASTExpressionStatement))
			return false;
		IASTExpression expression = ((IASTExpressionStatement) statement).getExpression();
		if (!(expression instanceof IASTFunctionCallExpression))
			return false;
		IASTExpression functionNameExpression = ((IASTFunctionCallExpression) expression)
				.getFunctionNameExpression();
		if (functionNameExpression instanceof IASTIdExpression) {
			IASTName name = ((IASTIdExpression) functionNameExpression).getName();

			IBinding binding = name.resolveBinding();
			if (binding instanceof ICPPFunction && ((IFunction) binding).isNoReturn()) {
				return true;
			}
		}
		return functionNameExpression.getRawSignature().equals("exit"); //$NON-NLS-1$
	}
}
