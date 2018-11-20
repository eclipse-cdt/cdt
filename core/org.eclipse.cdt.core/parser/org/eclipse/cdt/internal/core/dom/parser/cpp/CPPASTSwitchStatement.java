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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecSwitch;

/**
 * Switch statement in C++.
 */
public class CPPASTSwitchStatement extends CPPASTAttributeOwner implements ICPPASTSwitchStatement, ICPPExecutionOwner {
	private IScope scope;
	private IASTStatement initStatement;
	private IASTExpression controllerExpression;
	private IASTDeclaration controllerDeclaration;
	private IASTStatement body;

	public CPPASTSwitchStatement() {
	}

	public CPPASTSwitchStatement(IASTDeclaration controller, IASTStatement body) {
		setControllerDeclaration(controller);
		setBody(body);
	}

	public CPPASTSwitchStatement(IASTExpression controller, IASTStatement body) {
		setControllerExpression(controller);
		setBody(body);
	}

	@Override
	public CPPASTSwitchStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTSwitchStatement copy(CopyStyle style) {
		CPPASTSwitchStatement copy = new CPPASTSwitchStatement();
		copy.setInitializerStatement(initStatement == null ? null : initStatement.copy(style));
		copy.setControllerDeclaration(controllerDeclaration == null ? null : controllerDeclaration.copy(style));
		copy.setControllerExpression(controllerExpression == null ? null : controllerExpression.copy(style));
		copy.setBody(body == null ? null : body.copy(style));
		return copy(copy, style);
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
	public IASTExpression getControllerExpression() {
		return controllerExpression;
	}

	@Override
	public void setControllerExpression(IASTExpression controller) {
		assertNotFrozen();
		this.controllerExpression = controller;
		if (controller != null) {
			controller.setParent(this);
			controller.setPropertyInParent(CONTROLLER_EXP);
			controllerDeclaration = null;
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
		if (initStatement != null && !initStatement.accept(action))
			return false;
		if (controllerExpression != null && !controllerExpression.accept(action))
			return false;
		if (controllerDeclaration != null && !controllerDeclaration.accept(action))
			return false;
		if (body != null && !body.accept(action))
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
		if (initStatement == child) {
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
			initStatement = (IASTStatement) other;
			return;
		}
		if (body == child) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			body = (IASTStatement) other;
			return;
		} else if (controllerDeclaration == child || controllerExpression == child) {
			if (other instanceof IASTExpression) {
				setControllerExpression((IASTExpression) other);
			} else if (other instanceof IASTDeclaration) {
				setControllerDeclaration((IASTDeclaration) other);
			}
			return;
		}
		super.replace(child, other);
	}

	@Override
	public IASTDeclaration getControllerDeclaration() {
		return controllerDeclaration;
	}

	@Override
	public void setControllerDeclaration(IASTDeclaration d) {
		assertNotFrozen();
		controllerDeclaration = d;
		if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(CONTROLLER_DECLARATION);
			controllerExpression = null;
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
		ICPPASTExpression controllerExpr = (ICPPASTExpression) getControllerExpression();
		ICPPExecutionOwner controllerDecl = (ICPPExecutionOwner) getControllerDeclaration();
		ICPPEvaluation controllerExprEval = controllerExpr != null ? controllerExpr.getEvaluation() : null;
		ExecSimpleDeclaration controllerDeclExec = controllerDecl != null
				? (ExecSimpleDeclaration) controllerDecl.getExecution()
				: null;
		IASTStatement[] bodyStmts = null;
		if (body instanceof ICPPASTCompoundStatement) {
			ICPPASTCompoundStatement compoundStmt = (ICPPASTCompoundStatement) body;
			bodyStmts = compoundStmt.getStatements();
		} else {
			bodyStmts = new IASTStatement[] { body };
		}

		ICPPExecution[] bodyStmtExecutions = new ICPPExecution[bodyStmts.length];
		for (int i = 0; i < bodyStmts.length; i++) {
			bodyStmtExecutions[i] = EvalUtil.getExecutionFromStatement(bodyStmts[i]);
		}
		return new ExecSwitch(initStmtExec, controllerExprEval, controllerDeclExec, bodyStmtExecutions);
	}
}
