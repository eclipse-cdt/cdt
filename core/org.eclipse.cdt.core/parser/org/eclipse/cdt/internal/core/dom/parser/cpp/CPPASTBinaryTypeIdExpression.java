/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTBinaryTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;

public class CPPASTBinaryTypeIdExpression extends ASTNode implements IASTBinaryTypeIdExpression {
    private Operator fOperator;
    private IASTTypeId fOperand1;
    private IASTTypeId fOperand2;

    public CPPASTBinaryTypeIdExpression() {
	}

	public CPPASTBinaryTypeIdExpression(Operator op, IASTTypeId typeId1, IASTTypeId typeId2) {
		fOperator = op;
		setOperand1(typeId1);
		setOperand2(typeId2);
	}

	@Override
	public CPPASTBinaryTypeIdExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTBinaryTypeIdExpression copy(CopyStyle style) {
		CPPASTBinaryTypeIdExpression copy = new CPPASTBinaryTypeIdExpression(fOperator, 
				fOperand1 == null ? null : fOperand1.copy(),
				fOperand2 == null ? null : fOperand2.copy());
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public Operator getOperator() {
        return fOperator;
    }

    @Override
	public void setOperator(Operator value) {
        assertNotFrozen();
        fOperator = value;
    }

    @Override
	public void setOperand1(IASTTypeId typeId) {
    	assertNotFrozen();
       	fOperand1 = typeId;
       	if (typeId != null) {
       		typeId.setParent(this);
       		typeId.setPropertyInParent(OPERAND1);
       	} 
    }

    @Override
	public void setOperand2(IASTTypeId typeId) {
    	assertNotFrozen();
       	fOperand2 = typeId;
       	if (typeId != null) {
       		typeId.setParent(this);
       		typeId.setPropertyInParent(OPERAND2);
       	} 
    }

    @Override
	public IASTTypeId getOperand1() {
    	return fOperand1;
    }

    @Override
	public IASTTypeId getOperand2() {
    	return fOperand2;
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
      
        if (fOperand1 != null && !fOperand1.accept(action)) 
        	return false;
        if (fOperand2 != null && !fOperand2.accept(action)) 
        	return false;
        
        if (action.shouldVisitExpressions && action.leave(this) ==  ASTVisitor.PROCESS_ABORT) 
        	return false;

        return true;
    }
    
	@Override
	public IType getExpressionType() {
		switch (getOperator()) {
		case __is_base_of:
			return CPPBasicType.BOOLEAN;
		}
		return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
	}
	
	@Override
	public boolean isLValue() {
		return false;
	}

	@Override
	public ValueCategory getValueCategory() {
		return isLValue() ? LVALUE : PRVALUE;
	}
}
