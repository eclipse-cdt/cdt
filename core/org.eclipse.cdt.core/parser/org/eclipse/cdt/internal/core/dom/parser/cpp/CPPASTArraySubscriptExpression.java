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
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * @author jcamelon
 */
public class CPPASTArraySubscriptExpression extends ASTNode implements IASTArraySubscriptExpression, IASTAmbiguityParent {

    private IASTExpression subscriptExp;
    private IASTExpression arrayExpression;

    
    public CPPASTArraySubscriptExpression() {
	}

	public CPPASTArraySubscriptExpression(IASTExpression arrayExpression, IASTExpression subscriptExp) {
		setArrayExpression(arrayExpression);
		setSubscriptExpression(subscriptExp);
	}

	public IASTExpression getArrayExpression() {
        return arrayExpression;
    }

    public void setArrayExpression(IASTExpression expression) {
        arrayExpression = expression;        
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(ARRAY);
		}
    }

    public IASTExpression getSubscriptExpression() {
        return subscriptExp;
    }

    public void setSubscriptExpression(IASTExpression expression) {
        subscriptExp = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(SUBSCRIPT);
		}
    }
    
    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( arrayExpression != null ) 
            if( !arrayExpression.accept( action ) ) return false;
        if( subscriptExp != null )   
            if( !subscriptExp.accept( action ) ) return false;
        
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == subscriptExp )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            subscriptExp  = (IASTExpression) other;
        }
        if( child == arrayExpression )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            arrayExpression  = (IASTExpression) other;
        }
    }

    public IType getExpressionType() {
    	return CPPVisitor.getExpressionType(this);
    }
    
}
