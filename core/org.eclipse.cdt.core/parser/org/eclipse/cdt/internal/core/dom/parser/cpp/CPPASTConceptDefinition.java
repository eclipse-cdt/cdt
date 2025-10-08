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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConceptDefinition;

public class CPPASTConceptDefinition extends CPPASTAttributeOwner implements ICPPASTConceptDefinition {
	private IASTName name;
	private IASTExpression expression;

	public CPPASTConceptDefinition(IASTName name, IASTExpression expr) {
		setName(name);
		setExpression(expr);
	}

	@Override
	public IASTName getName() {
		return name;
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		this.name = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(CONCEPT_NAME);
		}
		this.name = name;
	}

	@Override
	public int getRoleForName(IASTName name) {
		if (getName() == name)
			return r_definition;
		return r_unclear;
	}

	@Override
	public IASTExpression getExpression() {
		return expression;
	}

	@Override
	public void setExpression(IASTExpression expr) {
		// TODO Auto-generated method stub
		assertNotFrozen();
		this.expression = expr;
		if (expr != null) {
			expr.setParent(this);
			expr.setPropertyInParent(CONSTRAINT_EXPRESSION);
		}
	}

	@Override
	public ICPPASTConceptDefinition copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public ICPPASTConceptDefinition copy(CopyStyle style) {
		IASTName nameCopy = name == null ? null : name.copy(style);
		IASTExpression expressionCopy = expression == null ? null : expression.copy(style);
		CPPASTConceptDefinition copy = new CPPASTConceptDefinition(nameCopy, expressionCopy);
		return copy(copy, style);
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (name != null && !name.accept(action))
			return false;
		if (!acceptByAttributeSpecifiers(action))
			return false;
		if (expression != null && !expression.accept(action))
			return false;

		if (action.shouldVisitDeclarations) {
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
}
