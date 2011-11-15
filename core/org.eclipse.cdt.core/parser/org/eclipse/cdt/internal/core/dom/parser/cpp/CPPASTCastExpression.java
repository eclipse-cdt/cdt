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
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromReturnType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromReturnType;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Cast expression for C++
 */
public class CPPASTCastExpression extends ASTNode implements ICPPASTCastExpression, IASTAmbiguityParent {
    private int op;
    private IASTExpression operand;
	private IASTTypeId typeId;
	private IType fType;
	private ValueCategory fValueCategory;
	
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
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
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
        operand = expression;
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
            operand  = (IASTExpression) other;
        }
    }
    
	@Override
	public IType getExpressionType() {
		if (fType == null) {
			IType t= CPPVisitor.createType(typeId.getAbstractDeclarator());
			fValueCategory= valueCategoryFromReturnType(t);
			fType= typeFromReturnType(t);
		}
		return fType;
	}

	@Override
	public ValueCategory getValueCategory() {
		if (fValueCategory == null) {
			getExpressionType(); // as a side effect fValueCategory is computed
		}
		return fValueCategory;
	}

	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}
}
