/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTInitializerExpression extends ASTNode implements
        IASTInitializerExpression, IASTAmbiguityParent {

    private IASTExpression exp;

    
    public CPPASTInitializerExpression() {
	}

	public CPPASTInitializerExpression(IASTExpression exp) {
		setExpression(exp);
	}

	public CPPASTInitializerExpression copy() {
		CPPASTInitializerExpression copy = new CPPASTInitializerExpression(exp == null ? null : exp.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTExpression getExpression() {
        return exp;
    }

    public void setExpression(IASTExpression expression) {
        assertNotFrozen();
        this.exp = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(INITIALIZER_EXPRESSION);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitInitializers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( exp != null ) if( !exp.accept( action ) ) return false;
        
        if( action.shouldVisitInitializers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == exp ) {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            exp  = (IASTExpression) other;
        }
    }

}
