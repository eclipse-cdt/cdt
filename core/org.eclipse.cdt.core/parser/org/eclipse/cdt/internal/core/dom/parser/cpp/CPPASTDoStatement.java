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
 *     IBM - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecDo;

/**
 * @author jcamelon
 */
public class CPPASTDoStatement extends CPPASTAttributeOwner implements IASTDoStatement, ICPPExecutionOwner {
	private IASTStatement body;
	private IASTExpression condition;

	public CPPASTDoStatement() {
	}

	public CPPASTDoStatement(IASTStatement body, IASTExpression condition) {
		setBody(body);
		setCondition(condition);
	}

	@Override
	public CPPASTDoStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTDoStatement copy(CopyStyle style) {
		CPPASTDoStatement copy = new CPPASTDoStatement();
		copy.setBody(body == null ? null : body.copy(style));
		copy.setCondition(condition == null ? null : condition.copy(style));
		return copy(copy, style);
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
	public IASTExpression getCondition() {
		return condition;
	}

	@Override
	public void setCondition(IASTExpression condition) {
		assertNotFrozen();
		this.condition = condition;
		if (condition != null) {
			condition.setParent(this);
			condition.setPropertyInParent(CONDITION);
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
		if (body != null && !body.accept(action))
			return false;
		if (condition != null && !condition.accept(action))
			return false;

		if (action.shouldVisitStatements) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (body == child) {
			other.setPropertyInParent(body.getPropertyInParent());
			other.setParent(body.getParent());
			body = (IASTStatement) other;
			return;
		}
		if (child == condition) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			condition = (IASTExpression) other;
			return;
		}
		super.replace(child, other);
	}

	@Override
	public ICPPExecution getExecution() {
		ICPPASTExpression conditionExpr = (ICPPASTExpression) getCondition();
		ICPPEvaluation conditionEval = conditionExpr.getEvaluation();
		ICPPExecution bodyExec = EvalUtil.getExecutionFromStatement(getBody());
		return new ExecDo(conditionEval, bodyExec);
	}
}
