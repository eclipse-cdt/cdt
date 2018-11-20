/*******************************************************************************
 * Copyright (c) 2014 Ericsson.
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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTGotoStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;

/**
 * GNU C goto statement.
 *
 * <code>
 * foo:
 *   void *labelPtr = &&foo;
 *   goto *labelPtr; // this is the statement
 * </code>
 *
 * @since 5.8
 */
public class GNUCASTGotoStatement extends ASTAttributeOwner implements IGNUASTGotoStatement {
	private IASTExpression fExpression;

	public GNUCASTGotoStatement() {
	}

	public GNUCASTGotoStatement(IASTExpression expression) {
		setLabelNameExpression(expression);
	}

	@Override
	public GNUCASTGotoStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public GNUCASTGotoStatement copy(CopyStyle style) {
		GNUCASTGotoStatement copy = new GNUCASTGotoStatement();
		copy.setLabelNameExpression(fExpression == null ? null : fExpression.copy(style));
		return copy(copy, style);
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitExpressions) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (fExpression != null && !fExpression.accept(action))
			return false;

		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public IASTExpression getLabelNameExpression() {
		return fExpression;
	}

	@Override
	public void setLabelNameExpression(IASTExpression expression) {
		assertNotFrozen();
		this.fExpression = expression;

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
