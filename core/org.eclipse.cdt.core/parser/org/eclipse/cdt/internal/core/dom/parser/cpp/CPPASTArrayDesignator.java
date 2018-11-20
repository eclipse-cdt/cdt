/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sergey Prigogin (Google) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Implementation of array designator.
 */
public class CPPASTArrayDesignator extends ASTNode implements ICPPASTArrayDesignator, IASTAmbiguityParent {
	private ICPPASTExpression expression;

	public CPPASTArrayDesignator() {
	}

	public CPPASTArrayDesignator(ICPPASTExpression exp) {
		setSubscriptExpression(exp);
	}

	@Override
	public CPPASTArrayDesignator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTArrayDesignator copy(CopyStyle style) {
		CPPASTArrayDesignator copy = new CPPASTArrayDesignator(
				expression == null ? null : (ICPPASTExpression) expression.copy(style));
		return copy(copy, style);
	}

	@Override
	public ICPPASTExpression getSubscriptExpression() {
		return expression;
	}

	@Override
	public void setSubscriptExpression(ICPPASTExpression value) {
		assertNotFrozen();
		expression = value;
		if (value != null) {
			value.setParent(this);
			value.setPropertyInParent(SUBSCRIPT_EXPRESSION);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDesignators) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		if (expression != null && !expression.accept(action))
			return false;

		if (action.shouldVisitDesignators && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == expression) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			expression = (ICPPASTExpression) other;
		}
	}
}
