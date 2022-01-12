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
 *     IBM Rational Software - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTDoStatement extends ASTAttributeOwner implements IASTDoStatement, IASTAmbiguityParent {
	private IASTStatement body;
	private IASTExpression condition;

	public CASTDoStatement() {
	}

	public CASTDoStatement(IASTStatement body, IASTExpression condition) {
		setBody(body);
		setCondition(condition);
	}

	@Override
	public CASTDoStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTDoStatement copy(CopyStyle style) {
		CASTDoStatement copy = new CASTDoStatement();
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
		}
		if (child == condition) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			condition = (IASTExpression) other;
		}
	}
}
