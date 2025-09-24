/*******************************************************************************
 * Copyright (c) 2025 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstraintOwner;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

public abstract class CPPASTConstraintOwner extends CPPASTAttributeOwner implements ICPPASTConstraintOwner {
	private IASTExpression[] constraintExpressions = IASTExpression.EMPTY_EXPRESSION_ARRAY;

	protected <T extends CPPASTConstraintOwner> T copy(T copy, CopyStyle style) {
		for (IASTExpression constraintExpression : getConstraintExpressions()) {
			copy.addConstraintExpression(constraintExpression.copy(style));
		}
		return super.copy(copy, style);
	}

	@Override
	public IASTExpression[] getConstraintExpressions() {
		constraintExpressions = ArrayUtil.trim(constraintExpressions);
		return constraintExpressions;
	}

	@Override
	public void addConstraintExpression(IASTExpression expression) {
		assertNotFrozen();
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(CONSTRAINT_SPECIFIER);
			constraintExpressions = ArrayUtil.append(constraintExpressions, expression);
		}
	}

	protected boolean acceptByConstraints(ASTVisitor action) {
		for (IASTExpression expr : constraintExpressions) {
			if (expr == null)
				break;
			if (!expr.accept(action))
				return false;
		}

		// return acceptByAttributeSpecifiers(action);
		// TODO: see if chaining call to parent.accept() is desireable instead
		return true;
	}

	@Override
	public boolean accept(ASTVisitor visitor) {
		return acceptByConstraints(visitor);
	}
}
