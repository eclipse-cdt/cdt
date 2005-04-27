/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTArraySubscriptExpression extends CPPASTNode implements
        IASTArraySubscriptExpression, IASTAmbiguityParent {

    private IASTExpression subscriptExp;
    private IASTExpression arrayExpression;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression#getArrayExpression()
     */
    public IASTExpression getArrayExpression() {
        return arrayExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression#setArrayExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setArrayExpression(IASTExpression expression) {
        arrayExpression = expression;        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression#getSubscriptExpression()
     */
    public IASTExpression getSubscriptExpression() {
        return subscriptExp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression#setSubscriptExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setSubscriptExpression(IASTExpression expression) {
        subscriptExp = expression;
    }
    
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

}
