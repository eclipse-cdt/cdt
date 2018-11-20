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
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecWhile;

/**
 * While statement in C++.
 */
public class CPPASTWhileStatement extends CPPASTAttributeOwner implements ICPPASTWhileStatement, ICPPExecutionOwner {
	private IASTExpression condition;
	private IASTDeclaration condition2;
	private IASTStatement body;
	private IScope scope;

	public CPPASTWhileStatement() {
	}

	public CPPASTWhileStatement(IASTDeclaration condition, IASTStatement body) {
		setConditionDeclaration(condition);
		setBody(body);
	}

	public CPPASTWhileStatement(IASTExpression condition, IASTStatement body) {
		setCondition(condition);
		setBody(body);
	}

	@Override
	public CPPASTWhileStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTWhileStatement copy(CopyStyle style) {
		CPPASTWhileStatement copy = new CPPASTWhileStatement();
		copy.setConditionDeclaration(condition2 == null ? null : condition2.copy(style));
		copy.setCondition(condition == null ? null : condition.copy(style));
		copy.setBody(body == null ? null : body.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getCondition() {
		return condition;
	}

	@Override
	public void setCondition(IASTExpression condition) {
		assertNotFrozen();
		this.condition = condition;
		if (condition != null) {
			condition.setParent(this);
			condition.setPropertyInParent(CONDITIONEXPRESSION);
			condition2 = null;
		}
	}

	@Override
	public IASTStatement getBody() {
		return body;
	}

	@Override
	public void setBody(IASTStatement body) {
		assertNotFrozen();
		this.body = body;
		if (body != null) {
			body.setParent(this);
			body.setPropertyInParent(BODY);
		}
	}

	@Override
	public IASTDeclaration getConditionDeclaration() {
		return condition2;
	}

	@Override
	public void setConditionDeclaration(IASTDeclaration declaration) {
		assertNotFrozen();
		condition2 = declaration;
		if (declaration != null) {
			declaration.setParent(this);
			declaration.setPropertyInParent(CONDITIONDECLARATION);
			condition = null;
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitStatements) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (!acceptByAttributeSpecifiers(action))
			return false;
		if (condition != null && !condition.accept(action))
			return false;
		if (condition2 != null && !condition2.accept(action))
			return false;
		if (body != null && !body.accept(action))
			return false;

		if (action.shouldVisitStatements && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (body == child) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			body = (IASTStatement) other;
			return;
		}
		if (child == condition || child == condition2) {
			if (other instanceof IASTExpression) {
				setCondition((IASTExpression) other);
			} else if (other instanceof IASTDeclaration) {
				setConditionDeclaration((IASTDeclaration) other);
			}
			return;
		}
		super.replace(child, other);
	}

	@Override
	public IScope getScope() {
		if (scope == null)
			scope = new CPPBlockScope(this);
		return scope;
	}

	@Override
	public ICPPExecution getExecution() {
		ICPPASTExpression conditionExpr = (ICPPASTExpression) getCondition();
		ICPPExecutionOwner conditionDecl = (ICPPExecutionOwner) getConditionDeclaration();
		ICPPEvaluation conditionExprEval = conditionExpr != null ? conditionExpr.getEvaluation() : null;
		ExecSimpleDeclaration conditionDeclExec = conditionDecl != null
				? (ExecSimpleDeclaration) conditionDecl.getExecution()
				: null;
		ICPPExecution bodyExec = EvalUtil.getExecutionFromStatement(getBody());
		return new ExecWhile(conditionExprEval, conditionDeclExec, bodyExec);
	}
}
