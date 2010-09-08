/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public class CPPASTConditionalExpression extends ASTNode implements IASTConditionalExpression,
		IASTAmbiguityParent {
	
    private IASTExpression condition;
    private IASTExpression negative;
    private IASTExpression positive;

    
    public CPPASTConditionalExpression() {
	}

	public CPPASTConditionalExpression(IASTExpression condition, IASTExpression postive, IASTExpression negative) {
    	setLogicalConditionExpression(condition);
    	setPositiveResultExpression(postive);
    	setNegativeResultExpression(negative);
	}

	
	public CPPASTConditionalExpression copy() {
		CPPASTConditionalExpression copy = new CPPASTConditionalExpression();
		copy.setLogicalConditionExpression(condition == null ? null : condition.copy());
		copy.setPositiveResultExpression(positive == null ? null : positive.copy());
		copy.setNegativeResultExpression(negative == null ? null : negative.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTExpression getLogicalConditionExpression() {
        return condition;
    }

    public void setLogicalConditionExpression(IASTExpression expression) {
        assertNotFrozen();
        condition = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(LOGICAL_CONDITION);
		}
    }

    public IASTExpression getPositiveResultExpression() {
        return positive;
    }

    public void setPositiveResultExpression(IASTExpression expression) {
        assertNotFrozen();
        this.positive = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(POSITIVE_RESULT);
		}
    }

    public IASTExpression getNegativeResultExpression() {
        return negative;
    }

    public void setNegativeResultExpression(IASTExpression expression) {
        assertNotFrozen();
        this.negative = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(NEGATIVE_RESULT);
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
        
        if( condition != null ) if( !condition.accept( action ) ) return false;
        if( positive != null ) if( !positive.accept( action ) ) return false;
        if( negative != null ) if( !negative.accept( action ) ) return false;
        
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
        if( child == condition )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            condition  = (IASTExpression) other;
        }
        if( child == positive )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            positive  = (IASTExpression) other;
        }
        if( child == negative )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            negative  = (IASTExpression) other;
        }
    }
    
    // mstodo conditional operator (type)
    public IType getExpressionType() {
		IASTExpression positiveExpression = getPositiveResultExpression();
		if (positiveExpression == null) {
			positiveExpression= getLogicalConditionExpression();
		}
		IType t2 = positiveExpression.getExpressionType();
		IType t3 = getNegativeResultExpression().getExpressionType();
		if (t3 instanceof IPointerType || t2 == null)
			return t3;
		return t2;
    }

    // mstodo conditional operator (value category)
    public ValueCategory getValueCategory() {
    	return PRVALUE;
    }
    
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}
}
