/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTForallStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTForStatement;

public class UPCASTForallStatement extends CASTForStatement implements IUPCASTForallStatement {

	private IASTExpression affinity;
	private boolean affinityContinue;
	

	public boolean isAffinityContinue() {
		return affinityContinue;
	}
	
	public IASTExpression getAffinityExpresiion() {
		return affinity;
	}

	public void setAffinityExpression(IASTExpression affinity) {
		if(affinity != null)
			this.affinityContinue = false;
		this.affinity = affinity;
	}

	public void setAffinityContinue(boolean affinityContinue) {
		if(affinityContinue)
			this.affinity = null;
		this.affinityContinue = affinityContinue;
	}
	

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
