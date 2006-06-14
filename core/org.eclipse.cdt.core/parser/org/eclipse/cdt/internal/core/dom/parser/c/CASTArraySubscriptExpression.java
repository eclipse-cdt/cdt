/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTArraySubscriptExpression extends CASTNode implements
        IASTArraySubscriptExpression, IASTAmbiguityParent {

    private IASTExpression array;
    private IASTExpression subscript;

    public IASTExpression getArrayExpression() {
        return array;
    }

    public void setArrayExpression(IASTExpression expression) {
        array = expression;
    }

    public IASTExpression getSubscriptExpression() {
        return subscript;
    }

    public void setSubscriptExpression(IASTExpression expression) {
        this.subscript = expression;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( array != null ) if( !array.accept( action ) ) return false;
        if( subscript != null ) if( !subscript.accept( action ) ) return false;
        return true;
    }
    
    public void replace(IASTNode child, IASTNode other) {
        if( child == array )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            array = (IASTExpression) other;
        }
        if( child == subscript)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            subscript = (IASTExpression) other;
        }
    }
    
    public IType getExpressionType() {
    	return CVisitor.getExpressionType(this);
    }
    
}
