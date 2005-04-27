/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTConditionalExpression extends CASTNode implements
        IASTConditionalExpression, IASTAmbiguityParent {

    private IASTExpression condition;
    private IASTExpression negative;
    private IASTExpression positive;

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
        return positive;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#setPositiveResultExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setPositiveResultExpression(IASTExpression expression) {
        this.positive = expression;
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
        if( positive != null ) if( !positive.accept( action ) ) return false;
        if( negative != null ) if( !negative.accept( action ) ) return false;
        return true;
    }
    
    public void replace(IASTNode child, IASTNode other) {
        if( child == condition )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            condition = (IASTExpression) other;
        }
        if( child == positive)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            positive= (IASTExpression) other;
        }
        if( child == negative)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            negative= (IASTExpression) other;
        }
    }
}
