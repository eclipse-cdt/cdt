/*******************************************************************************
 *  Copyright (c) 2006, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTForallStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTForStatement;

@SuppressWarnings("restriction")
public class UPCASTForallStatement extends CASTForStatement implements IUPCASTForallStatement {

	private IASTExpression affinity;
	private boolean affinityContinue;


	public UPCASTForallStatement() {
	}

	public UPCASTForallStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body, IASTExpression affinity) {
		super(init, condition, iterationExpression, body);
		setAffinityExpression(affinity);
	}

	@Override
	public UPCASTForallStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public UPCASTForallStatement copy(CopyStyle style) {
		UPCASTForallStatement copy = new UPCASTForallStatement();
		copyForStatement(copy, style);
		copy.setAffinityExpression(affinity == null ? null : affinity.copy(style));
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}


	@Override
	public boolean isAffinityContinue() {
		return affinityContinue;
	}

	@Override
	public IASTExpression getAffinityExpresiion() {
		return affinity;
	}

	@Override
	public void setAffinityExpression(IASTExpression affinity) {
		if(affinity != null)
			this.affinityContinue = false;
		this.affinity = affinity;
		if(affinity != null) {
			affinity.setParent(this);
			affinity.setPropertyInParent(AFFINITY);
		}
	}

	@Override
	public void setAffinityContinue(boolean affinityContinue) {
		if(affinityContinue)
			this.affinity = null;
		this.affinityContinue = affinityContinue;
	}


	@Override
	public boolean accept(ASTVisitor visitor) {
		if(visitor.shouldVisitStatements) {
			switch(visitor.visit(this)){
            	case ASTVisitor.PROCESS_ABORT : return false;
            	case ASTVisitor.PROCESS_SKIP  : return true;
			}
		}

		IASTStatement initializer = super.getInitializerStatement();
		if(initializer != null) if(!initializer.accept(visitor)) return false;

		IASTExpression condition = super.getConditionExpression();
		if(condition != null) if(!condition.accept(visitor)) return false;

		IASTExpression iteration = super.getIterationExpression();
		if(iteration != null) if(!iteration.accept(visitor)) return false;

		if(affinity != null) if(!affinity.accept(visitor)) return false;

		IASTStatement body = super.getBody();
		if(body != null) if(!body.accept(visitor)) return false;

		if(visitor.shouldVisitStatements) {
			switch(visitor.leave(this)){
            	case ASTVisitor.PROCESS_ABORT : return false;
            	case ASTVisitor.PROCESS_SKIP  : return true;
			}
		}

		return true;
	}

}
