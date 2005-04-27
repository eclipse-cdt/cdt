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
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTConditionalExpression extends CPPASTNode implements
        IASTConditionalExpression, IASTAmbiguityParent {
    private IASTExpression condition;
    private IASTExpression negative;
    private IASTExpression postive;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#getLogicalConditionExpression()
     */
    public IASTExpression getLogicalConditionExpression() {
        return condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#setLogicalConditionExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setLogicalConditionExpression(IASTExpression expression) {
        condition = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#getPositiveResultExpression()
     */
    public IASTExpression getPositiveResultExpression() {
        return postive;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#setPositiveResultExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setPositiveResultExpression(IASTExpression expression) {
        this.postive = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#getNegativeResultExpression()
     */
    public IASTExpression getNegativeResultExpression() {
        return negative;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#setNegativeResultExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setNegativeResultExpression(IASTExpression expression) {
        this.negative = expression;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( condition != null ) if( !condition.accept( action ) ) return false;
        if( postive != null ) if( !postive.accept( action ) ) return false;
        if( negative != null ) if( !negative.accept( action ) ) return false;
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == condition )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            condition  = (IASTExpression) other;
        }
        if( child == postive )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            postive  = (IASTExpression) other;
        }
        if( child == negative )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            negative  = (IASTExpression) other;
        }
    }
}
