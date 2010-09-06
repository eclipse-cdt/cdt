/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Cast expressions for c
 */
public class CASTCastExpression extends ASTNode implements IASTCastExpression, IASTAmbiguityParent {
    private int operator;
    private IASTExpression operand;
    private IASTTypeId typeId;


    public CASTCastExpression() {
    	this.operator = op_cast;
	}

	public CASTCastExpression(IASTTypeId typeId, IASTExpression operand) {
		this();
		setOperand(operand);
		setTypeId(typeId);
	}
	
	public CASTCastExpression copy() {
		CASTCastExpression copy = new CASTCastExpression();
		copy.setTypeId(typeId == null ? null : typeId.copy());
		IASTExpression operand = getOperand();
		copy.setOperand(operand == null ? null : operand.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}

	public int getOperator() {
        return operator;
    }

    public void setOperator(int value) {
        assertNotFrozen();
        this.operator = value;
    }

    public IASTExpression getOperand() {
        return operand;
    }

    public void setOperand(IASTExpression expression) {
        assertNotFrozen();
        operand = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND);
		}
    }
    
	public void setTypeId(IASTTypeId typeId) {
        assertNotFrozen();
        this.typeId = typeId;
        if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TYPE_ID);
		}
    }

    public IASTTypeId getTypeId() {
        return typeId;
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
        
        if( typeId != null ) if( !typeId.accept( action ) ) return false;
        IASTExpression operand = getOperand();
        if( operand != null ) if( !operand.accept( action ) ) return false;
        
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
		if (child == operand) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			operand = (IASTExpression) other;
		} 
	}
    
	public IType getExpressionType() {
        IASTTypeId id= getTypeId();
        return CVisitor.createType(id.getAbstractDeclarator());
    }

	public boolean isLValue() {
		return false;
	}
	
	public final ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}
}
