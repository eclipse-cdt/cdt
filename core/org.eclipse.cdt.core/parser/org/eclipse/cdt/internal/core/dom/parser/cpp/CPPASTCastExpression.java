/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;

/**
 * Cast expression for C++
 */
public class CPPASTCastExpression extends ASTNode implements ICPPASTCastExpression, IASTAmbiguityParent {
    private int op;
    private ICPPASTExpression operand;
	private IASTTypeId typeId;
	private ICPPEvaluation fEvaluation;

    public CPPASTCastExpression() {
	}
    
    public CPPASTCastExpression(int operator, IASTTypeId typeId, IASTExpression operand) {
		op = operator;
		setOperand(operand);
		setTypeId(typeId);
	}
    
	@Override
	public CPPASTCastExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
    
	@Override
	public CPPASTCastExpression copy(CopyStyle style) {
		CPPASTCastExpression copy = new CPPASTCastExpression();
		copy.setOperator(getOperator());
		copy.setTypeId(typeId == null ? null : typeId.copy(style));
		IASTExpression operand = getOperand();
		copy.setOperand(operand == null ? null : operand.copy(style));
		return copy(copy, style);
	}

	@Override
	public void setTypeId(IASTTypeId typeId) {
        assertNotFrozen();
        this.typeId = typeId;
        if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TYPE_ID);
		}
    }

    @Override
	public IASTTypeId getTypeId() {
        return typeId;
    }
    
	@Override
	public int getOperator() {
        return op;
    }

    @Override
	public void setOperator(int operator) {
        assertNotFrozen();
        op = operator;
    }

    @Override
	public IASTExpression getOperand() {
        return operand;
    }

    @Override
	public void setOperand(IASTExpression expression) {
        assertNotFrozen();
        operand = (ICPPASTExpression) expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND);
		}
    }
    
    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        
        if (typeId != null && !typeId.accept(action)) return false;
        IASTExpression op = getOperand();
        if (op != null && !op.accept(action)) return false;
        
        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (child == operand) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            operand  = (ICPPASTExpression) other;
        }
    }
    
    
	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) 
			fEvaluation= computeEvaluation();
		
		return fEvaluation;
	}
	
	private ICPPEvaluation computeEvaluation() {
		if (operand == null)
			return EvalFixed.INCOMPLETE;
		
		IType type= CPPVisitor.createType(getTypeId());
		if (type == null || type instanceof IProblemType)
			return EvalFixed.INCOMPLETE;
		
		return new EvalTypeId(type, this, operand.getEvaluation());
	}

    @Override
	public IType getExpressionType() {
    	return getEvaluation().getTypeOrFunctionSet(this);
    }

	@Override
	public ValueCategory getValueCategory() {
    	return getEvaluation().getValueCategory(this);
	}

	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}
}
