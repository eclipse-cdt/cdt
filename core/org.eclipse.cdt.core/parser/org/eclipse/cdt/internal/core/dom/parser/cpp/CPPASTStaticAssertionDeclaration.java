/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStaticAssertDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public class CPPASTStaticAssertionDeclaration extends ASTNode implements ICPPASTStaticAssertDeclaration, IASTAmbiguityParent {

	private IASTExpression fCondition;
	private final ICPPASTLiteralExpression fMessage;

	public CPPASTStaticAssertionDeclaration(IASTExpression condition, ICPPASTLiteralExpression message) {
		fCondition= condition;
		fMessage= message;
        if (condition != null) {
			condition.setParent(this);
			condition.setPropertyInParent(CONDITION);
		}
        if (message != null) {
        	message.setParent(this);
        	message.setPropertyInParent(MESSAGE);
		}
	}
	
	@Override
	public IASTExpression getCondition() {
		return fCondition;
	}

	@Override
	public ICPPASTLiteralExpression getMessage() {
		return fMessage;
	}


	@Override
	public CPPASTStaticAssertionDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTStaticAssertionDeclaration copy(CopyStyle style) {
		final IASTExpression condCopy = fCondition == null ? null : fCondition.copy(style);
		final ICPPASTLiteralExpression msgCopy = fMessage == null ? null : fMessage.copy(style);
		CPPASTStaticAssertionDeclaration copy = new CPPASTStaticAssertionDeclaration(condCopy,
				msgCopy);
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

    @Override
	public boolean accept( ASTVisitor action ){
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	        }
		}
        
		if (fCondition != null && !fCondition.accept(action))
			return false;
		if (fMessage != null && !fMessage.accept(action))
			return false;
		
		if (action.shouldVisitDeclarations && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;
        return true;
    }
    
    @Override
	public void replace(IASTNode child, IASTNode other) {
    	if (child == fCondition) {
    		fCondition= (IASTExpression) other;
    		other.setParent(child.getParent());
    		other.setPropertyInParent(child.getPropertyInParent());
    	}
	}
}
