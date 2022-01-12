/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anders Dahlberg (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTGotoStatement;

/**
 * GNU C++ goto statement.
 *
 * <code>
 * foo:
 *   void *labelPtr = &&foo;
 *   goto *labelPtr; // this is the statement
 * </code>
 *
 * @since 5.8
 */
public class GNUCPPASTGotoStatement extends CPPASTAttributeOwner implements IGNUASTGotoStatement {
	private IASTExpression expression;

	public GNUCPPASTGotoStatement() {
	}

	public GNUCPPASTGotoStatement(IASTExpression expression) {
		setLabelNameExpression(expression);
	}

	@Override
	public GNUCPPASTGotoStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public GNUCPPASTGotoStatement copy(CopyStyle style) {
		GNUCPPASTGotoStatement copy = new GNUCPPASTGotoStatement(expression == null ? null : expression.copy(style));
		return copy(copy, style);
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
	public IASTExpression getLabelNameExpression() {
		return expression;
	}

	@Override
	public void setLabelNameExpression(IASTExpression expression) {
		assertNotFrozen();
		this.expression = expression;

		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(LABEL_NAME);
		}
	}

	@Override
	public int getRoleForName(IASTName n) {
		return r_unclear;
	}
}
