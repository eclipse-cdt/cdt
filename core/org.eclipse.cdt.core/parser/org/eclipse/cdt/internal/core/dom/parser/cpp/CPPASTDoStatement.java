/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTDoStatement extends ASTNode implements IASTDoStatement, IASTAmbiguityParent {

	private IASTStatement body;
    private IASTExpression condition;

    
    public CPPASTDoStatement() {
	}

	public CPPASTDoStatement(IASTStatement body, IASTExpression condition) {
		setBody(body);
		setCondition(condition);
	}

	@Override
	public CPPASTDoStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTDoStatement copy(CopyStyle style) {
		CPPASTDoStatement copy = new CPPASTDoStatement();
		copy.setBody(body == null ? null : body.copy(style));
		copy.setCondition(condition == null ? null : condition.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public IASTStatement getBody() {
        return body;
    }

    @Override
	public void setBody(IASTStatement body) {
        assertNotFrozen();
        this.body = body;
        if (body != null) {
			body.setParent(this);
			body.setPropertyInParent(BODY);
		}
    }

    @Override
	public IASTExpression getCondition() {
        return condition;
    }

    @Override
	public void setCondition(IASTExpression condition) {
        assertNotFrozen();
        this.condition = condition;
        if (condition != null) {
			condition.setParent(this);
			condition.setPropertyInParent(CONDITION);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( body != null ) if( !body.accept( action ) ) return false;
        if( condition != null ) if( !condition.accept( action ) ) return false;
        if( action.shouldVisitStatements ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
    
    @Override
	public void replace(IASTNode child, IASTNode other) {
        if( body == child )
        {
            other.setPropertyInParent( body.getPropertyInParent() );
            other.setParent( body.getParent() );
            body = (IASTStatement) other;
        }
        if( child == condition )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            condition  = (IASTExpression) other;
        }
    }
}
