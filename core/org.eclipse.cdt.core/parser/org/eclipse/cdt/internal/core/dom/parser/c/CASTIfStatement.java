/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * If statements for C.
 */
public class CASTIfStatement extends ASTAttributeOwner implements IASTIfStatement, IASTAmbiguityParent {
	private IASTExpression condition;
	private IASTStatement thenClause;
	private IASTStatement elseClause;

	public CASTIfStatement() {
	}

	public CASTIfStatement(IASTExpression condition, IASTStatement thenClause) {
		setConditionExpression(condition);
		setThenClause(thenClause);
	}

	public CASTIfStatement(IASTExpression condition, IASTStatement thenClause, IASTStatement elseClause) {
		this(condition, thenClause);
		setElseClause(elseClause);
	}

	@Override
	public CASTIfStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTIfStatement copy(CopyStyle style) {
		CASTIfStatement copy = new CASTIfStatement();
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
		IASTIfStatement stmt = this;
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

			if (!((CASTIfStatement) stmt).acceptByAttributeSpecifiers(action))
				return false;

			IASTNode child = stmt.getConditionExpression();
			if (child != null && !child.accept(action))
				return false;
			child = stmt.getThenClause();
			if (child != null && !child.accept(action))
				return false;
			child = stmt.getElseClause();
			if (child instanceof IASTIfStatement) {
				if (action.shouldVisitStatements) {
					N n = new N(stmt);
					n.fNext = stack;
					stack = n;
				}
				stmt = (IASTIfStatement) child;
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
		if (thenClause == child) {
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
			thenClause = (IASTStatement) other;
		}
		if (elseClause == child) {
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
			elseClause = (IASTStatement) other;
		}
		if (child == condition) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			condition = (IASTExpression) other;
		}
	}
}
