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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTExpressionStatement extends ASTAttributeOwner implements IASTExpressionStatement, IASTAmbiguityParent {
	private IASTExpression expression;

	public CASTExpressionStatement() {
	}

	public CASTExpressionStatement(IASTExpression expression) {
		setExpression(expression);
	}

	@Override
	public CASTExpressionStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTExpressionStatement copy(CopyStyle style) {
		CASTExpressionStatement copy = new CASTExpressionStatement();
		copy.setExpression(expression == null ? null : expression.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getExpression() {
		return expression;
	}

	@Override
	public void setExpression(IASTExpression expression) {
		assertNotFrozen();
		this.expression = expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(EXPRESSION);
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
		if (expression != null && !expression.accept(action))
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
		if (child == expression) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			expression = (IASTExpression) other;
		}
	}
}
