/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public class CPPASTReturnStatement extends ASTNode implements IASTReturnStatement, IASTAmbiguityParent {
    
	private IASTInitializerClause retValue;
    
    public CPPASTReturnStatement() {
	}

	public CPPASTReturnStatement(IASTInitializerClause retValue) {
		setReturnArgument(retValue);
	}

	@Override
	public CPPASTReturnStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTReturnStatement copy(CopyStyle style) {
		CPPASTReturnStatement copy = new CPPASTReturnStatement(retValue == null ? null
				: retValue.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public IASTInitializerClause getReturnArgument() {
		return retValue;
	}
	
	@Override
	public IASTExpression getReturnValue() {
        if (retValue instanceof IASTExpression) {
        	return (IASTExpression) retValue;
        }
        return null;
    }

	
    @Override
	public void setReturnValue(IASTExpression returnValue) {
    	setReturnArgument(returnValue);
    }
    
    @Override
	public void setReturnArgument(IASTInitializerClause arg) {
        assertNotFrozen();
        retValue = arg;
        if (arg != null) {
			arg.setParent(this);
			arg.setPropertyInParent(RETURNVALUE);
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
		if (retValue != null && !retValue.accept(action))
			return false;
        
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
        if (child == retValue) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            retValue = (IASTInitializerClause) other;
        }
    }
}
