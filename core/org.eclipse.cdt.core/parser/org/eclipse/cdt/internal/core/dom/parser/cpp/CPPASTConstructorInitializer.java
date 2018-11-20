/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpressionList;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Initializer list in parenthesis.
 */
public class CPPASTConstructorInitializer extends ASTNode
		implements ICPPASTConstructorInitializer, IASTAmbiguityParent {
	private IASTInitializerClause[] fArguments;

	public CPPASTConstructorInitializer() {
		setArguments(null);
	}

	public CPPASTConstructorInitializer(IASTInitializerClause[] args) {
		setArguments(args);
	}

	@Override
	public CPPASTConstructorInitializer copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTConstructorInitializer copy(CopyStyle style) {
		IASTInitializerClause[] args = null;
		if (fArguments != null) {
			args = new IASTInitializerClause[fArguments.length];
			for (int i = 0; i < fArguments.length; i++) {
				args[i] = fArguments[i].copy(style);
			}
		}
		CPPASTConstructorInitializer copy = new CPPASTConstructorInitializer(args);
		return copy(copy, style);
	}

	@Override
	public IASTInitializerClause[] getArguments() {
		return fArguments;
	}

	@Override
	public void setArguments(IASTInitializerClause[] arguments) {
		assertNotFrozen();
		if (arguments == null) {
			fArguments = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		} else {
			fArguments = arguments;
			for (IASTInitializerClause arg : arguments) {
				arg.setParent(this);
				arg.setPropertyInParent(ARGUMENT);
			}
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitInitializers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		for (IASTInitializerClause arg : fArguments) {
			if (!arg.accept(action))
				return false;
		}

		if (action.shouldVisitInitializers && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		for (int i = 0; i < fArguments.length; ++i) {
			if (child == fArguments[i]) {
				other.setPropertyInParent(child.getPropertyInParent());
				other.setParent(child.getParent());
				fArguments[i] = (IASTExpression) other;
			}
		}
	}

	@Override
	@Deprecated
	public IASTExpression getExpression() {
		if (fArguments.length == 0)
			return null;
		if (fArguments.length == 1) {
			IASTInitializerClause arg = fArguments[0];
			if (arg instanceof IASTExpression)
				return (IASTExpression) arg;
			return null;
		}

		CPPASTExpressionList result = new CPPASTExpressionList();
		for (IASTInitializerClause arg : fArguments) {
			if (arg instanceof IASTExpression) {
				result.addExpression(((IASTExpression) arg).copy());
			}
		}
		result.setParent(this);
		result.setPropertyInParent(EXPRESSION);
		return result;
	}

	@Override
	@Deprecated
	public void setExpression(IASTExpression expression) {
		assertNotFrozen();
		if (expression == null) {
			setArguments(null);
		} else if (expression instanceof ICPPASTExpressionList) {
			setArguments(((ICPPASTExpressionList) expression).getExpressions());
		} else {
			setArguments(new IASTExpression[] { expression });
		}
	}
}
