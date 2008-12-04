/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;

/**
 * @author jcamelon
 */
public class CPPASTCastExpression extends CPPASTUnaryExpression implements ICPPASTCastExpression {

	private IASTTypeId typeId;
    
    public CPPASTCastExpression() {
	}
    
    public CPPASTCastExpression(int operator, IASTTypeId typeId, IASTExpression operand) {
    	super(operator, operand);
		setTypeId(typeId);
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
	public void setOperand(IASTExpression expression) {
        assertNotFrozen();
        super.setOperand(expression);
        // this needs to be overridden because CPPASTUnaryExpression sets
        // propertyInParent to ICPPASTUnaryExpression.OPERAND, we want 
        // ICPPASTCastExpression.OPERAND
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(IASTCastExpression.OPERAND);
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
        
        if( typeId != null ) if( !typeId.accept( action ) ) return false;
        IASTExpression op = getOperand();
        if( op != null ) if( !op.accept( action ) ) return false;
        
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
}
