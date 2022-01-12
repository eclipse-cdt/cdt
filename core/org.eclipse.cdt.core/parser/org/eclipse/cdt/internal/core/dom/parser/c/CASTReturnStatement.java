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
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public class CASTReturnStatement extends ASTAttributeOwner implements IASTReturnStatement, IASTAmbiguityParent {
	private IASTExpression retValue;

	public CASTReturnStatement() {
	}

	public CASTReturnStatement(IASTExpression retValue) {
		setReturnValue(retValue);
	}

	@Override
	public CASTReturnStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTReturnStatement copy(CopyStyle style) {
		CASTReturnStatement copy = new CASTReturnStatement(retValue == null ? null : retValue.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getReturnValue() {
		return retValue;
	}

	@Override
	public void setReturnValue(IASTExpression returnValue) {
		assertNotFrozen();
		retValue = returnValue;
		if (returnValue != null) {
			returnValue.setParent(this);
			returnValue.setPropertyInParent(RETURNVALUE);
		}
	}

	@Override
	public IASTInitializerClause getReturnArgument() {
		return getReturnValue();
	}

	@Override
	public void setReturnArgument(IASTInitializerClause returnValue) {
		if (returnValue instanceof IASTExpression) {
			setReturnValue((IASTExpression) returnValue);
		} else {
			setReturnValue(null);
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
		if (retValue != null && !retValue.accept(action))
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
		if (child == retValue) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			retValue = (IASTExpression) other;
		}
	}
}