/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecIf;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecSimpleDeclaration;

/**
 * If statement in C++
 */
public class CPPASTIfStatement extends CPPASTAttributeOwner implements ICPPASTIfStatement, ICPPExecutionOwner {
	private boolean isConstexpr;
	private IASTStatement initStatement;
	private IASTExpression condition;
	private IASTStatement thenClause;
	private IASTStatement elseClause;
	private IASTDeclaration condDecl;
	private IScope scope;

	public CPPASTIfStatement() {
	}

	public CPPASTIfStatement(IASTDeclaration condition, IASTStatement thenClause, IASTStatement elseClause) {
		setConditionDeclaration(condition);
		setThenClause(thenClause);
		setElseClause(elseClause);
	}

	public CPPASTIfStatement(IASTExpression condition, IASTStatement thenClause, IASTStatement elseClause) {
		setConditionExpression(condition);
		setThenClause(thenClause);
		setElseClause(elseClause);
	}

	@Override
	public CPPASTIfStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTIfStatement copy(CopyStyle style) {
		CPPASTIfStatement copy = new CPPASTIfStatement();
		copy.setIsConstexpr(isConstexpr);
		copy.setInitializerStatement(initStatement == null ? null : initStatement.copy(style));
		copy.setConditionDeclaration(condDecl == null ? null : condDecl.copy(style));
		copy.setConditionExpression(condition == null ? null : condition.copy(style));
		copy.setThenClause(thenClause == null ? null : thenClause.copy(style));
		copy.setElseClause(elseClause == null ? null : elseClause.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getConditionExpression() {
		return condition;
	}

	@Override
	public void setConditionExpression(IASTExpression condition) {
		assertNotFrozen();
		this.condition = condition;
		if (condition != null) {
			condition.setParent(this);
			condition.setPropertyInParent(CONDITION);
			condDecl = null;
		}
	}

	@Override
	public IASTStatement getThenClause() {
		return thenClause;
	}

	@Override
	public void setThenClause(IASTStatement thenClause) {
		assertNotFrozen();
		this.thenClause = thenClause;
		if (thenClause != null) {
			thenClause.setParent(this);
			thenClause.setPropertyInParent(THEN);
		}
	}

	@Override
	public IASTStatement getElseClause() {
		return elseClause;
	}

	@Override
	public void setElseClause(IASTStatement elseClause) {
		assertNotFrozen();
		this.elseClause = elseClause;
		if (elseClause != null) {
			elseClause.setParent(this);
			elseClause.setPropertyInParent(ELSE);
		}
	}

	private static class N {
		final IASTIfStatement fIfStatement;
		N fNext;

		N(IASTIfStatement stmt) {
			fIfStatement = stmt;
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		N stack = null;
		ICPPASTIfStatement stmt = this;
		loop: for (;;) {
			if (action.shouldVisitStatements) {
				switch (action.visit(stmt)) {
				case ASTVisitor.PROCESS_ABORT:
					return false;
				case ASTVisitor.PROCESS_SKIP:
					stmt = null;
					break loop;
				default:
					break;
				}
			}

			if (!((CPPASTIfStatement) stmt).acceptByAttributeSpecifiers(action))
				return false;

			IASTNode child = stmt.getInitializerStatement();
			if (child != null && !child.accept(action))
				return false;
			child = stmt.getConditionExpression();
			if (child != null && !child.accept(action))
				return false;
			child = stmt.getConditionDeclaration();
			if (child != null && !child.accept(action))
				return false;
			child = stmt.getThenClause();
			if (child != null && !child.accept(action))
				return false;
			child = stmt.getElseClause();
			if (child instanceof ICPPASTIfStatement) {
				if (action.shouldVisitStatements) {
					N n = new N(stmt);
					n.fNext = stack;
					stack = n;
				}
				stmt = (ICPPASTIfStatement) child;
			} else {
				if (child != null && !child.accept(action))
					return false;
				break loop;
			}
		}

		if (action.shouldVisitStatements) {
			if (stmt != null && action.leave(stmt) == ASTVisitor.PROCESS_ABORT)
				return false;
			while (stack != null) {
				if (action.leave(stack.fIfStatement) == ASTVisitor.PROCESS_ABORT)
					return false;
				stack = stack.fNext;
			}
		}
		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (initStatement == child) {
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
			initStatement = (IASTStatement) other;
			return;
		}

		if (thenClause == child) {
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
			thenClause = (IASTStatement) other;
			return;
		}

		if (elseClause == child) {
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
			elseClause = (IASTStatement) other;
			return;
		}

		if (condition == child || condDecl == child) {
			if (other instanceof IASTExpression) {
				setConditionExpression((IASTExpression) other);
			} else if (other instanceof IASTDeclaration) {
				setConditionDeclaration((IASTDeclaration) other);
			}
			return;
		}

		super.replace(child, other);
	}

	@Override
	public IASTDeclaration getConditionDeclaration() {
		return condDecl;
	}

	@Override
	public void setConditionDeclaration(IASTDeclaration d) {
		assertNotFrozen();
		condDecl = d;
		if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(CONDITION);
			condition = null;
		}
	}

	@Override
	public boolean isConstexpr() {
		return isConstexpr;
	}

	@Override
	public void setIsConstexpr(boolean isConstexpr) {
		assertNotFrozen();
		this.isConstexpr = isConstexpr;
	}

	@Override
	public IASTStatement getInitializerStatement() {
		return initStatement;
	}

	@Override
	public void setInitializerStatement(IASTStatement statement) {
		assertNotFrozen();
		this.initStatement = statement;
		if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(INIT_STATEMENT);
			statement = null;
		}
	}

	@Override
	public IScope getScope() {
		if (scope == null)
			scope = new CPPBlockScope(this);
		return scope;
	}

	@Override
	public ICPPExecution getExecution() {
		ICPPExecution initStmtExec = EvalUtil.getExecutionFromStatement(getInitializerStatement());
		ICPPASTExpression conditionExpr = (ICPPASTExpression) getConditionExpression();
		ICPPExecutionOwner conditionDecl = (ICPPExecutionOwner) getConditionDeclaration();
		ICPPEvaluation conditionExprEval = conditionExpr != null ? conditionExpr.getEvaluation() : null;
		ExecSimpleDeclaration conditionDeclExec = conditionDecl != null
				? (ExecSimpleDeclaration) conditionDecl.getExecution()
				: null;
		ICPPExecution thenClauseExec = EvalUtil.getExecutionFromStatement(getThenClause());
		ICPPExecution elseClauseExec = EvalUtil.getExecutionFromStatement(getElseClause());
		return new ExecIf(isConstexpr, initStmtExec, conditionExprEval, conditionDeclExec, thenClauseExec,
				elseClauseExec);
	}
}