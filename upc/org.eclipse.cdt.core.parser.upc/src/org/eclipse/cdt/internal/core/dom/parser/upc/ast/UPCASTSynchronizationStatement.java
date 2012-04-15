/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSynchronizationStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;

@SuppressWarnings("restriction")
public class UPCASTSynchronizationStatement extends ASTAttributeOwner implements IUPCASTSynchronizationStatement {
	private int statmentKind;
	private IASTExpression barrierExpression;

	public UPCASTSynchronizationStatement() {
	}

	public UPCASTSynchronizationStatement(IASTExpression barrierExpression, int statmentKind) {
		setBarrierExpression(barrierExpression);
		this.statmentKind = statmentKind;
	}

	@Override
	public UPCASTSynchronizationStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public UPCASTSynchronizationStatement copy(CopyStyle style) {
		UPCASTSynchronizationStatement copy = new UPCASTSynchronizationStatement();
		copy.statmentKind = statmentKind;
		copy.setBarrierExpression(barrierExpression == null ? null : barrierExpression.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getBarrierExpression() {
		return barrierExpression;
	}

	@Override
	public int getStatementKind() {
		return statmentKind;
	}

	@Override
	public void setBarrierExpression(IASTExpression expr) {
		this.barrierExpression = expr;
		if (expr != null) {
			expr.setParent(this);
			expr.setPropertyInParent(BARRIER_EXPRESSION);
		}
	}

	@Override
	public void setStatementKind(int kind) {
		this.statmentKind = kind;
	}

	@Override
	public boolean accept(ASTVisitor visitor) {
		if (visitor.shouldVisitStatements) {
			switch (visitor.visit(this)) {
				case ASTVisitor.PROCESS_ABORT: return false;
				case ASTVisitor.PROCESS_SKIP: return true;
			}
		}

        if (!acceptByAttributes(visitor)) return false;
		if (barrierExpression != null && !barrierExpression.accept(visitor)) return false;

		if (visitor.shouldVisitStatements) {
			switch (visitor.leave(this)) {
				case ASTVisitor.PROCESS_ABORT: return false;
				case ASTVisitor.PROCESS_SKIP: return true;
			}
		}
		return true;
	}
}